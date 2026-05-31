<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-s-custom"></i> 角色权限</h2>
        <el-button type="primary" @click="handleAdd" :disabled="!canAdd">
          <i class="el-icon-plus"></i> 新增角色
        </el-button>
      </div>

      <div class="stats-row">
        <el-card class="stat-card">
          <div class="stat-value">{{ roleList.length }}</div>
          <div class="stat-label">角色总数</div>
        </el-card>
        <el-card class="stat-card success">
          <div class="stat-value">{{ permissionCount }}</div>
          <div class="stat-label">权限总数</div>
        </el-card>
        <el-card class="stat-card warning">
          <div class="stat-value">{{ roleList.filter(r => r.userCount > 0).length }}</div>
          <div class="stat-label">已分配角色</div>
        </el-card>
      </div>

      <el-table :data="roleList" border stripe v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="角色名称" width="150"></el-table-column>
        <el-table-column prop="code" label="角色编码" width="150"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="userCount" label="用户数" width="100"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="220">
          <template slot-scope="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="handleAssign(row)" :disabled="!canAssign">
                <i class="el-icon-key"></i> 分配权限
              </el-button>
              <el-button
                size="small"
                type="danger"
                :disabled="row.code === 'ADMIN' || !canDelete"
                @click="handleDelete(row)">
                <i class="el-icon-delete"></i> 删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </span>
    </el-dialog>

    <el-dialog :visible.sync="permDialogVisible" title="分配权限" width="750px">
      <el-tabs v-model="activePermTab">
        <el-tab-pane label="菜单权限" name="menus">
          <el-tree
            :data="menuTree"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedMenuIds"
            ref="menuTreeRef"
            :props="{ label: 'name' }"></el-tree>
        </el-tab-pane>
        <el-tab-pane label="接口权限" name="permissions">
          <el-tree
            :data="permissionTree"
            show-checkbox
            node-key="id"
            :default-checked-keys="checkedPermIds"
            ref="permTreeRef"
            :props="{ label: 'name' }"></el-tree>
        </el-tab-pane>
      </el-tabs>
      <span slot="footer" class="dialog-footer">
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermSubmit">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { roleApi, menuApi, permissionApi } from '@/api/system'

export default {
  name: 'RolePermission',
  data() {
    return {
      loading: false,
      roleList: [],
      menuTree: [],
      permissionTree: [],
      permissionCount: 0,
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      form: {
        id: null,
        name: '',
        code: '',
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
        code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
      },
      permDialogVisible: false,
      activePermTab: 'menus',
      checkedMenuIds: [],
      checkedPermIds: [],
      currentRoleId: null
    }
  },
  computed: {
    canAdd() { return this.$store.getters.hasPermission('role:add') },
    canDelete() { return this.$store.getters.hasPermission('role:delete') },
    canAssign() { return this.$store.getters.hasPermission('role:assign') }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const [rolesRes, menusRes, permsRes] = await Promise.all([
          roleApi.list(),
          menuApi.tree(),
          permissionApi.tree()
        ])
        this.roleList = rolesRes.list || rolesRes.data || rolesRes
        this.menuTree = menusRes.list || menusRes.data || menusRes
        this.permissionTree = permsRes.list || permsRes.data || permsRes
        let count = 0
        for (const group of this.permissionTree) {
          count += (group.children || []).length
        }
        this.permissionCount = count
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      this.dialogTitle = '新增角色'
      this.isEdit = false
      Object.assign(this.form, { id: null, name: '', code: '', description: '' })
      this.dialogVisible = true
    },
    async handleSubmit() {
      await this.$refs.formRef.validate()
      try {
        if (this.isEdit) {
          await roleApi.update(this.form.id, this.form)
        } else {
          await roleApi.create(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    },
    async handleAssign(row) {
      this.currentRoleId = row.id
      this.checkedMenuIds = row.menuIds || []
      this.checkedPermIds = row.permissionIds || []
      try {
        const res = await roleApi.getPermissions(row.id)
        this.checkedMenuIds = res.menuIds || []
        this.checkedPermIds = res.permissionIds || []
      } catch (e) {
        console.error(e)
      }
      this.permDialogVisible = true
    },
    async handlePermSubmit() {
      const menuIds = this.$refs.menuTreeRef ? this.$refs.menuTreeRef.getCheckedKeys() : this.checkedMenuIds
      const permIds = this.$refs.permTreeRef ? this.$refs.permTreeRef.getCheckedKeys() : this.checkedPermIds
      try {
        await roleApi.assignPermissions(this.currentRoleId, {
          menuIds: menuIds,
          permissionIds: permIds
        })
        this.$message.success('权限分配成功')
        this.permDialogVisible = false
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    },
    handleDelete(row) {
      this.$confirm('确定删除角色 ' + row.name + '?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await roleApi.delete(row.id)
        this.$message.success('删除成功')
        this.loadData()
      }).catch(() => {})
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
.stats-row {
  display: flex;
  gap: 16px;
}
.stat-card {
  flex: 1;
  text-align: center;
}
.stat-card .stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
}
.stat-card.success .stat-value { color: #67C23A; }
.stat-card.warning .stat-value { color: #E6A23C; }
.stat-card .stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
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
</style>
