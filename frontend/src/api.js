import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 10000
})

let currentUserId = null

http.interceptors.request.use((config) => {
  if (currentUserId) {
    config.headers['X-User-Id'] = currentUserId
  }
  return config
})

export function setCurrentUserId(userId) {
  currentUserId = userId
}

export const api = {
  leads: (params) => http.get('/leads', { params }).then((r) => r.data),
  leadDetail: (id) => http.get(`/leads/${id}`).then((r) => r.data),
  createLead: (payload) => http.post('/leads', payload).then((r) => r.data),
  updateStatus: (id, payload) => http.patch(`/leads/${id}/status`, payload).then((r) => r.data),
  assign: (id, payload) => http.patch(`/leads/${id}/assign`, payload).then((r) => r.data),
  addFollowUp: (id, payload) => http.post(`/leads/${id}/follow-ups`, payload).then((r) => r.data),
  importLeads: (formData) => http.post('/leads/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then((r) => r.data),
  salesUsers: () => http.get('/sales-users').then((r) => r.data),
  funnel: () => http.get('/dashboard/funnel').then((r) => r.data),
  anomalies: () => http.get('/dashboard/anomalies').then((r) => r.data),
  callbackCandidates: () => http.get('/callbacks/candidates').then((r) => r.data),
  callCallback: (payload) => http.post('/callbacks/call-result', payload).then((r) => r.data),
  wechatCallback: (payload) => http.post('/callbacks/wechat-result', payload).then((r) => r.data),
  ask: (question) => http.post('/query/natural-language', { question }).then((r) => r.data)
}

export function errorMessage(error) {
  return error?.response?.data?.message || error?.message || '操作失败'
}
