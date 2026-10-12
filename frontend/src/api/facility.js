import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

export async function fetchFacilities() {
  const { data } = await api.get('/facilities')
  return data
}

export async function fetchTypeCounts() {
  const { data } = await api.get('/facilities/types')
  return data
}
