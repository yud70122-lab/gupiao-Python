import api from './index'

export const userApi = {
  list: (params) => api.get('/users', { params }),
  get: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  delete: (id) => api.delete(`/users/${id}`),
  toggleStatus: (id) => api.put(`/users/${id}/toggle`),
  currentPermissions: () => api.get('/users/current/permissions')
}

export const roleApi = {
  list: () => api.get('/roles'),
  all: () => api.get('/roles/all'),
  get: (id) => api.get(`/roles/${id}`),
  create: (data) => api.post('/roles', data),
  update: (id, data) => api.put(`/roles/${id}`, data),
  delete: (id) => api.delete(`/roles/${id}`),
  assignPermissions: (id, data) => api.post(`/roles/${id}/permissions`, data),
  getPermissions: (id) => api.get(`/roles/${id}/permissions`)
}

export const menuApi = {
  list: () => api.get('/menus'),
  flat: () => api.get('/menus/flat'),
  tree: () => api.get('/menus/tree'),
  get: (id) => api.get(`/menus/${id}`),
  create: (data) => api.post('/menus', data),
  update: (id, data) => api.put(`/menus/${id}`, data),
  delete: (id) => api.delete(`/menus/${id}`)
}

export const permissionApi = {
  list: () => api.get('/permissions'),
  flat: () => api.get('/permissions/flat'),
  tree: () => api.get('/permissions/tree'),
  get: (id) => api.get(`/permissions/${id}`),
  create: (data) => api.post('/permissions', data),
  update: (id, data) => api.put(`/permissions/${id}`, data),
  delete: (id) => api.delete(`/permissions/${id}`)
}

export const dataPermApi = {
  list: () => api.get('/data-permissions'),
  getByUserId: (userId) => api.get(`/data-permissions/user/${userId}`),
  saveByUserId: (userId, data) => api.put(`/data-permissions/user/${userId}`, data),
  deleteByUserId: (userId) => api.delete(`/data-permissions/user/${userId}`),
  stockOptions: () => api.get('/data-permissions/stocks')
}

export const logApi = {
  list: (params) => api.get('/logs', { params }),
  delete: (id) => api.delete(`/logs/${id}`),
  clear: () => api.delete('/logs/clear')
}

export const configApi = {
  list: () => api.get('/configs'),
  get: (key) => api.get(`/configs/${key}`),
  save: (data) => api.post('/configs', data),
  delete: (key) => api.delete(`/configs/${key}`)
}

export const taskApi = {
  list: () => api.get('/tasks'),
  get: (id) => api.get(`/tasks/${id}`),
  create: (data) => api.post('/tasks', data),
  update: (id, data) => api.put(`/tasks/${id}`, data),
  delete: (id) => api.delete(`/tasks/${id}`),
  start: (id) => api.put(`/tasks/${id}/start`),
  stop: (id) => api.put(`/tasks/${id}/stop`),
  execute: (id) => api.post(`/tasks/${id}/execute`)
}

export const statisticsApi = {
  getDataStatus: () => api.get('/statistics/data-status'),
  getCollectionLogs: (params) => api.get('/statistics/logs', { params }),
  retryLog: (id) => api.post(`/statistics/logs/${id}/retry`),
  retryBatch: (ids) => api.post('/statistics/logs/retry-batch', ids)
}