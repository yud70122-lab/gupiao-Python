<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-data-line"></i> 行情数据采集</h2>
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

          <el-form-item label="K线周期">
            <el-radio-group v-model="form.period" @change="handleSearch">
              <el-radio-button label="daily">日线</el-radio-button>
              <el-radio-button label="weekly">周线</el-radio-button>
              <el-radio-button label="monthly">月线</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="分钟周期">
            <el-radio-group v-model="form.minute" @change="handleSearch">
              <el-radio-button label="1min">1分钟</el-radio-button>
              <el-radio-button label="5min">5分钟</el-radio-button>
              <el-radio-button label="15min">15分钟</el-radio-button>
              <el-radio-button label="30min">30分钟</el-radio-button>
              <el-radio-button label="60min">60分钟</el-radio-button>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="日期范围">
            <el-date-picker
              v-model="form.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="handleSearch">
            </el-date-picker>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <el-tab-pane label="K线数据" name="kline">
          <div ref="klineChart" class="chart-container"></div>
        </el-tab-pane>
        <el-tab-pane label="数据列表" name="list">
          <el-table :data="tableData" border stripe v-loading="loading" height="calc(100vh - 460px)">
            <el-table-column prop="date" label="日期时间" width="180" fixed="left"></el-table-column>
            <el-table-column prop="open" label="开盘价" width="120">
              <template slot-scope="{ row }">
                <span :class="{ 'color-red': row.open >= row.close, 'color-green': row.open < row.close }">
                  {{ row.open?.toFixed(2) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="high" label="最高价" width="120">
              <template slot-scope="{ row }">
                <span class="color-red">{{ row.high?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="low" label="最低价" width="120">
              <template slot-scope="{ row }">
                <span class="color-green">{{ row.low?.toFixed(2) }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="close" label="收盘价" width="120">
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
            <el-table-column prop="volume" label="成交量(手)" width="140">
              <template slot-scope="{ row }">
                {{ formatNumber(row.volume) }}
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="成交额(万元)" width="140">
              <template slot-scope="{ row }">
                {{ formatNumber(row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="turnover" label="换手率(%)" width="120">
              <template slot-scope="{ row }">
                {{ row.turnover?.toFixed(3) }}
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-bar">
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="pagination.page"
              :page-sizes="[20, 50, 100, 200]"
              :page-size="pagination.size"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total">
            </el-pagination>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getCollectionMarketStocks, getCollectionMarketData, collectMarketData } from '@/api/collectionApi'

export default {
  name: 'MarketData',
  data() {
    return {
      loading: false,
      collecting: false,
      activeTab: 'kline',
      chartInstance: null,
      stockList: [],
      form: {
        stock: null,
        period: 'daily',
        minute: '5min',
        dateRange: []
      },
      tableData: [],
      pagination: {
        page: 1,
        size: 20,
        total: 0
      }
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
      if (this.$refs.klineChart && !this.chartInstance) {
        this.chartInstance = echarts.init(this.$refs.klineChart)
      }
    },
    async loadStockList() {
      try {
        const res = await getCollectionMarketStocks()
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
    aggregateToWeekly(dailyData) {
      const weeklyMap = {}
      dailyData.forEach(d => {
        const date = new Date(d.date || d.tradeDate)
        const weekStart = new Date(date)
        weekStart.setDate(date.getDate() - date.getDay() + 1)
        const key = weekStart.toISOString().split('T')[0]
        if (!weeklyMap[key]) {
          weeklyMap[key] = { date: key, open: d.openPrice, high: d.highPrice, low: d.lowPrice, close: d.closePrice, volume: d.volume, amount: d.amount, turnover: d.turnover, change: d.changePercent }
        } else {
          weeklyMap[key].high = Math.max(weeklyMap[key].high, d.highPrice)
          weeklyMap[key].low = Math.min(weeklyMap[key].low, d.lowPrice)
          weeklyMap[key].close = d.closePrice
          weeklyMap[key].volume += d.volume
          weeklyMap[key].amount += d.amount
        }
      })
      const result = Object.values(weeklyMap).sort((a, b) => a.date.localeCompare(b.date))
      for (let i = 0; i < result.length; i++) {
        if (i === 0) {
          result[i].change = 0
        } else {
          result[i].change = Math.round((result[i].close - result[i - 1].close) / result[i - 1].close * 10000) / 100
        }
      }
      return result
    },
    aggregateToMonthly(dailyData) {
      const monthlyMap = {}
      dailyData.forEach(d => {
        const dateStr = d.date || d.tradeDate
        const key = dateStr.substring(0, 7) + '-01'
        if (!monthlyMap[key]) {
          monthlyMap[key] = { date: key, open: d.openPrice, high: d.highPrice, low: d.lowPrice, close: d.closePrice, volume: d.volume, amount: d.amount, turnover: d.turnover, change: d.changePercent }
        } else {
          monthlyMap[key].high = Math.max(monthlyMap[key].high, d.highPrice)
          monthlyMap[key].low = Math.min(monthlyMap[key].low, d.lowPrice)
          monthlyMap[key].close = d.closePrice
          monthlyMap[key].volume += d.volume
          monthlyMap[key].amount += d.amount
        }
      })
      const result = Object.values(monthlyMap).sort((a, b) => a.date.localeCompare(b.date))
      for (let i = 0; i < result.length; i++) {
        if (i === 0) {
          result[i].change = 0
        } else {
          result[i].change = Math.round((result[i].close - result[i - 1].close) / result[i - 1].close * 10000) / 100
        }
      }
      return result
    },
    async loadData() {
      if (!this.form.stock) return
      this.loading = true
      try {
        let startDate = null
        let endDate = null
        if (this.form.dateRange && this.form.dateRange.length === 2) {
          startDate = this.form.dateRange[0].toISOString().split('T')[0]
          endDate = this.form.dateRange[1].toISOString().split('T')[0]
        }

        const res = await getCollectionMarketData(this.form.stock.code, this.form.period, startDate, endDate)
        if (res && res.code === 200) {
          let rawData = res.data || []
          let data = rawData.map(d => ({
            date: d.tradeDate,
            open: d.openPrice,
            high: d.highPrice,
            low: d.lowPrice,
            close: d.closePrice,
            volume: d.volume,
            amount: d.amount,
            turnover: d.turnover,
            change: d.changePercent
          }))

          if (this.form.period === 'weekly') {
            data = this.aggregateToWeekly(rawData)
          } else if (this.form.period === 'monthly') {
            data = this.aggregateToMonthly(rawData)
          }

          this.tableData = data
          this.pagination.total = data.length
          this.pagination.page = 1
          this.renderKlineChart(data)
        }
      } catch (e) {
        console.error('加载行情数据失败:', e)
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.loadData()
    },
    renderKlineChart(data) {
      const dates = data.map(d => d.date)
      const klineData = data.map(d => [d.open, d.close, d.low, d.high])
      const volumes = data.map(d => d.volume)
      const colors = data.map(d => d.close >= d.open ? '#ef5350' : '#26a69a')

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' },
          formatter: (params) => {
            const k = params.find(p => p.seriesName === 'K线')
            const v = params.find(p => p.seriesName === '成交量')
            if (!k) return ''
            const [open, close, low, high] = k.value
            return `<div style="font-weight: bold;">${k.axisValue}</div>
                    <div>开盘: ${open.toFixed(2)}</div>
                    <div>收盘: ${close.toFixed(2)}</div>
                    <div>最高: ${high.toFixed(2)}</div>
                    <div>最低: ${low.toFixed(2)}</div>
                    ${v ? `<div>成交量: ${v.value.toLocaleString()} 手</div>` : ''}`
          }
        },
        legend: { data: ['K线', '成交量'], top: 0 },
        grid: [
          { left: '10%', right: '8%', top: '8%', height: '55%' },
          { left: '10%', right: '8%', top: '73%', height: '16%' }
        ],
        xAxis: [
          { type: 'category', data: dates, gridIndex: 0, axisLabel: { show: false } },
          { type: 'category', data: dates, gridIndex: 1 }
        ],
        yAxis: [
          { scale: true, gridIndex: 0, splitArea: { show: true } },
          { scale: true, gridIndex: 1, splitNumber: 2 }
        ],
        dataZoom: [
          { type: 'inside', xAxisIndex: [0, 1], start: 50, end: 100 },
          { show: true, xAxisIndex: [0, 1], type: 'slider', bottom: '2%', start: 50, end: 100 }
        ],
        series: [
          {
            name: 'K线',
            type: 'candlestick',
            data: klineData,
            itemStyle: {
              color: '#ef5350',
              color0: '#26a69a',
              borderColor: '#ef5350',
              borderColor0: '#26a69a'
            }
          },
          {
            name: '成交量',
            type: 'bar',
            xAxisIndex: 1,
            yAxisIndex: 1,
            data: volumes,
            itemStyle: {
              color: (params) => colors[params.dataIndex]
            }
          }
        ]
      }
      this.chartInstance.setOption(option, true)
    },
    async handleCollect() {
      if (!this.form.stock) {
        this.$message.warning('请先选择股票')
        return
      }
      this.collecting = true
      try {
        await this.$confirm(`确定要采集 ${this.form.stock?.code || ''} ${this.form.stock?.name || ''} 的行情数据吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        await collectMarketData(this.form.stock.code)
        this.$message.success('行情数据采集任务已启动')
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
    },
    handleSizeChange(val) {
      this.pagination.size = val
    },
    handleCurrentChange(val) {
      this.pagination.page = val
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
.chart-container {
  height: calc(100vh - 420px);
  min-height: 400px;
}
.pagination-bar {
  padding: 16px 0 0 0;
  display: flex;
  justify-content: flex-end;
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
  overflow: hidden;
}
</style>
