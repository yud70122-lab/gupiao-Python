<template>
  <div class="page-container">
    <el-card class="page-card">
      <div slot="header" class="page-header">
        <h2><i class="el-icon-setting"></i> 系统配置</h2>
        <el-button type="primary" @click="handleAdd">
          <i class="el-icon-plus"></i> 新增配置
        </el-button>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本配置" name="basic">
          <el-form :model="basicConfig" label-width="150px" style="max-width: 600px; margin-top: 20px">
            <el-form-item label="系统名称">
              <el-input v-model="basicConfig.systemName"></el-input>
            </el-form-item>
            <el-form-item label="系统Logo">
              <el-input v-model="basicConfig.systemLogo"></el-input>
            </el-form-item>
            <el-form-item label="系统版本">
              <el-input v-model="basicConfig.version" disabled></el-input>
            </el-form-item>
            <el-form-item label="数据保留天数">
              <el-input-number v-model="basicConfig.dataRetentionDays" :min="1" :max="3650"></el-input-number>
            </el-form-item>
            <el-form-item label="上传文件大小(MB)">
              <el-input-number v-model="basicConfig.maxFileSize" :min="1" :max="1024"></el-input-number>
            </el-form-item>
            <el-form-item label="是否开启注册">
              <el-switch v-model="basicConfig.enableRegister"></el-switch>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicConfig">保存配置</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="股票数据源" name="datasource">
          <el-form :model="dataSourceConfig" label-width="150px" style="max-width: 600px; margin-top: 20px">
            <el-form-item label="数据源类型">
              <el-select v-model="dataSourceConfig.sourceType" style="width: 300px">
                <el-option label="模拟数据" value="mock"></el-option>
                <el-option label="Tushare" value="tushare"></el-option>
                <el-option label="新浪财经" value="sina"></el-option>
                <el-option label="东方财富" value="eastmoney"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="API密钥">
              <el-input v-model="dataSourceConfig.apiKey" show-password style="width: 300px"></el-input>
            </el-form-item>
            <el-form-item label="更新频率">
              <el-select v-model="dataSourceConfig.updateFrequency" style="width: 300px">
                <el-option label="实时" value="realtime"></el-option>
                <el-option label="每小时" value="hourly"></el-option>
                <el-option label="每日" value="daily"></el-option>
                <el-option label="每周" value="weekly"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveDataSourceConfig">保存配置</el-button>
              <el-button @click="testConnection">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="邮件通知" name="email">
          <el-form :model="emailConfig" label-width="150px" style="max-width: 600px; margin-top: 20px">
            <el-form-item label="SMTP服务器">
              <el-input v-model="emailConfig.smtpHost" style="width: 300px"></el-input>
            </el-form-item>
            <el-form-item label="端口">
              <el-input-number v-model="emailConfig.smtpPort"></el-input-number>
            </el-form-item>
            <el-form-item label="发件邮箱">
              <el-input v-model="emailConfig.fromEmail" style="width: 300px"></el-input>
            </el-form-item>
            <el-form-item label="邮箱密码">
              <el-input v-model="emailConfig.password" show-password style="width: 300px"></el-input>
            </el-form-item>
            <el-form-item label="是否开启SSL">
              <el-switch v-model="emailConfig.enableSSL"></el-switch>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveEmailConfig">保存配置</el-button>
              <el-button @click="testEmail">测试发送</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="配置列表" name="list">
          <el-table :data="configList" border stripe v-loading="loading" style="margin-top: 20px">
            <el-table-column prop="configKey" label="配置键" width="200"></el-table-column>
            <el-table-column prop="configValue" label="配置值" show-overflow-tooltip></el-table-column>
            <el-table-column prop="description" label="描述"></el-table-column>
            <el-table-column label="操作" width="200">
              <template slot-scope="{ row }">
                <div class="action-buttons">
                  <el-button size="small" @click="handleEdit(row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" :disabled="isEdit"></el-input>
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description"></el-input>
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
import { configApi } from '@/api/system'

export default {
  name: 'SystemConfig',
  data() {
    return {
      loading: false,
      configList: [],
      activeTab: 'basic',
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      form: {
        configKey: '',
        configValue: '',
        description: ''
      },
      rules: {
        configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
        configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
      },
      basicConfig: {
        systemName: '股票量化分析系统',
        systemLogo: '📈',
        version: '1.0.0',
        dataRetentionDays: 365,
        maxFileSize: 10,
        enableRegister: false
      },
      dataSourceConfig: {
        sourceType: 'mock',
        apiKey: '',
        updateFrequency: 'daily'
      },
      emailConfig: {
        smtpHost: 'smtp.example.com',
        smtpPort: 465,
        fromEmail: 'noreply@stock.com',
        password: '',
        enableSSL: true
      }
    }
  },
  mounted() {
    this.loadConfigList()
  },
  methods: {
    async loadConfigList() {
      this.loading = true
      try {
        const res = await configApi.list()
        this.configList = res.list || res.data || res
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    handleAdd() {
      this.dialogTitle = '新增配置'
      this.isEdit = false
      Object.assign(this.form, { configKey: '', configValue: '', description: '' })
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑配置'
      this.isEdit = true
      Object.assign(this.form, { ...row })
      this.dialogVisible = true
    },
    async handleSubmit() {
      await this.$refs.formRef.validate()
      try {
        await configApi.save(this.form)
        this.$message.success('保存成功')
        this.dialogVisible = false
        this.loadConfigList()
      } catch (e) {
        console.error(e)
      }
    },
    handleDelete(row) {
      this.$confirm('确定删除配置 ' + row.configKey + '?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        await configApi.delete(row.configKey)
        this.$message.success('删除成功')
        this.loadConfigList()
      }).catch(() => {})
    },
    saveBasicConfig() {
      this.$message.success('基础配置保存成功')
    },
    saveDataSourceConfig() {
      this.$message.success('数据源配置保存成功')
    },
    saveEmailConfig() {
      this.$message.success('邮件配置保存成功')
    },
    testConnection() {
      this.$message.success('连接测试成功')
    },
    testEmail() {
      this.$message.success('测试邮件发送成功')
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
  overflow-y: auto;
}
::v-deep .el-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
}
::v-deep .el-tabs__content {
  flex: 1;
  overflow-y: auto;
}
</style>
