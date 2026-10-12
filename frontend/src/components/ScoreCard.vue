<script setup>
import { GRADE_COLOR, typeInfo } from '../facilityTypes'

defineProps({
  score: { type: Object, required: true }, // /api/score 응답
  title: { type: String, default: '선택한 위치' },
})
const emit = defineEmits(['close'])

function km(m) {
  if (m == null) return '없음'
  return m >= 1000 ? `${(m / 1000).toFixed(1)}km` : `${Math.round(m)}m`
}
</script>

<template>
  <div class="score">
    <div class="score-head">
      <div>
        <p class="score-title">{{ title }}</p>
        <p class="score-total">
          <strong>{{ score.total.toFixed(1) }}</strong><span>/ 100</span>
        </p>
      </div>
      <span class="grade" :style="{ background: GRADE_COLOR[score.grade] }">{{ score.grade }}</span>
      <button class="close" @click="emit('close')" aria-label="닫기">×</button>
    </div>

    <ul class="items">
      <li v-for="it in score.items" :key="it.type">
        <div class="row">
          <span class="dot" :style="{ background: typeInfo(it.type).color }"></span>
          <span class="name">{{ typeInfo(it.type).label }}</span>
          <span class="pt">{{ it.point.toFixed(1) }}<small> / {{ it.weight }}</small></span>
        </div>
        <div class="bar">
          <div
            class="fill"
            :style="{ width: (it.point / it.weight) * 100 + '%', background: typeInfo(it.type).color }"
          ></div>
        </div>
        <p class="near">
          <template v-if="it.name">{{ it.name }} · {{ km(it.distM) }}</template>
          <template v-else>주변에 없음</template>
          <span class="rule">{{ km(it.fullDistM) }} 안 만점 · {{ km(it.zeroDistM) }} 밖 0점</span>
        </p>
      </li>
    </ul>
    <p class="note">가장 가까운 시설까지의 직선거리로 계산한 참고 점수입니다.</p>
  </div>
</template>

<style scoped>
.score { margin-top: 14px; padding: 14px; border: 1px solid #d9d9d4; border-radius: 10px; background: #fbfbf9; }
.score-head { display: flex; align-items: flex-start; gap: 10px; }
.score-head > div { flex: 1; }
.score-title { margin: 0; font-size: 12px; color: #6b6f76; }
.score-total { margin: 2px 0 0; }
.score-total strong { font-size: 32px; font-variant-numeric: tabular-nums; }
.score-total span { margin-left: 4px; color: #6b6f76; font-size: 13px; }
.grade { margin-top: 6px; padding: 4px 10px; border-radius: 999px; color: #fff; font-size: 13px; font-weight: 700; }
.close { border: 0; background: none; font-size: 20px; line-height: 1; cursor: pointer; color: #6b6f76; }
.items { list-style: none; margin: 12px 0 0; padding: 0; display: grid; gap: 10px; }
.row { display: flex; align-items: center; gap: 8px; font-size: 13px; }
.name { flex: 1; }
.pt { font-weight: 700; font-variant-numeric: tabular-nums; }
.pt small { color: #6b6f76; font-weight: 400; }
.dot { width: 10px; height: 10px; border-radius: 50%; }
.bar { height: 6px; margin-top: 4px; border-radius: 3px; background: #e9e9e5; overflow: hidden; }
.fill { height: 100%; border-radius: 3px; }
.near { margin: 3px 0 0; font-size: 12px; color: #3c4047; }
.rule { display: block; color: #8b8d91; font-size: 11px; }
.note { margin: 12px 0 0; font-size: 11px; color: #8b8d91; }
</style>
