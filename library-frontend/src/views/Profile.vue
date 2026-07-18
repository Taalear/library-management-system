<template>
  <div style="padding: 0;">
    <!-- 加载中 -->
    <div v-if="loading" style="text-align: center; padding: 60px 0;">
      <el-icon class="is-loading" style="font-size: 32px;"><Loading /></el-icon>
      <p style="margin-top: 12px; color: #86909c;">加载中...</p>
    </div>

    <!-- 加载失败 -->
    <div v-else-if="error" style="text-align: center; padding: 60px 0;">
      <el-icon style="font-size: 32px; color: #f56c6c;"><CircleClose /></el-icon>
      <p style="margin-top: 12px; color: #f56c6c;">{{ error }}</p>
      <el-button type="primary" size="small" @click="loadProfile" style="margin-top: 16px;">重新加载</el-button>
    </div>

    <!-- 正常显示 -->
    <template v-else-if="profile && profile.id">
      <div style="margin-bottom: 24px;">
        <h3 style="margin: 0; font-size: 20px; font-weight: 600; color: #3c3a47;">👤 个人中心</h3>
      </div>

      <el-row :gutter="20">
        <el-col :span="16">
          <el-card shadow="hover" style="border-radius: 16px;">
            <template #header>
              <span style="font-weight: 600; color: #3c3a47;">📋 个人信息</span>
            </template>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px 40px;">
              <div>
                <div style="font-size: 13px; color: #86909c;">用户名</div>
                <div style="font-size: 16px; font-weight: 500; color: #3c3a47; margin-top: 4px;">
                  {{ profile.username || '-' }}
                </div>
              </div>
              <div>
                <div style="font-size: 13px; color: #86909c;">角色</div>
                <div style="font-size: 16px; font-weight: 500; color: #3c3a47; margin-top: 4px;">
                  <el-tag :type="profile.role === 'ADMIN' ? 'danger' : 'info'" size="small">
                    {{ profile.role === 'ADMIN' ? '管理员' : '普通用户' }}
                  </el-tag>
                </div>
              </div>
              <div>
                <div style="font-size: 13px; color: #86909c;">昵称</div>
                <div style="font-size: 16px; font-weight: 500; color: #3c3a47; margin-top: 4px;">
                  {{ profile.nickname || '未设置' }}
                </div>
              </div>
              <div>
                <div style="font-size: 13px; color: #86909c;">用户 ID</div>
                <div style="font-size: 16px; font-weight: 500; color: #3c3a47; margin-top: 4px;">
                  {{ profile.id }}
                </div>
              </div>
            </div>

            <!-- 头像上传 -->
            <el-divider style="margin: 20px 0 16px 0;" />
            <div style="display: flex; align-items: center; gap: 20px;">
              <div>
                <div style="font-size: 14px; color: #3c3a47; font-weight: 500; margin-bottom: 8px;">头像</div>
                <el-upload
                  class="avatar-uploader"
                  action="/api/user/avatar"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :on-error="handleAvatarError"
                  :before-upload="beforeAvatarUpload"
                >
                  <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </div>
              <div style="color: #86909c; font-size: 13px;">
                支持 JPG/PNG，大小不超过 2MB
              </div>
            </div>

            <!-- 修改昵称 -->
            <el-divider style="margin: 20px 0 16px 0;" />
            <div style="display: flex; align-items: center; gap: 16px;">
              <span style="font-size: 14px; color: #3c3a47; font-weight: 500;">修改昵称</span>
              <el-input
                v-model="nicknameForm.nickname"
                placeholder="输入新昵称"
                style="width: 200px;"
                maxlength="20"
                clearable
                @keyup.enter="handleUpdateNickname"
              />
              <el-button type="primary" size="small" @click="handleUpdateNickname">更新</el-button>
            </div>

            <!-- 修改密码（带强度指示） -->
            <el-divider style="margin: 20px 0 16px 0;" />
            <div style="display: flex; flex-direction: column; gap: 12px; max-width: 360px;">
              <span style="font-size: 14px; color: #3c3a47; font-weight: 500;">修改密码</span>
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（至少6位）"
                show-password
                @input="checkPasswordStrength"
              />
              <div style="margin-top: -4px; margin-bottom: 4px;">
                <el-progress
                  v-if="passwordForm.newPassword"
                  :percentage="passwordStrength.percent"
                  :color="passwordStrength.color"
                  :format="() => passwordStrength.text"
                />
              </div>
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
              <span v-if="passwordForm.confirmPassword && passwordForm.newPassword !== passwordForm.confirmPassword" style="color: #f56c6c; font-size: 12px;">两次密码不一致</span>
              <span v-else-if="passwordForm.confirmPassword && passwordForm.newPassword === passwordForm.confirmPassword" style="color: #67c23a; font-size: 12px;">✓ 匹配</span>
              <el-button type="primary" @click="handleUpdatePassword" style="align-self: flex-start;">
                修改密码
              </el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card shadow="hover" style="border-radius: 16px; text-align: center;">
            <template #header>
              <span style="font-weight: 600; color: #3c3a47;">📊 借阅统计</span>
            </template>

            <div style="display: flex; flex-direction: column; gap: 20px; padding: 8px 0;">
              <div>
                <div style="font-size: 12px; color: #86909c;">累计借阅</div>
                <div style="font-size: 36px; font-weight: 700; color: #b39ddb; margin-top: 4px;">
                  {{ profile.borrowTotal || 0 }}
                </div>
              </div>
              <el-divider style="margin: 0;" />
              <div>
                <div style="font-size: 12px; color: #86909c;">当前借出中</div>
                <div style="font-size: 36px; font-weight: 700; color: #f56c6c; margin-top: 4px;">
                  {{ profile.borrowActive || 0 }}
                </div>
              </div>
              <el-divider style="margin: 0;" />
              <div>
                <div style="font-size: 12px; color: #86909c;">已归还</div>
                <div style="font-size: 36px; font-weight: 700; color: #67c23a; margin-top: 4px;">
                  {{ (profile.borrowTotal || 0) - (profile.borrowActive || 0) }}
                </div>
              </div>
            </div>

            <div style="margin-top: 16px; display: flex; gap: 12px; justify-content: center;">
              <el-button size="small" @click="router.push('/books')">📖 去借书</el-button>
              <el-button size="small" @click="router.push('/user/my-borrow')">📋 查看借阅记录</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 无数据（兜底） -->
    <div v-else style="text-align: center; padding: 60px 0; color: #86909c;">
      <p>暂无个人信息</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Loading, CircleClose } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { appState } from '@/utils/state'

const router = useRouter()
const loading = ref(false)
const error = ref('')
const profile = ref({})
const avatarUrl = ref(localStorage.getItem('avatar') || '')
const nicknameForm = reactive({ nickname: '' })
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordStrength = reactive({
  percent: 0,
  text: '弱',
  color: '#f56c6c'
})

const checkPasswordStrength = () => {
  const pwd = passwordForm.newPassword
  if (!pwd) {
    passwordStrength.percent = 0
    passwordStrength.text = ''
    return
  }
  let score = 0
  if (pwd.length >= 6) score += 20
  if (pwd.length >= 10) score += 20
  if (/[a-z]/.test(pwd)) score += 20
  if (/[A-Z]/.test(pwd)) score += 20
  if (/\d/.test(pwd)) score += 20
  if (/[^a-zA-Z0-9]/.test(pwd)) score += 20
  if (score >= 80) {
    passwordStrength.percent = 100
    passwordStrength.text = '强'
    passwordStrength.color = '#67c23a'
  } else if (score >= 50) {
    passwordStrength.percent = 60
    passwordStrength.text = '中'
    passwordStrength.color = '#e6a23c'
  } else {
    passwordStrength.percent = 30
    passwordStrength.text = '弱'
    passwordStrength.color = '#f56c6c'
  }
}

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token') || ''
  return { Authorization: `Bearer ${token}` }
})

const loadProfile = async () => {
  loading.value = true
  error.value = ''
  try {
    const res = await request.get('/api/user/profile')
    if (res.code === 200) {
      profile.value = res.data
      if (res.data.nickname) {
        localStorage.setItem('username', res.data.nickname)
      }
    } else {
      error.value = res.msg || '加载个人信息失败'
    }
  } catch (err) {
    error.value = '网络异常，请稍后重试'
    console.error(err)
  } finally {
    loading.value = false
  }
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    const url = res.data + '?t=' + Date.now()
    avatarUrl.value = url
    localStorage.setItem('avatar', url)
    appState.avatar = url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

const handleAvatarError = () => {
  ElMessage.error('头像上传失败，请检查网络')
}

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isJPG) ElMessage.error('仅支持 JPG/PNG 格式')
  if (!isLt2M) ElMessage.error('图片大小不能超过 2MB')
  return isJPG && isLt2M
}

const handleUpdateNickname = async () => {
  if (!nicknameForm.nickname.trim()) {
    ElMessage.warning('请输入新昵称')
    return
  }
  try {
    const res = await request.put('/api/user/nickname', {
      nickname: nicknameForm.nickname.trim()
    })
    if (res.code === 200) {
      ElMessage.success('昵称修改成功')
      profile.value.nickname = nicknameForm.nickname.trim()
      localStorage.setItem('username', nicknameForm.nickname.trim())
      nicknameForm.nickname = ''
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  }
}

const handleUpdatePassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm

  if (!oldPassword) {
    ElMessage.warning('请输入原密码')
    return
  }
  if (!newPassword || newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return
  }
  if (newPassword !== confirmPassword) {
    ElMessage.warning('两次输入密码不一致')
    return
  }

  try {
    const res = await request.put('/api/user/password', passwordForm)
    if (res.code === 200) {
      ElMessageBox.alert('密码修改成功，请重新登录', '提示', {
        confirmButtonText: '去登录',
        callback: () => {
          localStorage.removeItem('token')
          localStorage.removeItem('role')
          localStorage.removeItem('username')
          localStorage.removeItem('avatar')
          appState.avatar = ''
          router.push('/login')
        }
      })
    } else {
      ElMessage.error(res.msg || '修改失败')
    }
  } catch {
    ElMessage.error('网络异常，请稍后重试')
  }
}

onMounted(() => {
  loadProfile()
  const avatar = localStorage.getItem('avatar')
  if (avatar) {
    avatarUrl.value = avatar
    appState.avatar = avatar
  }
})
</script>

<style scoped>
.el-card :deep(.el-card__body) {
  padding: 20px 24px;
}

.avatar-uploader {
  border: 2px dashed #d9d9d9;
  border-radius: 50%;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.3s;
}
.avatar-uploader:hover {
  border-color: #b39ddb;
}
.avatar-uploader .avatar {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 50%;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>