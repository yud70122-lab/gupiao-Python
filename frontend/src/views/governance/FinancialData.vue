<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-edit"></i> 财务数据治理</h2>
        <div class="header-actions">
          <el-button type="primary" @click="handleAudit" :disabled="selectedRows.length === 0">
            <i class="el-icon-check"></i> 批量审核
          </el-button>
          <el-button type="warning" @click="handleStandardize" :disabled="selectedRows.length === 0">
            <i class="el-icon-s-grid"></i> 格式标准化
          </el-button>
          <el-button type="success" @click="handleVerify" :disabled="selectedRows.length === 0">
            <i class="el-icon-tickets"></i> 勾稽校验
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
          <el-form-item label="报告类型">
            <el-radio-group v-model="form.reportType" @change="handleSearch">
              <el-radio-button label="income">利润表</el-radio-button>
              <el-radio-button label="balance">资产负债表</el-radio-button>
              <el-radio-button label="cashflow">现金流表</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="数据状态">
            <el-select v-model="form.status" placeholder="全部" clearable @change="handleSearch" style="width: 120px;">
              <el-option label="已同步" value="synced"></el-option>
              <el-option label="待审核" value="pending"></el-option>
              <el-option label="异常" value="abnormal"></el-option>
              <el-option label="格式错误" value="format_error"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="filteredData" border stripe v-loading="loading" height="calc(100vh - 380px)" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="period" label="报告期" width="120" fixed="left"></el-table-column>
        <el-table-column prop="code" label="股票代码" width="100"></el-table-column>
        <el-table-column prop="name" label="股票名称" width="100"></el-table-column>
        <el-table-column prop="item1" :label="itemLabels[0]" width="140">
          <template slot-scope="{ row }">
            {{ formatMoney(row.item1) }}
          </template>
        </el-table-column>
        <el-table-column prop="item2" :label="itemLabels[1]" width="140">
          <template slot-scope="{ row }">
            {{ formatMoney(row.item2) }}
          </template>
        </el-table-column>
        <el-table-column prop="item3" :label="itemLabels[2]" width="140">
          <template slot-scope="{ row }">
            {{ formatMoney(row.item3) }}
          </template>
        </el-table-column>
        <el-table-column prop="item4" :label="itemLabels[3]" width="140">
          <template slot-scope="{ row }">
            {{ formatMoney(row.item4) }}
          </template>
        </el-table-column>
        <el-table-column prop="item5" :label="itemLabels[4]" width="140">
          <template slot-scope="{ row }">
            {{ formatMoney(row.item5) }}
          </template>
        </el-table-column>
        <el-table-column prop="dataStatus" label="数据状态" width="100">
          <template slot-scope="{ row }">
            <el-tag v-if="row.dataStatus === 'synced'" size="small" type="success">已同步</el-tag>
            <el-tag v-else-if="row.dataStatus === 'pending'" size="small" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.dataStatus === 'abnormal'" size="small" type="danger">异常</el-tag>
            <el-tag v-else-if="row.dataStatus === 'format_error'" size="small" type="info">格式错误</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="verifyStatus" label="勾稽状态" width="90">
          <template slot-scope="{ row }">
            <el-tag v-if="row.verifyStatus === 'pass'" size="small" type="success">通过</el-tag>
            <el-tag v-else-if="row.verifyStatus === 'fail'" size="small" type="danger">失败</el-tag>
            <el-tag v-else size="small" type="info">未校验</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160"></el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="handleAuditSingle(row)">审核</el-button>
            <el-button type="text" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleView(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.page"
          :page-sizes="[20, 50, 100]"
          :page-size="pagination.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total">
        </el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import { getGovernanceFinancialData, auditGovernanceFinancialData, standardizeGovernanceFinancialData, verifyGovernanceFinancialData } from '@/api/governanceApi'

export default {
  name: 'GovernanceFinancialData',
  data() {
    return {
      loading: false,
      selectedRows: [],
      form: {
        stock: '',
        reportType: 'income',
        status: ''
      },
      stockList: [],
      tableData: [],
      allData: [],
      pagination: {
        page: 1,
        size: 20,
        total: 0
      }
    }
  },
  computed: {
    itemLabels() {
      const labels = {
        income: ['营业收入', '营业成本', '净利润', '每股收益', '市盈率'],
        balance: ['总资产', '总负债', '净资产', '流动资产', '流动负债'],
        cashflow: ['经营现金流', '投资现金流', '筹资现金流', '现金净增', '期末现金']
      }
      return labels[this.form.reportType] || []
    },
    filteredData() {
      return this.allData.filter(item => {
        if (this.form.stock && item.code !== this.form.stock) return false
        if (this.form.status && item.dataStatus !== this.form.status) return false
        return true
      })
    }
  },
  mounted() {
    this.loadStockList()
    this.loadData()
  },
  methods: {
    async loadStockList() {
      try {
        const res = await getGovernanceFinancialData(null, this.form.reportType, null)
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
    formatMoney(num) {
      if (!num && num !== 0) return '-'
      return (num / 100000000).toFixed(2) + '亿'
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getGovernanceFinancialData(this.form.stock || null, this.form.reportType, this.form.status || null)
        if (res && res.code === 200) {
          this.allData = res.data || []
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
      this.form = { stock: '', reportType: 'income', status: '' }
      this.loadData()
    },
    handleSearch() {
      this.loadData()
    },
    async handleAudit() {
      const ids = this.selectedRows.map(r => r.id).join(',')
      try {
        await this.$confirm(`确定要审核选中的 ${this.selectedRows.length} 条财务数据吗？`, '批量审核', {
          confirmButtonText: '通过',
          cancelButtonText: '取消',
          type: 'success'
        })
        await auditGovernanceFinancialData(ids, true)
        this.$message.success('批量审核通过成功')
        this.selectedRows = []
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('审核失败:', e)
        }
      }
    },
    async handleStandardize() {
      const ids = this.selectedRows.map(r => r.id).join(',')
      try {
        await this.$confirm(`确定要对选中的 ${this.selectedRows.length} 条数据进行格式标准化吗？`, '格式标准化', {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await standardizeGovernanceFinancialData(ids)
        this.$message.success('格式标准化任务已启动')
        this.selectedRows = []
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('标准化失败:', e)
        }
      }
    },
    async handleVerify() {
      const ids = this.selectedRows.length > 0 ? this.selectedRows.map(r => r.id).join(',') : null
      try {
        await verifyGovernanceFinancialData(ids)
        this.$message.success('勾稽校验任务已启动，正在后台执行')
        setTimeout(() => {
          this.loadData()
        }, 1000)
      } catch (e) {
        console.error('校验失败:', e)
      }
    },
    async handleAuditSingle(row) {
      try {
        const { value } = await this.$prompt('请输入审核意见', `审核 ${row.period} 财务数据`, {
          confirmButtonText: '通过',
          cancelButtonText: '拒绝',
          inputType: 'textarea'
        })
        await auditGovernanceFinancialData(row.id, true)
        this.$message.success('审核通过')
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          try {
            await auditGovernanceFinancialData(row.id, false)
            this.$message.info('已拒绝')
            this.loadData()
          } catch (err) {
            console.error('拒绝失败:', err)
          }
        }
      }
    },
    handleEdit(row) {
      this.$alert(`编辑 ${row.code} ${row.name} ${row.period} 的财务数据`, '编辑数据', {
        confirmButtonText: '保存',
        callback: () => {
          this.$message.success('数据更新成功')
          this.loadData()
        }
      })
    },
    handleView(row) {
      this.$alert(JSON.stringify(row, null, 2), '详细信息', { type: 'info' })
    },
    handleSizeChange(val) {
      this.pagination.size = val
      this.loadData()
    },
    handleCurrentChange(val) {
      this.pagination.page = val
      this.loadData()
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
.pagination-bar {
  padding: 16px 0 0 0;
  display: flex;
  justify-content: flex-end;
}
::v-deep .el-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
::v-deep .el-table {
  flex: 1;
}
</style>
