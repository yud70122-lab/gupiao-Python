<template>
  <div class="layout-container">
    <el-header class="header">
      <h1><i class="el-icon-data-line"></i> 股票量化关系数据分析与可视化系统</h1>
      <div class="user-info">
        <el-avatar :size="32" :src="userInfo.avatar">
          {{ (userInfo.realName || userInfo.username || 'U').charAt(0) }}
        </el-avatar>
        <span>{{ userInfo.realName || userInfo.username || '用户' }}</span>
        <el-tag v-if="userInfo.role" size="small" type="info">{{ userInfo.role }}</el-tag>
        <el-dropdown @command="handleUserCommand">
          <i class="el-icon-arrow-down"></i>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="profile" icon="el-icon-user">个人中心</el-dropdown-item>
            <el-dropdown-item command="logout" divided icon="el-icon-switch-button">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="layout-body">
      <el-aside width="220px" class="menu-sidebar">
        <el-menu
          :default-active="$route.path"
          :default-openeds="openedMenus"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          router>
          <el-submenu v-if="hasAnalysisAccess" index="/analysis">
            <template slot="title">
              <i class="el-icon-data-analysis"></i>
              <span>数据分析</span>
            </template>
            <el-menu-item v-if="hasMenuAccess('/analysis/stock') || hasPerm('stock:view')" index="/analysis/stock">
              <i class="el-icon-data-line"></i>
              <span>股票分析</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu v-if="hasQuantAccess" index="/quant">
            <template slot="title">
              <i class="el-icon-calculator"></i>
              <span>基础量化计算</span>
            </template>
            <el-menu-item v-if="hasMenuAccess('/quant/correlation') || hasPerm('quant:correlation:view')" index="/quant/correlation">
              <i class="el-icon-connection"></i>
              <span>相关性分析</span>
            </el-menu-item>
            <el-menu-item v-if="hasMenuAccess('/quant/linkage') || hasPerm('quant:linkage:view')" index="/quant/linkage">
              <i class="el-icon-share"></i>
              <span>联动性分析</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu v-if="hasCollectionAccess" index="/collection">
            <template slot="title">
              <i class="el-icon-download"></i>
              <span>数据采集类型</span>
            </template>
            <el-menu-item v-if="hasPerm('collection:basic:view') || hasMenuAccess('/collection/basic')" index="/collection/basic">
              <i class="el-icon-info"></i>
              <span>基础信息</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('collection:market:view') || hasMenuAccess('/collection/market')" index="/collection/market">
              <i class="el-icon-data-line"></i>
              <span>行情数据</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('collection:financial:view') || hasMenuAccess('/collection/financial')" index="/collection/financial">
              <i class="el-icon-s-data"></i>
              <span>财务数据</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('collection:overview:view') || hasMenuAccess('/collection/overview')" index="/collection/overview">
              <i class="el-icon-pie-chart"></i>
              <span>市场数据</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu v-if="hasGovernanceAccess" index="/governance">
            <template slot="title">
              <i class="el-icon-edit"></i>
              <span>数据治理</span>
            </template>
            <el-menu-item v-if="hasPerm('governance:basic:view') || hasMenuAccess('/governance/basic')" index="/governance/basic">
              <i class="el-icon-info"></i>
              <span>基础信息</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('governance:market:view') || hasMenuAccess('/governance/market')" index="/governance/market">
              <i class="el-icon-data-line"></i>
              <span>行情数据</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('governance:financial:view') || hasMenuAccess('/governance/financial')" index="/governance/financial">
              <i class="el-icon-s-data"></i>
              <span>财务数据</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('governance:overview:view') || hasMenuAccess('/governance/overview')" index="/governance/overview">
              <i class="el-icon-pie-chart"></i>
              <span>市场数据</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu v-if="hasStatisticsAccess" index="/statistics">
            <template slot="title">
              <i class="el-icon-data-board"></i>
              <span>数据统计</span>
            </template>
            <el-menu-item v-if="hasPerm('statistics:status:view') || hasMenuAccess('/statistics/status')" index="/statistics/status">
              <i class="el-icon-data-line"></i>
              <span>数据状态</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('statistics:logs:view') || hasMenuAccess('/statistics/logs')" index="/statistics/logs">
              <i class="el-icon-document-checked"></i>
              <span>采集日志</span>
            </el-menu-item>
            <el-menu-item index="/statistics/favorite">
              <i class="el-icon-star-on"></i>
              <span>自选股中心</span>
            </el-menu-item>
          </el-submenu>

          <el-submenu v-if="hasSystemAccess" index="/system">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>系统管理</span>
            </template>
            <el-menu-item v-if="hasPerm('user:view')" index="/system/user">
              <i class="el-icon-user"></i>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('role:view')" index="/system/role">
              <i class="el-icon-s-custom"></i>
              <span>角色权限</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('menu:view')" index="/system/menu">
              <i class="el-icon-menu"></i>
              <span>菜单管理</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('log:view')" index="/system/log">
              <i class="el-icon-document"></i>
              <span>操作日志</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('config:view')" index="/system/config">
              <i class="el-icon-setting"></i>
              <span>系统配置</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('task:view')" index="/system/task">
              <i class="el-icon-time"></i>
              <span>定时更新任务</span>
            </el-menu-item>
            <el-menu-item v-if="hasPerm('dataperm:view')" index="/system/dataperm">
              <i class="el-icon-lock"></i>
              <span>数据权限</span>
            </el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'MainLayout',
  computed: {
    userInfo() {
      return this.$store.state.userInfo || {}
    },
    hasAnalysisAccess() {
      return this.hasPerm('stock:view') || this.hasPerm('stock:analysis')
    },
    hasCollectionAccess() {
      const perms = ['collection:basic:view', 'collection:market:view', 'collection:financial:view', 'collection:overview:view']
      return perms.some(p => this.hasPerm(p))
    },
    hasGovernanceAccess() {
      const perms = ['governance:basic:view', 'governance:market:view', 'governance:financial:view', 'governance:overview:view']
      return perms.some(p => this.hasPerm(p))
    },
    hasSystemAccess() {
      const perms = ['user:view', 'role:view', 'menu:view', 'log:view', 'config:view', 'task:view', 'dataperm:view']
      return perms.some(p => this.hasPerm(p))
    },
    hasQuantAccess() {
      const perms = ['quant:view', 'quant:correlation:view', 'quant:linkage:view']
      return perms.some(p => this.hasPerm(p)) || true
    },
    hasStatisticsAccess() {
      const perms = ['statistics:status:view', 'statistics:logs:view', 'statistics:favorite:view']
      return perms.some(p => this.hasPerm(p)) || true
    },
    openedMenus() {
      const opened = []
      if (this.hasAnalysisAccess) opened.push('/analysis')
      if (this.hasQuantAccess) opened.push('/quant')
      if (this.hasCollectionAccess) opened.push('/collection')
      if (this.hasGovernanceAccess) opened.push('/governance')
      if (this.hasStatisticsAccess) opened.push('/statistics')
      if (this.hasSystemAccess) opened.push('/system')
      return opened
    }
  },
  methods: {
    hasPerm(perm) {
      return this.$store.getters.hasPermission(perm)
    },
    hasMenuAccess(path) {
      return this.$store.getters.hasMenu(path)
    },
    async handleUserCommand(command) {
      if (command === 'profile') {
        this.$router.push('/profile')
      } else if (command === 'logout') {
        try {
          await this.$confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          await this.$store.dispatch('logout')
          this.$message.success('已退出登录')
          this.$router.push('/login')
        } catch (e) {
          // cancelled
        }
      }
    }
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 24px;
}

.header h1 {
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.user-info .el-avatar {
  background: rgba(255,255,255,0.2);
}

.layout-body {
  flex: 1;
  overflow: hidden;
}

.menu-sidebar {
  background: #304156;
  overflow-y: auto;
}

.menu-sidebar ::v-deep .el-menu {
  border-right: none;
}

.main-content {
  background: #f0f2f5;
  padding: 16px;
  overflow: hidden;
}
</style>
