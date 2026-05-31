import axios from 'axios'
import { Message } from 'element-ui'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('permissions')
      localStorage.removeItem('menus')
      localStorage.removeItem('dataScope')
      localStorage.removeItem('allowedStocks')
    }
    const msg = (error.response && error.response.data && error.response.data.message) || error.message || '请求失败'
    Message.error(msg)
    return Promise.reject(error)
  }
)

export default api
