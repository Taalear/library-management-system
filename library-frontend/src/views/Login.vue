<template>
  <div style="width: 400px; margin: 100px auto; border: 1px solid #ddd; padding: 30px; border-radius: 8px;">
    <h2 style="text-align:center;">图书管理系统登录</h2>
    <el-form :model="form" label-width="80px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleLogin" style="width:100%">登录</el-button>
      </el-form-item>
      <div style="text-align:center; color: #aaa;">
        还没有账号？<router-link to="/register" style="color: #b39ddb;">去注册</router-link>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { appState } from '@/utils/state'

const router = useRouter()
const form = reactive({ username: '', password: '' })

const handleLogin = async () => {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  try {
    const res = await request.post('/api/auth/login', form)
    if (res.code === 200) {
      const { token, role, username, avatar } = res.data
      localStorage.setItem('token', token)
      localStorage.setItem('role', role)
      localStorage.setItem('username', username)
      // 保存头像
      if (avatar) {
        localStorage.setItem('avatar', avatar)
        appState.avatar = avatar
      } else {
        localStorage.removeItem('avatar')
        appState.avatar = ''
      }
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error(res.msg || '登录失败')
    }
  } catch (e) {
    ElMessage.error('网络异常，请检查后端是否启动')
  }
}
</script>