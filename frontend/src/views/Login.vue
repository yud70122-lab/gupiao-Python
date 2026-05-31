<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <h2>股票量化分析系统</h2>
        <p>欢迎回来，请登录您的账号</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginForm" :model="loginForm" :rules="loginRules" label-width="80px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
            </el-form-item>
            <div class="login-footer">
              <el-link type="primary" @click="activeTab = 'register'">立即注册</el-link>
              <el-link type="primary" @click="activeTab = 'reset'">忘记密码？</el-link>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerForm" :model="registerForm" :rules="registerRules" label-width="80px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password></el-input>
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="registerForm.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <el-input v-model="registerForm.code" placeholder="请输入验证码" style="width: 60%">
                <el-button slot="append" @click="sendCode('phone')" :disabled="codeDisabled">{{ codeText }}</el-button>
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="handleRegister" :loading="loading">注册</el-button>
            </el-form-item>
            <div class="login-footer">
              <el-link type="primary" @click="activeTab = 'login'">已有账号，去登录</el-link>
            </div>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="找回密码" name="reset">
          <el-form ref="resetForm" :model="resetForm" :rules="resetRules" label-width="80px">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="resetForm.phone" placeholder="请输入注册手机号"></el-input>
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <el-input v-model="resetForm.code" placeholder="请输入验证码" style="width: 60%">
                <el-button slot="append" @click="sendCode('reset')" :disabled="codeDisabled">{{ codeText }}</el-button>
              </el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="resetForm.newPassword" type="password" placeholder="请输入新密码" show-password></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="handleReset" :loading="loading">重置密码</el-button>
            </el-form-item>
            <div class="login-footer">
              <el-link type="primary" @click="activeTab = 'login'">返回登录</el-link>
            </div>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { authApi } from '@/api/auth'

export default {
  name: 'Login',
  data() {
    return {
      activeTab: 'login',
      loading: false,
      codeDisabled: false,
      codeText: '获取验证码',
      loginForm: {
        username: 'admin',
        password: '123'
      },
      registerForm: {
        username: '',
        password: '',
        phone: '',
        code: ''
      },
      resetForm: {
        phone: '',
        code: '',
        newPassword: ''
      },
      loginRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      registerRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
      },
      resetRules: {
        phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
        code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
        newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
      }
    }
  },
  methods: {
    async sendCode(type) {
      const phone = type === 'phone' ? this.registerForm.phone : this.resetForm.phone
      if (!phone) {
        this.$message.warning('请先输入手机号')
        return
      }
      try {
        this.codeDisabled = true
        const res = await authApi.sendCode({ phone })
        this.$message.success('验证码已发送: ' + res.code)
        let count = 60
        this.codeText = count + '秒后重发'
        const timer = setInterval(() => {
          count--
          if (count <= 0) {
            clearInterval(timer)
            this.codeDisabled = false
            this.codeText = '获取验证码'
          } else {
            this.codeText = count + '秒后重发'
          }
        }, 1000)
      } catch (e) {
        this.codeDisabled = false
        this.$message.error(e.message || '发送失败')
      }
    },
    async handleLogin() {
      try {
        this.loading = true
        const res = await this.$store.dispatch('login', this.loginForm)
        if (res.differentLocation) {
          this.$message.warning('检测到异地登录，请确认是否为本人操作')
        }
        this.$message.success('登录成功')
        this.$router.push('/analysis/stock')
      } catch (e) {
        this.$message.error(e.message || '登录失败')
      } finally {
        this.loading = false
      }
    },
    async handleRegister() {
      try {
        this.loading = true
        await authApi.register(this.registerForm)
        this.$message.success('注册成功，请登录')
        this.activeTab = 'login'
        this.loginForm.username = this.registerForm.username
        this.loginForm.password = this.registerForm.password
      } catch (e) {
        this.$message.error(e.message || '注册失败')
      } finally {
        this.loading = false
      }
    },
    async handleReset() {
      try {
        this.loading = true
        await authApi.resetPassword(this.resetForm)
        this.$message.success('密码重置成功，请登录')
        this.activeTab = 'login'
      } catch (e) {
        this.$message.error(e.message || '重置失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 450px;
  padding: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}
.login-header {
  text-align: center;
  margin-bottom: 20px;
}
.login-header h2 {
  margin: 0 0 8px;
  color: #303133;
}
.login-header p {
  margin: 0;
  color: #909399;
}
.login-btn {
  width: 100%;
}
.login-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}
</style>
