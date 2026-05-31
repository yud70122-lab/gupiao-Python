import api from './index'

export const getGovernanceBasicInfo = (keyword, status, quality) =>
  api.get('/governance/basic', { params: { keyword, status, quality } })

export const auditGovernanceBasicInfo = (ids, approved, comment) =>
  api.post('/governance/basic/audit', { ids, approved, comment })

export const cleanGovernanceBasicInfo = (ids) =>
  api.post('/governance/basic/clean', { ids })

export const getGovernanceMarketOverview = () =>
  api.get('/governance/market/overview')

export const getGovernanceMarketData = (code, status, quality) =>
  api.get('/governance/market', { params: { code, status, quality } })

export const auditGovernanceMarketData = (ids, approved) =>
  api.post('/governance/market/audit', { ids, approved })

export const fixGovernanceMarketData = (ids) =>
  api.post('/governance/market/fix', { ids })

export const validateGovernanceMarketData = () =>
  api.post('/governance/market/validate')

export const getGovernanceFinancialData = (code, reportType, status) =>
  api.get('/governance/financial', { params: { code, reportType, status } })

export const auditGovernanceFinancialData = (ids, approved) =>
  api.post('/governance/financial/audit', { ids, approved })

export const standardizeGovernanceFinancialData = (ids) =>
  api.post('/governance/financial/standardize', { ids })

export const verifyGovernanceFinancialData = (ids) =>
  api.post('/governance/financial/verify', { ids })

export const getGovernanceOverviewIndex = (indexType, status) =>
  api.get('/governance/overview/index', { params: { indexType, status } })

export const getGovernanceOverviewNorthbound = (startDate, endDate) =>
  api.get('/governance/overview/northbound', { params: { startDate, endDate } })

export const getGovernanceOverviewStats = () =>
  api.get('/governance/overview/stats')

export const auditGovernanceMarketOverview = (ids, approved) =>
  api.post('/governance/overview/audit', { ids, approved })

export const mergeGovernanceMarketOverview = (ids) =>
  api.post('/governance/overview/merge', { ids })

export default api
