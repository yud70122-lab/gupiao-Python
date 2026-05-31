<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-user"></i> 用户管理</h2>
        <div class="header-actions">
          <el-input
            v-model="searchForm.keyword"
            placeholder="搜索用户"
            style="width: 200px; margin-right: 10px"
            clearable
            @keyup.enter.native="loadData">
            <i slot="prefix" class="el-icon-search"></i>
          </el-input>
          <el-button type="primary" @click="handleAdd" :disabled="!canAdd">
            <i class="el-icon-plus"></i> 新增用户
          </el-button>
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="realName" label="姓名" width="100"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="roleNames" label="角色" width="180">
          <template slot-scope="{ row }">
            <el-tag v-for="name in (row.roleNames || [row.role])" :key="name" :type="getRoleTagType(name)" size="small" style="margin-right: 4px">
              {{ name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataScope" label="数据范围" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.dataScope === 'ALL' ? 'success' : 'warning'" size="small">
              {{ row.dataScope === 'ALL' ? '全部' : '自定义' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template slot-scope="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="handleToggle(row)"
              :disabled="row.username === 'admin' || !canToggle"></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="handleEdit(row)" :disabled="!canEdit">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
              <el-button
                size="small"
                type="danger"
                :disabled="row.username === 'admin' || !canDelete"
                @click="handleDelete(row)">
                <i class="el-icon-delete"></i> 删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        :current-page.sync="pageNum"
        :page-size.sync="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; text-align: right"></el-pagination>
    </el-card>

    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="650px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple style="width: 100%" placeholder="请选择角色">
            <el-option v-for="r in roleOptions" :key="r.id" :label="r.name" :value="r.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-radio-group v-model="form.dataScope">
            <el-radio label="ALL">全部数据</el-radio>
            <el-radio label="CUSTOM">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled"></el-switch>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { userApi, roleApi } from '@/api/system'

export default {
  name: 'UserManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      searchForm: { keyword: '' },
      roleOptions: [],
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      form: {
        id: null,
        username: '',
        realName: '',
        email: '',
        phone: '',
        roleIds: [],
        dataScope: 'ALL',
        password: '',
        enabled: true
      },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
        email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
        roleIds: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  computed: {
    canAdd() { return this.$store.getters.hasPermission('user:add') },
    canEdit() { return this.$store.getters.hasPermission('user:edit') },
    canDelete() { return this.$store.getters.hasPermission('user:delete') },
    canToggle() { return this.$store.getters.hasPermission('user:toggle') }
  },
  mounted() {
    this.loadData()
    this.loadRoles()
  },
  methods: {
    getRoleTagType(role) {
      const types = { '管理员': 'danger', '超级管理员': 'danger', '分析师': 'warning', '访客': 'info' }
      return types[role] || 'info'
    },
    async loadData() {
      this.loading = true
      try {
        const res = await userApi.list({
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          keyword: this.searchForm.keyword
        })
        this.tableData = res.list || res.data || res
        this.total = res.total || this.tableData.length
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    async loadRoles() {
      try {
        const res = await roleApi.all()
        this.roleOptions = res
      } catch (e) {
        console.error(e)
      }
    },
    handleAdd() {
      this.dialogTitle = '新增用户'
      this.isEdit = false
      Object.assign(this.form, { id: null, username: '', realName: '', email: '', phone: '', roleIds: [], dataScope: 'ALL', password: '', enabled: true })
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑用户'
      this.isEdit = true
      Object.assign(this.form, { ...row, password: '', roleIds: row.roleIds || [] })
      this.dialogVisible = true
    },
    async handleSubmit() {
      await this.$refs.formRef.validate()
      try {
        if (this.isEdit) {
          await userApi.update(this.form.id, this.form)
        } else {
          await userApi.create(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    },
    handleDelete(row) {
      this.$confirm('确定删除用户 ' + row.username + '?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await userApi.delete(row.id)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
    },
    async handleToggle(row) {
      try {
        await userApi.toggleStatus(row.id)
        this.$message.success('状态更新成功')
      } catch (e) {
        row.enabled = !row.enabled
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
  align-items: center;
}
.action-buttons {
  display: flex;
  flex-wrap: wrap;
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
