<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { loadKakaoMap } from '../api/kakaoLoader'
import { markerSvg, GRADE_COLOR, MARKER_W, MARKER_H } from '../facilityTypes'

const props = defineProps({
  facilities: { type: Array, required: true }, // 화면에 보일 시설
  colorOf: { type: Function, required: true }, // 시설 → 마커 색
  selectedId: { type: [Number, String], default: null },
  pin: { type: Object, default: null }, // 점수 계산 지점 {lat, lng}
  links: { type: Array, default: () => [] }, // 점수 근거 시설 [{id, type, name, lat, lng, color}]
  scores: { type: Object, default: () => ({}) }, // 캠핑장 id → {totalScore, grade}
  pinScore: { type: Object, default: null }, // 클릭 지점 점수 {total, grade}
  hidePin: { type: Boolean, default: false }, // 점수 지점이 기준 위치와 같으면 기본 핀은 숨김 (기준 핀이 대신 보임)
  pickMode: { type: Boolean, default: false }, // 📌 위치 찍기 모드: 이때 지도 클릭은 기준 위치 저장, 아니면 그냥 점수 보기
  base: { type: Object, default: null }, // 기준 위치 {lat, lng} — 드래그로 옮길 수 있음
  baseLink: { type: Object, default: null }, // 기준 위치 → 선택한 추천 캠핑장 연결선 {lat, lng}
})
const emit = defineEmits(['select', 'pick', 'move-base'])

const mapEl = ref(null)
const error = ref('')
let kakao = null
let map = null
let clusterer = null
let firstFit = true
let pinMarker = null
let lines = []
let linkMarkers = [] // 점선 끝 시설 아이콘 (그 종류가 꺼져 있어도 보이게)
let campMarkers = [] // 점수 있는 캠핑장은 묶지 않고 따로 표시
let labels = [] // 선택한 캠핑장 위 점수 라벨
let pinLabel = null
let baseMarker = null
let baseLine = null
const BASE_COLOR = '#1c2024' // 기준 위치 핀 색

// 지금 지도에 그려진 시설 id (점선 끝 아이콘 중복 방지용)
const visibleIds = computed(() => new Set(props.facilities.map((f) => f.id)))

const hintText = computed(() =>
  props.pickMode ? '지도를 클릭해 위치를 저장하세요' : '지도를 클릭하세요',
)
const imageCache = {}

// 같은 색·종류의 마커 이미지는 한 번만 만들어 재사용
function markerImage(color, type) {
  const key = color + '|' + type
  if (!imageCache[key]) {
    imageCache[key] = new kakao.maps.MarkerImage(
      markerSvg(color, type),
      new kakao.maps.Size(MARKER_W, MARKER_H),
      { offset: new kakao.maps.Point(MARKER_W / 2, MARKER_H) }, // 핀 끝(아래 가운데)이 좌표에 오게
    )
  }
  return imageCache[key]
}

// 마커 위 말풍선: "84.2 안심"
// gap: 라벨 꼬리 끝과 좌표 사이 거리(px) — 마커 높이만큼 띄움
function labelEl(total, grade, gap, onClick) {
  const color = GRADE_COLOR[grade] ?? '#444'
  const el = document.createElement('div')
  el.className = 'score-label'
  el.style.background = color
  el.style.setProperty('--tail', color)
  el.style.marginBottom = gap + 'px'
  el.innerHTML = `<b>${Number(total).toFixed(1)}</b><span>${grade}</span>`
  if (onClick) el.addEventListener('click', onClick)
  return el
}

// 선택한 캠핑장 하나에만 점수 라벨 표시
function drawSelectedLabel() {
  labels.forEach((o) => o.setMap(null))
  labels = []
  if (!map || props.selectedId == null) return
  const f = props.facilities.find((x) => x.id === props.selectedId)
  const sc = f && f.type === 'camp' ? props.scores[f.id] : null
  if (!sc) return
  const o = new kakao.maps.CustomOverlay({
    position: new kakao.maps.LatLng(f.lat, f.lng),
    content: labelEl(sc.totalScore, sc.grade, MARKER_H + 4),
    yAnchor: 1,
    zIndex: 6,
  })
  o.setMap(map)
  labels.push(o)
}

function drawMarkers() {
  if (!map) return
  clusterer.clear()
  campMarkers.forEach((m) => m.setMap(null))
  labels.forEach((o) => o.setMap(null))
  campMarkers = []
  labels = []
  const bounds = new kakao.maps.LatLngBounds()
  const clustered = []

  for (const f of props.facilities) {
    const pos = new kakao.maps.LatLng(f.lat, f.lng)
    bounds.extend(pos)
    const marker = new kakao.maps.Marker({
      position: pos,
      image: markerImage(props.colorOf(f), f.type),
      title: f.name,
    })
    kakao.maps.event.addListener(marker, 'click', () => emit('select', f))

    const sc = f.type === 'camp' ? props.scores[f.id] : null
    if (sc) {
      marker.setZIndex(5)
      marker.setMap(map)
      campMarkers.push(marker)
    } else {
      clustered.push(marker)
    }
  }
  clusterer.addMarkers(clustered)
  drawSelectedLabel()

  if (firstFit && props.facilities.length > 0) {
    map.setBounds(bounds)
    firstFit = false
  }
}

// 점수 지점 핀 + 가장 가까운 시설까지 점선
function drawScore() {
  if (!map) return
  pinMarker?.setMap(null)
  pinLabel?.setMap(null)
  lines.forEach((l) => l.setMap(null))
  linkMarkers.forEach((m) => m.setMap(null))
  lines = []
  linkMarkers = []
  if (!props.pin) return

  const p = new kakao.maps.LatLng(props.pin.lat, props.pin.lng)
  if (!props.hidePin) {
    pinMarker = new kakao.maps.Marker({ position: p, zIndex: 10 })
    pinMarker.setMap(map)
  }
  if (props.pinScore) {
    pinLabel = new kakao.maps.CustomOverlay({
      position: p,
      content: labelEl(props.pinScore.total, props.pinScore.grade, props.hidePin ? MARKER_H + 4 : 44),
      yAnchor: 1,
      zIndex: 20,
    })
    pinLabel.setMap(map)
  }

  for (const l of props.links) {
    if (l.lat == null || l.lng == null) continue
    const line = new kakao.maps.Polyline({
      path: [p, new kakao.maps.LatLng(l.lat, l.lng)],
      strokeWeight: 3,
      strokeColor: l.color,
      strokeOpacity: 0.9,
      strokeStyle: 'shortdash',
    })
    line.setMap(map)
    lines.push(line)

    // 점선 끝 시설 아이콘: 그 시설이 지금 지도에 안 보이면(종류가 꺼져 있으면) 따로 찍어줌
    if (l.type && !visibleIds.value.has(l.id)) {
      const m = new kakao.maps.Marker({
        position: new kakao.maps.LatLng(l.lat, l.lng),
        image: markerImage(l.color, l.type),
        title: l.name ?? '',
        zIndex: 8,
      })
      kakao.maps.event.addListener(m, 'click', () =>
        emit('select', { id: l.id, type: l.type, name: l.name, lat: l.lat, lng: l.lng }),
      )
      m.setMap(map)
      linkMarkers.push(m)
    }
  }
}

// 기준 위치 핀 (드래그 가능) + 추천 캠핑장까지 연결선
function drawBase() {
  if (!map) return
  baseMarker?.setMap(null)
  baseMarker = null
  if (!props.base) return
  baseMarker = new kakao.maps.Marker({
    position: new kakao.maps.LatLng(props.base.lat, props.base.lng),
    image: markerImage(BASE_COLOR, 'base'),
    title: '기준 위치 (드래그해서 옮기기)',
    draggable: true,
    zIndex: 15,
  })
  baseMarker.setMap(map)
  kakao.maps.event.addListener(baseMarker, 'dragend', () => {
    const pos = baseMarker.getPosition()
    emit('move-base', { lat: pos.getLat(), lng: pos.getLng() })
  })
}

function drawBaseLink() {
  if (!map) return
  baseLine?.setMap(null)
  baseLine = null
  if (!props.base || !props.baseLink) return
  baseLine = new kakao.maps.Polyline({
    path: [
      new kakao.maps.LatLng(props.base.lat, props.base.lng),
      new kakao.maps.LatLng(props.baseLink.lat, props.baseLink.lng),
    ],
    strokeWeight: 4,
    strokeColor: BASE_COLOR,
    strokeOpacity: 0.8,
    strokeStyle: 'dash',
  })
  baseLine.setMap(map)
}

// 기준 위치가 바뀌면 핀을 다시 그리고 그쪽으로 지도 이동
watch(
  () => props.base,
  (b) => {
    drawBase()
    drawBaseLink()
    if (!map || !b) return
    map.setLevel(Math.min(map.getLevel(), 8))
    map.panTo(new kakao.maps.LatLng(b.lat, b.lng))
  },
)
watch(() => props.baseLink, drawBaseLink)
// 위치 찍기 모드일 때 커서를 십자 모양으로
watch(
  () => props.pickMode,
  (on) => map?.setCursor(on ? 'crosshair' : ''),
)

watch(
  () => props.selectedId,
  (id) => {
    drawSelectedLabel()
    if (!map || id == null) return
    const f = props.facilities.find((x) => x.id === id)
    if (!f) return
    map.setLevel(Math.min(map.getLevel(), 5))
    map.panTo(new kakao.maps.LatLng(f.lat, f.lng))
  },
)

watch(() => [props.facilities, props.colorOf, props.scores], drawMarkers)
watch(() => [props.pin, props.links, props.pinScore, props.hidePin, props.facilities], drawScore)

onMounted(async () => {
  try {
    kakao = await loadKakaoMap()
    map = new kakao.maps.Map(mapEl.value, {
      center: new kakao.maps.LatLng(38.07, 128.17),
      level: 9,
    })
    map.addControl(new kakao.maps.ZoomControl(), kakao.maps.ControlPosition.RIGHT)
    map.addControl(new kakao.maps.MapTypeControl(), kakao.maps.ControlPosition.TOPRIGHT)
    clusterer = new kakao.maps.MarkerClusterer({ map, averageCenter: true, minLevel: 7 })

    // 지도 클릭 → App에서 처리 (찍기 모드면 위치 저장, 아니면 그 지점 점수 보기)
    kakao.maps.event.addListener(map, 'click', (e) => {
      emit('pick', { lat: e.latLng.getLat(), lng: e.latLng.getLng() })
    })

    drawMarkers()
    drawScore()
    drawBase()
    drawBaseLink()
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <div class="map-wrap">
    <div ref="mapEl" class="map"></div>
    <div v-if="!error" class="hint" :class="{ picking: pickMode }">{{ hintText }}</div>
    <div v-if="error" class="map-error">{{ error }}</div>
  </div>
</template>

<style scoped>
.map-wrap { position: relative; width: 100%; height: 100%; }
.map { width: 100%; height: 100%; }
.hint.picking { background: #2563eb; }
/* 좁은 화면: 왼쪽 위 버튼과 겹치지 않게 안내는 찍기 모드일 때만, 버튼 아래에 */
@media (max-width: 760px) {
  .hint { top: 60px; }
  .hint:not(.picking) { display: none; }
}
.hint {
  position: absolute; left: 50%; top: 12px; z-index: 2; transform: translateX(-50%);
  padding: 7px 12px; border-radius: 999px; background: #1c2024e6; color: #fff;
  font-size: 13px; pointer-events: none; white-space: nowrap;
}
.map-error {
  position: absolute; inset: 0; display: grid; place-items: center; padding: 24px;
  background: #f4f4f2; color: #b42318; font-weight: 600; text-align: center;
}
</style>

<style>
/* 카카오 CustomOverlay는 scoped 스타일이 안 먹어서 전역으로 */
.score-label {
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 999px;
  color: #fff;
  font-size: 12px;
  line-height: 1.2;
  white-space: nowrap;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
  cursor: pointer;
}
.score-label b { font-size: 13px; font-variant-numeric: tabular-nums; }
.score-label span { font-weight: 600; opacity: 0.95; }
.score-label::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -5px;
  transform: translateX(-50%);
  border: 5px solid transparent;
  border-bottom: 0;
  border-top-color: var(--tail, currentColor);
}
</style>
