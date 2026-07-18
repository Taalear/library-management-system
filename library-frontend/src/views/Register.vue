<template>
  <div style="max-width: 420px; margin: 40px auto; padding: 32px; background: #ffffff; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.06);">
    <h2 style="text-align:center; color: #3c3a47; margin-bottom: 24px;">📝 注册新账号</h2>

    <el-form :model="form" label-width="80px" @submit.prevent="handleRegister">
      <el-form-item label="用户名">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" placeholder="至少6位" show-password @input="checkPasswordStrength" />
        <!-- 密码强度指示器 -->
        <div style="margin-top: 4px;">
          <el-progress :percentage="passwordStrength.percent" :color="passwordStrength.color" :format="() => passwordStrength.text" />
        </div>
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" show-password />
        <span v-if="form.confirmPassword && form.password !== form.confirmPassword" style="color: #f56c6c; font-size: 12px;">两次密码不一致</span>
        <span v-else-if="form.confirmPassword && form.password === form.confirmPassword" style="color: #67c23a; font-size: 12px;">✓ 匹配</span>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" placeholder="可选" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" style="width:100%;" :loading="loading" @click="handleRegister">
          注册
        </el-button>
      </el-form-item>
      <div style="text-align:center; color: #86909c; font-size: 14px;">
        已有账号？<router-link to="/login" style="color: #b39ddb; text-decoration: none;">去登录</router-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

const passwordStrength = reactive({
  percent: 0,
  text: '弱',
  color: '#f56c6c'
})

const checkPasswordStrength = () => {
  const pwd = form.password
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

const handleRegister = async () => {
  if (!form.username || form.username.length < 3) {
    ElMessage.warning('用户名至少3位')
    return
  }
  if (!form.password || form.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }

  loading.value = true
  try {
    const res = await request.post('/api/auth/register', {
      username: form.username,
      password: form.password,
      nickname: form.nickname || form.username
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.msg || '注册失败')
    }
  } catch {
    ElMessage.error('网络异常')
  } finally {
    loading.value = false
  }
}
</script>