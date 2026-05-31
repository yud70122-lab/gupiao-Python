<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-time"></i> 定时更新任务</h2>
        <el-button type="primary" @click="handleAdd">
          <i class="el-icon-plus"></i> 新增任务
        </el-button>
      </div>

      <div class="stats-row">
        <el-card class="stat-card">
          <div class="stat-value">{{ taskList.length }}</div>
          <div class="stat-label">任务总数</div>
        </el-card>
        <el-card class="stat-card success">
          <div class="stat-value">{{ runningCount }}</div>
          <div class="stat-label">运行中</div>
        </el-card>
        <el-card class="stat-card warning">
          <div class="stat-value">{{ stoppedCount }}</div>
          <div class="stat-label">已停止</div>
        </el-card>
      </div>

      <el-table :data="taskList" border stripe v-loading="loading" style="margin-top: 20px">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="name" label="任务名称" width="200"></el-table-column>
        <el-table-column prop="cron" label="Cron表达式" width="150"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="{ row }">
            <el-tag :type="row.status === '运行中' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastExecuteTime" label="上次执行时间" width="180"></el-table-column>
        <el-table-column label="操作" width="280">
          <template slot-scope="{ row }">
            <div class="action-buttons">
              <el-button
                v-if="row.status === '运行中'"
                size="small"
                type="warning"
                @click="handleToggle(row)">
                <i class="el-icon-video-pause"></i> 暂停
              </el-button>
              <el-button
                v-else
                size="small"
                type="success"
                @click="handleToggle(row)">
                <i class="el-icon-video-play"></i> 启动
              </el-button>
              <el-button size="small" @click="handleExecute(row)">
                <i class="el-icon-refresh"></i> 执行
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">
                <i class="el-icon-delete"></i> 删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name"></el-input>
        </el-form-item>
        <el-form-item label="任务分组">
          <el-select v-model="form.group" style="width: 100%">
            <el-option label="股票数据" value="STOCK"></el-option>
            <el-option label="系统任务" value="SYSTEM"></el-option>
            <el-option label="数据清理" value="CLEANUP"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Cron表达式" prop="cron">
          <el-input v-model="form.cron" placeholder="如: 0 0 2 * * ?"></el-input>
        </el-form-item>
        <el-form-item label="目标方法">
          <el-input v-model="form.targetMethod"></el-input>
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
  </div>
</template>

<script>
import { taskApi } from '@/api/system'

export default {
  name: 'ScheduledTask',
  data() {
    return {
      loading: false,
      taskList: [],
      dialogVisible: false,
      dialogTitle: '',
      form: {
        id: null,
        name: '',
        group: 'STOCK',
        cron: '',
        targetMethod: '',
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
        cron: [{ required: true, message: '请输入Cron表达式', trigger: 'blur' }]
      }
    }
  },
  computed: {
    runningCount() { return this.taskList.filter(t => t.status === '运行中').length },
    stoppedCount() { return this.taskList.filter(t => t.status !== '运行中').length }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await taskApi.list()
        this.taskList = res.list || res.data || res
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      this.dialogTitle = '新增任务'
      Object.assign(this.form, { id: null, name: '', group: 'STOCK', cron: '', targetMethod: '', description: '' })
      this.dialogVisible = true
    },
    async handleSubmit() {
      await this.$refs.formRef.validate()
      try {
        if (this.form.id) {
          await taskApi.update(this.form.id, this.form)
        } else {
          await taskApi.create(this.form)
        }
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.loadData()
      } catch (e) {
        console.error(e)
      }
    },
    async handleToggle(row) {
      try {
        if (row.status === '运行中') {
          await taskApi.stop(row.id)
          row.status = '已停止'
          this.$message.success('任务已暂停')
        } else {
          await taskApi.start(row.id)
          row.status = '运行中'
          this.$message.success('任务已启动')
        }
      } catch (e) {
        console.error(e)
      }
    },
    async handleExecute(row) {
      this.$message.info('正在执行任务: ' + row.name)
      try {
        await taskApi.execute(row.id)
        row.lastExecuteTime = new Date().toLocaleString()
        this.$message.success('任务执行成功')
      } catch (e) {
        console.error(e)
      }
    },
    handleDelete(row) {
      this.$confirm('确定删除任务 ' + row.name + '?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await taskApi.delete(row.id)
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
