<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-document-checked"></i> 采集日志</h2>
        <div class="header-actions">
          <el-button type="danger" @click="handleRetryBatch" :disabled="selectedIds.length === 0" :loading="batchRetrying">
            <i class="el-icon-refresh-left"></i> 批量重试 ({{ selectedIds.length }})
          </el-button>
          <el-button type="primary" @click="loadData" :loading="loading">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-form :inline="true" :model="filters" size="small">
          <el-form-item label="状态">
            <el-select v-model="filters.status" placeholder="全部状态" clearable @change="loadData">
              <el-option label="全部" value=""></el-option>
              <el-option label="失败" value="失败"></el-option>
              <el-option label="成功" value="成功"></el-option>
              <el-option label="重试中" value="重试中"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="数据类型">
            <el-select v-model="filters.dataType" placeholder="全部类型" clearable @change="loadData">
              <el-option label="全部" value=""></el-option>
              <el-option label="基础信息" value="basic"></el-option>
              <el-option label="行情数据" value="market"></el-option>
              <el-option label="财务数据" value="financial"></el-option>
              <el-option label="市场概览" value="overview"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="text" @click="showOnlyFailed = !showOnlyFailed; loadData()">
              <i :class="showOnlyFailed ? 'el-icon-star-on' : 'el-icon-star-off'"></i>
              {{ showOnlyFailed ? '显示全部' : '仅显示失败' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="stats-row">
        <el-card class="stat-card">
          <div class="stat-value">{{ total }}</div>
          <div class="stat-label">总记录数</div>
        </el-card>
        <el-card class="stat-card danger">
          <div class="stat-value">{{ failCount }}</div>
          <div class="stat-label">失败记录</div>
        </el-card>
      </div>

      <el-table 
        :data="tableData" 
        border 
        stripe 
        v-loading="loading" 
        @selection-change="handleSelectionChange"
        style="margin-top: 20px">
        <el-table-column type="selection" width="55" :selectable="row => row.status === '失败'"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="dataType" label="数据类型" width="120">
          <template slot-scope="{ row }">
            <el-tag size="small">{{ getDataTypeName(row.dataType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="股票代码" width="100"></el-table-column>
        <el-table-column prop="name" label="股票名称" width="120"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip></el-table-column>
        <el-table-column prop="retryCount" label="重试次数" width="90">
          <template slot-scope="{ row }">
            <span :class="{ 'retry-highlight': row.retryCount > 0 }">{{ row.retryCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="operator" label="操作人" width="100"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="{ row }">
            <el-button 
              v-if="row.status === '失败'" 
              type="text" 
              size="small" 
              @click="handleRetry(row)"
              :loading="retryingId === row.id">
              重试
            </el-button>
            <el-button type="text" size="small" @click="viewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        :current-page.sync="pageNum"
        :page-size.sync="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; text-align: right"></el-pagination>
    </el-card>

    <el-dialog title="日志详情" :visible.sync="detailVisible" width="500px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="数据类型">{{ getDataTypeName(currentLog.dataType) }}</el-descriptions-item>
        <el-descriptions-item label="股票代码">{{ currentLog.code || '-' }}</el-descriptions-item>
        <el-descriptions-item label="股票名称">{{ currentLog.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentLog.status)" size="small">{{ currentLog.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentLog.retryCount }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentLog.createTime }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" v-if="currentLog.errorMessage">
          <div style="color: #F56C6C; white-space: pre-wrap;">{{ currentLog.errorMessage }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button 
          v-if="currentLog && currentLog.status === '失败'" 
          type="primary" 
          @click="handleRetry(currentLog); detailVisible = false">
          重试采集
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { statisticsApi } from '@/api/system'

export default {
  name: 'CollectionLog',
  data() {
    return {
      loading: false,
      retryingId: null,
      batchRetrying: false,
      tableData: [],
      total: 0,
      failCount: 0,
      pageNum: 1,
      pageSize: 10,
      selectedIds: [],
      showOnlyFailed: false,
      filters: {
        status: '',
        dataType: ''
      },
      detailVisible: false,
      currentLog: null
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          status: this.showOnlyFailed ? '失败' : this.filters.status,
          dataType: this.filters.dataType
        }
        const res = await statisticsApi.getCollectionLogs(params)
        this.tableData = res.list || res.data || []
        this.total = res.total || this.tableData.length
        this.failCount = res.failCount || 0
      } catch (e) {
        console.error(e)
        this.$message.error('加载采集日志失败')
      } finally {
        this.loading = false
      }
    },
    getDataTypeName(type) {
      const names = {
        basic: '基础信息',
        market: '行情数据',
        financial: '财务数据',
        overview: '市场概览'
      }
      return names[type] || type
    },
    getStatusType(status) {
      const types = {
        '成功': 'success',
        '失败': 'danger',
        '重试中': 'warning'
      }
      return types[status] || 'info'
    },
    handleSelectionChange(selection) {
      this.selectedIds = selection.map(item => item.id)
    },
    async handleRetry(row) {
      this.retryingId = row.id
      try {
        await statisticsApi.retryLog(row.id)
        this.$message.success('重试任务已启动')
        this.loadData()
      } catch (e) {
        this.$message.error('重试失败')
      } finally {
        this.retryingId = null
      }
    },
    async handleRetryBatch() {
      if (this.selectedIds.length === 0) return
      this.batchRetrying = true
      try {
        await statisticsApi.retryBatch(this.selectedIds)
        this.$message.success(`已启动 ${this.selectedIds.length} 条重试任务`)
        this.selectedIds = []
        this.loadData()
      } catch (e) {
        this.$message.error('批量重试失败')
      } finally {
        this.batchRetrying = false
      }
    },
    viewDetail(row) {
      this.currentLog = row
      this.detailVisible = true
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
.header-actions {
  display: flex;
  gap: 8px;
}
.filter-bar {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}
.filter-bar ::v-deep .el-form {
  margin: 0;
}
.stats-row {
  display: flex;
  gap: 16px;
}
.stat-card {
  flex: 1;
  text-align: center;
}
.stat-card .stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}
.stat-card.danger .stat-value { color: #F56C6C; }
.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
.retry-highlight {
  color: #E6A23C;
  font-weight: 600;
}
::v-deep .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
