<template>
  <div>
    <div style="margin-bottom: 24px;">
      <h2 style="font-size: 22px; color: #3c3a47; margin: 0;">❤️ 我的收藏</h2>
      <p style="color: #86909c; font-size: 14px; margin-top: 4px;">共 {{ favorites.length }} 本收藏图书</p>
    </div>

    <el-row :gutter="20" v-if="favorites.length > 0">
      <el-col :span="6" v-for="item in favorites" :key="item.id" style="margin-bottom: 20px;">
        <div class="favorite-card" @click="$router.push(`/book/${item.bookId}`)">
          <div class="favorite-cover">
            <span style="font-size: 36px;">📖</span>
            <span style="font-size: 28px; font-weight: 300; color: #2d2a3e; margin-top: 4px;">
              {{ getFirstLetter(item.bookName) }}
            </span>
          </div>
          <div style="padding: 12px 16px;">
            <div style="font-weight: 600; color: #2d2a3e; font-size: 15px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
              {{ item.bookName }}
            </div>
            <div style="font-size: 13px; color: #86909c;">{{ item.author }}</div>
            <div style="display: flex; justify-content: space-between; margin-top: 8px;">
              <el-tag size="small" type="info">{{ item.categoryName || '未分类' }}</el-tag>
              <span style="font-weight: 600; color: #b39ddb;">¥{{ item.price }}</span>
            </div>
            <div style="margin-top: 6px; font-size: 12px; color: #b0b6c0;">
              收藏于 {{ formatDate(item.favoritedAt) }}
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 🔥 空状态优化 -->
    <div v-else style="text-align: center; padding: 60px 0;">
      <div style="font-size: 64px; margin-bottom: 16px;">💔</div>
      <h3 style="color: #3c3a47; font-weight: 500; margin-bottom: 8px;">还没有收藏的图书</h3>
      <p style="color: #86909c; font-size: 14px; margin-bottom: 20px;">遇到喜欢的书，记得点击收藏哦</p>
      <el-button type="primary" @click="$router.push('/books')">去逛逛</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const favorites = ref([])

const getFirstLetter = (bookName) => {
  if (!bookName) return '?'
  return bookName.charAt(0).toUpperCase()
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { hour12: false })
}

const loadFavorites = async () => {
  try {
    const res = await request.get('/api/favorites/my')
    if (res.code === 200) {
      favorites.value = res.data || []
    }
  } catch {
    ElMessage.error('加载收藏列表失败')
  }
}

onMounted(loadFavorites)
</script>

<style scoped>
.favorite-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}
.favorite-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.10);
}
.favorite-cover {
  background: #faf8fc;
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-bottom: 1px solid #f0ecf5;
}
</style>