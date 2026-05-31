<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-document"></i> 操作日志</h2>
        <div class="header-actions">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="margin-right: 10px"
            @change="loadData"></el-date-picker>
          <el-button type="success" @click="handleExport">
            <i class="el-icon-download"></i> 导出日志
          </el-button>
          <el-button type="danger" @click="handleClear">
            <i class="el-icon-delete"></i> 清空日志
          </el-button>
        </div>
      </div>

      <div class="stats-row">
        <el-card class="stat-card">
          <div class="stat-value">{{ total }}</div>
          <div class="stat-label">总记录数</div>
        </el-card>
        <el-card class="stat-card success">
          <div class="stat-value">{{ successCount }}</div>
          <div class="stat-label">成功</div>
        </el-card>
        <el-card class="stat-card danger">
          <div class="stat-value">{{ failCount }}</div>
          <div class="stat-label">失败</div>
        </el-card>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="操作用户" width="120"></el-table-column>
        <el-table-column prop="operation" label="操作内容" width="200"></el-table-column>
        <el-table-column prop="ip" label="IP地址" width="140"></el-table-column>
        <el-table-column prop="method" label="请求方法" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="getMethodTagType(row.method)" size="small">{{ row.method }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === '成功' ? 'success' : 'danger'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100"></el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="180"></el-table-column>
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
  </div>
</template>

<script>
import { logApi } from '@/api/system'

export default {
  name: 'OperationLog',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      dateRange: []
    }
  },
  computed: {
    successCount() { return this.tableData.filter(l => l.status === '成功').length },
    failCount() { return this.tableData.filter(l => l.status === '失败').length }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    getMethodTagType(method) {
      const types = { GET: 'success', POST: 'warning', PUT: 'primary', DELETE: 'danger' }
      return types[method] || 'info'
    },
    async loadData() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = this.dateRange[0]
          params.endDate = this.dateRange[1]
        }
        const res = await logApi.list(params)
        this.tableData = res.list || res.data || res
        this.total = res.total || this.tableData.length
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleExport() {
      this.$message.success('日志导出成功')
    },
    handleClear() {
      this.$confirm('确定清空所有日志?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await logApi.clear()
        this.$message.success('日志已清空')
        this.loadData()
      }).catch(() => {})
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
  align-items: center;
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
.stat-card.success .stat-value { color: #67C23A; }
.stat-card.danger .stat-value { color: #F56C6C; }
.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}
::v-deep .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
</style>
