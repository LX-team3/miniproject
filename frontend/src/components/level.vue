<script setup>
// 캠핑 수준 진단 화면 — 주소: localhost:5173/#/level
import { computed, onMounted, reactive, ref } from 'vue'
import { fetchLevelQuestions, postLevelAnswers } from '../api/level'

// 설문이 끝나고 갈 주소 → 지도 화면 (Root.vue의 '#/map')
const MAIN_HASH = '#/'
const MAP_HASH = '#/map'

const LEVEL_COLOR = { 입문: '#c8892b', 중급: '#4f7d4a', 숙련: '#2f5d3a' }
const LEVEL_ICON = { 입문: '🌱', 중급: '🌲', 숙련: '⛰️' }

const view = ref('intro') // intro / quiz / result
const questions = ref([])
const answers = reactive({}) // { 질문id: [선택지id, ...] }
const step = ref(0)
const result = ref(null)
const loadError = ref('')
const submitError = ref('')
const submitting = ref(false)

const current = computed(() => questions.value[step.value])
const picked = computed(() => (current.value && answers[current.value.id]) || [])
const isLast = computed(() => step.value === questions.value.length - 1)
const progress = computed(() => ((step.value + 1) / questions.value.length) * 100)
const levelColor = computed(() => LEVEL_COLOR[result.value?.level] || '#2f5d3a')
const levelIcon = computed(() => LEVEL_ICON[result.value?.level] || '')
const regionText = computed(() =>
  result.value?.regionLabels?.length ? result.value.regionLabels.join(', ') : '전국',
)

// 질문지 불러오기 (비동기)
onMounted(async () => {
  try {
    questions.value = await fetchLevelQuestions()
  } catch (e) {
    loadError.value = '질문을 불러오지 못했어요. 스프링부트 서버(8080)가 켜져 있는지 확인하세요.'
    console.error(e)
  }
})

function goMain() {
  window.location.hash = MAIN_HASH
}

function goMap() {
  window.location.hash = MAP_HASH
}

function start() {
  Object.keys(answers).forEach((k) => delete answers[k])
  step.value = 0
  submitError.value = ''
  view.value = 'quiz'
}

function pick(option, checked) {
  const q = current.value
  let list = answers[q.id] || []

  if (q.inputType === 'single') {
    list = [option.id]
  } else if (checked) {
    // '해당 없음/상관없어요'를 고르면 나머지 해제, 다른 걸 고르면 그 항목 해제
    list = option.exclusive
      ? [option.id]
      : list.filter((id) => !q.options.find((o) => o.id === id).exclusive).concat(option.id)
  } else {
    list = list.filter((id) => id !== option.id)
  }
  answers[q.id] = list
  submitError.value = ''

  // 하나만 고르는 질문은 고르면 바로 다음으로
  if (q.inputType === 'single' && !isLast.value) setTimeout(next, 220)
}

function prev() {
  if (step.value > 0) step.value--
}

function next() {
  if (!picked.value.length) return
  if (!isLast.value) {
    step.value++
    return
  }
  submit()
}

// 답변 전송 → 점수 바로 표시 (비동기)
async function submit() {
  submitting.value = true
  submitError.value = ''
  try {
    const data = await postLevelAnswers(answers)
    result.value = data
    // 지도 화면에서도 쓸 수 있게 저장 → JSON.parse(localStorage.getItem('campLevel'))
    try {
      localStorage.setItem(
        'campLevel',
        JSON.stringify({ totalScore: data.totalScore, level: data.level, minSafeScore: data.minSafeScore }),
      )
    } catch {}
    view.value = 'result'
    window.scrollTo(0, 0)
  } catch (e) {
    submitError.value = e.response?.data?.message || '결과를 계산하지 못했어요. 서버 콘솔을 확인하세요.'
    console.error(e)
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="lv-page">
    <header class="lv-top">
      <div class="lv-logo">⛺ 캠핑세이프</div>
      <button class="lv-btn-main" @click="goMain">메인 화면으로 →</button>
    </header>

    <div class="lv-wrap">
      <!-- 1. 시작 화면 -->
      <section v-if="view === 'intro'" class="lv-card">
        <h1 class="lv-h1">나의 캠핑 수준 진단</h1>
        <p class="lv-lead">
          몇 가지 질문에 답하면 캠핑 경험을 100점 만점으로 점수화하고,<br />
          내 수준에 맞는 캠핑장 안심 기준을 알려 드립니다. (약 1분)
        </p>
        <div class="lv-levels">
          <div><b>🌱 입문 (0~39점)</b>'안심' 등급 캠핑장 권장</div>
          <div><b>🌲 중급 (40~69점)</b>'보통' 이상 캠핑장 권장</div>
          <div><b>⛰️ 숙련 (70점~)</b>모든 캠핑장 가능</div>
        </div>
        <button class="lv-btn primary" :disabled="!questions.length" @click="start">
          {{ questions.length ? '진단 시작하기' : '질문 불러오는 중…' }}
        </button>
        <div class="lv-error">{{ loadError }}</div>
      </section>

      <!-- 2. 질문 화면 -->
      <section v-else-if="view === 'quiz' && current" class="lv-card">
        <div class="lv-progress"><span :style="{ width: progress + '%' }"></span></div>
        <div class="lv-step">{{ step + 1 }} / {{ questions.length }}</div>

        <h2 class="lv-q-title">
          {{ current.title }}
          <span v-if="current.inputType === 'multi'" class="lv-tag">여러 개 선택</span>
        </h2>
        <p class="lv-q-help">{{ current.helpText || '' }}</p>

        <div class="lv-options">
          <label
            v-for="o in current.options"
            :key="o.id"
            class="lv-opt"
            :class="{ checked: picked.includes(o.id) }"
          >
            <input
              :type="current.inputType === 'multi' ? 'checkbox' : 'radio'"
              :name="current.id"
              :checked="picked.includes(o.id)"
              @change="pick(o, $event.target.checked)"
            />
            <span>{{ o.label }}</span>
          </label>
        </div>

        <div class="lv-error">{{ submitError }}</div>
        <div class="lv-nav">
          <button class="lv-btn" :style="{ visibility: step === 0 ? 'hidden' : 'visible' }" @click="prev">
            ← 이전
          </button>
          <button class="lv-btn primary" :disabled="!picked.length || submitting" @click="next">
            {{ submitting ? '계산 중…' : isLast ? '결과 보기' : '다음 →' }}
          </button>
        </div>
      </section>

      <!-- 3. 결과 화면 -->
      <section v-else-if="view === 'result' && result" class="lv-card">
        <div class="lv-result-top">
          <div
            class="lv-gauge"
            :style="{ background: `conic-gradient(${levelColor} ${result.totalScore * 3.6}deg, #e2ddd0 0)` }"
          >
            <div class="lv-gauge-inner">
              <div><b>{{ result.totalScore }}</b><small>/ 100점</small></div>
            </div>
          </div>
          <div>
            <span class="lv-badge" :style="{ background: levelColor }">{{ levelIcon }} {{ result.level }}</span>
            <h2 class="lv-r-title">당신은 {{ result.level }} 캠퍼예요</h2>
            <p class="lv-r-desc">{{ result.description }}</p>
          </div>
        </div>

        <h3 class="lv-h3">점수 근거</h3>
        <div v-for="it in result.items" :key="it.questionId" class="lv-bar-row">
          <span>{{ it.title }}</span>
          <span class="lv-pt">{{ it.point }} / {{ it.maxPoint }}</span>
          <div class="lv-bar">
            <span :style="{ width: (it.maxPoint ? (it.point / it.maxPoint) * 100 : 0) + '%' }"></span>
          </div>
          <span class="lv-ans">{{ it.answer }}</span>
        </div>

        <h3 class="lv-h3">나에게 맞는 캠핑장 기준</h3>
        <div class="lv-filter">
          선호 지역: <b>{{ regionText }}</b><br />
          권장 안심 점수:
          <b>{{ result.minSafeScore > 0 ? result.minSafeScore + '점 이상' : '제한 없음' }}</b>
          <span v-if="result.withKids" class="lv-muted"> (아이 동반이라 한 단계 더 엄격하게 잡았어요)</span><br />
          <span class="lv-muted">메인 화면 지도에서 이 기준으로 캠핑장을 골라 보세요.</span>
        </div>

        <div class="lv-nav">
          <button class="lv-btn" @click="view = 'intro'">다시 진단하기</button>
          <button class="lv-btn primary" @click="goMap">지도 화면으로 →</button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* 지도 화면 style.css 와 겹치지 않도록 모든 클래스에 lv- 를 붙임 */
.lv-page {
  --bg: #f4f1ea; --card: #fffdf8; --ink: #22301f; --muted: #6b7363; --line: #e2ddd0;
  --green: #2f5d3a; --green-soft: #e3ecdf; --red: #b3483a;
  min-height: 100%; background: var(--bg); color: var(--ink); overflow-y: auto;
}
button { font: inherit; cursor: pointer; }

.lv-top { display: flex; justify-content: space-between; align-items: center;
          max-width: 720px; margin: 0 auto; padding: 20px 16px; }
.lv-logo { font-weight: 800; font-size: 20px; color: var(--green); letter-spacing: -0.5px; }
.lv-btn-main { border: 1px solid var(--green); background: transparent; color: var(--green);
               padding: 8px 14px; border-radius: 999px; font-weight: 600; }
.lv-btn-main:hover { background: var(--green); color: #fff; }

.lv-wrap { max-width: 720px; margin: 0 auto; padding: 0 16px 48px; }
.lv-card { background: var(--card); border: 1px solid var(--line); border-radius: 18px; padding: 28px; }

.lv-h1 { margin: 0 0 10px; font-size: 28px; letter-spacing: -0.8px; }
.lv-lead { color: var(--muted); line-height: 1.6; margin: 0 0 22px; }
.lv-levels { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-bottom: 24px; }
.lv-levels div { background: var(--green-soft); border-radius: 12px; padding: 12px; font-size: 14px; }
.lv-levels b { display: block; margin-bottom: 4px; }

.lv-progress { height: 6px; background: var(--line); border-radius: 99px; overflow: hidden; margin-bottom: 8px; }
.lv-progress span { display: block; height: 100%; background: var(--green); transition: width 0.3s; }
.lv-step { font-size: 13px; color: var(--muted); margin-bottom: 18px; }

.lv-q-title { font-size: 22px; font-weight: 700; margin: 0 0 6px; letter-spacing: -0.5px; }
.lv-q-help { color: var(--muted); font-size: 14px; margin: 0 0 18px; min-height: 1em; }
.lv-tag { display: inline-block; font-size: 12px; padding: 2px 8px; border-radius: 99px;
          background: var(--green-soft); color: var(--green); margin-left: 6px; vertical-align: middle; }
.lv-options { display: grid; gap: 10px; }
.lv-opt { display: flex; align-items: center; gap: 12px; padding: 14px 16px; border: 1.5px solid var(--line);
          border-radius: 12px; background: #fff; cursor: pointer; transition: border-color 0.15s, background 0.15s; }
.lv-opt:hover { border-color: #b9c4b0; }
.lv-opt input { width: 18px; height: 18px; accent-color: var(--green); margin: 0; }
.lv-opt.checked { border-color: var(--green); background: var(--green-soft); }

.lv-nav { display: flex; justify-content: space-between; margin-top: 26px; gap: 10px; }
.lv-btn { padding: 12px 22px; border-radius: 12px; border: 1px solid var(--line); background: #fff; font-weight: 600; }
.lv-btn.primary { background: var(--green); color: #fff; border-color: var(--green); }
.lv-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.lv-error { color: var(--red); font-size: 14px; margin-top: 12px; min-height: 1em; }

.lv-result-top { display: flex; gap: 24px; align-items: center; flex-wrap: wrap; }
.lv-gauge { width: 140px; height: 140px; border-radius: 50%; display: grid; place-items: center; flex-shrink: 0; }
.lv-gauge-inner { width: 112px; height: 112px; border-radius: 50%; background: var(--card);
                  display: grid; place-items: center; text-align: center; }
.lv-gauge-inner b { font-size: 34px; display: block; line-height: 1; }
.lv-gauge-inner small { color: var(--muted); }
.lv-badge { display: inline-block; padding: 4px 12px; border-radius: 99px; color: #fff; font-weight: 700; font-size: 14px; }
.lv-r-title { margin: 8px 0; font-size: 24px; }
.lv-r-desc { margin: 0; color: var(--muted); line-height: 1.6; }
.lv-h3 { font-size: 16px; margin: 28px 0 12px; }
.lv-bar-row { display: grid; grid-template-columns: 1fr 64px; gap: 4px 12px; margin-bottom: 12px; font-size: 14px; }
.lv-ans { color: var(--muted); font-size: 13px; grid-column: 1 / -1; }
.lv-bar { grid-column: 1 / -1; height: 8px; background: var(--line); border-radius: 99px; overflow: hidden; }
.lv-bar span { display: block; height: 100%; background: var(--green); }
.lv-pt { text-align: right; font-weight: 600; }
.lv-filter { background: var(--green-soft); border-radius: 12px; padding: 12px 14px; font-size: 14px; line-height: 1.6; }
.lv-muted { color: var(--muted); }

@media (max-width: 520px) {
  .lv-card { padding: 20px; }
  .lv-levels { grid-template-columns: 1fr; }
  .lv-h1 { font-size: 24px; }
}
</style>