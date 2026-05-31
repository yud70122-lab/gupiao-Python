<template>
  <div class="analysis-layout">
    <div class="control-panel">
      <h3><i class="el-icon-s-operation"></i> 控制面板</h3>

      <el-form label-position="top">
        <el-form-item label="选择股票">
          <el-select v-model="selectedStock" filterable placeholder="请选择股票" @change="loadData">
            <el-option v-for="stock in stockList" :key="stock.code"
                       :label="stock.code + ' - ' + stock.name" :value="stock">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="对比股票 (多选)">
          <el-select v-model="compareStocks" multiple filterable placeholder="选择多只股票对比">
            <el-option v-for="stock in stockList" :key="stock.code"
                       :label="stock.code + ' - ' + stock.name" :value="stock">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="时间周期">
          <el-radio-group v-model="timeRange" @change="loadData">
            <el-radio-button label="1M">近1月</el-radio-button>
            <el-radio-button label="3M">近3月</el-radio-button>
            <el-radio-button label="6M">近6月</el-radio-button>
            <el-radio-button label="1Y">近1年</el-radio-button>
            <el-radio-button label="2Y">近2年</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="图表类型">
          <el-radio-group v-model="chartType" @change="loadData">
            <el-radio-button label="kline">K线图</el-radio-button>
            <el-radio-button label="return">收益对比</el-radio-button>
            <el-radio-button label="correlation">相关性热力图</el-radio-button>
            <el-radio-button label="riskReturn">风险收益散点</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="loadData" :loading="loading" style="width: 100%">
            <i class="el-icon-refresh"></i> 刷新数据
          </el-button>
        </el-form-item>
      </el-form>

      <div v-if="volatilityData" class="metrics-card">
        <h4><i class="el-icon-data-analysis"></i> 风险指标</h4>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="年化波动率">
            {{ (volatilityData.annualizedVolatility * 100).toFixed(2) }}%
          </el-descriptions-item>
          <el-descriptions-item label="日波动率">
            {{ (volatilityData.dailyVolatility * 100).toFixed(2) }}%
          </el-descriptions-item>
          <el-descriptions-item label="夏普比率">
            {{ volatilityData.sharpeRatio.toFixed(3) }}
          </el-descriptions-item>
          <el-descriptions-item label="最大回撤">
            {{ (volatilityData.maxDrawdown * 100).toFixed(2) }}%
          </el-descriptions-item>
          <el-descriptions-item label="年化收益">
            {{ (volatilityData.averageReturn * 252 * 100).toFixed(2) }}%
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <div class="chart-area">
      <div class="chart-card">
        <div class="chart-card-header">
          <i class="el-icon-data-line"></i> {{ getChartTitle() }}
        </div>
        <div ref="chartRef" class="chart-container"></div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'StockAnalysis',
  data() {
    return {
      stockList: [
        { code: '000001', name: '平安银行', basePrice: 12 },
        { code: '000002', name: '万科A', basePrice: 10 },
        { code: '600036', name: '招商银行', basePrice: 35 },
        { code: '600519', name: '贵州茅台', basePrice: 1800 },
        { code: '000858', name: '五粮液', basePrice: 160 },
        { code: '601318', name: '中国平安', basePrice: 45 },
        { code: '002594', name: '比亚迪', basePrice: 260 },
        { code: '601899', name: '紫金矿业', basePrice: 12 },
        { code: '002415', name: '海康威视', basePrice: 35 },
        { code: '300750', name: '宁德时代', basePrice: 200 }
      ],
      selectedStock: null,
      compareStocks: [],
      timeRange: '1Y',
      chartType: 'kline',
      loading: false,
      volatilityData: null,
      chartInstance: null
    }
  },
  mounted() {
    this.initChart()
    this.loadData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
  },
  methods: {
    getChartTitle() {
      const stockName = this.selectedStock ? this.selectedStock.code + ' ' + this.selectedStock.name : ''
      switch (this.chartType) {
        case 'kline': return stockName + ' K线图'
        case 'return': return '累计收益率对比'
        case 'correlation': return '相关性热力图'
        case 'riskReturn': return '风险收益散点图'
        default: return '图表'
      }
    },
    seededRandom(seed) {
      let s = seed
      return function() {
        s = (s * 9301 + 49297) % 233280
        return s / 233280
      }
    },
    gaussianRandom(rand) {
      let u = 0, v = 0
      while (u === 0) u = rand()
      while (v === 0) v = rand()
      return Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v)
    },
    generateStockData(stock, startDate, endDate, seedOffset) {
      const rand = this.seededRandom(42 + seedOffset)
      const data = []
      let currentPrice = stock.basePrice
      let previousClose = stock.basePrice
      const start = new Date(startDate)
      const end = new Date(endDate)
      for (let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
        const dayOfWeek = d.getDay()
        if (dayOfWeek >= 1 && dayOfWeek <= 5) {
          const change = this.gaussianRandom(rand) * 0.02 + 0.0005
          const open = currentPrice * (1 + this.gaussianRandom(rand) * 0.005)
          const close = currentPrice * (1 + change)
          const high = Math.max(open, close) * (1 + Math.abs(this.gaussianRandom(rand)) * 0.01)
          const low = Math.min(open, close) * (1 - Math.abs(this.gaussianRandom(rand)) * 0.01)
          const volume = Math.floor(1000000 + rand() * 5000000)
          const dailyReturn = previousClose !== 0 ? (close - previousClose) / previousClose : 0
          data.push({
            date: new Date(d).toISOString().split('T')[0],
            open: Math.round(open * 100) / 100,
            high: Math.round(high * 100) / 100,
            low: Math.round(low * 100) / 100,
            close: Math.round(close * 100) / 100,
            volume: volume,
            dailyReturn: Math.round(dailyReturn * 10000) / 10000
          })
          previousClose = close
          currentPrice = close
        }
      }
      return data
    },
    getDateRange(range) {
      const end = new Date()
      const start = new Date()
      switch (range) {
        case '1M': start.setMonth(start.getMonth() - 1); break
        case '3M': start.setMonth(start.getMonth() - 3); break
        case '6M': start.setMonth(start.getMonth() - 6); break
        case '1Y': start.setFullYear(start.getFullYear() - 1); break
        case '2Y': start.setFullYear(start.getFullYear() - 2); break
      }
      return { startDate: start.toISOString().split('T')[0], endDate: end.toISOString().split('T')[0] }
    },
    calculateVolatility(data) {
      const returns = data.map(d => d.dailyReturn)
      const n = returns.length
      const mean = returns.reduce((a, b) => a + b, 0) / n
      const variance = returns.reduce((sum, r) => sum + Math.pow(r - mean, 2), 0) / (n - 1)
      const dailyVol = Math.sqrt(variance)
      const annualizedVol = dailyVol * Math.sqrt(252)
      const sharpe = (mean * 252) / annualizedVol
      let peak = data[0].close
      let maxDrawdown = 0
      for (const d of data) {
        if (d.close > peak) peak = d.close
        const drawdown = (peak - d.close) / peak
        if (drawdown > maxDrawdown) maxDrawdown = drawdown
      }
      return { dailyVolatility: dailyVol, annualizedVolatility: annualizedVol, sharpeRatio: sharpe, maxDrawdown: maxDrawdown, averageReturn: mean }
    },
    initChart() {
      if (this.$refs.chartRef && !this.chartInstance) {
        this.chartInstance = echarts.init(this.$refs.chartRef)
      }
    },
    loadData() {
      this.loading = true
      setTimeout(() => {
        if (!this.selectedStock) {
          this.selectedStock = this.stockList[0]
          this.compareStocks = this.stockList.slice(1, 5)
        }
        const { startDate, endDate } = this.getDateRange(this.timeRange)
        const data = this.generateStockData(this.selectedStock, startDate, endDate, 0)
        if (this.chartType === 'kline') {
          this.loadKLineData(data)
          this.volatilityData = this.calculateVolatility(data)
        } else if (this.chartType === 'return') {
          this.loadReturnData(startDate, endDate)
          this.volatilityData = null
        } else if (this.chartType === 'correlation') {
          this.loadCorrelationData(startDate, endDate)
          this.volatilityData = null
        } else if (this.chartType === 'riskReturn') {
          this.loadRiskReturnData(startDate, endDate)
          this.volatilityData = null
        }
        this.loading = false
      }, 300)
    },
    loadKLineData(data) {
      const dates = data.map(d => d.date)
      const klineData = data.map(d => [d.open, d.close, d.low, d.high])
      const volumes = data.map(d => d.volume)
      const option = {
        tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
        legend: { data: ['K线', '成交量'], top: 0 },
        grid: [
          { left: '10%', right: '8%', top: '10%', height: '50%' },
          { left: '10%', right: '8%', top: '72%', height: '16%' }
        ],
        xAxis: [
          { type: 'category', data: dates, gridIndex: 0 },
          { type: 'category', data: dates, gridIndex: 1 }
        ],
        yAxis: [
          { scale: true, gridIndex: 0, splitArea: { show: true } },
          { scale: true, gridIndex: 1, splitNumber: 2 }
        ],
        dataZoom: [
          { type: 'inside', xAxisIndex: [0, 1], start: 0, end: 100 },
          { show: true, xAxisIndex: [0, 1], type: 'slider', bottom: '2%', start: 0, end: 100 }
        ],
        series: [
          { name: 'K线', type: 'candlestick', data: klineData, itemStyle: { color: '#ef5350', color0: '#26a69a' } },
          { name: '成交量', type: 'bar', xAxisIndex: 1, yAxisIndex: 1, data: volumes }
        ]
      }
      this.chartInstance.setOption(option, true)
    },
    loadReturnData(startDate, endDate) {
      const stocks = [this.selectedStock, ...this.compareStocks].filter(Boolean)
      const allData = []
      const stockColors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
      stocks.forEach((stock, i) => {
        const data = this.generateStockData(stock, startDate, endDate, i)
        let cumReturn = 0
        const seriesData = data.map((d, idx) => {
          if (idx === 0) {
            cumReturn = d.dailyReturn
          } else {
            cumReturn = (1 + cumReturn) * (1 + d.dailyReturn) - 1
          }
          return cumReturn * 100
        })
        allData.push({ name: stock.name, data: seriesData, color: stockColors[i % stockColors.length] })
      })
      const option = {
        tooltip: { trigger: 'axis' },
        legend: { data: allData.map(d => d.name), top: 0 },
        grid: { left: '10%', right: '8%', top: '10%', bottom: '10%' },
        xAxis: { type: 'category', boundaryGap: false, data: allData[0].data.map((_, i) => i) },
        yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
        dataZoom: [
          { type: 'inside', start: 0, end: 100 },
          { show: true, type: 'slider', bottom: '2%', start: 0, end: 100 }
        ],
        series: allData.map(d => ({
          name: d.name, type: 'line', data: d.data, smooth: true,
          lineStyle: { color: d.color, width: 2 }
        }))
      }
      this.chartInstance.setOption(option, true)
    },
    loadCorrelationData(startDate, endDate) {
      const stocks = [this.selectedStock, ...this.compareStocks].filter(Boolean).slice(0, 6)
      const names = stocks.map(s => s.name)
      const heatmapData = []
      for (let i = 0; i < stocks.length; i++) {
        for (let j = 0; j < stocks.length; j++) {
          let corr = i === j ? 1 : (Math.random() - 0.5) * 2
          heatmapData.push([j, i, parseFloat(corr.toFixed(4))])
        }
      }
      const option = {
        tooltip: { formatter: (params) => names[params.value[1]] + ' × ' + names[params.value[0]] + '<br/>相关系数: ' + params.value[2] },
        grid: { left: '15%', right: '10%', top: '5%', bottom: '15%' },
        xAxis: { type: 'category', data: names, axisLabel: { rotate: 45 } },
        yAxis: { type: 'category', data: names },
        visualMap: { min: -1, max: 1, calculable: true, orient: 'horizontal', left: 'center', bottom: '0%', inRange: { color: ['#26a69a', '#ffffff', '#ef5350'] } },
        series: [{ type: 'heatmap', data: heatmapData, label: { show: true, fontSize: 10, formatter: (p) => p.value[2].toFixed(2) } }]
      }
      this.chartInstance.setOption(option, true)
    },
    loadRiskReturnData(startDate, endDate) {
      const stocks = [this.selectedStock, ...this.compareStocks].filter(Boolean)
      const stockColors = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de']
      const scatterData = stocks.map((stock, i) => ({
        name: stock.name,
        value: [20 + Math.random() * 30, Math.random() * 40 - 10],
        itemStyle: { color: stockColors[i % stockColors.length] }
      }))
      const option = {
        tooltip: { formatter: (params) => params.name + '<br/>风险: ' + params.value[0].toFixed(2) + '%<br/>收益: ' + params.value[1].toFixed(2) + '%' },
        grid: { left: '10%', right: '10%', top: '5%', bottom: '10%' },
        xAxis: { name: '风险 (%)', type: 'value' },
        yAxis: { name: '收益 (%)', type: 'value' },
        series: [{ type: 'scatter', data: scatterData, symbolSize: 20, label: { show: true, position: 'top', formatter: (p) => p.name } }]
      }
      this.chartInstance.setOption(option, true)
    },
    handleResize() {
      if (this.chartInstance) this.chartInstance.resize()
    }
  }
}
</script>

<style scoped>
.analysis-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
  gap: 16px;
}

.control-panel {
  width: 280px;
  background: white;
  border-radius: 4px;
  padding: 16px;
  overflow-y: auto;
  border: 1px solid #e4e7ed;
  flex-shrink: 0;
}

.control-panel h3 {
  margin-bottom: 16px;
  color: #303133;
  font-size: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409EFF;
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
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
  flex: 1;
  min-height: 400px;
}

.metrics-card {
  margin-top: 20px;
}

.metrics-card h4 {
  margin-bottom: 12px;
  color: #303133;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
