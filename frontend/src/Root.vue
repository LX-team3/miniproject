<script setup>
// 화면 전환 (vue-router 없이 주소 뒤 # 값으로 고름)
//   localhost:5173/          → 메인 화면      (views/HomeView.vue)
//   localhost:5173/#/level   → 설문(수준 진단) (components/level.vue)
//   localhost:5173/#/map     → 지도 화면      (App.vue)
// 브라우저 뒤로가기·앞으로가기도 그대로 동작합니다.
import { computed, onMounted, onUnmounted, ref } from 'vue'
import HomeView from './views/HomeView.vue'
import Level from './components/level.vue'
import App from './App.vue'

// 주소 → 화면
const ROUTES = {
  '#/level': Level,
  '#/map': App,
}

// HomeView 버튼이 보내는 이름 → 이동할 주소
//   '내 캠핑 점수 확인' 버튼 → emit('go', 'score') → 설문
//   '안전 지도 보기'    버튼 → emit('go', 'map')   → 지도
const GO = {
  home: '#/',
  score: '#/level',
  map: '#/map',
}

// '#/map?type=A' 처럼 뒤에 값이 붙어도 화면은 찾도록 ? 앞만 봄
const hash = ref(window.location.hash.split('?')[0])
const view = computed(() => ROUTES[hash.value] ?? HomeView)

function onHashChange() {
  hash.value = window.location.hash.split('?')[0]
  window.scrollTo(0, 0)
}

function go(name) {
  window.location.hash = GO[name] ?? '#/'
}

onMounted(() => window.addEventListener('hashchange', onHashChange))
onUnmounted(() => window.removeEventListener('hashchange', onHashChange))
</script>

<template>
  <component :is="view" @go="go" />
</template>
