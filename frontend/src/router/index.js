import Vue from 'vue'
import VueRouter from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/analysis/stock',
    children: [
      {
        path: '/analysis/stock',
        name: 'StockAnalysis',
        component: () => import('@/views/analysis/StockAnalysis.vue')
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: '/system/user',
        name: 'UserManagement',
        component: () => import('@/views/system/UserManagement.vue'),
        meta: { permission: 'user:view' }
      },
      {
        path: '/system/role',
        name: 'RolePermission',
        component: () => import('@/views/system/RolePermission.vue'),
        meta: { permission: 'role:view' }
      },
      {
        path: '/system/menu',
        name: 'MenuManagement',
        component: () => import('@/views/system/MenuManagement.vue'),
        meta: { permission: 'menu:view' }
      },
      {
        path: '/system/log',
        name: 'OperationLog',
        component: () => import('@/views/system/OperationLog.vue'),
        meta: { permission: 'log:view' }
      },
      {
        path: '/system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/SystemConfig.vue'),
        meta: { permission: 'config:view' }
      },
      {
        path: '/system/task',
        name: 'ScheduledTask',
        component: () => import('@/views/system/ScheduledTask.vue'),
        meta: { permission: 'task:view' }
      },
      {
        path: '/system/dataperm',
        name: 'DataPermission',
        component: () => import('@/views/system/DataPermission.vue'),
        meta: { permission: 'dataperm:view' }
      },
      {
        path: '/collection/basic',
        name: 'BasicInfo',
        component: () => import('@/views/collection/BasicInfo.vue'),
        meta: { permission: 'collection:basic:view' }
      },
      {
        path: '/collection/market',
        name: 'MarketData',
        component: () => import('@/views/collection/MarketData.vue'),
        meta: { permission: 'collection:market:view' }
      },
      {
        path: '/collection/financial',
        name: 'FinancialData',
        component: () => import('@/views/collection/FinancialData.vue'),
        meta: { permission: 'collection:financial:view' }
      },
      {
        path: '/collection/overview',
        name: 'MarketOverview',
        component: () => import('@/views/collection/MarketOverview.vue'),
        meta: { permission: 'collection:overview:view' }
      },
      {
        path: '/governance/basic',
        name: 'GovernanceBasicInfo',
        component: () => import('@/views/governance/BasicInfo.vue'),
        meta: { permission: 'governance:basic:view' }
      },
      {
        path: '/governance/market',
        name: 'GovernanceMarketData',
        component: () => import('@/views/governance/MarketData.vue'),
        meta: { permission: 'governance:market:view' }
      },
      {
        path: '/governance/financial',
        name: 'GovernanceFinancialData',
        component: () => import('@/views/governance/FinancialData.vue'),
        meta: { permission: 'governance:financial:view' }
      },
      {
        path: '/governance/overview',
        name: 'GovernanceMarketOverview',
        component: () => import('@/views/governance/MarketOverview.vue'),
        meta: { permission: 'governance:overview:view' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const hasPermission = function(perm) {
    const perms = JSON.parse(localStorage.getItem('permissions') || '[]')
    return perms.includes(perm) || perms.includes('*')
  }

  if (to.name !== 'Login' && !token) {
    next({ name: 'Login' })
  } else if (to.name === 'Login' && token) {
    next({ path: '/analysis/stock' })
  } else if (to.meta && to.meta.permission && !hasPermission(to.meta.permission)) {
    next({ path: '/analysis/stock' })
  } else {
    next()
  }
})

export default router
