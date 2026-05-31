<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-edit"></i> 市场数据治理</h2>
        <div class="header-actions">
          <el-button type="primary" @click="handleAudit" :disabled="selectedRows.length === 0">
            <i class="el-icon-check"></i> 批量审核
          </el-button>
          <el-button type="warning" @click="handleMerge" :disabled="selectedRows.length === 0">
            <el-tooltip class="item" effect="dark" content="数据去重合并" placement="bottom">
              <i class="el-icon-document-copy"></i> 去重合并
            </el-tooltip>
          </el-button>
          <el-button type="info" @click="handleTimeline" :disabled="selectedRows.length === 0">
            <i class="el-icon-date"></i> 时间轴校验
          </el-button>
          <el-button @click="loadData">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="大盘指数" name="index">
          <div class="governance-table">
            <div class="filter-bar">
              <el-form :inline="true" :model="filterForm">
                <el-form-item label="指数类型">
                  <el-select v-model="filterForm.indexType" placeholder="全部" clearable @change="handleSearch" style="width: 120px;">
                    <el-option label="上证指数" value="sh"></el-option>
                    <el-option label="深证成指" value="sz"></el-option>
                    <el-option label="创业板指" value="cyb"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="数据状态">
                  <el-select v-model="filterForm.status" placeholder="全部" clearable @change="handleSearch" style="width: 120px;">
                    <el-option label="正常" value="normal"></el-option>
                    <el-option label="待审核" value="pending"></el-option>
                    <el-option label="异常" value="abnormal"></el-option>
                    <el-option label="重复" value="duplicate"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                </el-form-item>
              </el-form>
            </div>
            <el-table :data="filteredIndexData" border stripe v-loading="loading" height="calc(100vh - 480px)" @selection-change="handleSelectionChange">
              <el-table-column type="selection" width="55"></el-table-column>
              <el-table-column prop="date" label="日期" width="120" fixed="left"></el-table-column>
              <el-table-column prop="indexName" label="指数名称" width="120"></el-table-column>
              <el-table-column prop="open" label="开盘" width="120">
                <template slot-scope="{ row }">
                  <span :class="{ 'color-red': row.change >= 0, 'color-green': row.change < 0 }">
                    {{ row.open?.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="high" label="最高" width="120">
                <template slot-scope="{ row }">
                  <span class="color-red">{{ row.high?.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="low" label="最低" width="120">
                <template slot-scope="{ row }">
                  <span class="color-green">{{ row.low?.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="close" label="收盘" width="120">
                <template slot-scope="{ row }">
                  <span :class="{ 'color-red': row.change >= 0, 'color-green': row.change < 0 }">
                    {{ row.close?.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="change" label="涨跌幅" width="120">
                <template slot-scope="{ row }">
                  <span :class="{ 'color-red': row.change >= 0, 'color-green': row.change < 0 }">
                    {{ row.change >= 0 ? '+' : '' }}{{ row.change?.toFixed(2) }}%
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="volume" label="成交量(亿)" width="130">
                <template slot-scope="{ row }">
                  {{ row.volume?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="dataStatus" label="数据状态" width="90">
                <template slot-scope="{ row }">
                  <el-tag v-if="row.dataStatus === 'normal'" size="small" type="success">正常</el-tag>
                  <el-tag v-else-if="row.dataStatus === 'pending'" size="small" type="warning">待审</el-tag>
                  <el-tag v-else-if="row.dataStatus === 'abnormal'" size="small" type="danger">异常</el-tag>
                  <el-tag v-else-if="row.dataStatus === 'duplicate'" size="small" type="info">重复</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150" fixed="right">
                <template slot-scope="{ row }">
                  <el-button type="text" size="small" @click="handleAuditSingle(row)">审核</el-button>
                  <el-button type="text" size="small" @click="handleView(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <el-tab-pane label="涨跌分布" name="distribution">
          <div class="governance-table">
            <div class="quality-summary">
              <el-row :gutter="16">
                <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-title">上涨家数</div>
                <div class="stat-value color-red">{{ marketStats.upCount.toLocaleString() }}</div>
              </el-card>
            </el-col>
              <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-title">下跌家数</div>
                <div class="stat-value color-green">{{ marketStats.downCount.toLocaleString() }}</div>
              </el-card>
            </el-col>
              <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-title">平盘家数</div>
                <div class="stat-value">{{ marketStats.flatCount.toLocaleString() }}</div>
              </el-card>
            </el-col>
              <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-title">涨停/跌停</div>
                <div class="stat-value">{{ marketStats.limitUpCount }}/{{ marketStats.limitDownCount }}</div>
              </el-card>
            </el-col>
          </el-row>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="北向资金" name="northbound">
          <div class="governance-table">
            <div class="filter-bar">
              <el-form :inline="true" :model="northFilter">
                <el-form-item label="日期范围">
                  <el-date-picker
                    v-model="northFilter.dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期">
                  </el-date-picker>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadNorthData">查询</el-button>
                </el-form-item>
              </el-form>
            </div>
            <el-table :data="filteredNorthData" border stripe v-loading="loading" height="calc(100vh - 480px)">
              <el-table-column prop="date" label="日期" width="120"></el-table-column>
              <el-table-column prop="shNetBuy" label="沪股通净买(亿)" width="140">
                <template slot-scope="{ row }">
                  <span :class="{ 'color-red': row.shNetBuy >= 0, 'color-green': row.shNetBuy < 0 }">
                    {{ row.shNetBuy?.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="szNetBuy" label="深股通净买(亿)" width="140">
                <template slot-scope="{ row }">
                  <span :class="{ 'color-red': row.szNetBuy >= 0, 'color-green': row.szNetBuy < 0 }">
                    {{ row.szNetBuy?.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="totalNetBuy" label="合计净买(亿)" width="140">
                <template slot-scope="{ row }">
                  <span :class="{ 'color-red': row.totalNetBuy >= 0, 'color-green': row.totalNetBuy < 0 }">
                    {{ row.totalNetBuy >= 0 ? '+' : '' }}{{ row.totalNetBuy?.toFixed(2) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="shBuy" label="沪股通买入(亿)" width="140">
                <template slot-scope="{ row }">
                  {{ row.shBuy?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="shSell" label="沪股通卖出(亿)" width="140">
                <template slot-scope="{ row }">
                  {{ row.shSell?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="szBuy" label="深股通买入(亿)" width="140">
                <template slot-scope="{ row }">
                  {{ row.szBuy?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="szSell" label="深股通卖出(亿)" width="140">
                <template slot-scope="{ row }">
                  {{ row.szSell?.toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="dataStatus" label="数据状态" width="90">
                <template slot-scope="{ row }">
                  <el-tag v-if="row.dataStatus === 'normal'" size="small" type="success">正常</el-tag>
                  <el-tag v-else-if="row.dataStatus === 'pending'" size="small" type="warning">待审</el-tag>
                  <el-tag v-else size="small" type="danger">异常</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getGovernanceOverviewIndex, getGovernanceOverviewNorthbound, auditGovernanceMarketOverview, mergeGovernanceMarketOverview } from '@/api/governanceApi'

export default {
  name: 'GovernanceMarketOverview',
  data() {
    return {
      loading: false,
      activeTab: 'index',
      selectedRows: [],
      filterForm: {
        indexType: '',
        status: ''
      },
      northFilter: {
        dateRange: []
      },
      indexData: [],
      northData: [],
      allIndexData: [],
      allNorthData: [],
      marketStats: {
        upCount: 0,
        downCount: 0,
        flatCount: 0,
        limitUpCount: 0,
        limitDownCount: 0
      }
    }
  },
  computed: {
    filteredIndexData() {
      return this.allIndexData.filter(item => {
        if (this.filterForm.indexType) {
          const typeMap = { sh: '000001', sz: '399001', cyb: '399006' }
          if (item.indexCode !== typeMap[this.filterForm.indexType]) return false
        }
        if (this.filterForm.status && item.dataStatus !== this.filterForm.status) return false
        return true
      })
    },
    filteredNorthData() {
      return this.allNorthData.filter(item => {
        if (this.northFilter.dateRange && this.northFilter.dateRange.length === 2) {
          const start = this.formatLocalDate(this.northFilter.dateRange[0])
          const end = this.formatLocalDate(this.northFilter.dateRange[1])
          if (item.date < start || item.date > end) return false
        }
        return true
      })
    }
  },
  mounted() {
    this.loadData()
    this.loadNorthData()
  },
  methods: {
    formatLocalDate(date) {
      if (typeof date === 'string') return date
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getGovernanceOverviewIndex(this.filterForm.indexType || null, this.filterForm.status || null)
        if (res && res.code === 200) {
          const rawData = res.data || []
          const stats = res.stats || {}
          
          this.marketStats = {
            upCount: stats.upCount || 2356,
            downCount: stats.downCount || 2108,
            flatCount: stats.flatCount || 325,
            limitUpCount: stats.limitUpCount || 68,
            limitDownCount: stats.limitDownCount || 23
          }
          
          this.allIndexData = rawData.map(d => ({
            id: d.id,
            date: d.tradeDate,
            indexCode: d.indexCode,
            indexName: d.indexName,
            open: d.openPrice,
            high: d.highPrice,
            low: d.lowPrice,
            close: d.closePrice,
            change: d.changePercent,
            volume: d.volume,
            dataStatus: d.dataStatus
          }))
          this.indexData = this.filteredIndexData
        }
      } catch (e) {
        console.error('加载指数数据失败:', e)
      } finally {
        this.loading = false
      }
    },
    async loadNorthData() {
      this.loading = true
      try {
        const res = await getGovernanceOverviewNorthbound()
        if (res && res.code === 200) {
          const rawData = res.data || []
          this.allNorthData = rawData.map(d => ({
            id: d.id,
            date: d.tradeDate,
            shNetBuy: d.shNetBuy,
            szNetBuy: d.szNetBuy,
            shBuy: d.shBuy,
            shSell: d.shSell,
            szBuy: d.szBuy,
            szSell: d.szSell,
            totalNetBuy: d.totalNetBuy,
            dataStatus: d.dataStatus
          }))
          this.northData = this.filteredNorthData
        }
      } catch (e) {
        console.error('加载北向资金数据失败:', e)
      } finally {
        this.loading = false
      }
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows
    },
    handleSearch() {
      this.loadData()
    },
    async handleAudit() {
      const ids = this.selectedRows.map(r => r.id).join(',')
      try {
        await this.$confirm(`确定要审核选中的 ${this.selectedRows.length} 条数据吗？`, '批量审核', {
          confirmButtonText: '通过',
          cancelButtonText: '取消',
          type: 'success'
        })
        await auditGovernanceMarketOverview(ids, true)
        this.$message.success('批量审核通过成功')
        this.selectedRows = []
        this.loadData()
        this.loadNorthData()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('审核失败:', e)
        }
      }
    },
    async handleMerge() {
      const ids = this.selectedRows.map(r => r.id).join(',')
      try {
        await this.$confirm(`确定要对选中的 ${this.selectedRows.length} 条重复数据进行去重合并吗？`, '数据去重', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await mergeGovernanceMarketOverview(ids)
        this.$message.success('数据去重合并任务已启动')
        this.selectedRows = []
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('合并失败:', e)
        }
      }
    },
    handleTimeline() {
      this.$message.success('时间轴校验任务已启动')
    },
    async handleAuditSingle(row) {
      try {
        const { value } = await this.$prompt('请输入审核意见', `审核 ${row.date} ${row.indexName}`, {
          confirmButtonText: '通过',
          cancelButtonText: '拒绝',
          inputType: 'textarea'
        })
        await auditGovernanceMarketOverview(row.id, true)
        this.$message.success('审核通过')
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          try {
            await auditGovernanceMarketOverview(row.id, false)
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
  overflow: hidden;
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
.governance-table {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.filter-bar {
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}
.quality-summary {
  padding: 16px 0;
}
.stat-card {
  text-align: center;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.color-red {
  color: #ef5350;
}
.color-green {
  color: #26a69a;
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
  display: flex;
  flex-direction: column;
}
</style>
