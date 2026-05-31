<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-edit"></i> 基础信息治理</h2>
        <div class="header-actions">
          <el-button type="primary" @click="handleAudit" :disabled="selectedRows.length === 0">
            <i class="el-icon-check"></i> 批量审核
          </el-button>
          <el-button type="warning" @click="handleClean" :disabled="selectedRows.length === 0">
            <i class="el-icon-delete-solid"></i> 数据清洗
          </el-button>
          <el-button @click="loadData">
            <i class="el-icon-refresh"></i> 刷新
          </el-button>
        </div>
      </div>

      <div class="filter-bar">
        <el-form :inline="true" :model="filterForm">
          <el-form-item label="股票代码">
            <el-input v-model="filterForm.code" placeholder="请输入股票代码" clearable @clear="handleSearch" @keyup.enter.native="handleSearch"></el-input>
          </el-form-item>
          <el-form-item label="数据状态">
            <el-select v-model="filterForm.status" placeholder="全部" clearable @change="handleSearch">
              <el-option label="待审核" value="pending"></el-option>
              <el-option label="已通过" value="approved"></el-option>
              <el-option label="已拒绝" value="rejected"></el-option>
              <el-option label="需清洗" value="need_clean"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="质量等级">
            <el-select v-model="filterForm.quality" placeholder="全部" clearable @change="handleSearch">
              <el-option label="优质" value="A"></el-option>
              <el-option label="良好" value="B"></el-option>
              <el-option label="一般" value="C"></el-option>
              <el-option label="较差" value="D"></el-option>
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
        <el-table-column prop="code" label="股票代码" width="100" fixed="left"></el-table-column>
        <el-table-column prop="name" label="股票名称" width="100"></el-table-column>
        <el-table-column prop="exchange" label="交易所" width="100">
          <template slot-scope="{ row }">
            <el-tag v-if="row.exchange === 'SH'" size="small" type="danger">上交所</el-tag>
            <el-tag v-else-if="row.exchange === 'SZ'" size="small" type="success">深交所</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="industry" label="行业" width="100"></el-table-column>
        <el-table-column prop="totalShares" label="总股本(万股)" width="120">
          <template slot-scope="{ row }">
            {{ formatNumber(row.totalShares) }}
          </template>
        </el-table-column>
        <el-table-column prop="dataStatus" label="数据状态" width="100">
          <template slot-scope="{ row }">
            <el-tag v-if="row.dataStatus === 'approved'" size="small" type="success">已通过</el-tag>
            <el-tag v-else-if="row.dataStatus === 'pending'" size="small" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.dataStatus === 'rejected'" size="small" type="danger">已拒绝</el-tag>
            <el-tag v-else-if="row.dataStatus === 'need_clean'" size="small" type="info">需清洗</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="qualityLevel" label="质量等级" width="90">
          <template slot-scope="{ row }">
            <el-tag :type="getQualityType(row.qualityLevel)" size="small">{{ row.qualityLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="completeness" label="完整度" width="100">
          <template slot-scope="{ row }">
            <el-progress :percentage="row.completeness" :stroke-width="12"></el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="accuracy" label="准确度" width="100">
          <template slot-scope="{ row }">
            <el-progress :percentage="row.accuracy" :stroke-width="12"></el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160"></el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
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
import { getGovernanceBasicInfo, auditGovernanceBasicInfo, cleanGovernanceBasicInfo } from '@/api/governanceApi'

export default {
  name: 'GovernanceBasicInfo',
  data() {
    return {
      loading: false,
      allData: [],
      selectedRows: [],
      filterForm: {
        code: '',
        status: '',
        quality: ''
      },
      pagination: {
        page: 1,
        size: 20,
        total: 0
      }
    }
  },
  mounted() {
    this.loadData()
  },
  computed: {
    filteredData() {
      return this.allData.filter(item => {
        if (this.filterForm.code && !item.code.includes(this.filterForm.code) && !item.name.includes(this.filterForm.code)) return false
        if (this.filterForm.status && item.dataStatus !== this.filterForm.status) return false
        if (this.filterForm.quality && item.qualityLevel !== this.filterForm.quality) return false
        return true
      })
    }
  },
  methods: {
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
        const res = await getGovernanceBasicInfo(this.filterForm.code || null, this.filterForm.status || null, this.filterForm.quality || null)
        if (res && res.code === 200) {
          this.allData = res.data || []
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
      this.filterForm = { code: '', status: '', quality: '' }
    },
    handleSearch() {
      this.pagination.page = 1
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
        await auditGovernanceBasicInfo(ids, true, '')
        this.$message.success('批量审核通过成功')
        this.selectedRows = []
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('审核失败:', e)
        }
      }
    },
    async handleClean() {
      const ids = this.selectedRows.map(r => r.id).join(',')
      try {
        await this.$confirm(`确定要清洗选中的 ${this.selectedRows.length} 条数据吗？`, '数据清洗', {
          confirmButtonText: '清洗',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await cleanGovernanceBasicInfo(ids)
        this.$message.success('数据清洗任务已启动')
        this.selectedRows = []
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('清洗失败:', e)
        }
      }
    },
    async handleAuditSingle(row) {
      try {
        const { value } = await this.$prompt('请输入审核意见', `审核 ${row.code} ${row.name}`, {
          confirmButtonText: '通过',
          cancelButtonText: '拒绝',
          inputType: 'textarea',
          inputPlaceholder: '请输入审核意见（选填）'
        })
        await auditGovernanceBasicInfo(row.id, true, value || '')
        this.$message.success(`审核通过: ${row.code} ${row.name}`)
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          try {
            await auditGovernanceBasicInfo(row.id, false, '审核拒绝')
            this.$message.info(`已拒绝: ${row.code} ${row.name}`)
            this.loadData()
          } catch (err) {
            console.error('拒绝失败:', err)
          }
        }
      }
    },
    handleEdit(row) {
      this.$alert(`编辑 ${row.code} ${row.name} 的基础信息`, '编辑数据', {
        confirmButtonText: '保存',
        callback: () => {
          this.$message.success('数据更新成功')
          this.loadData()
        }
      })
    },
    handleView(row) {
      this.$alert(JSON.stringify(row, null, 2), `${row.code} ${row.name} 详细信息`, {
        dangerouslyUseHTMLString: false,
        type: 'info'
      })
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
.filter-bar {
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
}
::v-deep .el-table {
  flex: 1;
}
</style>
