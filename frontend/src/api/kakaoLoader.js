// 카카오맵 SDK를 한 번만 불러오는 함수
let loading = null

export function loadKakaoMap() {
  if (window.kakao?.maps?.LatLng) return Promise.resolve(window.kakao)
  if (loading) return loading

  const key = import.meta.env.VITE_KAKAO_JS_KEY
  if (!key || key.startsWith('여기에')) {
    return Promise.reject(new Error('frontend/.env 에 VITE_KAKAO_JS_KEY 를 넣어주세요.'))
  }

  loading = new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${key}&autoload=false&libraries=clusterer`
    script.onload = () => window.kakao.maps.load(() => resolve(window.kakao))
    script.onerror = () =>
      reject(new Error('카카오맵 SDK를 불러오지 못했습니다. 키와 플랫폼(Web) 도메인 등록을 확인하세요.'))
    document.head.appendChild(script)
  })
  return loading
}
