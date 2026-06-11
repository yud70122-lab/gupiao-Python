import api from './index'

export const getAllStocks = () => api.get('/stocks')

export const searchStocks = (keyword) => api.get('/stocks/search', { params: { keyword } })

export const getKLineData = (code, startDate, endDate) =>
  api.get(`/stocks/${code}/kline`, { params: { startDate, endDate } })

export const getReturnAnalysis = (code, startDate, endDate) =>
  api.get(`/stocks/${code}/returns`, { params: { startDate, endDate } })

export const getVolatilityAnalysis = (code, startDate, endDate) =>
  api.get(`/stocks/${code}/volatility`, { params: { startDate, endDate } })

export const getCorrelationMatrix = (codes, startDate, endDate) =>
  api.post('/stocks/correlation', null, { params: { codes, startDate, endDate } })

export const getRiskReturnScatter = (codes, startDate, endDate) =>
  api.post('/stocks/risk-return', null, { params: { codes, startDate, endDate } })

export default api
