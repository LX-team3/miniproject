<script setup>
// 앱을 켜면 처음 나오는 메인 화면
// 버튼 2개: 내 캠핑 점수 확인 / 위치 지도로 가기
const emit = defineEmits(['go'])
</script>

<template>
  <div class="home">
    <header class="top">
      <h1>오늘 캠핑 여기서 해도 괜찮을까?</h1>
      <p class="sub">주변 응급실·파출소·편의점·CCTV까지의 거리로 캠핑 장소의 안심 점수를 알려드려요.</p>
    </header>

    <main class="menu">
      <button class="tile score" @click="emit('go', 'score')">
        <span class="icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 3l7 3v5c0 4.5-3 8.5-7 10-4-1.5-7-5.5-7-10V6l7-3z" />
            <path d="M9 12l2 2 4-4" />
          </svg>
        </span>
        <span class="tile-label">본인 캠핑 점수</span>
        <strong class="tile-title">내 캠핑 점수 확인</strong>
        <span class="tile-desc">지금 위치나 가려는 캠핑장의<br />안전 점수를 보여줘요</span>
        <span class="go">확인하기 →</span>
      </button>

      <button class="tile map" @click="emit('go', 'map')">
        <span class="icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="28" height="28" fill="none" stroke="currentColor" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <path d="M9 4L3 6v14l6-2 6 2 6-2V4l-6 2-6-2z" />
            <path d="M9 4v14M15 6v14" />
          </svg>
        </span>
        <span class="tile-label">위치 지도</span>
        <strong class="tile-title">안전 지도 보기</strong>
        <span class="tile-desc">캠핑장과 주변 안전 시설을<br />지도에서 한눈에 비교해요</span>
        <span class="go">지도로 가기 →</span>
      </button>
    </main>

    <p class="foot">점수는 가장 가까운 시설까지의 직선거리로 계산한 참고 값입니다.</p>
  </div>
</template>

<style scoped>
.home {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 36px;
  padding: 40px 16px;
  background:
    radial-gradient(circle at 20% 0%, #e6f4ea 0, transparent 45%),
    radial-gradient(circle at 100% 100%, #e1f3f1 0, transparent 40%),
    #f7f7f5;
}
.top { text-align: center; max-width: 560px; }
.eyebrow { margin: 0; font-size: 13px; font-weight: 700; color: #30a46c; letter-spacing: .04em; }
h1 { margin: 8px 0 10px; font-size: 28px; line-height: 1.3; word-break: keep-all; }
.sub { margin: 0; font-size: 15px; color: #6b6f76; line-height: 1.6; word-break: keep-all; }

.menu {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 260px));
  gap: 20px;
}
.tile {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  min-height: 260px;
  padding: 24px 22px;
  border: 1px solid #e4e4e0;
  border-radius: 18px;
  background: #fff;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 1px 2px #0000000a, 0 6px 20px #0000000a;
  transition: transform .15s ease, box-shadow .15s ease, border-color .15s ease;
}
.tile:hover { transform: translateY(-3px); box-shadow: 0 2px 4px #0000000d, 0 12px 28px #00000014; }
.tile:focus-visible { outline: 3px solid #30a46c66; outline-offset: 3px; }
.icon {
  display: grid;
  place-items: center;
  width: 52px;
  height: 52px;
  margin-bottom: 10px;
  border-radius: 14px;
  color: #fff;
}
.score .icon { background: #18794e; }
.map .icon { background: #0d9488; }
.score:hover { border-color: #18794e55; }
.map:hover { border-color: #0d948855; }
.tile-label { font-size: 12px; font-weight: 700; color: #8b8d91; }
.tile-title { font-size: 20px; }
.tile-desc { font-size: 13px; line-height: 1.6; color: #6b6f76; word-break: keep-all; }
.go { margin-top: auto; padding-top: 14px; font-size: 14px; font-weight: 700; }
.score .go { color: #18794e; }
.map .go { color: #0d9488; }

.foot { margin: 0; font-size: 12px; color: #8b8d91; text-align: center; }

@media (max-width: 600px) {
  .home { justify-content: flex-start; gap: 28px; }
  h1 { font-size: 23px; }
  .menu { grid-template-columns: 1fr; width: 100%; max-width: 420px; }
  .tile { min-height: 0; }
}
</style>
