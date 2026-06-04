import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 60000
})

export const calculateCorrelation = (params) =>
  api.post('/quant/correlation', params)

export const calculateLinkage = (params) =>
  api.post('/quant/linkage', params)

export const getSectors = () =>
  api.get('/quant/sectors')

export const getIndices = () =>
  api.get('/quant/indices')

export const exportCorrelationExcel = (rows) =>
  api.post('/quant/export/correlation', rows, {
    responseType: 'blob'
  })

export const exportLinkageExcel = (rows) =>
  api.post('/quant/export/linkage', rows, {
    responseType: 'blob'
  })

export default api
