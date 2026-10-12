<script setup>
import { computed, onMounted, ref } from 'vue'
import FacilityMap from './components/FacilityMap.vue'
import ScoreCard from './components/ScoreCard.vue'
import MapLegend from './components/MapLegend.vue'
import NearbyList from './components/NearbyList.vue'
import { fetchFacilities } from './api/facility'
import { fetchScore, fetchCampScores } from './api/score'
import { typeInfo, DEFAULT_HIDDEN, GRADE_COLOR } from './facilityTypes'

const all = ref([])
const loading = ref(true)
const error = ref('')
const hiddenTypes = ref(new Set(DEFAULT_HIDDEN))
const keyword = ref('')
const selected = ref(null)

// 점수
const score = ref(null) // /api/score 결과
const scoreTitle = ref('')
const scoreError = ref('')
const campScores = ref([]) // /api/score/camps 결과
const dashOpen = ref(false) // 오른쪽 점수 패널 열림/접힘
let scoreSeq = 0 // 빠르게 여러 번 클릭할 때 마지막 결과만 쓰기 위한 번호

// ── 기준 위치 (📍 내 위치 / 📌 위치 찍기) ──
const base = ref(null) // { lat, lng, source: 'gps' | 'pin' }
const pickMode = ref(false) // 📌 위치 찍기 모드
const radiusKm = ref(10) // 주변 추천 반경
const locating = ref(false) // 내 위치 찾는 중
const baseMsg = ref('') // 위치 관련 안내 문구
const nearbyActiveId = ref(null) // 추천 목록에서 고른 캠핑장

// ── 저장한 위치 (북마크) ──
// 브라우저 저장소(localStorage)에 저장 → 이 브라우저에서만 보이고, 서버/DB에는 안 올라감
const BOOKMARK_KEY = 'campingsafe.bookmarks'
function readBookmarks() {
  try {
    return JSON.parse(localStorage.getItem(BOOKMARK_KEY)) ?? []
  } catch {
    return []
  }
}
const bookmarks = ref(readBookmarks()) // [{ id, name, lat, lng, total, grade }]
function saveBookmarks() {
  try {
    localStorage.setItem(BOOKMARK_KEY, JSON.stringify(bookmarks.value))
  } catch {
    // 저장소를 못 쓰는 환경(시크릿 창 등)이면 이번 접속 동안만 유지
  }
}

const types = computed(() => {
  const counts = {}
  for (const f of all.value) counts[f.type] = (counts[f.type] ?? 0) + 1
  return Object.keys(counts)
    .sort()
    .map((type, i) => ({ type, count: counts[type], ...typeInfo(type, i) }))
})
// 범례에 보여줄 종류 = 지금 체크되어 켜진 종류
const legendTypes = computed(() => types.value.filter((t) => !hiddenTypes.value.has(t.type)))
const colorByType = computed(() => Object.fromEntries(types.value.map((t) => [t.type, t.color])))
const labelByType = computed(() => Object.fromEntries(types.value.map((t) => [t.type, t.label])))
const campScoreById = computed(() => Object.fromEntries(campScores.value.map((c) => [c.id, c])))

// 캠핑장 마커는 안심 등급 색, 나머지는 종류 색
const colorOf = computed(() => (f) => {
  const cs = f.type === 'camp' ? campScoreById.value[f.id] : null
  return cs ? GRADE_COLOR[cs.grade] : colorByType.value[f.type]
})

const visible = computed(() => {
  const kw = keyword.value.trim()
  return all.value.filter(
    (f) =>
      !hiddenTypes.value.has(f.type) &&
      (!kw || f.name?.includes(kw) || f.address?.includes(kw)),
  )
})

// 두 좌표 사이 직선거리(km) — 하버사인 공식
function distanceKm(a, b) {
  const R = 6371
  const toRad = (d) => (d * Math.PI) / 180
  const dLat = toRad(b.lat - a.lat)
  const dLng = toRad(b.lng - a.lng)
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(toRad(a.lat)) * Math.cos(toRad(b.lat)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(h))
}

// 기준 위치 반경 안 캠핑장을 점수 높은 순으로 5곳
const nearby = computed(() => {
  if (!base.value) return []
  return campScores.value
    .map((c) => ({ ...c, distKm: distanceKm(base.value, c) }))
    .filter((c) => c.distKm <= radiusKm.value)
    .sort((a, b) => b.totalScore - a.totalScore || a.distKm - b.distKm)
    .slice(0, 5)
})

// 데이터가 있는 범위 (내 위치가 이 밖이면 안내)
const dataBounds = computed(() => {
  const pts = all.value.filter((f) => f.lat != null && f.lng != null)
  if (!pts.length) return null
  const lats = pts.map((f) => f.lat)
  const lngs = pts.map((f) => f.lng)
  const m = 0.05 // 약 5km 여유
  return { minLat: Math.min(...lats) - m, maxLat: Math.max(...lats) + m, minLng: Math.min(...lngs) - m, maxLng: Math.max(...lngs) + m }
})
function inDataArea(p) {
  const b = dataBounds.value
  return !b || (p.lat >= b.minLat && p.lat <= b.maxLat && p.lng >= b.minLng && p.lng <= b.maxLng)
}

// 지금 보이는 점수가 기준 위치의 점수인지 (그렇다면 기본 핀 대신 기준 핀만 보임)
const scoreAtBase = computed(
  () =>
    !!(score.value && base.value) &&
    Math.abs(score.value.lat - base.value.lat) < 1e-6 &&
    Math.abs(score.value.lng - base.value.lng) < 1e-6,
)
const baseLink = computed(() => {
  if (!base.value || !nearbyActiveId.value) return null
  const c = campScores.value.find((x) => x.id === nearbyActiveId.value)
  return c ? { lat: c.lat, lng: c.lng } : null
})

const scorePin = computed(() => (score.value ? { lat: score.value.lat, lng: score.value.lng } : null))
// 점수 근거 시설(종류별 가장 가까운 곳) — 지도에 점선 + 아이콘으로 표시
const scoreLinks = computed(() =>
  (score.value?.items ?? []).map((it) => ({
    id: it.facilityId,
    type: it.type,
    name: it.name,
    lat: it.lat,
    lng: it.lng,
    color: typeInfo(it.type).color,
  })),
)

function toggleType(type) {
  const next = new Set(hiddenTypes.value)
  next.has(type) ? next.delete(type) : next.add(type)
  hiddenTypes.value = next
}

async function loadScore(lat, lng, title) {
  const seq = ++scoreSeq
  scoreError.value = ''
  try {
    const data = await fetchScore(lat, lng)
    if (seq !== scoreSeq) return
    score.value = data
    scoreTitle.value = title
    dashOpen.value = true // 점수가 나오면 패널 펼치기
    // 북마크 위치의 점수면 북마크에도 점수 기록
    const id = base.value?.bookmarkId
    if (id && Math.abs(data.lat - base.value.lat) < 1e-6 && Math.abs(data.lng - base.value.lng) < 1e-6) {
      bookmarks.value = bookmarks.value.map((b) => (b.id === id ? { ...b, total: data.total, grade: data.grade } : b))
      saveBookmarks()
    }
  } catch (e) {
    if (seq !== scoreSeq) return
    scoreError.value = '점수를 계산하지 못했습니다. 서버 콘솔을 확인하세요.'
    console.error(e)
  }
}

// 기준 위치 정하기 (내 위치 / 찍은 위치 / 드래그로 옮긴 위치 모두 여기로)
function setBase(lat, lng, source, bookmarkId = null) {
  base.value = { lat, lng, source, bookmarkId }
  selected.value = null
  nearbyActiveId.value = null
  baseMsg.value = inDataArea(base.value)
    ? ''
    : '데이터가 있는 지역(인제군) 밖이에요. 📌 위치 찍기로 인제군 안을 골라보세요.'
  loadScore(lat, lng, baseTitle())
}

function baseTitle() {
  if (!base.value) return ''
  if (base.value.source === 'gps') return '내 위치'
  return bookmarks.value.find((b) => b.id === base.value.bookmarkId)?.name ?? '찍은 위치'
}

// 지도 클릭
//  - 📌 위치 찍기 모드: 그 위치를 북마크로 저장하고 기준 위치로
//  - 평소: 원래처럼 그 지점 점수만 보기 (기준 위치는 그대로)
function onPick({ lat, lng }) {
  if (pickMode.value) {
    pickMode.value = false
    const n = bookmarks.value.length + 1
    const bm = { id: Date.now(), name: `저장 위치 ${n}`, lat, lng, total: null, grade: null }
    bookmarks.value = [...bookmarks.value, bm]
    saveBookmarks()
    setBase(lat, lng, 'pin', bm.id)
    return
  }
  selected.value = null
  nearbyActiveId.value = null
  loadScore(lat, lng, `선택한 위치 (${lat.toFixed(4)}, ${lng.toFixed(4)})`)
}

// 기준 핀을 드래그해서 옮김 — 북마크에서 온 핀이면 그 북마크 위치도 같이 바꿈
function onMoveBase({ lat, lng }) {
  const id = base.value?.bookmarkId ?? null
  if (id) {
    bookmarks.value = bookmarks.value.map((b) => (b.id === id ? { ...b, lat, lng } : b))
    saveBookmarks()
  }
  setBase(lat, lng, base.value?.source ?? 'pin', id)
}

// 저장한 위치 누르기 → 그 위치를 기준 위치로
function openBookmark(b) {
  pickMode.value = false
  setBase(b.lat, b.lng, 'pin', b.id)
}

function removeBookmark(b) {
  bookmarks.value = bookmarks.value.filter((x) => x.id !== b.id)
  saveBookmarks()
  if (base.value?.bookmarkId === b.id) clearBase()
}

// 📍 내 위치 (브라우저 위치 기능 — localhost나 https에서만 동작)
function locateMe() {
  pickMode.value = false
  if (!navigator.geolocation) {
    baseMsg.value = '이 브라우저는 위치 기능을 지원하지 않아요.'
    return
  }
  locating.value = true
  baseMsg.value = ''
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      locating.value = false
      setBase(pos.coords.latitude, pos.coords.longitude, 'gps')
    },
    (err) => {
      locating.value = false
      baseMsg.value =
        err.code === err.PERMISSION_DENIED
          ? '위치 권한이 거부됐어요. 브라우저 주소창 왼쪽 자물쇠에서 허용하거나 📌 위치 찍기를 써주세요.'
          : '위치를 가져오지 못했어요. 📌 위치 찍기를 써주세요.'
    },
    { enableHighAccuracy: true, timeout: 10000 },
  )
}

function togglePick() {
  pickMode.value = !pickMode.value
  baseMsg.value = ''
}

// 추천 목록에서 캠핑장 선택 — 기준 핀은 그대로
function onNearbySelect(c) {
  nearbyActiveId.value = c.id
  selectCamp(c)
}

function backToBase() {
  if (!base.value) return
  selected.value = null
  nearbyActiveId.value = null
  loadScore(base.value.lat, base.value.lng, baseTitle())
}

function clearBase() {
  base.value = null
  nearbyActiveId.value = null
  baseMsg.value = ''
  if (scoreAtBase.value) closeScore()
}

// 마커·목록에서 시설 선택 — 캠핑장이면 점수도 계산
function onSelect(f) {
  selected.value = f
  if (f.type === 'camp') loadScore(f.lat, f.lng, f.name)
}

function selectCamp(c) {
  const f = all.value.find((x) => x.id === c.id) ?? { ...c, type: 'camp' }
  onSelect(f)
}

function closeScore() {
  scoreSeq++
  score.value = null
}

onMounted(async () => {
  try {
    all.value = await fetchFacilities()
  } catch (e) {
    error.value = '시설 데이터를 불러오지 못했습니다. 스프링부트 서버(8080)가 켜져 있는지 확인하세요.'
    console.error(e)
  } finally {
    loading.value = false
  }
  try {
    campScores.value = await fetchCampScores()
  } catch (e) {
    console.error('캠핑장 점수 불러오기 실패', e)
  }
})
</script>

<template>
  <div class="layout">
    <aside class="side">
      <header>
        <p class="eyebrow">혼자 차박 안심 판정</p>
        <h1>주변 안전 시설 지도</h1>
        <p class="sub">정리한 시설 데이터 {{ all.length.toLocaleString() }}건</p>
      </header>

      <p v-if="loading" class="info">불러오는 중…</p>
      <p v-else-if="error" class="info err">{{ error }}</p>

      <section v-else>
        <template v-if="campScores.length">
          <h2 class="first">캠핑장 안심 순위 <small>{{ campScores.length }}곳</small></h2>
          <ol class="rank">
            <li
              v-for="(c, i) in campScores"
              :key="c.id"
              :class="{ active: selected?.id === c.id }"
              @click="selectCamp(c)"
            >
              <span class="no">{{ i + 1 }}</span>
              <span class="cname">{{ c.name }}</span>
              <span class="pill" :style="{ background: GRADE_COLOR[c.grade] }">{{ c.totalScore.toFixed(1) }}</span>
            </li>
          </ol>
        </template>

        <h2>📌 저장한 위치 <small>{{ bookmarks.length }}곳</small></h2>
        <ul v-if="bookmarks.length" class="bookmarks">
          <li v-for="b in bookmarks" :key="b.id" :class="{ active: base?.bookmarkId === b.id }" @click="openBookmark(b)">
            <span class="bname">
              <strong>{{ b.name }}</strong>
              <small>{{ b.lat.toFixed(4) }}, {{ b.lng.toFixed(4) }}</small>
            </span>
            <span v-if="b.total != null" class="pill" :style="{ background: GRADE_COLOR[b.grade] }">{{ b.total.toFixed(1) }}</span>
            <button class="bdel" @click.stop="removeBookmark(b)" aria-label="삭제">×</button>
          </li>
        </ul>
        <p v-else class="info">지도 위 📌 위치 찍기를 누르고 지도를 클릭하면 여기 저장돼요.</p>

        <h2>시설 종류</h2>
        <ul class="types">
          <li v-for="t in types" :key="t.type">
            <label>
              <input type="checkbox" :checked="!hiddenTypes.has(t.type)" @change="toggleType(t.type)" />
              <span class="dot" :style="{ background: t.color }"></span>
              <span class="tlabel">{{ t.label }}</span>
              <span class="count">{{ t.count }}</span>
            </label>
          </li>
        </ul>
        <p v-if="campScores.length" class="legend">
          캠핑장 마커 색 =
          <span v-for="(c, g) in GRADE_COLOR" :key="g"><i class="dot" :style="{ background: c }"></i>{{ g }}</span>
        </p>


        <input v-model="keyword" class="search" placeholder="이름·주소 검색" />

        <div v-if="selected" class="card">
          <div class="card-head">
            <span class="dot" :style="{ background: colorByType[selected.type] }"></span>
            <span class="badge">{{ labelByType[selected.type] }}</span>
            <button class="close" @click="selected = null" aria-label="닫기">×</button>
          </div>
          <h3>{{ selected.name }}</h3>
          <dl>
            <template v-if="selected.category"><dt>분류</dt><dd>{{ selected.category }}</dd></template>
            <template v-if="selected.address"><dt>주소</dt><dd>{{ selected.address }}</dd></template>
            <template v-if="selected.phone">
              <dt>전화</dt><dd><a :href="`tel:${selected.phone}`">{{ selected.phone }}</a></dd>
            </template>
            <template v-if="selected.hours"><dt>운영</dt><dd>{{ selected.hours }}</dd></template>
            <template v-if="selected.source">
              <dt>출처</dt><dd>{{ selected.source }}<span v-if="selected.dataDate"> · {{ selected.dataDate }}</span></dd>
            </template>
          </dl>
        </div>

        <h2>목록 <small>{{ visible.length }}건</small></h2>
        <ul class="list">
          <li
            v-for="f in visible.slice(0, 200)"
            :key="f.id"
            :class="{ active: selected?.id === f.id }"
            @click="onSelect(f)"
          >
            <span class="dot" :style="{ background: colorByType[f.type] }"></span>
            <div>
              <strong>{{ f.name }}</strong>
              <small>{{ f.address }}</small>
            </div>
          </li>
        </ul>
        <p v-if="visible.length > 200" class="info">앞 200건만 표시합니다. 검색으로 좁혀보세요.</p>
      </section>
    </aside>

    <main class="main">
      <FacilityMap
        :facilities="visible"
        :color-of="colorOf"
        :selected-id="selected?.id ?? null"
        :pin="scorePin"
        :links="scoreLinks"
        :scores="campScoreById"
        :pin-score="selected?.type === 'camp' ? null : score"
        :hide-pin="scoreAtBase"
        :pick-mode="pickMode"
        :base="base"
        :base-link="baseLink"
        @select="onSelect"
        @pick="onPick"
        @move-base="onMoveBase"
      />

      <!-- 기준 위치 도구 (지도 왼쪽 위) -->
      <div class="base-tools">
        <button @click="locateMe" :disabled="locating">{{ locating ? '찾는 중…' : '📍 내 위치' }}</button>
        <button @click="togglePick" :class="{ on: pickMode }">{{ pickMode ? '취소' : '📌 위치 찍기' }}</button>
        <button v-if="base" class="ghost" @click="clearBase" title="기준 위치 지우기">✕</button>
      </div>
      <p v-if="baseMsg" class="base-msg">{{ baseMsg }}</p>
      <MapLegend :types="legendTypes" :show-grades="campScores.length > 0 && !hiddenTypes.has('camp')" />
    </main>

    <!-- 오른쪽 점수 패널: 지도 위에 떠 있고, 탭으로 접고 펼 수 있음 -->
    <aside class="dash" :class="{ open: dashOpen }">
      <button class="dash-tab" @click="dashOpen = !dashOpen" :aria-expanded="dashOpen">
        <span class="dash-tab-arrow">{{ dashOpen ? '›' : '‹' }}</span>
        <span class="dash-tab-text">안심 점수</span>
        <span v-if="score" class="dash-tab-pill" :style="{ background: GRADE_COLOR[score.grade] }">
          {{ score.total.toFixed(0) }}
        </span>
      </button>
      <div class="dash-body">
        <p class="eyebrow">SAFETY SCORE</p>
        <h1>안심 점수</h1>
        <p v-if="scoreError" class="info err">{{ scoreError }}</p>
        <button v-if="base && score && !scoreAtBase" class="back-base" @click="backToBase">
          ← 기준 위치 점수로 돌아가기
        </button>
        <ScoreCard v-if="score" :score="score" :title="scoreTitle" @close="closeScore" />
        <div v-else class="dash-empty">
          <strong>위치를 골라주세요</strong>
          <span>📍 내 위치나 📌 위치 찍기로 기준 위치를 정하거나, 왼쪽 순위에서 캠핑장을 선택하세요.</span>
        </div>
        <NearbyList
          v-if="base"
          :items="nearby"
          v-model:radius-km="radiusKm"
          :active-id="nearbyActiveId"
          @select="onNearbySelect"
        />
      </div>
    </aside>
  </div>
</template>
