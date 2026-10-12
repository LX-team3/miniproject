<script setup>
import { GRADE_COLOR } from '../facilityTypes'

// 기준 위치 주변 추천 목록.
// items: [{ id, name, distKm, totalScore, grade }] — 나중에 도로 기반 노지 후보 API로 바꿔도 이 모양만 맞추면 됨
defineProps({
  items: { type: Array, required: true },
  radiusKm: { type: Number, required: true },
  activeId: { type: [String, Number], default: null },
})
const emit = defineEmits(['select', 'update:radiusKm'])
const RADIUS_OPTIONS = [5, 10, 20]
</script>

<template>
  <section class="nearby">
    <div class="nearby-head">
      <h2>주변 안전 캠핑장 추천</h2>
      <div class="radius" role="group" aria-label="반경">
        <button
          v-for="r in RADIUS_OPTIONS"
          :key="r"
          :class="{ on: r === radiusKm }"
          @click="emit('update:radiusKm', r)"
        >
          {{ r }}km
        </button>
      </div>
    </div>

    <ol v-if="items.length" class="nearby-list">
      <li
        v-for="(c, i) in items"
        :key="c.id"
        :class="{ active: c.id === activeId }"
        @click="emit('select', c)"
      >
        <span class="no">{{ i + 1 }}</span>
        <span class="nname">
          <strong>{{ c.name }}</strong>
          <small>기준 위치에서 {{ c.distKm.toFixed(1) }}km</small>
        </span>
        <span class="pill" :style="{ background: GRADE_COLOR[c.grade] }">{{ c.totalScore.toFixed(1) }}</span>
      </li>
    </ol>
    <p v-else class="nearby-none">반경 {{ radiusKm }}km 안에 캠핑장이 없어요. 반경을 넓혀보세요.</p>
    <p class="nearby-note">등록 캠핑장 기준 · 직선거리 · 점수 높은 순</p>
  </section>
</template>

<style scoped>
.nearby { margin-top: 18px; }
.nearby-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.nearby-head h2 { margin: 0; font-size: 14px; }
.radius { display: flex; border: 1px solid #d9d9d4; border-radius: 8px; overflow: hidden; }
.radius button { padding: 4px 8px; border: 0; background: #fff; font-size: 12px; cursor: pointer; color: #3c4047; }
.radius button + button { border-left: 1px solid #d9d9d4; }
.radius button.on { background: #1c2024; color: #fff; }
.nearby-list { list-style: none; margin: 10px 0 0; padding: 0; border: 1px solid #ececea; border-radius: 8px; }
.nearby-list li { display: flex; align-items: center; gap: 8px; padding: 8px 10px; cursor: pointer; border-bottom: 1px solid #f1f1ee; }
.nearby-list li:last-child { border-bottom: 0; }
.nearby-list li:hover { background: #f1f1ee; }
.nearby-list li.active { background: #e6f4ea; }
.no { width: 16px; color: #8b8d91; font-size: 12px; }
.nname { flex: 1; min-width: 0; }
.nname strong { display: block; font-size: 13px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.nname small { color: #6b6f76; font-size: 11px; }
.pill { padding: 2px 8px; border-radius: 999px; color: #fff; font-size: 12px; font-weight: 700; }
.nearby-none { margin: 10px 0 0; padding: 12px; border: 1px dashed #d0d0ca; border-radius: 8px; color: #6b6f76; font-size: 13px; }
.nearby-note { margin: 6px 0 0; color: #8b8d91; font-size: 11px; }
</style>
