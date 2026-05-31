import Vue from 'vue'
import Vuex from 'vuex'
import { authApi } from '@/api/auth'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]'),
    menus: JSON.parse(localStorage.getItem('menus') || '[]'),
    dataScope: localStorage.getItem('dataScope') || 'ALL',
    allowedStocks: JSON.parse(localStorage.getItem('allowedStocks') || '[]')
  },
  getters: {
    token: state => state.token,
    userInfo: state => state.userInfo,
    hasPermission: state => (perm) => {
      return state.permissions.includes(perm) || state.permissions.includes('*')
    },
    hasMenu: state => (path) => {
      if (state.menus.includes(path)) return true
      for (const p of state.menus) {
        if (path.startsWith(p + '/')) return true
      }
      return false
    }
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    SET_USER_INFO(state, info) {
      state.userInfo = info
      localStorage.setItem('userInfo', JSON.stringify(info))
    },
    SET_PERMISSIONS(state, perms) {
      state.permissions = perms || []
      localStorage.setItem('permissions', JSON.stringify(state.permissions))
    },
    SET_MENUS(state, menuPaths) {
      state.menus = menuPaths || []
      localStorage.setItem('menus', JSON.stringify(state.menus))
    },
    SET_DATA_SCOPE(state, scope) {
      state.dataScope = scope || 'ALL'
      localStorage.setItem('dataScope', state.dataScope)
    },
    SET_ALLOWED_STOCKS(state, stocks) {
      state.allowedStocks = stocks || []
      localStorage.setItem('allowedStocks', JSON.stringify(state.allowedStocks))
    },
    CLEAR_USER(state) {
      state.token = ''
      state.userInfo = null
      state.permissions = []
      state.menus = []
      state.dataScope = 'ALL'
      state.allowedStocks = []
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      localStorage.removeItem('permissions')
      localStorage.removeItem('menus')
      localStorage.removeItem('dataScope')
      localStorage.removeItem('allowedStocks')
    }
  },
  actions: {
    async login({ commit }, loginForm) {
      const res = await authApi.login(loginForm)
      commit('SET_TOKEN', res.token)
      commit('SET_USER_INFO', res.user)
      commit('SET_PERMISSIONS', res.permissions)
      commit('SET_MENUS', res.menus)
      commit('SET_DATA_SCOPE', res.dataScope)
      commit('SET_ALLOWED_STOCKS', res.allowedStocks)
      return res
    },
    async logout({ commit }) {
      try {
        await authApi.logout()
      } catch (e) {
        console.error('Logout error:', e)
      }
      commit('CLEAR_USER')
    }
  }
})
