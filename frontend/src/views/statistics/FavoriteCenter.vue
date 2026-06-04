<template>
  <div class="favorite-center">
    <el-row :gutter="16">
      <el-col :span="4">
        <el-card class="group-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-folder"></i> 分组管理</span>
            <el-button type="primary" size="mini" icon="el-icon-plus" @click="showCreateGroupDialog">
              新建
            </el-button>
          </div>
          <div class="group-list">
            <div 
              class="group-item" 
              :class="{ active: currentGroupId === 0 }"
              @click="selectGroup(0)">
              <i class="el-icon-folder-opened"></i>
              <span class="group-name">全部自选</span>
              <span class="group-count">({{ allStocksCount }})</span>
            </div>
            <div 
              v-for="group in groups" 
              :key="group.id"
              class="group-item" 
              :class="{ active: currentGroupId === group.id }"
              @click="selectGroup(group.id)">
              <i class="el-icon-folder"></i>
              <span class="group-name">{{ group.name }}</span>
              <span class="group-count">({{ getGroupStockCount(group.id) }})</span>
              <div class="group-actions" v-if="!group.isDefault">
                <el-button type="text" size="mini" icon="el-icon-edit" @click.stop="showEditGroupDialog(group)">
                </el-button>
                <el-button type="text" size="mini" icon="el-icon-delete" style="color: #F56C6C" @click.stop="confirmDeleteGroup(group)">
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="20">
        <el-card>
          <div slot="header" class="card-header">
            <span><i class="el-icon-star-on"></i> 自选股中心</span>
            <div class="header-actions">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索股票代码或名称"
                prefix-icon="el-icon-search"
                style="width: 200px; margin-right: 10px;"
                clearable
                @input="handleSearch"
              />
              <el-button type="primary" icon="el-icon-plus" @click="showAddStockDialog">
                添加股票
              </el-button>
              <el-button type="success" icon="el-icon-document" @click="showBatchAddDialog">
                批量添加
              </el-button>
              <el-button 
                type="danger" 
                icon="el-icon-delete" 
                :disabled="selectedStocks.length === 0"
                @click="confirmBatchDelete">
                批量删除 ({{ selectedStocks.length }})
              </el-button>
              <el-dropdown @command="handleExport">
                <el-button type="warning" icon="el-icon-download">
                  导出 <i class="el-icon-arrow-down el-icon--right"></i>
                </el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="excel">导出 Excel</el-dropdown-item>
                  <el-dropdown-item command="csv">导出 CSV</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>

          <el-table
            ref="stockTable"
            :data="filteredStocks"
            style="width: 100%"
            @selection-change="handleSelectionChange"
            v-loading="loading">
            <el-table-column type="selection" width="55" align="center"></el-table-column>
            <el-table-column prop="code" label="股票代码" width="120" align="center">
              <template slot-scope="scope">
                <span class="stock-code">{{ scope.row.code }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="股票名称" width="150" align="center"></el-table-column>
            <el-table-column prop="price" label="当前价格" width="120" align="center">
              <template slot-scope="scope">
                <span class="price">{{ scope.row.price ? scope.row.price.toFixed(2) : '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="changePercent" label="涨跌幅" width="120" align="center">
              <template slot-scope="scope">
                <span :class="getChangeClass(scope.row.changePercent)">
                  {{ scope.row.changePercent !== null && scope.row.changePercent !== undefined ? (scope.row.changePercent > 0 ? '+' : '') + scope.row.changePercent.toFixed(2) + '%' : '--' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="groupName" label="所属分组" width="150" align="center">
              <template slot-scope="scope">
                <el-tag size="mini" type="info">{{ getGroupName(scope.row.groupId) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="addTime" label="添加时间" align="center">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.addTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" icon="el-icon-delete" style="color: #F56C6C" @click="confirmDeleteStock(scope.row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-empty v-if="filteredStocks.length === 0 && !loading" description="暂无自选股数据"></el-empty>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="新建分组" :visible.sync="createGroupDialogVisible" width="400px">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="分组名称">
          <el-input v-model="groupForm.name" placeholder="请输入分组名称" maxlength="20"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="createGroupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createGroup" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="修改分组" :visible.sync="editGroupDialogVisible" width="400px">
      <el-form :model="groupForm" label-width="80px">
        <el-form-item label="分组名称">
          <el-input v-model="groupForm.name" placeholder="请输入分组名称" maxlength="20"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editGroupDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateGroup" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="添加股票" :visible.sync="addStockDialogVisible" width="500px">
      <el-form :model="addStockForm" label-width="80px">
        <el-form-item label="选择分组">
          <el-select v-model="addStockForm.groupId" placeholder="请选择分组" style="width: 100%;">
            <el-option v-for="group in groups" :key="group.id" :label="group.name" :value="group.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="股票代码">
          <el-autocomplete
            v-model="addStockForm.code"
            :fetch-suggestions="querySearchAsync"
            placeholder="输入股票代码或名称"
            @select="handleStockSelect"
            clearable
            style="width: 100%;">
          </el-autocomplete>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addStockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addStock" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="批量添加股票" :visible.sync="batchAddDialogVisible" width="500px">
      <el-form :model="batchAddForm" label-width="80px">
        <el-form-item label="选择分组">
          <el-select v-model="batchAddForm.groupId" placeholder="请选择分组" style="width: 100%;">
            <el-option v-for="group in groups" :key="group.id" :label="group.name" :value="group.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="股票代码">
          <el-input
            type="textarea"
            v-model="batchAddForm.codes"
            placeholder="请输入股票代码，多个代码用换行、逗号或空格分隔&#10;例如：&#10;000001,000002&#10;000003"
            :rows="8"
            maxlength="2000"
            show-word-limit>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="batchAddDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="batchAddStocks" :loading="submitting">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  getGroups,
  createGroup,
  updateGroup,
  deleteGroup,
  getFavoriteStocks,
  addFavoriteStock,
  addFavoriteStocksBatch,
  deleteFavoriteStock,
  deleteFavoriteStocksBatch,
  exportToExcel,
  exportToCSV,
  searchAvailableStocks
} from '@/api/favoriteStockApi'

export default {
  name: 'FavoriteCenter',
  data() {
    return {
      loading: false,
      submitting: false,
      groups: [],
      favoriteStocks: [],
      currentGroupId: 0,
      searchKeyword: '',
      selectedStocks: [],
      createGroupDialogVisible: false,
      editGroupDialogVisible: false,
      addStockDialogVisible: false,
      batchAddDialogVisible: false,
      groupForm: {
        id: null,
        name: ''
      },
      addStockForm: {
        code: '',
        groupId: null
      },
      batchAddForm: {
        codes: '',
        groupId: null
      }
    }
  },
  computed: {
    allStocksCount() {
      return this.favoriteStocks.length
    },
    filteredStocks() {
      if (!this.searchKeyword) {
        return this.currentGroupId === 0 
          ? this.favoriteStocks 
          : this.favoriteStocks.filter(s => s.groupId === this.currentGroupId)
      }
      const keyword = this.searchKeyword.toLowerCase()
      let stocks = this.currentGroupId === 0 
        ? this.favoriteStocks 
        : this.favoriteStocks.filter(s => s.groupId === this.currentGroupId)
      return stocks.filter(s => 
        s.code.toLowerCase().includes(keyword) || 
        (s.name && s.name.toLowerCase().includes(keyword))
      )
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadGroups(),
          this.loadFavoriteStocks()
        ])
      } catch (error) {
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },
    async loadGroups() {
      const res = await getGroups()
      this.groups = res.data
      if (this.groups.length > 0 && !this.addStockForm.groupId) {
        this.addStockForm.groupId = this.groups[0].id
        this.batchAddForm.groupId = this.groups[0].id
      }
    },
    async loadFavoriteStocks() {
      const res = await getFavoriteStocks()
      this.favoriteStocks = res.data
    },
    getGroupStockCount(groupId) {
      return this.favoriteStocks.filter(s => s.groupId === groupId).length
    },
    getGroupName(groupId) {
      const group = this.groups.find(g => g.id === groupId)
      return group ? group.name : '未知分组'
    },
    selectGroup(groupId) {
      this.currentGroupId = groupId
    },
    showCreateGroupDialog() {
      this.groupForm = { id: null, name: '' }
      this.createGroupDialogVisible = true
    },
    async createGroup() {
      if (!this.groupForm.name.trim()) {
        this.$message.warning('请输入分组名称')
        return
      }
      this.submitting = true
      try {
        await createGroup(this.groupForm.name.trim())
        this.$message.success('创建成功')
        this.createGroupDialogVisible = false
        await this.loadGroups()
      } catch (error) {
        this.$message.error(error.response?.data?.message || '创建失败')
      } finally {
        this.submitting = false
      }
    },
    showEditGroupDialog(group) {
      this.groupForm = { id: group.id, name: group.name }
      this.editGroupDialogVisible = true
    },
    async updateGroup() {
      if (!this.groupForm.name.trim()) {
        this.$message.warning('请输入分组名称')
        return
      }
      this.submitting = true
      try {
        await updateGroup(this.groupForm.id, this.groupForm.name.trim())
        this.$message.success('修改成功')
        this.editGroupDialogVisible = false
        await this.loadGroups()
      } catch (error) {
        this.$message.error(error.response?.data?.message || '修改失败')
      } finally {
        this.submitting = false
      }
    },
    async confirmDeleteGroup(group) {
      try {
        await this.$confirm(
          `确定要删除分组"${group.name}"吗？分组内的股票将自动移到默认分组。`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        await deleteGroup(group.id)
        this.$message.success('删除成功')
        if (this.currentGroupId === group.id) {
          this.currentGroupId = 0
        }
        await this.loadGroups()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.response?.data?.message || '删除失败')
        }
      }
    },
    showAddStockDialog() {
      this.addStockForm = { code: '', groupId: this.groups.length > 0 ? this.groups[0].id : null }
      this.addStockDialogVisible = true
    },
    async querySearchAsync(queryString, cb) {
      if (!queryString) {
        cb([])
        return
      }
      try {
        const res = await searchAvailableStocks(queryString)
        const stocks = res.data.map(s => ({
          value: `${s.code} - ${s.name}`,
          code: s.code,
          name: s.name
        }))
        cb(stocks)
      } catch (error) {
        cb([])
      }
    },
    handleStockSelect(item) {
      this.addStockForm.code = item.code
    },
    async addStock() {
      if (!this.addStockForm.code.trim()) {
        this.$message.warning('请输入股票代码')
        return
      }
      this.submitting = true
      try {
        const res = await addFavoriteStock(this.addStockForm.code.trim(), this.addStockForm.groupId)
        this.$message.success('添加成功')
        this.addStockDialogVisible = false
        this.favoriteStocks = res.data.favorites
      } catch (error) {
        this.$message.error(error.response?.data?.message || '添加失败')
      } finally {
        this.submitting = false
      }
    },
    showBatchAddDialog() {
      this.batchAddForm = { codes: '', groupId: this.groups.length > 0 ? this.groups[0].id : null }
      this.batchAddDialogVisible = true
    },
    async batchAddStocks() {
      if (!this.batchAddForm.codes.trim()) {
        this.$message.warning('请输入股票代码')
        return
      }
      const codes = this.batchAddForm.codes
        .split(/[,，\s\n]+/)
        .map(c => c.trim())
        .filter(c => c)
      
      if (codes.length === 0) {
        this.$message.warning('请输入有效的股票代码')
        return
      }
      this.submitting = true
      try {
        const res = await addFavoriteStocksBatch(codes, this.batchAddForm.groupId)
        const data = res.data
        let msg = `成功添加 ${data.success.length} 只股票`
        if (data.duplicate.length > 0) {
          msg += `，${data.duplicate.length} 只已存在`
        }
        if (data.failed.length > 0) {
          msg += `，${data.failed.length} 只添加失败`
        }
        this.$message.success(msg)
        if (data.failed.length > 0) {
          this.$message.warning('失败的股票：' + data.failed.join('、'))
        }
        this.batchAddDialogVisible = false
        this.favoriteStocks = data.favorites
      } catch (error) {
        this.$message.error(error.response?.data?.message || '添加失败')
      } finally {
        this.submitting = false
      }
    },
    async confirmDeleteStock(stock) {
      try {
        await this.$confirm(
          `确定要删除股票"${stock.name || stock.code}"吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        const res = await deleteFavoriteStock(stock.code)
        this.$message.success('删除成功')
        this.favoriteStocks = res.data.favorites
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.response?.data?.message || '删除失败')
        }
      }
    },
    handleSelectionChange(selection) {
      this.selectedStocks = selection
    },
    async confirmBatchDelete() {
      if (this.selectedStocks.length === 0) {
        this.$message.warning('请选择要删除的股票')
        return
      }
      try {
        await this.$confirm(
          `确定要删除选中的 ${this.selectedStocks.length} 只股票吗？`,
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        const codes = this.selectedStocks.map(s => s.code)
        const res = await deleteFavoriteStocksBatch(codes)
        this.$message.success('删除成功')
        this.favoriteStocks = res.data.favorites
        this.selectedStocks = []
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(error.response?.data?.message || '删除失败')
        }
      }
    },
    async handleExport(type) {
      try {
        let res
        const groupId = this.currentGroupId === 0 ? null : this.currentGroupId
        let exportCodes = null
        
        if (this.selectedStocks.length > 0) {
          exportCodes = this.selectedStocks.map(s => s.code)
          this.$message.info(`正在导出选中的 ${exportCodes.length} 只股票...`)
        } else {
          exportCodes = this.filteredStocks.map(s => s.code)
          this.$message.info(`正在导出当前筛选条件下的 ${exportCodes.length} 只股票...`)
        }
        
        if (type === 'excel') {
          res = await exportToExcel(groupId, exportCodes)
          this.downloadFile(res.data, 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', '自选股.xlsx')
        } else {
          res = await exportToCSV(groupId, exportCodes)
          this.downloadFile(res.data, 'text/csv;charset=utf-8', '自选股.csv')
        }
        this.$message.success('导出成功')
      } catch (error) {
        this.$message.error('导出失败')
      }
    },
    downloadFile(blob, mimeType, filename) {
      const url = window.URL.createObjectURL(new Blob([blob], { type: mimeType }))
      const link = document.createElement('a')
      link.href = url
      link.download = filename
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    },
    handleSearch() {
    },
    getChangeClass(changePercent) {
      if (changePercent === null || changePercent === undefined) return ''
      if (changePercent > 0) return 'change-up'
      if (changePercent < 0) return 'change-down'
      return 'change-flat'
    },
    formatDateTime(dateStr) {
      if (!dateStr) return '--'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.favorite-center {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.group-card {
  height: calc(100vh - 150px);
}

.group-list {
  max-height: calc(100vh - 250px);
  overflow-y: auto;
}

.group-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 4px;
  margin-bottom: 4px;
  transition: all 0.2s;
  position: relative;
}

.group-item:hover {
  background-color: #f5f7fa;
}

.group-item.active {
  background-color: #ecf5ff;
  color: #409EFF;
}

.group-item i {
  margin-right: 8px;
  font-size: 16px;
}

.group-name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.group-count {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}

.group-actions {
  display: none;
  position: absolute;
  right: 4px;
  background: inherit;
}

.group-item:hover .group-actions {
  display: flex;
}

.stock-code {
  font-family: 'Consolas', monospace;
  font-weight: 600;
}

.price {
  font-weight: 600;
  font-family: 'Consolas', monospace;
}

.change-up {
  color: #F56C6C;
  font-weight: 600;
}

.change-down {
  color: #67C23A;
  font-weight: 600;
}

.change-flat {
  color: #909399;
}
</style>
