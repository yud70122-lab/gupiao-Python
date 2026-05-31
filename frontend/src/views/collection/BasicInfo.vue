<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-info"></i> 基础信息采集</h2>
        <div class="header-actions">
          <el-button type="primary" @click="handleCollect" :loading="collecting">
            <i class="el-icon-download"></i> 采集数据
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
          <el-form-item label="交易所">
            <el-select v-model="filterForm.exchange" placeholder="全部" clearable @change="handleSearch">
              <el-option label="上海证券交易所" value="SH"></el-option>
              <el-option label="深圳证券交易所" value="SZ"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="行业">
            <el-select v-model="filterForm.industry" placeholder="全部" clearable @change="handleSearch">
              <el-option v-for="item in industryList" :key="item" :label="item" :value="item"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="filteredData" border stripe v-loading="loading" height="calc(100vh - 340px)">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="code" label="股票代码" width="120" fixed="left"></el-table-column>
        <el-table-column prop="name" label="股票名称" width="120"></el-table-column>
        <el-table-column prop="exchange" label="交易所" width="140">
          <template slot-scope="{ row }">
            <el-tag v-if="row.exchange === 'SH'" size="small" type="danger">上交所</el-tag>
            <el-tag v-else-if="row.exchange === 'SZ'" size="small" type="success">深交所</el-tag>
            <span v-else>{{ row.exchange }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="industry" label="行业" width="120"></el-table-column>
        <el-table-column prop="concept" label="概念板块" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="totalShares" label="总股本(万股)" width="140">
          <template slot-scope="{ row }">
            {{ formatNumber(row.totalShares) }}
          </template>
        </el-table-column>
        <el-table-column prop="floatShares" label="流通股本(万股)" width="140">
          <template slot-scope="{ row }">
            {{ formatNumber(row.floatShares) }}
          </template>
        </el-table-column>
        <el-table-column prop="listDate" label="上市日期" width="120"></el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180"></el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" @click="handleView(row)">详情</el-button>
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
    </el-card>
  </div>
</template>

<script>
import { getCollectionBasicInfo, collectBasicInfo } from '@/api/collectionApi'

export default {
  name: 'BasicInfo',
  data() {
    return {
      loading: false,
      collecting: false,
      allData: [],
      industryList: [],
      filterForm: {
        code: '',
        exchange: '',
        industry: ''
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
        if (this.filterForm.exchange && item.exchange !== this.filterForm.exchange) return false
        if (this.filterForm.industry && item.industry !== this.filterForm.industry) return false
        return true
      })
    }
  },
  methods: {
    formatNumber(num) {
      if (!num && num !== 0) return '-'
      return num.toLocaleString('zh-CN')
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getCollectionBasicInfo(this.filterForm.code || null, this.filterForm.exchange || null, this.filterForm.industry || null)
        if (res && res.code === 200) {
          this.allData = res.data || []
          this.industryList = res.industries || []
          this.pagination.total = this.filteredData.length
        }
      } catch (e) {
        console.error('加载数据失败:', e)
      } finally {
        this.loading = false
      }
    },
    resetFilter() {
      this.filterForm = {
        code: '',
        exchange: '',
        industry: ''
      }
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    async handleCollect() {
      this.collecting = true
      try {
        await this.$confirm('确定要采集股票基础信息数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        await collectBasicInfo()
        this.$message.success('数据采集任务已启动')
        this.loadData()
      } catch (e) {
        if (e !== 'cancel') {
          console.error('采集失败:', e)
        }
      } finally {
        this.collecting = false
      }
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
