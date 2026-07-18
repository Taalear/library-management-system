<template>
  <div v-if="book">
    <el-button type="text" @click="$router.back()" style="margin-bottom: 16px; font-size: 14px; color: #86909c;">
      ← 返回
    </el-button>

    <el-card shadow="hover" style="border-radius: 20px; overflow: hidden; padding: 0;">
      <el-row :gutter="0">
        <el-col :span="10" style="display: flex; align-items: center; justify-content: center; min-height: 400px; padding: 40px; background: #faf8fc;">
          <div style="text-align: center;">
            <div style="font-size: 56px; color: #d1c4e9; margin-bottom: 8px;">📖</div>
            <div style="font-size: 72px; font-weight: 300; color: #2d2a3e;">{{ getFirstLetter(book.bookName) }}</div>
            <div style="color: #b0b6c0; font-size: 14px; margin-top: 12px; letter-spacing: 2px;">
              {{ book.categoryName || '未分类' }}
            </div>
          </div>
        </el-col>

        <el-col :span="14" style="padding: 40px 48px 48px 40px;">
          <h1 style="font-size: 28px; color: #2d2a3e; margin: 0 0 8px 0; font-weight: 700;">{{ book.bookName }}</h1>
          <p style="font-size: 16px; color: #86909c; margin-bottom: 16px;">✍️ {{ book.author }}</p>

          <div v-if="book.description" style="margin-bottom: 20px; padding: 16px 20px; background: #f7f5fa; border-radius: 12px; color: #3c3a47; font-size: 14px; line-height: 1.8;">
            {{ book.description }}
          </div>

          <div style="display: flex; gap: 12px; flex-wrap: wrap; margin-bottom: 24px;">
            <el-tag size="large" :type="book.stock > 0 ? 'success' : 'danger'" effect="plain">
              {{ book.stock > 0 ? `📦 库存 ${book.stock}` : '❌ 已售罄' }}
            </el-tag>
            <el-tag size="large" type="warning" effect="plain">💰 ¥{{ book.price }}</el-tag>
            <el-tag size="large" type="info" effect="plain">{{ book.categoryName || '未分类' }}</el-tag>
          </div>

          <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap;">
            <el-button
              type="warning"
              size="large"
              plain
              @click="toggleFavorite"
              style="border-radius: 12px; padding: 12px 24px;"
            >
              {{ isFavorited ? '❤️ 已收藏' : '🤍 收藏' }}
            </el-button>

            <el-tag v-if="hasBorrowed" type="info" size="large" style="font-weight: 600;">
              ✅ 您已借阅此书（{{ borrowedStatus }}）
            </el-tag>

            <el-button
              v-if="!hasBorrowed && isLoggedIn && book.stock > 0"
              type="primary"
              size="large"
              @click="handleBorrow"
              style="border-radius: 12px; padding: 12px 40px; font-weight: 600;"
            >📖 立即借阅</el-button>

            <el-button
              v-if="!isLoggedIn"
              type="primary"
              size="large"
              @click="$router.push('/login')"
              style="border-radius: 12px; padding: 12px 40px;"
            >登录后借阅</el-button>

            <el-button
              v-if="book.stock <= 0 && isLoggedIn && !hasBorrowed"
              size="large"
              disabled
              style="border-radius: 12px; padding: 12px 40px;"
            >已售罄</el-button>

            <el-button size="large" @click="$router.push('/books')" style="border-radius: 12px; padding: 12px 24px;">
              浏览更多
            </el-button>
          </div>

          <div v-if="isLoggedIn && !hasBorrowed && book.stock > 0" style="margin-top: 20px; padding: 12px 16px; background: #f7f5fa; border-radius: 12px; color: #86909c; font-size: 13px;">
            💡 借阅后请在 <strong>30 天</strong> 内归还，逾期将无法借阅其他图书。
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 评分/评论区域 -->
    <div style="margin-top: 32px;">
      <h3 style="font-size: 18px; color: #3c3a47; margin-bottom: 16px;">⭐ 读者评价</h3>
      <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 16px;">
        <span style="font-size: 24px; font-weight: 700; color: #b39ddb;">{{ avgRating }}</span>
        <span style="color: #86909c;">/ 5.0</span>
        <span style="color: #86909c; margin-left: 12px;">共 {{ reviewCount }} 条评价</span>
      </div>
      <!-- 评分配置 -->
      <div v-if="isLoggedIn" style="margin-bottom: 20px;">
        <el-rate v-model="newRating" :texts="['很差', '较差', '一般', '较好', '很好']" show-text />
        <el-input v-model="newComment" type="textarea" placeholder="说说你的读后感..." rows="2" style="margin-top: 8px; max-width: 500px;" />
        <el-button type="primary" size="small" @click="submitReview" style="margin-top: 8px;">提交评价</el-button>
      </div>
      <!-- 评论列表 -->
      <div v-for="item in reviews" :key="item.id" style="border-bottom: 1px solid #f0ecf5; padding: 12px 0;">
        <div style="display: flex; align-items: center; gap: 12px;">
          <span style="font-weight: 600; color: #3c3a47;">{{ item.username }}</span>
          <el-rate v-model="item.rating" disabled show-score text-color="#ff9900" />
          <span style="color: #b0b6c0; font-size: 12px;">{{ formatDate(item.createdAt) }}</span>
        </div>
        <p style="color: #3c3a47; margin-top: 4px;">{{ item.comment || '（无文字评价）' }}</p>
      </div>
      <el-empty v-if="reviews.length === 0" description="暂无评价，快来发表你的读后感吧！" />
    </div>

    <!-- 推荐图书 -->
    <div v-if="recommendBooks.length > 0" style="margin-top: 32px;">
      <h3 style="font-size: 18px; color: #3c3a47; margin-bottom: 16px;">📚 同分类推荐</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in recommendBooks" :key="item.id">
          <div class="recommend-card" @click="$router.push(`/book/${item.id}`)">
            <div style="font-size: 36px; text-align: center; padding: 12px 0;">📖</div>
            <div style="padding: 8px 12px;">
              <div style="font-weight: 600; color: #2d2a3e; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">{{ item.bookName }}</div>
              <div style="font-size: 12px; color: #86909c;">{{ item.author }}</div>
              <div style="display: flex; justify-content: space-between; margin-top: 4px;">
                <el-tag size="small" type="info">{{ item.categoryName }}</el-tag>
                <span style="font-weight: 600; color: #b39ddb;">¥{{ item.price }}</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>

  <el-empty v-else description="图书不存在" />
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const book = ref(null)
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const hasBorrowed = ref(false)
const borrowedStatus = ref('')
const isFavorited = ref(false)
const recommendBooks = ref([])

// 评分评论相关
const reviews = ref([])
const avgRating = ref('0.0')
const reviewCount = ref(0)
const newRating = ref(0)
const newComment = ref('')

const getFirstLetter = (bookName) => {
  if (!bookName) return '?'
  return bookName.charAt(0).toUpperCase()
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { hour12: false })
}

const getCurrentUserId = async () => {
  try {
    const res = await request.get('/api/user/profile')
    if (res.code === 200) return res.data.id
  } catch { return null }
}

const checkBorrowed = async () => {
  if (!isLoggedIn.value || !book.value) return
  const uid = await getCurrentUserId()
  if (!uid) return
  try {
    const res = await request.get('/api/borrow/my')
    if (res.code === 200) {
      const records = res.data || []
      const found = records.find(r => r.bookId === book.value.id && r.status === 0)
      hasBorrowed.value = !!found
      borrowedStatus.value = found ? '借出中' : ''
    }
  } catch { /* ignore */ }
}

const checkFavorite = async () => {
  if (!isLoggedIn.value || !book.value) return
  try {
    const res = await request.get(`/api/favorites/check/${book.value.id}`)
    if (res.code === 200) {
      isFavorited.value = res.data
    }
  } catch { /* ignore */ }
}

const toggleFavorite = async () => {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    const res = await request.post(`/api/favorites/toggle/${book.value.id}`)
    if (res.code === 200) {
      isFavorited.value = res.data
      ElMessage.success({
        message: isFavorited.value ? '❤️ 已收藏' : '已取消收藏',
        duration: 1500
      })
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作异常，请稍后重试')
  }
}

// 加载推荐图书
const loadRecommendBooks = async () => {
  if (!book.value) return
  try {
    const res = await request.get(`/api/books/${book.value.id}/recommend`)
    if (res.code === 200) {
      recommendBooks.value = res.data || []
    }
  } catch { /* ignore */ }
}

// 加载评分评论
const loadReviews = async () => {
  if (!book.value) return
  try {
    const res = await request.get(`/api/reviews/book/${book.value.id}`)
    if (res.code === 200) {
      reviews.value = res.data.reviews || []
      avgRating.value = res.data.avgRating || '0.0'
      reviewCount.value = res.data.count || 0
    }
  } catch { /* ignore */ }
}

// 提交评价
const submitReview = async () => {
  if (newRating.value === 0) {
    ElMessage.warning('请选择评分')
    return
  }
  try {
    const res = await request.post('/api/reviews', {
      bookId: book.value.id,
      rating: newRating.value,
      comment: newComment.value
    })
    if (res.code === 200) {
      ElMessage.success('评价成功')
      newRating.value = 0
      newComment.value = ''
      loadReviews()
    } else {
      ElMessage.error(res.msg || '评价失败')
    }
  } catch {
    ElMessage.error('评价异常')
  }
}

const loadBook = async () => {
  const id = route.params.id
  if (!id) {
    ElMessage.error('无效的图书ID')
    return
  }
  try {
    const res = await request.get(`/api/books/${id}`)
    if (res.code === 200) {
      book.value = res.data
      await Promise.all([
        checkBorrowed(),
        checkFavorite(),
        loadRecommendBooks(),
        loadReviews()
      ])
    } else {
      ElMessage.error(res.msg || '加载图书详情失败')
    }
  } catch (error) {
    console.error('加载图书详情失败:', error)
    ElMessage.error('加载图书详情失败')
  }
}

const handleBorrow = async () => {
  if (!book.value) return
  try {
    const res = await request.post(`/api/borrow/${book.value.id}`)
    if (res.code === 200) {
      ElMessage.success('借书成功！')
      book.value.stock -= 1
      hasBorrowed.value = true
      borrowedStatus.value = '借出中'
    } else {
      ElMessage.error(res.msg || '借书失败')
    }
  } catch {
    ElMessage.error('借书异常')
  }
}

onMounted(loadBook)
</script>

<style scoped>
.recommend-card {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  cursor: pointer;
  transition: all 0.3s;
}
.recommend-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.10);
}
</style>