<template>
  <div class="correlation-analysis">
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="100px" @submit.native.prevent>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="股票代码">
              <el-select
                v-model="searchForm.stockCodes"
                multiple
                filterable
                remote
                placeholder="请输入股票代码或名称"
                :remote-method="searchStock"
                :loading="stockLoading"
                style="width: 100%">
                <el-option
                  v-for="item in stockOptions"
                  :key="item.code"
                  :label="item.code + ' ' + item.name"
                  :value="item.code">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="对比标的">
              <el-select
                v-model="searchForm.compareBenchmark"
                filterable
                placeholder="请选择对比标的"
                style="width: 100%">
                <el-option
                  v-for="item in indexOptions"
                  :key="item.code"
                  :label="item.code + ' ' + item.name"
                  :value="item.code">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="日期范围">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 100%">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="计算类型">
              <el-select v-model="searchForm.dataType" style="width: 100%">
                <el-option label="价格" value="PRICE"></el-option>
                <el-option label="收益率" value="RETURN"></el-option>
                <el-option label="成交量" value="VOLUME"></el-option>
                <el-option label="波动率" value="VOLATILITY"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="系数类型">
              <el-select v-model="searchForm.coefficientType" style="width: 100%">
                <el-option label="Pearson" value="PEARSON"></el-option>
                <el-option label="Spearman" value="SPEARMAN"></el-option>
                <el-option label="双系数" value="BOTH"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="滚动周期">
              <el-input-number
                v-model="searchForm.rollingWindow"
                :min="5"
                :max="250"
                :step="5"
                style="width: 100%">
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="计算模式">
              <el-radio-group v-model="searchForm.calculationMode">
                <el-radio label="FIXED">固定周期</el-radio>
                <el-radio label="ROLLING">滚动周期</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="板块选择">
              <el-select
                v-model="searchForm.sectorCode"
                filterable
                placeholder="请选择板块"
                clearable
                style="width: 100%"
                @change="handleSectorChange">
                <el-option
                  v-for="item in sectorOptions"
                  :key="item.sectorCode"
                  :label="item.sectorName"
                  :value="item.sectorCode">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item>
              <el-button type="primary" @click="handleCalculate" :loading="calculating">
                <i class="el-icon-calculator"></i> 开始计算
              </el-button>
              <el-button @click="handleReset">
                <i class="el-icon-refresh"></i> 清空
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <div v-if="resultData" class="result-container">
      <el-card class="overview-card">
        <div slot="header">
          <span><i class="el-icon-document"></i> 系数概览</span>
        </div>
        <el-row :gutter="16">
          <el-col :span="12" v-for="(overview, index) in resultData.overviews" :key="index">
            <div class="overview-item">
              <div class="overview-header">
                <span class="stock-pair">
                  <el-tag type="primary">{{ overview.stockCodeA }}</el-tag>
                  {{ overview.stockNameA }}
                  <i class="el-icon-right" style="margin: 0 8px;"></i>
                  <el-tag type="success">{{ overview.stockCodeB }}</el-tag>
                  {{ overview.stockNameB }}
                </span>
              </div>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="统计起始日期">
                  {{ overview.statStartDate }}
                </el-descriptions-item>
                <el-descriptions-item label="统计结束日期">
                  {{ overview.statEndDate }}
                </el-descriptions-item>
                <el-descriptions-item label="数据类型">
                  {{ getDataTypeLabel(overview.dataType) }}
                </el-descriptions-item>
                <el-descriptions-item label="关联描述">
                  <el-tag :type="getCorrelationTagType(overview.correlationDescription)">
                    {{ overview.correlationDescription }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="Pearson系数">
                  <span class="coefficient-value" :class="getCoefficientClass(overview.pearsonCoefficient)">
                    {{ overview.pearsonCoefficient.toFixed(4) }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="Spearman系数">
                  <span class="coefficient-value" :class="getCoefficientClass(overview.spearmanCoefficient)">
                    {{ overview.spearmanCoefficient.toFixed(4) }}
                  </span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card class="chart-card">
        <div slot="header">
          <span><i class="el-icon-picture"></i> 相关矩阵热力图</span>
        </div>
        <div ref="heatmapChart" class="chart-container"></div>
        <div class="matrix-table-container">
          <h4>相关系数矩阵表</h4>
          <el-table :data="matrixTableData" border size="small" class="matrix-table">
            <el-table-column
              prop="stockName"
              label="标的"
              width="120"
              fixed="left">
            </el-table-column>
            <el-table-column
              v-for="(name, index) in resultData.matrixStockNames"
              :key="index"
              :label="name"
              align="center">
              <template slot-scope="scope">
                <div class="matrix-cell">
                  <div class="pearson">P: {{ scope.row['pearson_' + index] }}</div>
                  <div class="spearman">S: {{ scope.row['spearman_' + index] }}</div>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>

      <el-card v-if="resultData.rollingCorrelation && resultData.rollingCorrelation.length > 0" class="chart-card">
        <div slot="header">
          <span><i class="el-icon-data-line"></i> 滚动相关性折线图</span>
        </div>
        <div ref="rollingChart" class="chart-container"></div>
      </el-card>

      <el-card class="detail-card">
        <div slot="header" class="detail-header">
          <span><i class="el-icon-s-grid"></i> 数据明细</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索..."
              size="small"
              style="width: 200px; margin-right: 8px;"
              prefix-icon="el-icon-search">
            </el-input>
            <el-button
              type="primary"
              size="small"
              :disabled="selectedRows.length === 0"
              @click="handleExportExcel">
              <i class="el-icon-download"></i> 导出Excel
            </el-button>
          </div>
        </div>
        <el-table
          :data="filteredDetailRows"
          border
          size="small"
          @selection-change="handleSelectionChange"
          :default-sort="{ prop: 'statDate', order: 'descending' }">
          <el-table-column type="selection" width="50"></el-table-column>
          <el-table-column prop="serialNumber" label="序号" width="60" sortable></el-table-column>
          <el-table-column prop="stockCodeA" label="标的A代码" width="100" sortable></el-table-column>
          <el-table-column prop="stockNameA" label="标的A名称" width="100" sortable></el-table-column>
          <el-table-column prop="stockCodeB" label="标的B代码" width="100" sortable></el-table-column>
          <el-table-column prop="stockNameB" label="标的B名称" width="100" sortable></el-table-column>
          <el-table-column prop="statDate" label="统计日期" width="120" sortable></el-table-column>
          <el-table-column prop="rollingWindow" label="滚动窗口" width="80" sortable></el-table-column>
          <el-table-column prop="pearsonCoefficient" label="Pearson系数" width="110" sortable>
            <template slot-scope="scope">
              <span :class="getCoefficientClass(scope.row.pearsonCoefficient)">
                {{ scope.row.pearsonCoefficient.toFixed(4) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="spearmanCoefficient" label="Spearman系数" width="110" sortable>
            <template slot-scope="scope">
              <span :class="getCoefficientClass(scope.row.spearmanCoefficient)">
                {{ scope.row.spearmanCoefficient.toFixed(4) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="dataType" label="计算数据类型" width="100" sortable>
            <template slot-scope="scope">
              {{ getDataTypeLabel(scope.row.dataType) }}
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredDetailRows.length"
            :page-size="pageSize"
            :current-page.sync="currentPage"
            :page-sizes="[10, 20, 50, 100]">
          </el-pagination>
        </div>
      </el-card>
    </div>

    <el-empty v-else-if="!calculating" description="请选择参数后点击开始计算"></el-empty>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { calculateCorrelation, getSectors, getIndices, exportCorrelationExcel } from '@/api/quantApi'
import { searchStocks, getAllStocks } from '@/api/stockApi'

export default {
  name: 'CorrelationAnalysis',
  data() {
    return {
      calculating: false,
      stockLoading: false,
      stockOptions: [],
      sectorOptions: [],
      indexOptions: [],
      searchKeyword: '',
      selectedRows: [],
      currentPage: 1,
      pageSize: 20,
      searchForm: {
        stockCodes: ['600519', '000858'],
        compareBenchmark: '',
        dateRange: ['2023-01-01', '2024-12-31'],
        dataType: 'RETURN',
        coefficientType: 'BOTH',
        rollingWindow: 20,
        calculationMode: 'FIXED',
        sectorCode: ''
      },
      resultData: null,
      heatmapChart: null,
      rollingChart: null
    }
  },
  computed: {
    filteredDetailRows() {
      if (!this.resultData || !this.resultData.detailRows) return []
      let rows = this.resultData.detailRows
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        rows = rows.filter(row =>
          row.stockCodeA.toLowerCase().includes(keyword) ||
          row.stockNameA.toLowerCase().includes(keyword) ||
          row.stockCodeB.toLowerCase().includes(keyword) ||
          row.stockNameB.toLowerCase().includes(keyword)
        )
      }
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return rows.slice(start, end)
    },
    matrixTableData() {
      if (!this.resultData || !this.resultData.matrixStockCodes) return []
      const codes = this.resultData.matrixStockCodes
      const names = this.resultData.matrixStockNames
      const matrix = this.resultData.correlationMatrix

      return names.map((name, i) => {
        const row = { stockName: name }
        codes.forEach((code, j) => {
          const cell = matrix.find(m =>
            m.stockCodeX === codes[i] && m.stockCodeY === codes[j]
          )
          row['pearson_' + j] = cell ? cell.pearsonCoefficient.toFixed(4) : '-'
          row['spearman_' + j] = cell ? cell.spearmanCoefficient.toFixed(4) : '-'
        })
        return row
      })
    }
  },
  mounted() {
    this.loadInitialData()
  },
  beforeDestroy() {
    if (this.heatmapChart) {
      this.heatmapChart.dispose()
    }
    if (this.rollingChart) {
      this.rollingChart.dispose()
    }
  },
  methods: {
    async loadInitialData() {
      try {
        const [stocksRes, sectorsRes, indicesRes] = await Promise.all([
          getAllStocks(),
          getSectors(),
          getIndices()
        ])
        this.stockOptions = stocksRes.data
        this.sectorOptions = sectorsRes.data
        this.indexOptions = indicesRes.data.map(item => ({ code: item[0], name: item[1] }))
      } catch (e) {
        this.$message.error('加载基础数据失败')
      }
    },
    async searchStock(query) {
      if (!query) return
      this.stockLoading = true
      try {
        const res = await searchStocks(query)
        this.stockOptions = res.data
      } catch (e) {
        this.$message.error('搜索股票失败')
      } finally {
        this.stockLoading = false
      }
    },
    handleSectorChange(sectorCode) {
      if (sectorCode) {
        const sector = this.sectorOptions.find(s => s.sectorCode === sectorCode)
        if (sector && sector.stockCodes) {
          this.searchForm.stockCodes = sector.stockCodes.split(',').map(c => c.trim())
        }
      }
    },
    async handleCalculate() {
      if (!this.searchForm.stockCodes || this.searchForm.stockCodes.length < 2) {
        this.$message.warning('请至少选择2只股票')
        return
      }
      if (!this.searchForm.dateRange || this.searchForm.dateRange.length !== 2) {
        this.$message.warning('请选择日期范围')
        return
      }

      this.calculating = true
      try {
        const request = {
          stockCodes: this.searchForm.stockCodes,
          compareBenchmark: this.searchForm.compareBenchmark,
          startDate: this.searchForm.dateRange[0],
          endDate: this.searchForm.dateRange[1],
          dataType: this.searchForm.dataType,
          coefficientType: this.searchForm.coefficientType,
          rollingWindow: this.searchForm.rollingWindow,
          calculationMode: this.searchForm.calculationMode,
          sectorCode: this.searchForm.sectorCode
        }

        const res = await calculateCorrelation(request)
        this.resultData = res.data

        this.$nextTick(() => {
          this.renderHeatmap()
          if (this.resultData.rollingCorrelation && this.resultData.rollingCorrelation.length > 0) {
            this.renderRollingChart()
          }
        })

        this.$message.success('计算完成')
      } catch (e) {
        this.$message.error(e.response?.data?.message || '计算失败')
      } finally {
        this.calculating = false
      }
    },
    handleReset() {
      this.searchForm = {
        stockCodes: ['600519', '000858'],
        compareBenchmark: '',
        dateRange: ['2023-01-01', '2024-12-31'],
        dataType: 'RETURN',
        coefficientType: 'BOTH',
        rollingWindow: 20,
        calculationMode: 'FIXED',
        sectorCode: ''
      }
      this.resultData = null
      this.searchKeyword = ''
      this.selectedRows = []
    },
    renderHeatmap() {
      if (this.heatmapChart) {
        this.heatmapChart.dispose()
      }
      this.heatmapChart = echarts.init(this.$refs.heatmapChart)

      const codes = this.resultData.matrixStockCodes
      const names = this.resultData.matrixStockNames
      const matrix = this.resultData.correlationMatrix

      const data = []
      const tooltipData = {}
      for (let i = 0; i < codes.length; i++) {
        for (let j = 0; j < codes.length; j++) {
          const cell = matrix.find(m => m.stockCodeX === codes[i] && m.stockCodeY === codes[j])
          const value = cell ? cell.pearsonCoefficient : 0
          data.push([j, i, value])
          tooltipData[`${j},${i}`] = cell
        }
      }

      const option = {
        tooltip: {
          formatter: (params) => {
            const cell = tooltipData[`${params.data[0]},${params.data[1]}`]
            if (cell) {
              return `
                <div style="padding: 8px;">
                  <div><strong>${cell.stockCodeX} ${cell.stockNameX}</strong></div>
                  <div><strong>× ${cell.stockCodeY} ${cell.stockNameY}</strong></div>
                  <div style="margin-top: 8px;">Pearson: <strong>${cell.pearsonCoefficient.toFixed(4)}</strong></div>
                  <div>Spearman: <strong>${cell.spearmanCoefficient.toFixed(4)}</strong></div>
                </div>
              `
            }
            return ''
          }
        },
        grid: {
          left: '12%',
          right: '5%',
          top: '5%',
          bottom: '15%'
        },
        xAxis: {
          type: 'category',
          data: names,
          axisLabel: {
            rotate: 45,
            interval: 0,
            fontSize: 11
          },
          splitArea: { show: true }
        },
        yAxis: {
          type: 'category',
          data: names,
          axisLabel: { fontSize: 11 },
          splitArea: { show: true }
        },
        visualMap: {
          min: -1,
          max: 1,
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: '0%',
          inRange: {
            color: ['#313695', '#4575b4', '#74add1', '#abd9e9', '#e0f3f8', '#ffffbf', '#fee090', '#fdae61', '#f46d43', '#d73027', '#a50026']
          }
        },
        series: [{
          name: '相关性',
          type: 'heatmap',
          data: data,
          label: {
            show: true,
            formatter: (params) => params.value[2].toFixed(2),
            fontSize: 10
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }

      this.heatmapChart.setOption(option)
      window.addEventListener('resize', () => this.heatmapChart.resize())
    },
    renderRollingChart() {
      if (this.rollingChart) {
        this.rollingChart.dispose()
      }
      this.rollingChart = echarts.init(this.$refs.rollingChart)

      const data = this.resultData.rollingCorrelation
      const dates = data.map(d => d.tradeDate)
      const pearsonData = data.map(d => d.pearsonCoefficient)
      const spearmanData = data.map(d => d.spearmanCoefficient)

      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: (params) => {
            const date = params[0].axisValue
            const windowSize = data[params[0].dataIndex]?.windowSize || '-'
            let html = `<div style="padding: 8px;"><strong>${date}</strong></div>`
            html += `<div>窗口周期: ${windowSize}个交易日</div>`
            params.forEach(p => {
              html += `<div style="margin-top: 4px;"><span style="color:${p.color};">●</span> ${p.seriesName}: <strong>${p.value.toFixed(4)}</strong></div>`
            })
            return html
          }
        },
        legend: {
          data: ['Pearson系数', 'Spearman系数'],
          top: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '12%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates,
          axisLabel: {
            rotate: 45,
            fontSize: 10
          }
        },
        yAxis: {
          type: 'value',
          min: -1,
          max: 1,
          splitLine: {
            lineStyle: { type: 'dashed' }
          }
        },
        series: [
          {
            name: 'Pearson系数',
            type: 'line',
            data: pearsonData,
            smooth: true,
            lineStyle: { width: 2 },
            itemStyle: { color: '#409EFF' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
              ])
            }
          },
          {
            name: 'Spearman系数',
            type: 'line',
            data: spearmanData,
            smooth: true,
            lineStyle: { width: 2 },
            itemStyle: { color: '#67C23A' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
                { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
              ])
            }
          }
        ]
      }

      this.rollingChart.setOption(option)
      window.addEventListener('resize', () => this.rollingChart.resize())
    },
    handleSelectionChange(rows) {
      this.selectedRows = rows
    },
    async handleExportExcel() {
      try {
        const rowsToExport = this.selectedRows.length > 0
          ? this.selectedRows
          : this.resultData.detailRows

        const res = await exportCorrelationExcel(rowsToExport)
        const url = window.URL.createObjectURL(new Blob([res.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', '相关性分析明细.xlsx')
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      } catch (e) {
        this.$message.error('导出失败')
      }
    },
    getDataTypeLabel(type) {
      const labels = {
        PRICE: '价格',
        RETURN: '收益率',
        VOLUME: '成交量',
        VOLATILITY: '波动率'
      }
      return labels[type] || type
    },
    getCorrelationTagType(desc) {
      if (desc.includes('强正')) return 'danger'
      if (desc.includes('弱正')) return 'warning'
      if (desc.includes('强负')) return 'success'
      if (desc.includes('弱负')) return 'info'
      return 'info'
    },
    getCoefficientClass(value) {
      if (value > 0.7) return 'strong-positive'
      if (value > 0.3) return 'weak-positive'
      if (value < -0.7) return 'strong-negative'
      if (value < -0.3) return 'weak-negative'
      return 'no-correlation'
    }
  }
}
</script>

<style scoped>
.correlation-analysis {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
}

.search-card ::v-deep .el-form-item {
  margin-bottom: 12px;
}

.result-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.overview-card, .chart-card, .detail-card {
  margin-bottom: 0;
}

.overview-item {
  margin-bottom: 16px;
}

.overview-header {
  margin-bottom: 12px;
}

.stock-pair {
  font-size: 14px;
  font-weight: 600;
}

.coefficient-value {
  font-family: 'Consolas', monospace;
  font-size: 16px;
  font-weight: 600;
}

.strong-positive {
  color: #F56C6C;
}

.weak-positive {
  color: #E6A23C;
}

.strong-negative {
  color: #67C23A;
}

.weak-negative {
  color: #909399;
}

.no-correlation {
  color: #606266;
}

.chart-container {
  height: 400px;
  width: 100%;
}

.matrix-table-container {
  margin-top: 20px;
}

.matrix-table-container h4 {
  margin: 0 0 12px 0;
  color: #606266;
}

.matrix-cell {
  padding: 4px;
}

.matrix-cell .pearson {
  color: #409EFF;
  font-size: 12px;
}

.matrix-cell .spearman {
  color: #67C23A;
  font-size: 12px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.matrix-table ::v-deep .el-table__cell {
  padding: 8px 4px;
}
</style>
