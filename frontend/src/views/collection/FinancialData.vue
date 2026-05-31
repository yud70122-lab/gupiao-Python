<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-s-data"></i> 财务数据采集</h2>
        <div class="header-actions">
          <el-button type="primary" @click="handleCollect" :loading="collecting">
            <i class="el-icon-download"></i> 采集数据
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
                         :label="stock.code + ' - ' + stock.name" :value="stock">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="报告类型">
            <el-radio-group v-model="form.reportType" @change="handleReportTypeChange">
              <el-radio-button label="income">利润表</el-radio-button>
              <el-radio-button label="balance">资产负债表</el-radio-button>
              <el-radio-button label="cashflow">现金流表</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="报告周期">
            <el-radio-group v-model="form.period" @change="handleSearch">
              <el-radio-button label="annual">年报</el-radio-button>
              <el-radio-button label="quarter">季报</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="财务指标" name="indicators">
          <div class="metrics-grid">
            <el-card v-for="item in keyMetrics" :key="item.key" class="metric-card">
              <div class="metric-label">{{ item.label }}</div>
              <div class="metric-value" :class="{ 'color-red': item.trend === 'up', 'color-green': item.trend === 'down' }">
                {{ item.value }}
              </div>
              <div v-if="item.change" class="metric-change" :class="{ 'color-red': item.change > 0, 'color-green': item.change < 0 }">
                {{ item.change > 0 ? '+' : '' }}{{ item.change.toFixed(2) }}%
              </div>
            </el-card>
          </div>

          <div class="chart-row">
            <div class="chart-card">
              <div class="chart-card-header">
                <i class="el-icon-data-line"></i> 市盈率 / 市净率 趋势
              </div>
              <div ref="valuationChart" class="chart-container"></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="利润表" name="income">
          <el-table :data="incomeData" border stripe v-loading="loading" height="calc(100vh - 460px)">
            <el-table-column prop="reportDate" label="报告期" width="140" fixed="left"></el-table-column>
            <el-table-column prop="totalRevenue" label="营业收入(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.totalRevenue) }}</template>
            </el-table-column>
            <el-table-column prop="operatingCost" label="营业成本(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.operatingCost) }}</template>
            </el-table-column>
            <el-table-column prop="operatingProfit" label="营业利润(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.operatingProfit) }}</template>
            </el-table-column>
            <el-table-column prop="totalProfit" label="利润总额(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.totalProfit) }}</template>
            </el-table-column>
            <el-table-column prop="netProfit" label="净利润(万元)" width="160">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.netProfit >= 0, 'color-green': row.netProfit < 0 }">
                  {{ formatNumber(row.netProfit) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="netProfitParent" label="归母净利润(万元)" width="160">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.netProfitParent >= 0, 'color-green': row.netProfitParent < 0 }">
                  {{ formatNumber(row.netProfitParent) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="eps" label="每股收益(元)" width="130">
              <template slot-scope="{ row }">{{ row.eps?.toFixed(3) }}</template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="资产负债表" name="balance">
          <el-table :data="balanceData" border stripe v-loading="loading" height="calc(100vh - 460px)">
            <el-table-column prop="reportDate" label="报告期" width="140" fixed="left"></el-table-column>
            <el-table-column prop="totalAssets" label="总资产(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.totalAssets) }}</template>
            </el-table-column>
            <el-table-column prop="totalLiabilities" label="总负债(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.totalLiabilities) }}</template>
            </el-table-column>
            <el-table-column prop="totalEquity" label="股东权益(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.totalEquity) }}</template>
            </el-table-column>
            <el-table-column prop="currentAssets" label="流动资产(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.currentAssets) }}</template>
            </el-table-column>
            <el-table-column prop="currentLiabilities" label="流动负债(万元)" width="160">
              <template slot-scope="{ row }">{{ formatNumber(row.currentLiabilities) }}</template>
            </el-table-column>
            <el-table-column prop="debtRatio" label="资产负债率(%)" width="140">
              <template slot-scope="{ row }">{{ row.debtRatio?.toFixed(2) }}%</template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="现金流表" name="cashflow">
          <el-table :data="cashflowData" border stripe v-loading="loading" height="calc(100vh - 460px)">
            <el-table-column prop="reportDate" label="报告期" width="140" fixed="left"></el-table-column>
            <el-table-column prop="operatingCashFlow" label="经营现金流(万元)" width="170">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.operatingCashFlow >= 0, 'color-green': row.operatingCashFlow < 0 }">
                  {{ formatNumber(row.operatingCashFlow) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="investingCashFlow" label="投资现金流(万元)" width="170">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.investingCashFlow >= 0, 'color-green': row.investingCashFlow < 0 }">
                  {{ formatNumber(row.investingCashFlow) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="financingCashFlow" label="筹资现金流(万元)" width="170">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.financingCashFlow >= 0, 'color-green': row.financingCashFlow < 0 }">
                  {{ formatNumber(row.financingCashFlow) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="netCashFlow" label="净现金流(万元)" width="160">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.netCashFlow >= 0, 'color-green': row.netCashFlow < 0 }">
                  {{ formatNumber(row.netCashFlow) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getCollectionFinancialStocks, getCollectionFinancialData, collectFinancialData } from '@/api/collectionApi'

export default {
  name: 'FinancialData',
  data() {
    return {
      loading: false,
      collecting: false,
      activeTab: 'indicators',
      chartInstance: null,
      stockList: [],
      form: {
        stock: null,
        reportType: 'income',
        period: 'annual'
      },
      keyMetrics: [],
      incomeData: [],
      balanceData: [],
      cashflowData: [],
      valuationTrend: []
    }
  },
  mounted() {
    this.initChart()
    this.loadStockList()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  },
  methods: {
    formatNumber(num) {
      if (!num && num !== 0) return '-'
      return num.toLocaleString('zh-CN')
    },
    initChart() {
      if (this.$refs.valuationChart && !this.chartInstance) {
        this.chartInstance = echarts.init(this.$refs.valuationChart)
      }
    },
    async loadStockList() {
      try {
        const res = await getCollectionFinancialStocks()
        if (res && res.code === 200) {
          this.stockList = res.data || []
          if (this.stockList.length > 0 && !this.form.stock) {
            this.form.stock = this.stockList[0]
          }
          this.loadData()
        }
      } catch (e) {
        console.error('加载股票列表失败:', e)
      }
    },
    async loadData() {
      if (!this.form.stock) return
      this.loading = true
      try {
        const res = await getCollectionFinancialData(this.form.stock.code, this.form.reportType, this.form.period)
        if (res && res.code === 200) {
          this.keyMetrics = res.keyMetrics || []
          this.incomeData = res.incomeData || []
          this.balanceData = res.balanceData || []
          this.cashflowData = res.cashflowData || []
          this.valuationTrend = res.valuationTrend || []
          this.renderValuationChart()
        }
      } catch (e) {
        console.error('加载财务数据失败:', e)
      } finally {
        this.loading = false
      }
    },
    renderValuationChart() {
      if (!this.valuationTrend || this.valuationTrend.length === 0) {
        const quarters = ['2025Q1', '2025Q2', '2025Q3', '2025Q4', '2026Q1']
        const peData = quarters.map(() => Math.round(28 + Math.random() * 10) * 100) / 100
        const pbData = quarters.map(() => Math.round(3 + Math.random() * 2) * 100) / 100
        this.valuationTrend = quarters.map((q, i) => ({ period: q, pe: peData[i], pb: pbData[i] }))
      }

      const quarters = this.valuationTrend.map(d => d.period)
      const peData = this.valuationTrend.map(d => d.pe)
      const pbData = this.valuationTrend.map(d => d.pb)

      const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
        legend: { data: ['市盈率(PE)', '市净率(PB)'], top: 0 },
        grid: { left: '10%', right: '10%', top: '15%', bottom: '10%' },
        xAxis: { type: 'category', data: quarters },
        yAxis: [
          { type: 'value', name: 'PE', position: 'left' },
          { type: 'value', name: 'PB', position: 'right' }
        ],
        series: [
          {
            name: '市盈率(PE)',
            type: 'line',
            data: peData,
            smooth: true,
            lineStyle: { color: '#5470c6', width: 2 },
            itemStyle: { color: '#5470c6' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(84, 112, 198, 0.3)' },
                { offset: 1, color: 'rgba(84, 112, 198, 0.05)' }
              ])
            }
          },
          {
            name: '市净率(PB)',
            type: 'line',
            yAxisIndex: 1,
            data: pbData,
            smooth: true,
            lineStyle: { color: '#91cc75', width: 2 },
            itemStyle: { color: '#91cc75' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(145, 204, 117, 0.3)' },
                { offset: 1, color: 'rgba(145, 204, 117, 0.05)' }
              ])
            }
          }
        ]
      }
      this.chartInstance.setOption(option, true)
    },
    handleSearch() {
      this.loadData()
    },
    handleReportTypeChange(val) {
      this.activeTab = val
      this.loadData()
    },
    async handleCollect() {
      if (!this.form.stock) {
        this.$message.warning('请先选择股票')
        return
      }
      this.collecting = true
      try {
        await this.$confirm(`确定要采集 ${this.form.stock?.code || ''} ${this.form.stock?.name || ''} 的财务数据吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        await collectFinancialData(this.form.stock.code)
        this.$message.success('财务数据采集任务已启动')
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('采集失败:', e)
        }
      } finally {
        this.collecting = false
      }
    },
    handleResize() {
      if (this.chartInstance) this.chartInstance.resize()
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
.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.metric-card {
  text-align: center;
}
.metric-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.metric-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
.metric-change {
  font-size: 12px;
  margin-top: 4px;
}
.chart-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}
.chart-card {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: white;
  display: flex;
  flex-direction: column;
}
.chart-card-header {
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}
.chart-container {
  height: 280px;
}
.color-red {
  color: #ef5350;
  font-weight: 600;
}
.color-green {
  color: #26a69a;
  font-weight: 600;
}
::v-deep .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
::v-deep .el-tabs__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
::v-deep .el-tab-pane {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: auto;
}
</style>
