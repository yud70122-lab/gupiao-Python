import api from './index'

export const getCollectionBasicInfo = (keyword, exchange, industry) =>
  api.get('/collection/basic', { params: { keyword, exchange, industry } })

export const collectBasicInfo = () =>
  api.post('/collection/basic/collect')

export const getCollectionMarketStocks = () =>
  api.get('/collection/market/stocks')

export const getCollectionMarketData = (code, period, startDate, endDate) =>
  api.get('/collection/market', { params: { code, period, startDate, endDate } })

export const collectMarketData = (code) =>
  api.post('/collection/market/collect', null, { params: { code } })

export const getCollectionFinancialStocks = () =>
  api.get('/collection/financial/stocks')

export const getCollectionFinancialData = (code, reportType, period) =>
  api.get('/collection/financial', { params: { code, reportType, period } })

export const collectFinancialData = (code) =>
  api.post('/collection/financial/collect', null, { params: { code } })

export const getCollectionMarketOverview = () =>
  api.get('/collection/overview')

export const collectMarketOverview = () =>
  api.post('/collection/overview/collect')

export default api
