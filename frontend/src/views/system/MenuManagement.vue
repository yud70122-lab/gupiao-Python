<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-menu"></i> 菜单管理</h2>
        <el-button type="primary" @click="handleAdd" :disabled="!canEdit">
          <i class="el-icon-plus"></i> 新增菜单
        </el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" row-key="id" :tree-props="{ children: 'children' }">
        <el-table-column prop="name" label="菜单名称" min-width="180"></el-table-column>
        <el-table-column prop="path" label="路径" width="200"></el-table-column>
        <el-table-column prop="component" label="组件" width="200"></el-table-column>
        <el-table-column prop="icon" label="图标" width="100"></el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80"></el-table-column>
        <el-table-column prop="permission" label="权限标识" width="150"></el-table-column>
        <el-table-column prop="visible" label="可见" width="80">
          <template slot-scope="{ row }">
            <el-tag :type="row.visible ? 'success' : 'info'" size="small">
              {{ row.visible ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="{ row }">
            <div class="action-buttons">
              <el-button size="small" @click="handleAddChild(row)" :disabled="!canEdit">
                <i class="el-icon-plus"></i> 子菜单
              </el-button>
              <el-button size="small" @click="handleEdit(row)" :disabled="!canEdit">
                <i class="el-icon-edit"></i> 编辑
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="550px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="路径" prop="path">
          <el-input v-model="form.path" placeholder="/example/path"></el-input>
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="form.component" placeholder="views/Example"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="Menu"></el-input>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999"></el-input-number>
        </el-form-item>
        <el-form-item label="权限标识">
          <el-input v-model="form.permission" placeholder="stock:view"></el-input>
        </el-form-item>
        <el-form-item label="是否可见">
          <el-switch v-model="form.visible"></el-switch>
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
import { menuApi } from '@/api/system'

export default {
  name: 'MenuManagement',
  data() {
    return {
      loading: false,
      tableData: [],
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        name: '',
        path: '',
        component: '',
        icon: '',
        sortOrder: 0,
        parentId: null,
        permission: '',
        visible: true
      },
      rules: {
        name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
        path: [{ required: true, message: '请输入路径', trigger: 'blur' }]
      }
    }
  },
  computed: {
    canEdit() { return this.$store.getters.hasPermission('menu:edit') }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await menuApi.list()
        this.tableData = res.list || []
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      this.dialogTitle = '新增菜单'
      Object.assign(this.form, { id: null, name: '', path: '', component: '', icon: '', sortOrder: 0, parentId: null, permission: '', visible: true })
      this.dialogVisible = true
    },
    handleAddChild(row) {
      this.dialogTitle = '新增子菜单'
      Object.assign(this.form, { id: null, name: '', path: '', component: '', icon: '', sortOrder: 0, parentId: row.id, permission: '', visible: true })
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑菜单'
      Object.assign(this.form, row)
      this.dialogVisible = true
    },
    async handleSubmit() {
      await this.$refs.formRef.validate()
      try {
        if (this.form.id) {
          await menuApi.update(this.form.id, this.form)
        } else {
          await menuApi.create(this.form)
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
