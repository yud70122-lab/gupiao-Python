<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-edit"></i> 行情数据治理</h2>
        <div class="header-actions">
          <el-button type="primary" @click="handleAudit" :disabled="selectedRows.length === 0">
            <i class="el-icon-check"></i> 批量审核
          </el-button>
          <el-button type="warning" @click="handleValidate" :disabled="selectedRows.length === 0">
            <i class="el-icon-circle-check"></i> 数据校验
          </el-button>
          <el-button type="danger" @click="handleFix" :disabled="selectedRows.length === 0">
            <i class="el-icon-set-up"></i> 异常修复
          </el-button>
          <el-button @click="loadData">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </div>

      <div class="control-bar">
        <el-form :inline="true" :model="form">
          <el-form-item label="选择股票">
            <el-select v-model="form.stock" filterable placeholder="请选择股票" style="width: 200px;" @change="handleSearch">
              <el-option v-for="stock in stockList" :key="stock.code"
                         :label="stock.code + ' - ' + stock.name" :value="stock.code">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="K线周期">
            <el-radio-group v-model="form.period" @change="handleSearch">
              <el-radio-button label="daily">日线</el-radio-button>
              <el-radio-button label="weekly">周线</el-radio-button>
              <el-radio-button label="monthly">月线</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="数据状态">
            <el-select v-model="form.status" placeholder="全部" clearable @change="handleSearch" style="width: 120px;">
              <el-option label="正常" value="normal"></el-option>
              <el-option label="异常" value="abnormal"></el-option>
              <el-option label="缺失" value="missing"></el-option>
              <el-option label="待审核" value="pending"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="质量等级">
            <el-select v-model="form.quality" placeholder="全部" clearable @change="handleSearch" style="width: 100px;">
              <el-option label="A" value="A"></el-option>
              <el-option label="B" value="B"></el-option>
              <el-option label="C" value="C"></el-option>
              <el-option label="D" value="D"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="质量概览" name="overview">
          <div class="quality-overview">
            <el-row :gutter="16">
              <el-col :span="6">
                <el-card class="quality-card">
                  <div class="quality-title">数据完整率</div>
                  <div class="quality-value">{{ qualityOverview.completeness }}%</div>
                  <el-progress :percentage="qualityOverview.completeness" :color="'#67C23A'"></el-progress>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="quality-card">
                  <div class="quality-title">数据准确率</div>
                  <div class="quality-value">{{ qualityOverview.accuracy }}%</div>
                  <el-progress :percentage="qualityOverview.accuracy" :color="'#409EFF'"></el-progress>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="quality-card">
                  <div class="quality-title">异常数据量</div>
                  <div class="quality-value error">{{ qualityOverview.abnormalCount }}条</div>
                  <el-progress :percentage="Math.min(Math.round(qualityOverview.abnormalCount / 10), 100)" :color="'#F56C6C'"></el-progress>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="quality-card">
                  <div class="quality-title">待审核数据</div>
                  <div class="quality-value warning">{{ qualityOverview.pendingCount }}条</div>
                  <el-progress :percentage="Math.min(Math.round(qualityOverview.pendingCount / 10), 100)" :color="'#E6A23C'"></el-progress>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
        <el-tab-pane label="数据列表" name="list">
          <el-table :data="filteredData" border stripe v-loading="loading" height="calc(100vh - 500px)" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="date" label="日期" width="120" fixed="left"></el-table-column>
            <el-table-column prop="code" label="股票代码" width="100"></el-table-column>
            <el-table-column prop="name" label="股票名称" width="100"></el-table-column>
            <el-table-column prop="open" label="开盘价" width="100">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.open >= row.close, 'color-green': row.open < row.close }">
                  {{ row.open?.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="high" label="最高价" width="100">
              <template slot-scope="{ row }">
                <span class="color-red">{{ row.high?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="low" label="最低价" width="100">
              <template slot-scope="{ row }">
                <span class="color-green">{{ row.low?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="close" label="收盘价" width="100">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.change >= 0, 'color-green': row.change < 0 }">
                  {{ row.close?.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="volume" label="成交量" width="120">
              <template slot-scope="{ row }">
                {{ formatNumber(row.volume) }}
              </template>
            </el-table-column>
            <el-table-column prop="dataStatus" label="数据状态" width="90">
              <template slot-scope="{ row }">
                <el-tag v-if="row.dataStatus === 'normal'" size="small" type="success">正常</el-tag>
                <el-tag v-else-if="row.dataStatus === 'abnormal'" size="small" type="danger">异常</el-tag>
                <el-tag v-else-if="row.dataStatus === 'missing'" size="small" type="info">缺失</el-tag>
                <el-tag v-else-if="row.dataStatus === 'pending'" size="small" type="warning">待审</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="qualityLevel" label="质量" width="70">
              <template slot-scope="{ row }">
                <el-tag :type="getQualityType(row.qualityLevel)" size="small">{{ row.qualityLevel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="abnormalType" label="异常类型" width="120">
              <template slot-scope="{ row }">
                <span v-if="row.abnormalType">{{ row.abnormalType }}</span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template slot-scope="{ row }">
                <el-button type="text" size="small" @click="handleAuditSingle(row)">审核</el-button>
                <el-button type="text" size="small" @click="handleFix(row)">修复</el-button>
                <el-button type="text" size="small" @click="handleView(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getGovernanceMarketData, getGovernanceMarketOverview, auditGovernanceMarketData, fixGovernanceMarketData, validateGovernanceMarketData } from '@/api/governanceApi'

export default {
  name: 'GovernanceMarketData',
  data() {
    return {
      loading: false,
      activeTab: 'overview',
      selectedRows: [],
      form: {
        stock: '',
        period: 'daily',
        status: '',
        quality: ''
      },
      stockList: [],
      qualityOverview: {
        completeness: 0,
        accuracy: 0,
        abnormalCount: 0,
        pendingCount: 0
      },
      tableData: [],
      allData: [],
      pagination: {
        page: 1,
        size: 20,
        total: 0
      }
    }
  },
  mounted() {
    this.loadStockList()
    this.loadOverview()
  },
  computed: {
    filteredData() {
      return this.allData.filter(item => {
        if (this.form.stock && item.code !== this.form.stock) return false
        if (this.form.status && item.dataStatus !== this.form.status) return false
        if (this.form.quality && item.qualityLevel !== this.form.quality) return false
        return true
      })
    }
  },
  methods: {
    async loadStockList() {
      try {
        const res = await getGovernanceMarketData()
        if (res && res.code === 200) {
          const data = res.data || []
          const uniqueStocks = {}
          data.forEach(item => {
            uniqueStocks[item.code] = { code: item.code, name: item.name }
          })
          this.stockList = Object.values(uniqueStocks)
        }
      } catch (e) {
        console.error('加载股票列表失败:', e)
      }
    },
    async loadOverview() {
      try {
        const res = await getGovernanceMarketOverview()
        if (res && res.code === 200) {
          this.qualityOverview = res.overview || {
            completeness: 96.8,
            accuracy: 94.2,
            abnormalCount: 128,
            pendingCount: 256
          }
        }
      } catch (e) {
        console.error('加载质量概览失败:', e)
      }
    },
    formatLocalDate(date) {
      if (typeof date === 'string') return date
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    formatNumber(num) {
      if (!num && num !== 0) return '-'
      return num.toLocaleString('zh-CN')
    },
    getQualityType(level) {
      const types = { A: 'success', B: 'primary', C: 'warning', D: 'danger' }
      return types[level] || 'info'
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getGovernanceMarketData(this.form.stock || null, this.form.status || null, this.form.quality || null)
        if (res && res.code === 200) {
          const rawData = res.data || []
          this.allData = rawData.map(d => ({
            id: d.id,
            date: d.tradeDate,
            code: d.code,
            name: d.name,
            open: d.openPrice,
            high: d.highPrice,
            low: d.lowPrice,
            close: d.closePrice,
            change: d.changePercent,
            volume: d.volume,
            dataStatus: d.dataStatus,
            qualityLevel: d.qualityLevel,
            abnormalType: d.abnormalType
          }))
          this.tableData = this.filteredData
          this.pagination.total = this.filteredData.length
        }
      } catch (e) {
        console.error('加载数据失败:', e)
      } finally {
        this.loading = false
      }
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows
    },
    resetFilter() {
      this.form = { stock: '', period: 'daily', status: '', quality: '' }
      this.loadData()
    },
    handleSearch() {
      this.loadData()
    },
    async handleAudit() {
      const ids = this.selectedRows.map(r => r.id).join(',')
      try {
        await this.$confirm(`确定要审核选中的 ${this.selectedRows.length} 条行情数据吗？`, '批量审核', {
          confirmButtonText: '通过',
          cancelButtonText: '取消',
          type: 'success'
        })
        await auditGovernanceMarketData(ids, true)
        this.$message.success('批量审核通过成功')
        this.selectedRows = []
        this.loadData()
        this.loadOverview()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('审核失败:', e)
        }
      }
    },
    async handleValidate() {
      try {
        await validateGovernanceMarketData()
        this.$message.success('数据校验任务已启动，正在后台运行')
        setTimeout(() => {
          this.loadOverview()
        }, 1000)
      } catch (e) {
        console.error('校验失败:', e)
      }
    },
    async handleFix(row) {
      const ids = row ? [row.id].join(',') : this.selectedRows.map(r => r.id).join(',')
      const count = row ? 1 : this.selectedRows.length
      try {
        await this.$confirm(row ? `确定要修复 ${row.date} 的异常数据吗？` : `确定要修复选中的 ${count} 条异常数据吗？`, '异常修复', {
          confirmButtonText: '修复',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await fixGovernanceMarketData(ids)
        this.$message.success('数据修复任务已启动')
        this.selectedRows = []
        this.loadData()
        this.loadOverview()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('修复失败:', e)
        }
      }
    },
    async handleAuditSingle(row) {
      try {
        const { value } = await this.$prompt('请输入审核意见', `审核 ${row.date} 行情数据`, {
          confirmButtonText: '通过',
          cancelButtonText: '拒绝',
          inputType: 'textarea'
        })
        await auditGovernanceMarketData(row.id, true)
        this.$message.success('审核通过')
        this.loadData()
        this.loadOverview()
      } catch (e) {
        if (e !== 'cancel') {
          try {
            await auditGovernanceMarketData(row.id, false)
            this.$message.info('已拒绝')
            this.loadData()
          } catch (err) {
            console.error('拒绝失败:', err)
          }
        }
      }
    },
    handleView(row) {
      this.$alert(JSON.stringify(row, null, 2), '详细信息', { type: 'info' })
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'list' && this.allData.length === 0) {
        this.loadData()
      }
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
.control-bar {
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}
.quality-overview {
  padding: 16px 0;
}
.quality-card {
  text-align: center;
}
.quality-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.quality-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
.quality-value.error {
  color: #F56C6C;
}
.quality-value.warning {
  color: #E6A23C;
}
.color-red {
  color: #ef5350;
}
.color-green {
  color: #26a69a;
}
.text-muted {
  color: #c0c4cc;
}
::v-deep .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
::v-deep .el-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}
::v-deep .el-tabs__content {
  flex: 1;
  overflow: auto;
}
::v-deep .el-tab-pane {
  height: 100%;
}
</style>
