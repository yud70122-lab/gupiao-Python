<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-data-line"></i> 数据状态</h2>
        <div class="header-actions">
          <el-button type="primary" @click="loadData" :loading="loading">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </div>

      <el-alert
        title="数据统计概览"
        :description="`总数据量: ${totalRecords} 条 | 最后更新时间: ${lastUpdateTime}`"
        type="info"
        :closable="false"
        style="margin-bottom: 20px">
      </el-alert>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="name" label="数据类型" width="150">
          <template slot-scope="{ row }">
            <span style="font-weight: 600">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="数据总量" width="150">
          <template slot-scope="{ row }">
            <span class="count-value">{{ formatNumber(row.totalCount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastUpdateTime" label="最后更新时间" width="200">
          <template slot-scope="{ row }">
            <span>{{ row.lastUpdateTime || lastUpdateTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="coverage" label="数据覆盖率">
          <template slot-scope="{ row }">
            <div class="coverage-bar">
              <el-progress
                :percentage="Math.round(row.coverage)"
                :status="getCoverageStatus(row.coverage)"
                :stroke-width="16">
              </el-progress>
              <span class="coverage-text">{{ row.coverage.toFixed(2) }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="getStatusType(row.coverage)" size="small">
              {{ getStatusText(row.coverage) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="stats-footer">
        <el-card class="mini-stat">
          <div class="mini-stat-icon blue">
            <i class="el-icon-data-base"></i>
          </div>
          <div class="mini-stat-content">
            <div class="mini-stat-value">{{ formatNumber(totalRecords) }}</div>
            <div class="mini-stat-label">总数据量</div>
          </div>
        </el-card>
        <el-card class="mini-stat">
          <div class="mini-stat-icon green">
            <i class="el-icon-circle-check"></i>
          </div>
          <div class="mini-stat-content">
            <div class="mini-stat-value">{{ tableData.filter(d => d.coverage >= 80).length }}</div>
            <div class="mini-stat-label">数据完整类型</div>
          </div>
        </el-card>
        <el-card class="mini-stat">
          <div class="mini-stat-icon orange">
            <i class="el-icon-warning"></i>
          </div>
          <div class="mini-stat-content">
            <div class="mini-stat-value">{{ tableData.filter(d => d.coverage < 50).length }}</div>
            <div class="mini-stat-label">待补充类型</div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { statisticsApi } from '@/api/system'

export default {
  name: 'DataStatus',
  data() {
    return {
      loading: false,
      tableData: [],
      totalRecords: 0,
      lastUpdateTime: '-'
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await statisticsApi.getDataStatus()
        this.tableData = res.dataTypes || []
        this.totalRecords = res.totalRecords || 0
        this.lastUpdateTime = res.lastUpdateTime || '-'
      } catch (e) {
        console.error(e)
        this.$message.error('加载数据状态失败')
      } finally {
        this.loading = false
      }
    },
    formatNumber(num) {
      if (num >= 10000) {
        return (num / 10000).toFixed(2) + '万'
      }
      return num?.toLocaleString() || '0'
    },
    getCoverageStatus(coverage) {
      if (coverage >= 80) return 'success'
      if (coverage >= 50) return 'warning'
      return 'exception'
    },
    getStatusType(coverage) {
      if (coverage >= 80) return 'success'
      if (coverage >= 50) return 'warning'
      return 'danger'
    },
    getStatusText(coverage) {
      if (coverage >= 80) return '良好'
      if (coverage >= 50) return '一般'
      return '不足'
    }
  }
}
</script>

<style scoped>
.page-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.page-card {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}
.count-value {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
}
.coverage-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}
.coverage-text {
  font-weight: 600;
  color: #606266;
  min-width: 60px;
}
.stats-footer {
  display: flex;
  gap: 16px;
  margin-top: 20px;
}
.mini-stat {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}
.mini-stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}
.mini-stat-icon.blue { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.mini-stat-icon.green { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); }
.mini-stat-icon.orange { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.mini-stat-content {
  flex: 1;
}
.mini-stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}
.mini-stat-label {
  font-size: 14px;
  color: #909399;
}
::v-deep .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
