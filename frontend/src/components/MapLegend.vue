<script setup>
import { computed, ref } from 'vue'
import { markerSvg, GRADE_COLOR } from '../facilityTypes'

// types: [{ type, label, color }] — 지금 지도에 켜져 있는 시설 종류
const props = defineProps({
  types: { type: Array, required: true },
  showGrades: { type: Boolean, default: false }, // 캠핑장 등급 색 설명 표시 여부
})

const open = ref(true)

// 등급을 따로 보여줄 때는 캠핑장을 위 목록에서 빼기 (캠핑장 마커는 등급 색으로 칠해지니까)
const list = computed(() => (props.showGrades ? props.types.filter((t) => t.type !== 'camp') : props.types))
</script>

<template>
  <div class="legend-box">
    <button class="legend-head" @click="open = !open" :aria-expanded="open">
      범례 <span>{{ open ? '▾' : '▸' }}</span>
    </button>
    <div v-if="open" class="legend-body">
      <ul>
        <li v-for="t in list" :key="t.type">
          <img :src="markerSvg(t.color, t.type)" alt="" width="18" height="23" />
          {{ t.label }}
        </li>
      </ul>
      <p v-if="!list.length && !showGrades" class="legend-none">켜진 시설이 없어요</p>
      <template v-if="showGrades">
        <p class="legend-sub" :class="{ first: !list.length }">캠핑장 (안심 등급)</p>
        <ul class="grades">
          <li v-for="(c, g) in GRADE_COLOR" :key="g">
            <img :src="markerSvg(c, 'camp')" alt="" width="18" height="23" />
            {{ g }}
          </li>
        </ul>
      </template>
    </div>
  </div>
</template>

<style scoped>
.legend-box {
  position: absolute; left: 12px; bottom: 32px; z-index: 3; /* 카카오 로고 위 */
  min-width: 132px; max-height: calc(100% - 80px); overflow-y: auto;
  background: rgba(255, 255, 255, 0.95); border: 1px solid #e4e4e0; border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.12); font-size: 12px;
}
.legend-head {
  display: flex; justify-content: space-between; align-items: center; width: 100%;
  padding: 8px 10px; border: 0; background: none; font-size: 12px; font-weight: 700; cursor: pointer; color: #1c2024;
}
.legend-body { padding: 0 10px 10px; }
ul { list-style: none; margin: 0; padding: 0; display: grid; gap: 4px; }
li { display: flex; align-items: center; gap: 6px; white-space: nowrap; }
.grades { grid-template-columns: repeat(3, auto); gap: 4px 8px; }
.legend-sub { margin: 8px 0 4px; padding-top: 6px; border-top: 1px solid #ececea; color: #6b6f76; font-size: 11px; }
.legend-sub.first { margin-top: 0; padding-top: 0; border-top: 0; }
.legend-none { margin: 0; color: #8b8d91; }
</style>
