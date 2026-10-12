import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

// 아무 지점의 안심 점수
export async function fetchScore(lat, lng) {
  const { data } = await api.get('/score', { params: { lat, lng } })
  return data
}

// 캠핑장 전체 점수 순위
export async function fetchCampScores() {
  const { data } = await api.get('/score/camps')
  return data
}
