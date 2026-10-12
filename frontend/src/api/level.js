import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

// 질문지 + 선택지
export async function fetchLevelQuestions() {
  const { data } = await api.get('/level/questions')
  return data
}

// 답변 채점 → 점수·수준
// answers 예) { count: ['count_5_9'], safety: ['safety_aid', 'safety_ext'], ... }
export async function postLevelAnswers(answers) {
  const { data } = await api.post('/level', { answers })
  return data
}
