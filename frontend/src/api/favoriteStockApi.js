import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export const getGroups = () => api.get('/favorite/groups')

export const createGroup = (name) => api.post('/favorite/groups', { name })

export const updateGroup = (groupId, name) => api.put(`/favorite/groups/${groupId}`, { name })

export const deleteGroup = (groupId) => api.delete(`/favorite/groups/${groupId}`)

export const getFavoriteStocks = (groupId) => api.get('/favorite/stocks', { params: { groupId } })

export const addFavoriteStock = (code, groupId) => api.post('/favorite/stocks', { code, groupId })

export const addFavoriteStocksBatch = (codes, groupId) => api.post('/favorite/stocks/batch', { codes, groupId })

export const deleteFavoriteStock = (code) => api.delete(`/favorite/stocks/${code}`)

export const deleteFavoriteStocksBatch = (codes) => api.delete('/favorite/stocks/batch', { data: { codes } })

export const exportToExcel = (groupId, codes) => api.get('/favorite/export/excel', { 
  params: { groupId, codes: codes ? codes.join(',') : null },
  responseType: 'blob'
})

export const exportToCSV = (groupId, codes) => api.get('/favorite/export/csv', {
  params: { groupId, codes: codes ? codes.join(',') : null },
  responseType: 'blob'
})

export const searchAvailableStocks = (keyword) => api.get('/favorite/stocks/search', { params: { keyword } })

export const checkStockExists = (code) => api.get(`/favorite/stocks/check/${code}`)

export default api
