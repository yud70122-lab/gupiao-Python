<template>
  <div class="correlation-analysis">
    <div class="top-toolbar">
      <div class="toolbar-left">
        <span class="page-title">相关性分析</span>
        <el-tag v-if="paramsChanged" type="warning" size="small" effect="dark">
          <i class="el-icon-warning"></i> 参数已变更，请重新计算
        </el-tag>
      </div>
      <div class="toolbar-right">
        <el-button size="small" @click="handleRefresh">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
        <el-dropdown @command="handleExport" trigger="click">
          <el-button size="small" type="success">
            <i class="el-icon-download"></i> 导出结果
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="excel">导出 Excel</el-dropdown-item>
            <el-dropdown-item command="csv">导出 CSV</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <el-button size="small" type="warning" @click="showSaveSchemeDialog">
          <i class="el-icon-folder-add"></i> 保存计算方案
        </el-button>
        <el-button size="small" @click="showHelpDialog">
          <i class="el-icon-question"></i> 帮助说明
        </el-button>
      </div>
    </div>

    <el-card class="config-card">
      <div slot="header" class="config-header">
        <span><i class="el-icon-setting"></i> 参数配置</span>
      </div>
      <el-form :model="searchForm" label-width="100px" @submit.native.prevent>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="标的选择" required>
              <el-select
                v-model="searchForm.stockCodes"
                multiple
                filterable
                remote
                placeholder="输入代码/名称搜索"
                :remote-method="searchStock"
                :loading="stockLoading"
                style="width: 100%"
                @change="markParamsChanged">
                <el-option
                  v-for="item in stockOptions"
                  :key="item.code"
                  :label="item.code + ' ' + item.name"
                  :value="item.code">
                </el-option>
              </el-select>
              <div class="batch-paste">
                <el-button type="text" size="mini" @click="showBatchPasteDialog">
                  <i class="el-icon-document-add"></i> 批量粘贴代码
                </el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时间区间" required>
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="yyyy-MM-dd"
                style="width: 100%"
                @change="markParamsChanged">
              </el-date-picker>
              <div class="quick-dates">
                <el-button type="text" size="mini" @click="setQuickDate(1)">近1月</el-button>
                <el-button type="text" size="mini" @click="setQuickDate(3)">近3月</el-button>
                <el-button type="text" size="mini" @click="setQuickDate(6)">近6月</el-button>
                <el-button type="text" size="mini" @click="setQuickDate(12)">近1年</el-button>
                <el-button type="text" size="mini" @click="setQuickDate('all')">全部</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="复权选择">
              <el-select v-model="searchForm.adjustmentType" style="width: 100%" @change="markParamsChanged">
                <el-option label="不复权" value="NONE"></el-option>
                <el-option label="前复权" value="FORWARD"></el-option>
                <el-option label="后复权" value="BACKWARD"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="计算类型" required>
              <el-radio-group v-model="searchForm.dataType" @change="markParamsChanged">
                <el-radio label="PRICE">价格相关性</el-radio>
                <el-radio label="RETURN">收益率相关性</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="系数算法" required>
              <el-radio-group v-model="searchForm.coefficientType" @change="markParamsChanged">
                <el-radio label="PEARSON">Pearson 皮尔逊</el-radio>
                <el-radio label="SPEARMAN">Spearman 斯皮尔曼</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="计算模式" required>
              <el-radio-group v-model="searchForm.calculationMode" @change="markParamsChanged">
                <el-radio label="STATIC">静态整体相关性</el-radio>
                <el-radio label="ROLLING">滚动周期相关性</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="6" v-if="searchForm.calculationMode === 'ROLLING'">
            <el-form-item label="滚动窗口">
              <el-select
                v-model="searchForm.rollingWindow"
                placeholder="选择周期"
                style="width: 100%"
                @change="markParamsChanged"
                filterable
                allow-create>
                <el-option :value="5" label="5日"></el-option>
                <el-option :value="10" label="10日"></el-option>
                <el-option :value="20" label="20日"></el-option>
                <el-option :value="60" label="60日"></el-option>
                <el-option :value="120" label="120日"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="3" v-if="searchForm.calculationMode === 'ROLLING'">
            <el-form-item label="滑动步长">
              <el-input-number
                v-model="searchForm.rollingStep"
                :min="1"
                :max="100"
                size="small"
                @change="markParamsChanged">
              </el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="辅助开关">
              <div class="switch-group">
                <el-switch
                  v-model="searchForm.outlierFilter"
                  active-text="异常值过滤"
                  @change="markParamsChanged">
                </el-switch>
                <el-switch
                  v-model="searchForm.resultCache"
                  active-text="结果缓存"
                  style="margin-left: 12px;"
                  @change="markParamsChanged">
                </el-switch>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item>
              <el-button type="primary" @click="handleCalculate" :loading="calculating" :disabled="calculating">
                <i class="el-icon-caret-right"></i> 开始计算
              </el-button>
              <el-button type="danger" @click="handleStopCalculate" :disabled="!calculating">
                <i class="el-icon-close"></i> 停止计算
              </el-button>
              <el-dropdown @command="handleLoadScheme" trigger="click" style="margin-left: 12px;">
                <el-button size="small">
                  <i class="el-icon-folder-opened"></i> 调用计算方案
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item
                    v-for="scheme in savedSchemes"
                    :key="scheme.id"
                    :command="scheme">
                    {{ scheme.schemeName }}
                  </el-dropdown-item>
                  <el-dropdown-item v-if="savedSchemes.length === 0" disabled>
                    暂无保存的方案
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-progress
        v-if="calculating && asyncTaskId"
        :percentage="asyncProgress"
        :status="asyncProgress === 100 ? 'success' : ''"
        :stroke-width="18"
        :text-inside="true"
        style="margin-top: 10px;">
      </el-progress>
    </el-card>

    <div v-if="resultData" class="result-container">
      <el-card class="table-card">
        <div slot="header" class="result-header">
          <span><i class="el-icon-s-grid"></i> 数据表格</span>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索股票代码/名称..."
              size="small"
              style="width: 200px; margin-right: 8px;"
              prefix-icon="el-icon-search">
            </el-input>
          </div>
        </div>
        <el-table
          :data="filteredDetailRows"
          border
          size="small"
          :default-sort="{ prop: 'pearsonCoefficient', order: 'descending' }"
          style="width: 100%">
          <el-table-column prop="stockCodeA" label="股票代码A" width="110" sortable></el-table-column>
          <el-table-column prop="stockNameA" label="股票名称A" width="110" sortable></el-table-column>
          <el-table-column prop="stockCodeB" label="股票代码B" width="110" sortable></el-table-column>
          <el-table-column prop="stockNameB" label="股票名称B" width="110" sortable></el-table-column>
          <el-table-column prop="pearsonCoefficient" label="Pearson系数" width="120" sortable>
            <template slot-scope="scope">
              <span :class="getCoefficientClass(scope.row.pearsonCoefficient)">
                {{ scope.row.pearsonCoefficient != null ? scope.row.pearsonCoefficient.toFixed(4) : '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="spearmanCoefficient" label="Spearman系数" width="130" sortable>
            <template slot-scope="scope">
              <span :class="getCoefficientClass(scope.row.spearmanCoefficient)">
                {{ scope.row.spearmanCoefficient != null ? scope.row.spearmanCoefficient.toFixed(4) : '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="correlationLevel" label="相关性等级" width="110" sortable
            :filters="[
              { text: '强正相关', value: '强正相关' },
              { text: '弱正相关', value: '弱正相关' },
              { text: '无相关', value: '无相关' },
              { text: '弱负相关', value: '弱负相关' },
              { text: '强负相关', value: '强负相关' }
            ]"
            :filter-method="filterCorrelationLevel">
            <template slot-scope="scope">
              <el-tag :type="getLevelTagType(scope.row.correlationLevel)" size="small">
                {{ scope.row.correlationLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sampleCount" label="有效样本数" width="100" sortable></el-table-column>
          <el-table-column prop="calculationTime" label="计算时间" width="160" sortable>
            <template slot-scope="scope">
              {{ formatDateTime(scope.row.calculationTime) }}
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-container">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalDetailRows"
            :page-size="pageSize"
            :current-page.sync="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleSizeChange">
          </el-pagination>
        </div>
      </el-card>

      <el-card v-if="resultData.correlationMatrix && resultData.correlationMatrix.length > 0 && searchForm.stockCodes.length > 2" class="chart-card">
        <div slot="header" class="result-header">
          <span><i class="el-icon-picture"></i> 相关性热力图</span>
          <el-button type="text" @click="toggleFullscreen('heatmapChart')">
            <i class="el-icon-full-screen"></i> 全屏
          </el-button>
        </div>
        <div ref="heatmapChart" class="chart-container"></div>
      </el-card>

      <el-card v-if="resultData.scatterData && resultData.scatterData.length > 0 && searchForm.stockCodes.length === 2" class="chart-card">
        <div slot="header" class="result-header">
          <span><i class="el-icon-data-analysis"></i> 散点图（线性关系）</span>
          <el-button type="text" @click="toggleFullscreen('scatterChart')">
            <i class="el-icon-full-screen"></i> 全屏
          </el-button>
        </div>
        <div ref="scatterChart" class="chart-container"></div>
      </el-card>

      <el-card v-if="resultData.rollingCorrelation && resultData.rollingCorrelation.length > 0 && searchForm.calculationMode === 'ROLLING'" class="chart-card">
        <div slot="header" class="result-header">
          <span><i class="el-icon-data-line"></i> 趋势折线图（相关系数变化走势）</span>
          <el-button type="text" @click="toggleFullscreen('rollingChart')">
            <i class="el-icon-full-screen"></i> 全屏
          </el-button>
        </div>
        <div ref="rollingChart" class="chart-container"></div>
      </el-card>
    </div>

    <el-empty v-else-if="!calculating && !resultData && calcHistory.length === 0" description="请配置参数后点击开始计算"></el-empty>

    <el-card v-if="calcHistory.length > 0" class="history-card">
      <div slot="header">
        <span><i class="el-icon-time"></i> 历史计算记录</span>
        <el-button type="text" style="float: right;" @click="clearHistory">清空记录</el-button>
      </div>
      <el-table :data="calcHistory" border size="mini" style="width: 100%">
        <el-table-column prop="time" label="计算时间" width="160"></el-table-column>
        <el-table-column prop="stocks" label="股票" min-width="200"></el-table-column>
        <el-table-column prop="dataType" label="计算类型" width="120">
          <template slot-scope="scope">
            {{ getDataTypeLabel(scope.row.dataType) }}
          </template>
        </el-table-column>
        <el-table-column prop="mode" label="计算模式" width="120">
          <template slot-scope="scope">
            {{ scope.row.mode === 'STATIC' ? '静态' : '滚动' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'" size="mini">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template slot-scope="scope">
            <el-button type="text" size="mini" @click="loadHistoryRecord(scope.row)">
              查看结果
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="批量粘贴股票代码" :visible.sync="batchPasteVisible" width="500px">
      <p style="color: #909399; font-size: 12px; margin-bottom: 10px;">
        每行一个股票代码，或用逗号/空格分隔
      </p>
      <el-input
        type="textarea"
        v-model="batchPasteContent"
        :rows="8"
        placeholder="600519&#10;000858&#10;601318">
      </el-input>
      <span slot="footer">
        <el-button @click="batchPasteVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatchPaste">确认添加</el-button>
      </span>
    </el-dialog>

    <el-dialog title="保存计算方案" :visible.sync="saveSchemeVisible" width="450px">
      <el-form label-width="100px">
        <el-form-item label="方案名称">
          <el-input v-model="schemeName" placeholder="请输入方案名称"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="saveSchemeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveScheme">保存</el-button>
      </span>
    </el-dialog>

    <el-dialog title="帮助说明" :visible.sync="helpVisible" width="650px">
      <div class="help-content">
        <h4>Pearson 皮尔逊相关系数</h4>
        <p>衡量两个变量之间的线性相关程度，取值范围 [-1, 1]。|r| > 0.7 为强相关，0.3 ~ 0.7 为弱相关，< 0.3 为无相关。适用于连续型正态分布数据。</p>
        <h4>Spearman 斯皮尔曼相关系数</h4>
        <p>基于秩次的非参数相关系数，衡量两个变量的单调关系。不要求正态分布，对异常值更鲁棒。取值范围同为 [-1, 1]。</p>
        <h4>滚动周期</h4>
        <p>在时间序列上以固定窗口大小滑动计算相关系数，得到相关系数随时间变化的走势。可发现相关性结构在不同时期的变动规律。</p>
        <h4>滑动步长</h4>
        <p>滚动窗口每次移动的交易日数。步长越小，结果越精细但计算量越大；步长越大，计算越快但粒度越粗。</p>
        <h4>联动指标</h4>
        <p>联动性分析衡量不同板块之间价格/成交量/波动率的同步运动程度。联动系数越高，说明两个板块走势越趋同。</p>
        <h4>相关性等级</h4>
        <ul>
          <li><strong>强正相关</strong>：系数 > 0.7，两标的高度同向运动</li>
          <li><strong>弱正相关</strong>：0.3 ~ 0.7，两标的倾向同向运动</li>
          <li><strong>无相关</strong>：-0.3 ~ 0.3，两标的运动无显著关系</li>
          <li><strong>弱负相关</strong>：-0.7 ~ -0.3，两标的倾向反向运动</li>
          <li><strong>强负相关</strong>：< -0.7，两标的高度反向运动</li>
        </ul>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="fullscreenVisible" :title="fullscreenTitle" fullscreen>
      <div ref="fullscreenChart" style="width: 100%; height: 80vh;"></div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import {
  calculateCorrelationSync,
  submitAsyncCorrelation,
  queryAsyncCorrelation,
  stopAsyncCorrelation,
  saveCorrelationScheme,
  listCorrelationSchemes,
  exportCorrelationExcel,
  exportCorrelationCSV
} from '@/api/quantApi'
import { searchStocks, getAllStocks } from '@/api/stockApi'

export default {
  name: 'CorrelationAnalysis',
  data() {
    return {
      calculating: false,
      stockLoading: false,
      stockOptions: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 20,
      paramsChanged: false,
      asyncTaskId: null,
      asyncProgress: 0,
      pollTimer: null,
      batchPasteVisible: false,
      batchPasteContent: '',
      saveSchemeVisible: false,
      schemeName: '',
      helpVisible: false,
      fullscreenVisible: false,
      fullscreenTitle: '',
      fullscreenChartType: '',
      savedSchemes: [],
      calcHistory: [],
      searchForm: {
        stockCodes: [],
        dateRange: [],
        adjustmentType: 'NONE',
        dataType: 'RETURN',
        coefficientType: 'PEARSON',
        calculationMode: 'STATIC',
        rollingWindow: 20,
        rollingStep: 1,
        outlierFilter: true,
        resultCache: true
      },
      resultData: null,
      charts: {
        heatmapChart: null,
        scatterChart: null,
        rollingChart: null
      }
    }
  },
  computed: {
    totalDetailRows() {
      if (!this.resultData || !this.resultData.detailRows) return 0
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
      return rows.length
    },
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
    }
  },
  mounted() {
    this.loadInitialData()
    this.loadSchemes()
  },
  beforeDestroy() {
    this.disposeCharts()
    if (this.pollTimer) {
      clearInterval(this.pollTimer)
    }
  },
  methods: {
    async loadInitialData() {
      try {
        const res = await getAllStocks()
        this.stockOptions = res
      } catch (e) {
        this.$message.error('加载基础数据失败')
      }
    },
    async loadSchemes() {
      try {
        const res = await listCorrelationSchemes()
        this.savedSchemes = res || []
      } catch (e) {
        this.savedSchemes = []
      }
    },
    async searchStock(query) {
      if (!query) return
      this.stockLoading = true
      try {
        const res = await searchStocks(query)
        this.stockOptions = res
      } catch (e) {
        this.$message.error('搜索股票失败')
      } finally {
        this.stockLoading = false
      }
    },
    markParamsChanged() {
      if (this.resultData) {
        this.paramsChanged = true
      }
    },
    setQuickDate(months) {
      const end = new Date()
      let start = new Date()
      if (months === 'all') {
        start = new Date('2020-01-01')
      } else {
        start.setMonth(start.getMonth() - months)
      }
      this.searchForm.dateRange = [
        start.toISOString().split('T')[0],
        end.toISOString().split('T')[0]
      ]
      this.markParamsChanged()
    },
    showBatchPasteDialog() {
      this.batchPasteVisible = true
      this.batchPasteContent = ''
    },
    handleBatchPaste() {
      const codes = this.batchPasteContent
        .split(/[\n,\s]+/)
        .map(c => c.trim())
        .filter(c => c.length > 0)
      const existing = new Set(this.searchForm.stockCodes)
      codes.forEach(code => {
        if (!existing.has(code)) {
          this.searchForm.stockCodes.push(code)
          existing.add(code)
        }
      })
      this.batchPasteVisible = false
      this.markParamsChanged()
      this.$message.success(`已添加 ${codes.length} 个股票代码`)
    },
    validateForm() {
      if (!this.searchForm.stockCodes || this.searchForm.stockCodes.length < 2) {
        this.$message.warning('请至少选择2只股票')
        return false
      }
      if (!this.searchForm.dataType) {
        this.$message.warning('请选择计算类型')
        return false
      }
      if (!this.searchForm.dateRange || this.searchForm.dateRange.length !== 2) {
        this.$message.warning('请选择有效的时间区间')
        return false
      }
      const start = new Date(this.searchForm.dateRange[0])
      const end = new Date(this.searchForm.dateRange[1])
      if (start >= end) {
        this.$message.warning('开始日期必须早于结束日期')
        return false
      }
      if (this.searchForm.calculationMode === 'ROLLING') {
        if (!this.searchForm.rollingWindow || this.searchForm.rollingWindow <= 0) {
          this.$message.warning('滚动窗口周期必须大于0')
          return false
        }
        if (!this.searchForm.rollingStep || this.searchForm.rollingStep <= 0) {
          this.$message.warning('滑动步长必须大于0')
          return false
        }
      }
      return true
    },
    async handleCalculate() {
      if (!this.validateForm()) return

      this.calculating = true
      this.paramsChanged = false
      this.asyncProgress = 0
      this.asyncTaskId = null

      const request = {
        stockCodes: this.searchForm.stockCodes,
        startDate: this.searchForm.dateRange[0],
        endDate: this.searchForm.dateRange[1],
        adjustmentType: this.searchForm.adjustmentType,
        dataType: this.searchForm.dataType,
        coefficientType: this.searchForm.coefficientType,
        calculationMode: this.searchForm.calculationMode,
        rollingWindow: this.searchForm.calculationMode === 'ROLLING' ? this.searchForm.rollingWindow : null,
        rollingStep: this.searchForm.calculationMode === 'ROLLING' ? this.searchForm.rollingStep : 1,
        outlierFilter: this.searchForm.outlierFilter,
        resultCache: this.searchForm.resultCache
      }

      const isSync = this.searchForm.stockCodes.length <= 20 && this.searchForm.calculationMode === 'STATIC'

      try {
        if (isSync) {
          const res = await calculateCorrelationSync(request)
          this.resultData = res
          this.addHistory('成功')
          this.$message.success('计算完成')
        } else {
          const submitRes = await submitAsyncCorrelation(request)
          this.asyncTaskId = submitRes.taskId
          this.startPolling()
        }
      } catch (e) {
        this.calculating = false
        this.addHistory('失败')
        this.$message.error(e.message || '计算失败')
      }

      if (isSync) {
        this.calculating = false
        this.$nextTick(() => {
          this.renderAllCharts()
        })
      }
    },
    startPolling() {
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
      }
      this.pollTimer = setInterval(async () => {
        try {
          const data = await queryAsyncCorrelation(this.asyncTaskId)

          this.asyncProgress = data.progress || 0

          if (data.status === 'COMPLETED') {
            clearInterval(this.pollTimer)
            this.pollTimer = null
            this.calculating = false
            this.resultData = data.result
            this.addHistory('成功')
            this.$message.success('异步计算完成')
            this.$nextTick(() => {
              this.renderAllCharts()
            })
          } else if (data.status === 'FAILED') {
            clearInterval(this.pollTimer)
            this.pollTimer = null
            this.calculating = false
            this.addHistory('失败')
            this.$message.error(data.message || '异步计算失败')
          } else if (data.status === 'STOPPED') {
            clearInterval(this.pollTimer)
            this.pollTimer = null
            this.calculating = false
            this.asyncProgress = 0
          }
        } catch (e) {
          clearInterval(this.pollTimer)
          this.pollTimer = null
          this.calculating = false
          this.$message.error('查询任务状态失败')
        }
      }, 1000)
    },
    async handleStopCalculate() {
      if (this.asyncTaskId) {
        try {
          await stopAsyncCorrelation(this.asyncTaskId)
          this.$message.info('已停止计算任务')
        } catch (e) {
          this.$message.error('停止任务失败')
        }
      }
      if (this.pollTimer) {
        clearInterval(this.pollTimer)
        this.pollTimer = null
      }
      this.calculating = false
      this.asyncTaskId = null
      this.asyncProgress = 0
    },
    handleRefresh() {
      this.searchForm = {
        stockCodes: [],
        dateRange: [],
        adjustmentType: 'NONE',
        dataType: 'RETURN',
        coefficientType: 'PEARSON',
        calculationMode: 'STATIC',
        rollingWindow: 20,
        rollingStep: 1,
        outlierFilter: true,
        resultCache: true
      }
      this.resultData = null
      this.searchKeyword = ''
      this.paramsChanged = false
      this.asyncProgress = 0
      this.asyncTaskId = null
      this.disposeCharts()
      this.$message.info('页面已重置')
    },
    async handleExport(format) {
      if (!this.resultData || !this.resultData.detailRows || this.resultData.detailRows.length === 0) {
        this.$message.warning('暂无数据可导出')
        return
      }
      try {
        const exportFn = format === 'csv' ? exportCorrelationCSV : exportCorrelationExcel
        const res = await exportFn(this.resultData.detailRows)
        const ext = format === 'csv' ? 'csv' : 'xlsx'
        const mimeType = format === 'csv' ? 'text/csv' : 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        const url = window.URL.createObjectURL(new Blob([res], { type: mimeType }))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `相关性分析明细.${ext}`)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        this.$message.success(`导出 ${ext.toUpperCase()} 成功`)
      } catch (e) {
        this.$message.error('导出失败')
      }
    },
    showSaveSchemeDialog() {
      this.schemeName = ''
      this.saveSchemeVisible = true
    },
    async handleSaveScheme() {
      if (!this.schemeName) {
        this.$message.warning('请输入方案名称')
        return
      }
      try {
        await saveCorrelationScheme({
          schemeName: this.schemeName,
          stockCodes: this.searchForm.stockCodes,
          startDate: this.searchForm.dateRange ? this.searchForm.dateRange[0] : null,
          endDate: this.searchForm.dateRange ? this.searchForm.dateRange[1] : null,
          adjustmentType: this.searchForm.adjustmentType,
          calculationType: this.searchForm.dataType,
          coefficientType: this.searchForm.coefficientType,
          calculationMode: this.searchForm.calculationMode,
          rollingWindow: this.searchForm.rollingWindow,
          rollingStep: this.searchForm.rollingStep,
          outlierFilter: this.searchForm.outlierFilter,
          resultCache: this.searchForm.resultCache
        })
        this.saveSchemeVisible = false
        this.$message.success('方案保存成功')
        this.loadSchemes()
      } catch (e) {
        this.$message.error('保存方案失败')
      }
    },
    handleLoadScheme(scheme) {
      this.searchForm.stockCodes = scheme.stockCodes || []
      if (scheme.startDate && scheme.endDate) {
        this.searchForm.dateRange = [scheme.startDate, scheme.endDate]
      }
      this.searchForm.adjustmentType = scheme.adjustmentType || 'NONE'
      this.searchForm.dataType = scheme.calculationType || 'RETURN'
      this.searchForm.coefficientType = scheme.coefficientType || 'PEARSON'
      this.searchForm.calculationMode = scheme.calculationMode || 'STATIC'
      this.searchForm.rollingWindow = scheme.rollingWindow || 20
      this.searchForm.rollingStep = scheme.rollingStep || 1
      this.searchForm.outlierFilter = scheme.outlierFilter !== false
      this.searchForm.resultCache = scheme.resultCache !== false
      this.markParamsChanged()
      this.$message.success(`已加载方案: ${scheme.schemeName}`)
    },
    showHelpDialog() {
      this.helpVisible = true
    },
    addHistory(status) {
      this.calcHistory.unshift({
        time: new Date().toLocaleString('zh-CN'),
        stocks: this.searchForm.stockCodes.join(', '),
        dataType: this.searchForm.dataType,
        mode: this.searchForm.calculationMode,
        status
      })
      if (this.calcHistory.length > 20) {
        this.calcHistory = this.calcHistory.slice(0, 20)
      }
    },
    loadHistoryRecord(record) {
      this.$message.info('历史记录已展示在结果区域')
    },
    clearHistory() {
      this.calcHistory = []
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    filterCorrelationLevel(value, row) {
      return row.correlationLevel === value
    },
    renderAllCharts() {
      if (this.resultData.correlationMatrix && this.resultData.correlationMatrix.length > 0 && this.searchForm.stockCodes.length > 2) {
        this.renderHeatmap()
      }
      if (this.resultData.scatterData && this.resultData.scatterData.length > 0 && this.searchForm.stockCodes.length === 2) {
        this.renderScatter()
      }
      if (this.resultData.rollingCorrelation && this.resultData.rollingCorrelation.length > 0 && this.searchForm.calculationMode === 'ROLLING') {
        this.renderRolling()
      }
    },
    renderHeatmap() {
      if (this.charts.heatmapChart) {
        this.charts.heatmapChart.dispose()
      }
      if (!this.$refs.heatmapChart) return
      this.charts.heatmapChart = echarts.init(this.$refs.heatmapChart)

      const codes = this.resultData.matrixStockCodes
      const names = this.resultData.matrixStockNames
      const matrix = this.resultData.correlationMatrix
      const coeffType = this.searchForm.coefficientType

      const data = []
      const tooltipData = {}
      for (let i = 0; i < codes.length; i++) {
        for (let j = 0; j < codes.length; j++) {
          const cell = matrix.find(m => m.stockCodeX === codes[i] && m.stockCodeY === codes[j])
          const value = cell ? (coeffType === 'SPEARMAN' ? cell.spearmanCoefficient : cell.pearsonCoefficient) : 0
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
          axisLabel: { rotate: 45, interval: 0, fontSize: 11 },
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
        dataZoom: [
          { type: 'inside', xAxisIndex: 0 },
          { type: 'inside', yAxisIndex: 0 }
        ],
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

      this.charts.heatmapChart.setOption(option)
      this.addResizeListener('heatmapChart')
    },
    renderScatter() {
      if (this.charts.scatterChart) {
        this.charts.scatterChart.dispose()
      }
      if (!this.$refs.scatterChart) return
      this.charts.scatterChart = echarts.init(this.$refs.scatterChart)

      const scatterData = this.resultData.scatterData
      const stockNames = this.resultData.matrixStockNames
      const xName = stockNames && stockNames.length > 0 ? stockNames[0] : '标的A'
      const yName = stockNames && stockNames.length > 1 ? stockNames[1] : '标的B'
      const dataTypeLabel = this.getDataTypeLabel(this.searchForm.dataType)

      const seriesData = scatterData.map(p => [p.xValue, p.yValue, p.date])

      const option = {
        tooltip: {
          formatter: (params) => {
            return `
              <div style="padding: 8px;">
                <div>日期: <strong>${params.data[2]}</strong></div>
                <div>${xName}: <strong>${params.data[0].toFixed(4)}</strong></div>
                <div>${yName}: <strong>${params.data[1].toFixed(4)}</strong></div>
              </div>
            `
          }
        },
        grid: {
          left: '8%',
          right: '5%',
          top: '10%',
          bottom: '12%'
        },
        xAxis: {
          type: 'value',
          name: `${xName} (${dataTypeLabel})`,
          nameLocation: 'center',
          nameGap: 30,
          splitLine: { lineStyle: { type: 'dashed' } }
        },
        yAxis: {
          type: 'value',
          name: `${yName} (${dataTypeLabel})`,
          nameLocation: 'center',
          nameGap: 40,
          splitLine: { lineStyle: { type: 'dashed' } }
        },
        dataZoom: [
          { type: 'inside', xAxisIndex: 0 },
          { type: 'inside', yAxisIndex: 0 }
        ],
        series: [{
          type: 'scatter',
          data: seriesData,
          symbolSize: 6,
          itemStyle: {
            color: 'rgba(64, 158, 255, 0.6)',
            borderColor: '#409EFF',
            borderWidth: 1
          },
          emphasis: {
            itemStyle: {
              color: '#F56C6C',
              borderColor: '#F56C6C',
              borderWidth: 2,
              shadowBlur: 10,
              shadowColor: 'rgba(245, 108, 108, 0.5)'
            }
          }
        }]
      }

      this.charts.scatterChart.setOption(option)
      this.addResizeListener('scatterChart')
    },
    renderRolling() {
      if (this.charts.rollingChart) {
        this.charts.rollingChart.dispose()
      }
      if (!this.$refs.rollingChart) return
      this.charts.rollingChart = echarts.init(this.$refs.rollingChart)

      const data = this.resultData.rollingCorrelation
      const dates = data.map(d => d.tradeDate)
      const coeffType = this.searchForm.coefficientType

      const series = []
      if (coeffType === 'PEARSON' || coeffType === 'BOTH') {
        series.push({
          name: 'Pearson系数',
          type: 'line',
          data: data.map(d => d.pearsonCoefficient),
          smooth: true,
          lineStyle: { width: 2 },
          itemStyle: { color: '#409EFF' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ])
          }
        })
      }
      if (coeffType === 'SPEARMAN' || coeffType === 'BOTH') {
        series.push({
          name: 'Spearman系数',
          type: 'line',
          data: data.map(d => d.spearmanCoefficient),
          smooth: true,
          lineStyle: { width: 2 },
          itemStyle: { color: '#67C23A' },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
            ])
          }
        })
      }

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
          data: series.map(s => s.name),
          top: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '12%',
          top: '12%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: dates,
          axisLabel: { rotate: 45, fontSize: 10 }
        },
        yAxis: {
          type: 'value',
          min: -1,
          max: 1,
          splitLine: { lineStyle: { type: 'dashed' } }
        },
        dataZoom: [
          { type: 'inside', start: 0, end: 100 },
          { type: 'slider', start: 0, end: 100 }
        ],
        series
      }

      this.charts.rollingChart.setOption(option)
      this.addResizeListener('rollingChart')
    },
    addResizeListener(chartKey) {
      const handler = () => {
        if (this.charts[chartKey]) {
          this.charts[chartKey].resize()
        }
      }
      window.addEventListener('resize', handler)
    },
    toggleFullscreen(chartRef) {
      this.fullscreenTitle = chartRef === 'heatmapChart' ? '相关性热力图' :
        chartRef === 'scatterChart' ? '散点图' : '趋势折线图'
      this.fullscreenChartType = chartRef
      this.fullscreenVisible = true
      this.$nextTick(() => {
        if (this.charts[chartRef]) {
          const option = this.charts[chartRef].getOption()
          const fullscreenChart = echarts.init(this.$refs.fullscreenChart)
          fullscreenChart.setOption(option)
          window.addEventListener('resize', () => fullscreenChart.resize())
        }
      })
    },
    disposeCharts() {
      Object.keys(this.charts).forEach(key => {
        if (this.charts[key]) {
          this.charts[key].dispose()
          this.charts[key] = null
        }
      })
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
    getCoefficientClass(value) {
      if (value == null) return ''
      if (value > 0.7) return 'strong-positive'
      if (value > 0.3) return 'weak-positive'
      if (value < -0.7) return 'strong-negative'
      if (value < -0.3) return 'weak-negative'
      return 'no-correlation'
    },
    getLevelTagType(level) {
      if (!level) return 'info'
      if (level.includes('强正')) return 'danger'
      if (level.includes('弱正')) return 'warning'
      if (level.includes('强负')) return 'success'
      if (level.includes('弱负')) return 'info'
      return 'info'
    },
    formatDateTime(dt) {
      if (!dt) return '-'
      if (typeof dt === 'string') return dt.replace('T', ' ').substring(0, 19)
      if (dt instanceof Array) {
        const [y, m, d, h, min, s] = dt
        return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h || 0).padStart(2, '0')}:${String(min || 0).padStart(2, '0')}:${String(s || 0).padStart(2, '0')}`
      }
      return String(dt)
    }
  }
}
</script>

<style scoped>
.correlation-analysis {
  padding: 0;
}

.top-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 4px;
  margin-bottom: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.06);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.toolbar-right {
  display: flex;
  gap: 8px;
}

.config-card {
  margin-bottom: 12px;
}

.config-card ::v-deep .el-form-item {
  margin-bottom: 12px;
}

.batch-paste {
  margin-top: 4px;
}

.quick-dates {
  margin-top: 4px;
  display: flex;
  gap: 4px;
}

.switch-group {
  display: flex;
  align-items: center;
}

.result-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
}

.chart-container {
  height: 420px;
  width: 100%;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.history-card {
  margin-top: 12px;
}

.strong-positive {
  color: #F56C6C;
  font-weight: 600;
}

.weak-positive {
  color: #E6A23C;
  font-weight: 600;
}

.strong-negative {
  color: #67C23A;
  font-weight: 600;
}

.weak-negative {
  color: #909399;
  font-weight: 600;
}

.no-correlation {
  color: #606266;
}

.help-content h4 {
  color: #303133;
  margin: 16px 0 8px 0;
  font-size: 15px;
}

.help-content h4:first-child {
  margin-top: 0;
}

.help-content p {
  color: #606266;
  font-size: 13px;
  line-height: 1.8;
  margin: 0 0 8px 0;
}

.help-content ul {
  color: #606266;
  font-size: 13px;
  line-height: 2;
  padding-left: 20px;
}
</style>
