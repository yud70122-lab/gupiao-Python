<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-pie-chart"></i> 市场数据采集</h2>
        <div class="header-actions">
          <el-button type="primary" @click="handleCollect" :loading="collecting">
            <i class="el-icon-download"></i> 采集数据
          </el-button>
          <el-button @click="loadData">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </div>

      <div class="market-summary">
        <div class="summary-card up">
          <div class="summary-title">上涨家数</div>
          <div class="summary-value">{{ marketStats.upCount }}</div>
          <div class="summary-sub">占比 {{ marketStats.upPercent }}%</div>
        </div>
        <div class="summary-card down">
          <div class="summary-title">下跌家数</div>
          <div class="summary-value">{{ marketStats.downCount }}</div>
          <div class="summary-sub">占比 {{ marketStats.downPercent }}%</div>
        </div>
        <div class="summary-card flat">
          <div class="summary-title">平盘家数</div>
          <div class="summary-value">{{ marketStats.flatCount }}</div>
          <div class="summary-sub">占比 {{ marketStats.flatPercent }}%</div>
        </div>
        <div class="summary-card north">
          <div class="summary-title">北向资金</div>
          <div class="summary-value" :class="{ 'color-red': marketStats.northFlow >= 0, 'color-green': marketStats.northFlow < 0 }">
            {{ marketStats.northFlow >= 0 ? '+' : '' }}{{ marketStats.northFlow }}亿
          </div>
          <div class="summary-sub">当日净流入</div>
        </div>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="大盘指数" name="index">
          <div class="index-grid">
            <div v-for="item in indexList" :key="item.code" class="index-card">
              <div class="index-header">
                <span class="index-name">{{ item.name }}</span>
                <span class="index-code">{{ item.code }}</span>
              </div>
              <div class="index-price" :class="{ 'color-red': item.change >= 0, 'color-green': item.change < 0 }">
                {{ item.price.toFixed(2) }}
              </div>
              <div class="index-change" :class="{ 'color-red': item.change >= 0, 'color-green': item.change < 0 }">
                <span>{{ item.change >= 0 ? '+' : '' }}{{ item.change.toFixed(2) }}</span>
                <span>({{ item.change >= 0 ? '+' : '' }}{{ item.changePercent.toFixed(2) }}%)</span>
              </div>
              <div class="index-stats">
                <span>成交量: {{ formatNumber(item.volume) }}亿</span>
                <span>成交额: {{ formatNumber(item.amount) }}亿</span>
              </div>
              <div ref="indexCharts" class="index-chart"></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="涨跌分布" name="distribution">
          <div class="chart-row">
            <div class="chart-card">
              <div class="chart-card-header">
                <i class="el-icon-data-line"></i> 涨跌幅分布
              </div>
              <div ref="distributionChart" class="chart-container"></div>
            </div>
            <div class="chart-card">
              <div class="chart-card-header">
                <i class="el-icon-pie-chart"></i> 涨跌家数占比
              </div>
              <div ref="pieChart" class="chart-container"></div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="市场热度" name="heat">
          <div class="heat-filter-bar">
            <el-form :inline="true" :model="heatFilter">
              <el-form-item label="股票代码/名称">
                <el-input v-model="heatFilter.keyword" placeholder="请输入股票代码或名称" clearable @clear="handleHeatSearch" @keyup.enter.native="handleHeatSearch"></el-input>
              </el-form-item>
              <el-form-item label="涨跌幅">
                <el-select v-model="heatFilter.changeRange" placeholder="全部" clearable @change="handleHeatSearch">
                  <el-option label="上涨" value="up"></el-option>
                  <el-option label="下跌" value="down"></el-option>
                  <el-option label="涨超5%" value="up5"></el-option>
                  <el-option label="跌超5%" value="down5"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="换手率">
                <el-select v-model="heatFilter.turnoverRange" placeholder="全部" clearable @change="handleHeatSearch">
                  <el-option label="大于5%" value="5"></el-option>
                  <el-option label="大于10%" value="10"></el-option>
                  <el-option label="大于15%" value="15"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleHeatSearch">查询</el-button>
                <el-button @click="resetHeatFilter">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <el-table :data="filteredHeatData" border stripe v-loading="loading" height="calc(100vh - 580px)">
            <el-table-column type="index" label="排名" width="70" fixed="left"></el-table-column>
            <el-table-column prop="code" label="股票代码" width="120"></el-table-column>
            <el-table-column prop="name" label="股票名称" width="120"></el-table-column>
            <el-table-column prop="price" label="最新价" width="120">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.change >= 0, 'color-green': row.change < 0 }">
                  {{ row.price.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="change" label="涨跌幅" width="120">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.change >= 0, 'color-green': row.change < 0 }">
                  {{ row.change >= 0 ? '+' : '' }}{{ row.change.toFixed(2) }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="turnover" label="换手率(%)" width="130">
              <template slot-scope="{ row }">
                <el-tag :type="row.turnover > 10 ? 'danger' : row.turnover > 5 ? 'warning' : 'success'" size="small">
                  {{ row.turnover.toFixed(2) }}%
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="volumeRatio" label="量比" width="120">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.volumeRatio >= 1, 'color-green': row.volumeRatio < 1 }">
                  {{ row.volumeRatio.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="成交额(万元)" width="150">
              <template slot-scope="{ row }">{{ formatNumber(row.amount) }}</template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="北向资金" name="northbound">
          <div class="chart-row">
            <div class="chart-card" style="flex: 2;">
              <div class="chart-card-header">
                <i class="el-icon-data-line"></i> 北向资金流向趋势
              </div>
              <div ref="northChart" class="chart-container"></div>
            </div>
            <div class="chart-card" style="flex: 1;">
              <div class="chart-card-header">
                <i class="el-icon-s-data"></i> 每日资金数据
              </div>
              <el-table :data="northData" border stripe height="calc(100% - 50px)">
                <el-table-column prop="date" label="日期" width="120"></el-table-column>
                <el-table-column prop="netFlow" label="净流入(亿)" width="130">
                  <template slot-scope="{ row }">
                    <span :class="{ 'color-red': row.netFlow >= 0, 'color-green': row.netFlow < 0 }">
                      {{ row.netFlow >= 0 ? '+' : '' }}{{ row.netFlow.toFixed(2) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="buy" label="买入(亿)" width="110"></el-table-column>
                <el-table-column prop="sell" label="卖出(亿)" width="110"></el-table-column>
              </el-table>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getCollectionMarketOverview, collectMarketOverview } from '@/api/collectionApi'

export default {
  name: 'MarketOverview',
  data() {
    return {
      loading: false,
      collecting: false,
      activeTab: 'index',
      distributionChart: null,
      pieChart: null,
      northChart: null,
      marketStats: {
        upCount: 2856,
        downCount: 1923,
        flatCount: 321,
        upPercent: 55.8,
        downPercent: 37.6,
        flatPercent: 6.6,
        northFlow: 45.68
      },
      indexList: [],
      heatData: [],
      northData: [],
      heatFilter: {
        keyword: '',
        changeRange: '',
        turnoverRange: ''
      }
    }
  },
  mounted() {
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  computed: {
    filteredHeatData() {
      return this.heatData.filter(item => {
        if (this.heatFilter.keyword) {
          const kw = this.heatFilter.keyword.toLowerCase()
          if (!item.code.toLowerCase().includes(kw) && !item.name.toLowerCase().includes(kw)) return false
        }
        if (this.heatFilter.changeRange) {
          if (this.heatFilter.changeRange === 'up' && item.change < 0) return false
          if (this.heatFilter.changeRange === 'down' && item.change >= 0) return false
          if (this.heatFilter.changeRange === 'up5' && item.change < 5) return false
          if (this.heatFilter.changeRange === 'down5' && item.change > -5) return false
        }
        if (this.heatFilter.turnoverRange && item.turnover < Number(this.heatFilter.turnoverRange)) return false
        return true
      })
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    this.disposeCharts()
  },
  methods: {
    formatNumber(num) {
      if (!num && num !== 0) return '-'
      return num.toLocaleString('zh-CN')
    },
    initCharts() {
      this.$nextTick(() => {
        if (this.activeTab === 'distribution') {
          if (this.$refs.distributionChart && !this.distributionChart) {
            this.distributionChart = echarts.init(this.$refs.distributionChart)
            this.renderDistributionChart()
          }
          if (this.$refs.pieChart && !this.pieChart) {
            this.pieChart = echarts.init(this.$refs.pieChart)
            this.renderPieChart()
          }
        } else if (this.activeTab === 'northbound') {
          if (this.$refs.northChart && !this.northChart) {
            this.northChart = echarts.init(this.$refs.northChart)
            this.renderNorthChart()
          }
        }
      })
    },
    disposeCharts() {
      if (this.distributionChart) this.distributionChart.dispose()
      if (this.pieChart) this.pieChart.dispose()
      if (this.northChart) this.northChart.dispose()
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getCollectionMarketOverview()
        if (res && res.code === 200) {
          if (res.marketStats) {
            this.marketStats = res.marketStats
          }
          this.indexList = (res.indices || []).map(idx => ({
            code: idx.indexCode,
            name: idx.indexName,
            price: idx.closePrice,
            change: idx.closePrice - idx.openPrice,
            changePercent: idx.changePercent,
            volume: idx.volume,
            amount: idx.amount
          }))
          this.heatData = (res.heatStocks || []).map(s => ({
            code: s.stockCode,
            name: s.stockName,
            price: s.stockPrice,
            change: s.stockChange,
            turnover: s.stockTurnover,
            volumeRatio: s.volumeRatio,
            amount: s.stockAmount,
            updateTime: s.updateTime
          })).sort((a, b) => b.turnover - a.turnover)
          this.northData = (res.northbound || []).map(n => ({
            date: n.tradeDate,
            netFlow: n.totalNetBuy,
            buy: n.shBuy + n.szBuy,
            sell: n.shSell + n.szSell
          }))
          this.initCharts()
        }
      } catch (e) {
        console.error('加载市场数据失败:', e)
      } finally {
        this.loading = false
      }
    },
    renderDistributionChart() {
      const ranges = ['跌停', '-10%~-7%', '-7%~-3%', '-3%~0%', '0%~3%', '3%~7%', '7%~10%', '涨停']
      const values = [15, 85, 356, 1467, 1856, 652, 186, 68]
      const colors = ['#26a69a', '#4db6ac', '#80cbc4', '#b2dfdb', '#ffcdd2', '#ef9a9a', '#e57373', '#ef5350']

      const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        grid: { left: '10%', right: '10%', top: '10%', bottom: '10%' },
        xAxis: { type: 'category', data: ranges, axisLabel: { rotate: 30 } },
        yAxis: { type: 'value', name: '家数' },
        series: [{
          type: 'bar',
          data: values,
          itemStyle: {
            color: (params) => colors[params.dataIndex]
          },
          label: {
            show: true,
            position: 'top'
          }
        }]
      }
      this.distributionChart.setOption(option, true)
    },
    renderPieChart() {
      const option = {
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
          label: { show: false, position: 'center' },
          emphasis: {
            label: { show: true, fontSize: 20, fontWeight: 'bold' }
          },
          labelLine: { show: false },
          data: [
            { value: this.marketStats.upCount, name: '上涨', itemStyle: { color: '#ef5350' } },
            { value: this.marketStats.downCount, name: '下跌', itemStyle: { color: '#26a69a' } },
            { value: this.marketStats.flatCount, name: '平盘', itemStyle: { color: '#909399' } }
          ]
        }]
      }
      this.pieChart.setOption(option, true)
    },
    renderNorthChart() {
      const dates = this.northData.map(d => d.date)
      const netFlows = this.northData.map(d => d.netFlow)

      const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
        legend: { data: ['北向资金净流入'], top: 0 },
        grid: { left: '10%', right: '10%', top: '15%', bottom: '10%' },
        xAxis: { type: 'category', data: dates, axisLabel: { rotate: 45 } },
        yAxis: { type: 'value', name: '亿元', axisLine: { lineStyle: { color: '#303133' } } },
        series: [{
          name: '北向资金净流入',
          type: 'bar',
          data: netFlows,
          itemStyle: {
            color: (params) => params.value >= 0 ? '#ef5350' : '#26a69a'
          }
        }]
      }
      this.northChart.setOption(option, true)
    },
    handleHeatSearch() {
    },
    resetHeatFilter() {
      this.heatFilter = {
        keyword: '',
        changeRange: '',
        turnoverRange: ''
      }
    },
    async handleCollect() {
      this.collecting = true
      try {
        await this.$confirm('确定要采集市场数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        await collectMarketOverview()
        this.$message.success('市场数据采集任务已启动')
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
      if (this.distributionChart) this.distributionChart.resize()
      if (this.pieChart) this.pieChart.resize()
      if (this.northChart) this.northChart.resize()
    }
  },
  watch: {
    activeTab() {
      this.disposeCharts()
      this.distributionChart = null
      this.pieChart = null
      this.northChart = null
      this.$nextTick(() => {
        this.initCharts()
      })
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
.heat-filter-bar {
  padding: 12px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 12px;
}
.market-summary {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.summary-card {
  padding: 16px;
  border-radius: 8px;
  text-align: center;
  border: 1px solid #e4e7ed;
}
.summary-card.up { background: linear-gradient(135deg, #ff6b6b1a 0%, #ee5a6f1a 100%); }
.summary-card.down { background: linear-gradient(135deg, #51cf661a 0%, #40c0571a 100%); }
.summary-card.flat { background: linear-gradient(135deg, #868e961a 0%, #4950571a 100%); }
.summary-card.north { background: linear-gradient(135deg, #ffd43b1a 0%, #fab0051a 100%); }
.summary-title { font-size: 14px; color: #606266; margin-bottom: 8px; }
.summary-value { font-size: 28px; font-weight: 600; color: #303133; }
.summary-sub { font-size: 12px; color: #909399; margin-top: 4px; }
.index-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.index-card {
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: white;
}
.index-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.index-name { font-size: 16px; font-weight: 600; color: #303133; }
.index-code { font-size: 12px; color: #909399; }
.index-price { font-size: 24px; font-weight: 600; margin-bottom: 4px; }
.index-change { font-size: 14px; margin-bottom: 8px; }
.index-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #606266;
  margin-bottom: 8px;
}
.index-chart { height: 60px; }
.chart-row {
  display: flex;
  gap: 16px;
  height: 100%;
}
.chart-card {
  flex: 1;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: white;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.chart-card-header {
  padding: 12px 20px;
  border-bottom: 1px solid #e4e7ed;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.chart-container {
  flex: 1;
  min-height: 300px;
}
.color-red { color: #ef5350; font-weight: 600; }
.color-green { color: #26a69a; font-weight: 600; }
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
