<template>
  <el-container style="height: 100vh;">
    <!-- ===== 左侧侧边栏 ===== -->
    <el-aside width="220px" style="background: #2d2a3e; color: #fff; display: flex; flex-direction: column;">
      <!-- Logo 区域 -->
      <div style="height: 60px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid #3f3a56; font-size: 18px; font-weight: 600; letter-spacing: 1px; color: #e8e0f0;">
        📚 图书管理系统
      </div>

      <!-- 菜单区域（所有路径前加 /admin） -->
      <el-menu
        :default-active="activeMenu"
        background-color="#2d2a3e"
        text-color="#b8b0c8"
        active-text-color="#ffffff"
        router
        style="flex: 1; border-right: none;"
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/books">
          <el-icon><Reading /></el-icon>
          <span>图书管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/logs">
          <el-icon><Document /></el-icon>
          <span>操作日志</span>
        </el-menu-item>

        <!-- 这里可以加一个“返回前台”的入口 -->
        <el-menu-item index="/" @click="goHome">
          <el-icon><HomeFilled /></el-icon>
          <span>返回前台</span>
        </el-menu-item>
      </el-menu>

      <!-- 底部用户信息 -->
      <div style="padding: 16px 20px; border-top: 1px solid #3f3a56; font-size: 14px; color: #b8b0c8; display: flex; align-items: center; flex-shrink: 0;">
        <el-icon><User /></el-icon>
        <span style="margin-left: 8px; flex: 1;">{{ username }}</span>
        <el-button type="danger" size="small" plain @click="handleLogout" style="border-radius: 6px; background: transparent; border-color: #a87c7c; color: #d4b8b8;">
          退出
        </el-button>
      </div>
    </el-aside>

    <!-- ===== 右侧内容区 ===== -->
    <el-container>
      <el-header height="60px" style="background: #ffffff; border-bottom: 1px solid #e8ecf1; display: flex; align-items: center; padding: 0 24px;">
        <span style="font-size: 16px; font-weight: 500; color: #3c3a47;">
          {{ currentTitle }}
        </span>
      </el-header>
      <el-main style="background: #f7f5fa; padding: 20px; overflow-y: auto;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { DataLine, Reading, Folder, User, HomeFilled } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const username = ref(localStorage.getItem('username') || '用户')

const activeMenu = ref('/admin/dashboard')

const titleMap = {
  '/admin/dashboard': '📊 数据看板',
  '/admin/books': '📖 图书管理',
  '/admin/categories': '📂 分类管理'
}
const currentTitle = computed(() => titleMap[route.path] || '后台管理')

watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
}, { immediate: true })

onMounted(() => {
  activeMenu.value = route.path
})

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('role')
    localStorage.removeItem('username')
    ElMessage.success('已退出')
    router.push('/login')
  }).catch(() => {})
}

// 返回前台首页
const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.el-aside {
  background: #2d2a3e !important;
}
.el-menu {
  background-color: #2d2a3e !important;
  border-right: none !important;
}
.el-menu-item {
  color: #b8b0c8 !important;
  height: 48px !important;
  line-height: 48px !important;
  margin: 4px 8px !important;
  border-radius: 8px !important;
}
.el-menu-item:hover {
  background-color: #3f3a56 !important;
  color: #e8e0f0 !important;
}
.el-menu-item.is-active {
  background-color: #3f3a56 !important;
  color: #ffffff !important;
}
.el-menu-item .el-icon {
  color: #b39ddb;
  margin-right: 8px;
}
.el-menu-item.is-active .el-icon {
  color: #ffffff;
}
.el-button--danger.is-plain {
  background: transparent !important;
  border-color: #a87c7c !important;
  color: #d4b8b8 !important;
}
.el-button--danger.is-plain:hover {
  background: #3f2e2e !important;
  border-color: #cfa0a0 !important;
  color: #f0d0d0 !important;
}
.el-header {
  background: #ffffff !important;
  border-bottom: 1px solid #e8ecf1 !important;
}
</style>