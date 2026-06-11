<template>
  <div class="linkage-analysis">
    <el-card class="search-card">
      <el-form :model="searchForm" label-width="100px" @submit.native.prevent>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="板块选择">
              <el-select
                v-model="searchForm.sectorCodes"
                multiple
                filterable
                placeholder="请选择板块（可多选）"
                style="width: 100%">
                <el-option
                  v-for="item in sectorOptions"
                  :key="item.sectorCode"
                  :label="item.sectorName + '(' + item.sectorCode + ')'"
                  :value="item.sectorCode">
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="对比指数">
              <el-select
                v-model="searchForm.compareBenchmark"
                filterable
                placeholder="请选择对比指数"
                clearable
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
            <el-form-item label="指标类型">
              <el-select v-model="searchForm.dataType" style="width: 100%">
                <el-option label="成交量" value="VOLUME"></el-option>
                <el-option label="波动率" value="VOLATILITY"></el-option>
                <el-option label="成交量+波动率" value="BOTH"></el-option>
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
          <el-col :span="6">
            <el-form-item>
              <el-button type="primary" @click="handleCalculate" :loading="calculating">
                <i class="el-icon-calculator"></i> 开始计算
              </el-button>
              <el-button @click="handleReset">
                <i class="el-icon-refresh"></i> 清空
              </el-button>
              <el-button type="success" @click="handleSaveTemplate" :disabled="!resultData">
                <i class="el-icon-document-copy"></i> 保存模板
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
                <span class="indicator-name">
                  <i class="el-icon-data-line"></i> {{ overview.indicatorName }}
                </span>
              </div>
              <el-descriptions :column="2" border size="small">
                <el-descriptions-item label="板块A">
                  <el-tag type="primary">{{ overview.sectorNameA }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="板块B">
                  <el-tag type="success">{{ overview.sectorNameB }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="联动系数" :span="2">
                  <span class="coefficient-value" :class="getCoefficientClass(overview.linkageCoefficient)">
                    {{ overview.linkageCoefficient.toFixed(4) }}
                  </span>
                </el-descriptions-item>
                <el-descriptions-item label="联动描述" :span="2">
                  <el-tag :type="getCorrelationTagType(overview.linkageDescription)">
                    {{ overview.linkageDescription }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-card class="rank-card">
        <div slot="header" class="rank-header">
          <span><i class="el-icon-trophy"></i> 板块联动排名</span>
          <div class="header-actions">
            <el-input
              v-model="rankSearchKeyword"
              placeholder="搜索板块..."
              size="small"
              style="width: 200px; margin-right: 8px;"
              prefix-icon="el-icon-search">
            </el-input>
            <el-button type="primary" size="small" @click="handleExportExcel">
              <i class="el-icon-download"></i> 导出Excel
            </el-button>
          </div>
        </div>
        <el-table
          :data="filteredRankings"
          border
          size="small"
          :default-sort="{ prop: 'avgLinkageCoefficient', order: 'descending' }">
          <el-table-column prop="rank" label="排名" width="70" align="center">
            <template slot-scope="scope">
              <el-tag v-if="scope.row.rank <= 3" :type="getRankTagType(scope.row.rank)" size="medium">
                {{ scope.row.rank }}
              </el-tag>
              <span v-else>{{ scope.row.rank }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="sectorCode" label="板块代码" width="100" sortable></el-table-column>
          <el-table-column prop="sectorName" label="板块名称" width="120" sortable></el-table-column>
          <el-table-column prop="avgLinkageCoefficient" label="平均联动系数" width="120" sortable>
            <template slot-scope="scope">
              <span :class="getCoefficientClass(scope.row.avgLinkageCoefficient)">
                {{ scope.row.avgLinkageCoefficient.toFixed(4) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="volumeLinkage" label="成交量联动" width="110" sortable>
            <template slot-scope="scope">
              <span :class="getCoefficientClass(scope.row.volumeLinkage)">
                {{ scope.row.volumeLinkage.toFixed(4) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="volatilityLinkage" label="波动率联动" width="110" sortable>
            <template slot-scope="scope">
              <span :class="getCoefficientClass(scope.row.volatilityLinkage)">
                {{ scope.row.volatilityLinkage.toFixed(4) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="stockCount" label="成分股数量" width="100" sortable></el-table-column>
        </el-table>
      </el-card>

      <el-card class="chart-card">
        <div slot="header">
          <span><i class="el-icon-picture"></i> 板块间联动热力图</span>
          <el-button-group style="float: right;">
            <el-button size="small" icon="el-icon-zoom-in" @click="zoomChart('heatmap', 0.1)">放大</el-button>
            <el-button size="small" icon="el-icon-zoom-out" @click="zoomChart('heatmap', -0.1)">缩小</el-button>
            <el-button size="small" icon="el-icon-refresh" @click="resetZoom('heatmap')">重置</el-button>
          </el-button-group>
        </div>
        <div ref="heatmapChart" class="chart-container"></div>
      </el-card>

      <el-card v-if="resultData.rollingLinkage && resultData.rollingLinkage.length > 0" class="chart-card">
        <div slot="header">
          <span><i class="el-icon-data-line"></i> 滚动联动折线图</span>
          <el-button-group style="float: right;">
            <el-button size="small" icon="el-icon-zoom-in" @click="zoomChart('rolling', 0.1)">放大</el-button>
            <el-button size="small" icon="el-icon-zoom-out" @click="zoomChart('rolling', -0.1)">缩小</el-button>
            <el-button size="small" icon="el-icon-refresh" @click="resetZoom('rolling')">重置</el-button>
          </el-button-group>
        </div>
        <div ref="rollingChart" class="chart-container"></div>
      </el-card>
    </div>

    <el-empty v-else-if="!calculating" description="请选择参数后点击开始计算"></el-empty>

    <el-dialog
      title="保存查询模板"
      :visible.sync="templateDialogVisible"
      width="400px">
      <el-form :model="templateForm" label-width="100px">
        <el-form-item label="模板名称">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称"></el-input>
        </el-form-item>
        <el-form-item label="模板描述">
          <el-input type="textarea" v-model="templateForm.description" placeholder="请输入模板描述"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSaveTemplate">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { calculateLinkage, getSectors, getIndices, exportLinkageExcel } from '@/api/quantApi'

export default {
  name: 'LinkageAnalysis',
  data() {
    return {
      calculating: false,
      sectorOptions: [],
      indexOptions: [],
      rankSearchKeyword: '',
      searchForm: {
        sectorCodes: [],
        compareBenchmark: '',
        dateRange: ['2023-01-01', '2024-12-31'],
        dataType: 'BOTH',
        coefficientType: 'PEARSON',
        rollingWindow: 20,
        calculationMode: 'FIXED',
        sectorCode: ''
      },
      resultData: null,
      heatmapChart: null,
      rollingChart: null,
      heatmapZoom: 1,
      rollingZoom: 1,
      templateDialogVisible: false,
      templateForm: {
        name: '',
        description: ''
      },
      savedTemplates: []
    }
  },
  computed: {
    filteredRankings() {
      if (!this.resultData || !this.resultData.sectorRankings) return []
      let rows = this.resultData.sectorRankings
      if (this.rankSearchKeyword) {
        const keyword = this.rankSearchKeyword.toLowerCase()
        rows = rows.filter(row =>
          row.sectorCode.toLowerCase().includes(keyword) ||
          row.sectorName.toLowerCase().includes(keyword)
        )
      }
      return rows
    }
  },
  mounted() {
    this.loadInitialData()
    this.loadTemplates()
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
        const [sectorsRes, indicesRes] = await Promise.all([
          getSectors(),
          getIndices()
        ])
        this.sectorOptions = sectorsRes
        this.indexOptions = indicesRes.map(item => ({ code: item[0], name: item[1] }))
        this.searchForm.sectorCodes = this.sectorOptions.filter(s => s.sectorType === 'INDUSTRY').map(s => s.sectorCode)
      } catch (e) {
        this.$message.error('加载基础数据失败')
      }
    },
    loadTemplates() {
      const saved = localStorage.getItem('linkageTemplates')
      if (saved) {
        this.savedTemplates = JSON.parse(saved)
      }
    },
    async handleCalculate() {
      if (!this.searchForm.sectorCodes || this.searchForm.sectorCodes.length < 2) {
        this.$message.warning('请至少选择2个板块')
        return
      }
      if (!this.searchForm.dateRange || this.searchForm.dateRange.length !== 2) {
        this.$message.warning('请选择日期范围')
        return
      }

      this.calculating = true
      try {
        const request = {
          stockCodes: [],
          compareBenchmark: this.searchForm.compareBenchmark,
          startDate: this.searchForm.dateRange[0],
          endDate: this.searchForm.dateRange[1],
          dataType: this.searchForm.dataType,
          coefficientType: this.searchForm.coefficientType,
          rollingWindow: this.searchForm.rollingWindow,
          calculationMode: this.searchForm.calculationMode,
          sectorCode: this.searchForm.sectorCodes[0]
        }

        const res = await calculateLinkage(request)
        this.resultData = res

        if (this.resultData.sectorRankings.length === 0 &&
            this.resultData.sectorLinkageMatrix.length === 0) {
          this.$message.warning('未查询到符合条件的数据，请调整查询条件')
        } else {
          this.$nextTick(() => {
            this.renderHeatmap()
            if (this.resultData.rollingLinkage && this.resultData.rollingLinkage.length > 0) {
              this.renderRollingChart()
            }
          })
          this.$message.success('计算完成')
        }
      } catch (e) {
        const errorMsg = e.message || '计算失败'
        this.$message.error(errorMsg)
        console.error('联动性分析计算错误:', e)
      } finally {
        this.calculating = false
      }
    },
    handleReset() {
      this.searchForm = {
        sectorCodes: this.sectorOptions.filter(s => s.sectorType === 'INDUSTRY').map(s => s.sectorCode),
        compareBenchmark: '',
        dateRange: ['2023-01-01', '2024-12-31'],
        dataType: 'BOTH',
        coefficientType: 'PEARSON',
        rollingWindow: 20,
        calculationMode: 'FIXED',
        sectorCode: ''
      }
      this.resultData = null
      this.rankSearchKeyword = ''
    },
    renderHeatmap() {
      if (this.heatmapChart) {
        this.heatmapChart.dispose()
      }
      this.heatmapChart = echarts.init(this.$refs.heatmapChart)

      const codes = this.resultData.matrixSectorCodes || []
      const names = this.resultData.matrixSectorNames || []
      const matrix = this.resultData.sectorLinkageMatrix || []

      if (codes.length === 0) {
        return
      }

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
                  <div><strong>${cell.stockNameX}</strong></div>
                  <div><strong>× ${cell.stockNameY}</strong></div>
                  <div style="margin-top: 8px;">成交量联动: <strong>${cell.pearsonCoefficient.toFixed(4)}</strong></div>
                  <div>波动率联动: <strong>${cell.spearmanCoefficient.toFixed(4)}</strong></div>
                </div>
              `
            }
            return ''
          }
        },
        grid: {
          left: '15%',
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
          name: '联动系数',
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

      const data = this.resultData.rollingLinkage
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
          data: ['成交量联动', '波动率联动'],
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
        dataZoom: [
          {
            type: 'inside',
            start: 0,
            end: 100
          },
          {
            start: 0,
            end: 100
          }
        ],
        series: [
          {
            name: '成交量联动',
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
            name: '波动率联动',
            type: 'line',
            data: spearmanData,
            smooth: true,
            lineStyle: { width: 2 },
            itemStyle: { color: '#E6A23C' },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(230, 162, 60, 0.3)' },
                { offset: 1, color: 'rgba(230, 162, 60, 0.05)' }
              ])
            }
          }
        ]
      }

      this.rollingChart.setOption(option)
      window.addEventListener('resize', () => this.rollingChart.resize())
    },
    zoomChart(type, factor) {
      if (type === 'heatmap' && this.heatmapChart) {
        this.heatmapZoom = Math.max(0.5, Math.min(2, this.heatmapZoom + factor))
        this.heatmapChart.setOption({
          series: [{
            label: {
              fontSize: 10 * this.heatmapZoom
            }
          }]
        })
      } else if (type === 'rolling' && this.rollingChart) {
        this.rollingZoom = Math.max(0.5, Math.min(2, this.rollingZoom + factor))
        const option = this.rollingChart.getOption()
        const dataZoom = option.dataZoom || []
        if (dataZoom.length > 0) {
          const range = dataZoom[0].end - dataZoom[0].start
          const newRange = range / (1 + factor)
          const center = (dataZoom[0].start + dataZoom[0].end) / 2
          const newStart = Math.max(0, center - newRange / 2)
          const newEnd = Math.min(100, center + newRange / 2)
          this.rollingChart.setOption({
            dataZoom: [
              { start: newStart, end: newEnd },
              { start: newStart, end: newEnd }
            ]
          })
        }
      }
    },
    resetZoom(type) {
      if (type === 'heatmap' && this.heatmapChart) {
        this.heatmapZoom = 1
        this.renderHeatmap()
      } else if (type === 'rolling' && this.rollingChart) {
        this.rollingZoom = 1
        this.rollingChart.setOption({
          dataZoom: [
            { start: 0, end: 100 },
            { start: 0, end: 100 }
          ]
        })
      }
    },
    async handleExportExcel() {
      try {
        const rowsToExport = this.filteredRankings
        const res = await exportLinkageExcel(rowsToExport)
        const url = window.URL.createObjectURL(new Blob([res]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', '板块联动排名.xlsx')
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      } catch (e) {
        this.$message.error('导出失败')
      }
    },
    handleSaveTemplate() {
      this.templateForm = {
        name: '',
        description: ''
      }
      this.templateDialogVisible = true
    },
    confirmSaveTemplate() {
      if (!this.templateForm.name) {
        this.$message.warning('请输入模板名称')
        return
      }

      const template = {
        id: Date.now(),
        name: this.templateForm.name,
        description: this.templateForm.description,
        params: JSON.parse(JSON.stringify(this.searchForm)),
        createTime: new Date().toISOString()
      }

      this.savedTemplates.push(template)
      localStorage.setItem('linkageTemplates', JSON.stringify(this.savedTemplates))
      this.templateDialogVisible = false
      this.$message.success('模板保存成功')
    },
    getRankTagType(rank) {
      if (rank === 1) return 'danger'
      if (rank === 2) return 'warning'
      if (rank === 3) return 'info'
      return ''
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
.linkage-analysis {
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

.overview-card, .rank-card, .chart-card {
  margin-bottom: 0;
}

.overview-item {
  margin-bottom: 16px;
}

.overview-header {
  margin-bottom: 12px;
}

.indicator-name {
  font-size: 14px;
  font-weight: 600;
  color: #409EFF;
}

.coefficient-value {
  font-family: 'Consolas', monospace;
  font-size: 20px;
  font-weight: 700;
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

.rank-header, .detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}
</style>
