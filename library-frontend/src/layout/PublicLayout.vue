<template>
  <div style="min-height: 100vh; display: flex; flex-direction: column; background: #f7f5fa;">
    <header style="background: #ffffff; border-bottom: 1px solid #e8ecf1; padding: 0 32px; height: 64px; display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 100; box-shadow: 0 1px 4px rgba(0,0,0,0.04);">
      <div style="display: flex; align-items: center; gap: 8px;">
        <span style="font-size: 22px;">📚</span>
        <span style="font-size: 18px; font-weight: 600; color: #3c3a47; letter-spacing: 0.5px;">书香阁</span>
      </div>

      <div style="display: flex; align-items: center; gap: 24px;">
        <router-link to="/" style="text-decoration: none; color: #3c3a47; font-size: 14px; font-weight: 500; transition: color 0.2s;" exact-active-class="active-link">首页</router-link>
        <router-link to="/books" style="text-decoration: none; color: #3c3a47; font-size: 14px; font-weight: 500; transition: color 0.2s;" active-class="active-link">全部图书</router-link>
        <router-link v-if="isLoggedIn" to="/user/my-borrow" style="text-decoration: none; color: #3c3a47; font-size: 14px; font-weight: 500; transition: color 0.2s;" active-class="active-link">我的借阅</router-link>
        <router-link v-if="isLoggedIn" to="/user/favorites" style="text-decoration: none; color: #3c3a47; font-size: 14px; font-weight: 500; transition: color 0.2s;" active-class="active-link">我的收藏</router-link>
        <router-link v-if="isAdmin" to="/admin/dashboard" style="text-decoration: none; color: #b39ddb; font-size: 14px; font-weight: 600; transition: color 0.2s;" active-class="active-link">⚙️ 进入后台</router-link>
      </div>

      <div style="display: flex; align-items: center; gap: 16px;">
        <!-- 搜索框 + 历史 -->
        <div style="position: relative; display: inline-block;">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索图书..."
            size="small"
            style="width: 180px;"
            clearable
            @keyup.enter="handleSearch"
            @focus="showHistory = true"
            @blur="hideHistoryLater"
          >
            <template #append>
              <el-button size="small" @click="handleSearch">搜索</el-button>
            </template>
          </el-input>
          <!-- 搜索历史 -->
          <div
            v-if="showHistory && searchHistory.length > 0"
            style="position: absolute; top: 100%; left: 0; width: 100%; background: #fff; border: 1px solid #e8ecf1; border-radius: 8px; margin-top: 4px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); z-index: 10; max-height: 200px; overflow-y: auto;"
          >
            <div style="padding: 8px 12px; font-size: 12px; color: #86909c; border-bottom: 1px solid #f0ecf5;">最近搜索</div>
            <div
              v-for="(item, index) in searchHistory"
              :key="index"
              style="padding: 6px 12px; cursor: pointer; font-size: 13px; color: #3c3a47; display: flex; justify-content: space-between; align-items: center;"
              @click="applyHistory(item)"
            >
              <span>{{ item }}</span>
              <span style="color: #ccc; font-size: 12px; cursor: pointer;" @click.stop="removeHistory(index)">✕</span>
            </div>
            <div
              style="padding: 6px 12px; text-align: center; font-size: 12px; color: #b39ddb; cursor: pointer; border-top: 1px solid #f0ecf5;"
              @click="clearHistory"
            >
              清空历史
            </div>
          </div>
        </div>

        <template v-if="isLoggedIn">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div style="display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 12px 4px 4px; border-radius: 20px; background: #f0ecf5; transition: background 0.2s; border: 1px solid transparent;">
              <el-avatar :size="32" v-if="appState.avatar" :src="appState.avatar" />
              <el-avatar v-else :size="32" style="background: #b39ddb; color: #fff; font-size: 16px; font-weight: 600;">
                {{ userInitial }}
              </el-avatar>
              <span style="font-size: 14px; font-weight: 500; color: #3c3a47; max-width: 80px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">{{ displayName }}</span>
              <el-icon style="color: #86909c;"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile"><el-icon><User /></el-icon> 个人中心</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" command="admin"><el-icon><Setting /></el-icon> 进入后台</el-dropdown-item>
                <el-dropdown-item divided command="logout" style="color: #f56c6c;"><el-icon><SwitchButton /></el-icon> 退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <template v-else>
          <router-link to="/login"><el-button type="primary" size="small">登录</el-button></router-link>
          <router-link to="/register"><el-button size="small">注册</el-button></router-link>
        </template>
      </div>
    </header>

    <main style="flex: 1; padding: 24px 32px; max-width: 1280px; width: 100%; margin: 0 auto;">
      <router-view />
    </main>

    <footer style="background: #ffffff; border-top: 1px solid #e8ecf1; padding: 20px 32px; text-align: center; color: #b0b6c0; font-size: 13px;">
      © 2026 📚 书香阁 · 图书管理系统 · 技术栈：Spring Boot + Vue 3
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, User, Setting, SwitchButton } from '@element-plus/icons-vue'
import { appState } from '@/utils/state'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

// ===== 用户状态 =====
const isLoggedIn = ref(!!localStorage.getItem('token'))
const userRole = ref(localStorage.getItem('role') || 'USER')
const username = ref(localStorage.getItem('username') || '用户')

// ===== 搜索历史 =====
const searchHistory = ref(JSON.parse(localStorage.getItem('searchHistory') || '[]'))
const showHistory = ref(false)

const addHistory = (keyword) => {
  if (!keyword) return
  const history = searchHistory.value.filter(item => item !== keyword)
  history.unshift(keyword)
  if (history.length > 5) history.pop()
  searchHistory.value = history
  localStorage.setItem('searchHistory', JSON.stringify(history))
}

const applyHistory = (keyword) => {
  searchKeyword.value = keyword
  showHistory.value = false
  handleSearch()
}

const removeHistory = (index) => {
  searchHistory.value.splice(index, 1)
  localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
  if (searchHistory.value.length === 0) showHistory.value = false
}

const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('searchHistory')
  showHistory.value = false
}

let hideTimer = null
const hideHistoryLater = () => {
  clearTimeout(hideTimer)
  hideTimer = setTimeout(() => { showHistory.value = false }, 200)
}

// ===== 检查逾期借阅 =====
const checkOverdue = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await request.get('/api/borrow/overdue')
    if (res.code === 200 && res.data && res.data.length > 0) {
      const titles = res.data.map(b => `《${b.bookName}》`).join('、')
      ElMessageBox.alert(
        `您有以下图书已逾期未还（超过28天）：\n${titles}\n\n请尽快归还，以免影响其他图书借阅！`,
        '📢 逾期提醒',
        { confirmButtonText: '知道了', type: 'warning' }
      )
    }
  } catch { /* 静默失败 */ }
}

// ===== 监听路由变化，同步 localStorage 状态 =====
watch(() => route.path, () => {
  const wasLoggedIn = isLoggedIn.value
  isLoggedIn.value = !!localStorage.getItem('token')
  userRole.value = localStorage.getItem('role') || 'USER'
  username.value = localStorage.getItem('username') || '用户'

  // 从未登录变为已登录时检查逾期
  if (!wasLoggedIn && isLoggedIn.value) {
    checkOverdue()
  }
}, { immediate: true })

const isAdmin = computed(() => userRole.value === 'ADMIN')
const displayName = computed(() => {
  const name = username.value
  return name.length > 6 ? name.substring(0, 6) + '..' : name
})
const userInitial = computed(() => (username.value || '用').charAt(0).toUpperCase())

const searchKeyword = ref('')
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    addHistory(searchKeyword.value.trim())
    router.push({ path: '/books', query: { keyword: searchKeyword.value.trim() } })
  }
}

// ===== 下拉菜单命令 =====
const handleUserCommand = (command) => {
  nextTick(() => {
    switch (command) {
      case 'profile':
        router.push('/user/profile').catch(() => {})
        break
      case 'admin':
        router.push('/admin/dashboard').catch(() => {})
        break
      case 'logout':
        ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 手动更新状态（即使路由不变也能立即生效）
          isLoggedIn.value = false
          userRole.value = 'USER'
          username.value = '用户'
          localStorage.removeItem('token')
          localStorage.removeItem('role')
          localStorage.removeItem('username')
          localStorage.removeItem('avatar')
          appState.avatar = ''
          ElMessage.success('已退出')
          router.push('/')
        }).catch(() => {})
        break
    }
  })
}

watch(() => route.path, () => {
  if (route.path === '/books' && route.query.keyword) {
    searchKeyword.value = route.query.keyword
  }
}, { immediate: true })
</script>

<style scoped>
.active-link {
  color: #b39ddb !important;
  position: relative;
}
.active-link::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  right: 0;
  height: 2px;
  background: #b39ddb;
  border-radius: 2px;
}
.el-dropdown :deep(.el-dropdown-selfdefine) {
  transition: border-color 0.2s, background 0.2s;
}
.el-dropdown :deep(.el-dropdown-selfdefine):hover {
  border-color: #b39ddb;
  background: #e8e0f0 !important;
}
</style>