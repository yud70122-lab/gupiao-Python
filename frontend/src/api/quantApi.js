import api from './index'

export const calculateCorrelation = (params) =>
  api.post('/quant/correlation', params)

export const calculateCorrelationSync = (params) =>
  api.post('/quant/correlation/sync', params)

export const submitAsyncCorrelation = (params) =>
  api.post('/quant/correlation/async/submit', params)

export const queryAsyncCorrelation = (taskId) =>
  api.get('/quant/correlation/async/query', { params: { taskId } })

export const stopAsyncCorrelation = (taskId) =>
  api.post('/quant/correlation/async/stop', { taskId })

export const saveCorrelationScheme = (scheme) =>
  api.post('/quant/correlation/scheme', scheme)

export const listCorrelationSchemes = () =>
  api.get('/quant/correlation/scheme')

export const deleteCorrelationScheme = (id) =>
  api.delete(`/quant/correlation/scheme/${id}`)

export const calculateLinkage = (params) =>
  api.post('/quant/linkage', params)

export const getSectors = () =>
  api.get('/quant/sectors')

export const getIndices = () =>
  api.get('/quant/indices')

export const exportCorrelationExcel = (rows) =>
  api.post('/quant/export/correlation/excel', rows, {
    responseType: 'blob'
  })

export const exportCorrelationCSV = (rows) =>
  api.post('/quant/export/correlation/csv', rows, {
    responseType: 'blob'
  })

export const exportLinkageExcel = (rows) =>
  api.post('/quant/export/linkage', rows, {
    responseType: 'blob'
  })

export default api
