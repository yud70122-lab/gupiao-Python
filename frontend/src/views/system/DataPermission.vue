<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-lock"></i> 数据权限</h2>
      </div>

      <el-alert
        title="数据权限说明"
        type="info"
        :closable="false"
        style="margin-bottom: 16px">
        <div>
          <p>• <strong>全部数据</strong>：用户可查看所有股票数据</p>
          <p>• <strong>自定义</strong>：用户仅可查看指定股票代码的数据</p>
        </div>
      </el-alert>

      <el-table :data="userList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="140"></el-table-column>
        <el-table-column prop="realName" label="姓名" width="120"></el-table-column>
        <el-table-column prop="dataScope" label="数据范围" width="120">
          <template slot-scope="{ row }">
            <el-tag :type="row.dataScope === 'ALL' ? 'success' : 'warning'" size="small">
              {{ row.dataScope === 'ALL' ? '全部数据' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="allowedStocks" label="允许的股票" min-width="250">
          <template slot-scope="{ row }">
            <el-tag v-for="code in (row.allowedStockList || [])" :key="code" size="small" style="margin: 2px">
              {{ code }}
            </el-tag>
            <span v-if="!row.allowedStockList || row.allowedStockList.length === 0" style="color: #909399">无限制</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="{ row }">
            <el-button size="small" @click="handleEdit(row)" :disabled="!canEdit">
              <i class="el-icon-edit"></i> 配置
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :visible.sync="dialogVisible" :title="'配置数据权限 - ' + (currentUser ? currentUser.username : '')" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="数据范围">
          <el-radio-group v-model="form.dataScope">
            <el-radio label="ALL">全部数据</el-radio>
            <el-radio label="CUSTOM">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.dataScope === 'CUSTOM'" label="允许的股票">
          <el-select
            v-model="form.stockCodes"
            multiple
            filterable
            style="width: 100%"
            placeholder="请选择允许的股票代码">
            <el-option
              v-for="opt in stockOptions"
              :key="opt.code"
              :label="opt.code + ' - ' + opt.name"
              :value="opt.code"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :disabled="!canEdit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { dataPermApi, userApi } from '@/api/system'

export default {
  name: 'DataPermission',
  data() {
    return {
      loading: false,
      userList: [],
      stockOptions: [],
      dialogVisible: false,
      currentUser: null,
      form: {
        userId: null,
        dataScope: 'ALL',
        stockCodes: []
      }
    }
  },
  computed: {
    canEdit() { return this.$store.getters.hasPermission('dataperm:edit') }
  },
  mounted() {
    this.loadData()
    this.loadStockOptions()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await userApi.list({ pageNum: 1, pageSize: 100 })
        const users = res.list || []
        const dpList = await dataPermApi.list()
        const dpMap = new Map()
        for (const dp of (dpList.list || [])) {
          dpMap.set(dp.userId, dp)
        }
        this.userList = users.map(u => {
          const dp = dpMap.get(u.id)
          const stockCodes = dp && dp.allowedStockCodes ? dp.allowedStockCodes.split(',').map(s => s.trim()).filter(Boolean) : []
          return {
            ...u,
            allowedStockList: stockCodes,
            dataScope: u.dataScope || 'ALL'
          }
        })
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    async loadStockOptions() {
      try {
        const res = await dataPermApi.stockOptions()
        this.stockOptions = res || []
      } catch (e) {
        console.error(e)
      }
    },
    handleEdit(row) {
      this.currentUser = row
      this.form.userId = row.id
      this.form.dataScope = row.dataScope || 'ALL'
      this.form.stockCodes = row.allowedStockList || []
      this.dialogVisible = true
    },
    async handleSave() {
      try {
        await userApi.update(this.form.userId, {
          id: this.form.userId,
          dataScope: this.form.dataScope
        })
        if (this.form.dataScope === 'CUSTOM') {
          await dataPermApi.saveByUserId(this.form.userId, {
            userId: this.form.userId,
            allowedStockCodes: this.form.stockCodes.join(','),
            scope: 'CUSTOM'
          })
        } else {
          await dataPermApi.deleteByUserId(this.form.userId)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.loadData()
      } catch (e) {
        console.error(e)
      }
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
.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
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
