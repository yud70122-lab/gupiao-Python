<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="avatar-card">
          <div class="avatar-wrapper">
            <el-avatar :size="120" :src="userInfo.avatar || defaultAvatar">
              {{ (userInfo.realName || 'U').charAt(0) }}
            </el-avatar>
            <h3>{{ userInfo.realName || userInfo.username }}</h3>
            <p class="role-tag">
              <el-tag type="primary">{{ userInfo.role || '访客' }}</el-tag>
            </p>
          </div>
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="uploadAvatar">
            <el-button size="small" type="primary">更换头像</el-button>
          </el-upload>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="基本信息" name="basic">
              <el-form :model="profileForm" label-width="80px">
                <el-form-item label="用户名">
                  <el-input v-model="profileForm.username" disabled></el-input>
                </el-form-item>
                <el-form-item label="真实姓名">
                  <el-input v-model="profileForm.realName"></el-input>
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email"></el-input>
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone"></el-input>
                </el-form-item>
                <el-form-item label="注册时间">
                  <el-input :value="profileForm.createTime" disabled></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile" :loading="loading">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="修改密码" name="password">
              <el-form :model="passwordForm" label-width="100px">
                <el-form-item label="原密码">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="新密码">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item label="确认密码">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword" :loading="loading">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="安全设置" name="security">
              <el-table :data="securityList" border>
                <el-table-column prop="name" label="安全项" width="150"></el-table-column>
                <el-table-column prop="status" label="状态">
                  <template slot-scope="{ row }">
                    <el-tag :type="row.status === '已启用' ? 'success' : 'info'">
                      {{ row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="desc" label="说明"></el-table-column>
                <el-table-column label="操作" width="120">
                  <template slot-scope="{ row }">
                    <el-button size="small" type="primary">{{ row.action }}</el-button>
                  </template>
                </el-table-column>
              </el-table>

              <el-alert
                v-if="showLocationWarning"
                title="异地登录提醒"
                type="warning"
                :closable="false"
                class="mt20">
                <p>检测到您的账号在新的设备或地点登录，IP: {{ lastLoginIp }}</p>
                <p>如果不是您本人操作，请立即修改密码或联系管理员。</p>
              </el-alert>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { authApi } from '@/api/auth'

export default {
  name: 'Profile',
  data() {
    return {
      activeTab: 'basic',
      loading: false,
      showLocationWarning: false,
      lastLoginIp: '',
      defaultAvatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTIwIiBoZWlnaHQ9IjEyMCIgdmlld0JveD0iMCAwIDEyMCAxMjAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIxMjAiIGhlaWdodD0iMTIwIiBmaWxsPSIjNDA5RUZGIi8+Cjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkb21pbmFudC1iYXNlbGluZT0ibWlkZGxlIiBmaWxsPSJ3aGl0ZSIgZm9udC1zaXplPSI0MCIgZm9udC1mYW1pbHk9IkFyaWFsIj5VPC90ZXh0Pgo8L3N2Zz4=',
      profileForm: {
        username: '',
        realName: '',
        email: '',
        phone: '',
        createTime: '',
        avatar: ''
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      securityList: [
        { name: '单账号互斥登录', status: '已启用', desc: '同一时间只能在一个设备登录，新登录会使旧会话失效', action: '查看' },
        { name: '异地登录提醒', status: '已启用', desc: '检测到非常用IP登录时发送提醒通知', action: '设置' },
        { name: '密码强度检测', status: '已启用', desc: '要求密码长度至少6位，包含数字和字母', action: '设置' }
      ]
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.userInfo || {}
    }
  },
  async mounted() {
    try {
      const profile = await authApi.getProfile()
      Object.assign(this.profileForm, {
        username: profile.username,
        realName: profile.realName || '',
        email: profile.email || '',
        phone: profile.phone || '',
        createTime: profile.createTime || ''
      })
      this.lastLoginIp = profile.lastLoginIp || ''
    } catch (e) {
      console.error('Failed to load profile:', e)
    }
  },
  methods: {
    beforeAvatarUpload(file) {
      const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
      const isLt2M = file.size / 1024 / 1024 < 2
      if (!isJPG) {
        this.$message.error('头像只能是 JPG 或 PNG 格式!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('头像大小不能超过 2MB!')
        return false
      }
      return true
    },
    uploadAvatar({ file }) {
      const reader = new FileReader()
      reader.onload = (e) => {
        this.profileForm.avatar = e.target.result
        this.$store.commit('SET_USER_INFO', { ...this.userInfo, avatar: e.target.result })
        this.$message.success('头像更新成功')
      }
      reader.readAsDataURL(file)
    },
    async saveProfile() {
      try {
        this.loading = true
        const res = await authApi.updateProfile({
          realName: this.profileForm.realName,
          email: this.profileForm.email,
          phone: this.profileForm.phone
        })
        this.$store.commit('SET_USER_INFO', { ...this.userInfo, ...res.user })
        this.$message.success('资料更新成功')
      } catch (e) {
        this.$message.error(e.message || '保存失败')
      } finally {
        this.loading = false
      }
    },
    async changePassword() {
      if (!this.passwordForm.oldPassword || !this.passwordForm.newPassword || !this.passwordForm.confirmPassword) {
        this.$message.warning('请填写完整密码信息')
        return
      }
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        this.$message.warning('两次输入的密码不一致')
        return
      }
      if (this.passwordForm.newPassword.length < 6) {
        this.$message.warning('密码长度至少6位')
        return
      }
      try {
        this.loading = true
        await authApi.changePassword({
          oldPassword: this.passwordForm.oldPassword,
          newPassword: this.passwordForm.newPassword
        })
        this.$message.success('密码修改成功')
        this.passwordForm.oldPassword = ''
        this.passwordForm.newPassword = ''
        this.passwordForm.confirmPassword = ''
      } catch (e) {
        this.$message.error(e.message || '修改失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.avatar-card {
  text-align: center;
}
.avatar-wrapper {
  margin-bottom: 20px;
}
.avatar-wrapper h3 {
  margin: 12px 0 8px;
}
.role-tag {
  margin: 0;
}
.avatar-uploader {
  margin-top: 15px;
}
.mt20 {
  margin-top: 20px;
}
</style>
