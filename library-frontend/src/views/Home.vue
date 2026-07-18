<template>
  <div>
    <!-- 欢迎横幅 -->
    <div style="background: linear-gradient(135deg, #f0ecf5 0%, #e8e0f0 100%); border-radius: 20px; padding: 48px 40px; margin-bottom: 32px; text-align: center;">
      <h1 style="font-size: 32px; color: #3c3a47; margin-bottom: 12px;">📚 欢迎来到书香阁</h1>
      <p style="font-size: 16px; color: #6d6a7a; max-width: 500px; margin: 0 auto 20px;">探索数千本图书，随时随地借阅心仪的书籍。</p>
      <el-button type="primary" size="large" @click="$router.push('/books')">开始浏览 →</el-button>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 32px;">
      <el-col :span="8" v-for="item in stats" :key="item.label">
        <el-card shadow="hover" style="text-align: center; padding: 20px 0; border-radius: 16px;">
          <div style="font-size: 14px; color: #86909c;">{{ item.label }}</div>
          <div style="font-size: 28px; font-weight: 700; color: #3c3a47; margin-top: 4px;">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 轮播 -->
    <div v-if="topBooks.length > 0" style="margin-bottom: 32px;">
      <h3 style="font-size: 18px; color: #3c3a47; margin-bottom: 16px;">⭐ 精选推荐</h3>
      <el-carousel :interval="4000" arrow="always" height="280px" style="border-radius: 16px; overflow: hidden; box-shadow: 0 2px 12px rgba(0,0,0,0.06);">
        <el-carousel-item v-for="book in topBooks.slice(0, 3)" :key="book.id">
          <div class="carousel-item" @click="$router.push(`/book/${book.id}`)">
            <div class="carousel-cover">
              <div class="book-cover-art">
                <div class="book-icon">📖</div>
                <div class="book-letter">{{ getFirstLetter(book.bookName) }}</div>
                <div class="book-sub">{{ book.categoryName || '文学' }}</div>
              </div>
            </div>
            <div class="carousel-info">
              <div class="info-title">{{ book.bookName }}</div>
              <div class="info-author">✍️ {{ book.author || '未知作者' }}</div>
              <div class="info-desc">{{ book.description || '这本书暂时没有简介，但值得一读。' }}</div>
              <div class="info-tags">
                <el-tag size="small" type="warning">💰 ¥{{ book.price != null ? book.price : '0.00' }}</el-tag>
                <el-tag size="small" :type="(book.stock || 0) > 0 ? 'success' : 'danger'">
                  {{ (book.stock || 0) > 0 ? `库存 ${book.stock}` : '已售罄' }}
                </el-tag>
                <el-tag size="small" type="info">{{ book.categoryName || '未分类' }}</el-tag>
              </div>
              <div class="info-action">
                <el-button type="primary" size="small" plain>查看详情 →</el-button>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </div>

    <!-- 热门图书 -->
    <div style="margin-bottom: 16px;">
      <h3 style="font-size: 18px; color: #3c3a47; margin-bottom: 16px;">🔥 热门图书</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="book in topBooks" :key="book.id" style="margin-bottom: 20px;">
          <div class="hot-book-card" @click="$router.push(`/book/${book.id}`)">
            <div class="hot-cover">
              <div class="hot-icon">📖</div>
              <div class="hot-letter">{{ getFirstLetter(book.bookName) }}</div>
            </div>
            <div style="padding: 12px 4px 4px;">
              <div style="font-weight: 600; color: #2d2a3e; font-size: 14px; text-align: center; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                {{ book.bookName }}
              </div>
              <div style="font-size: 12px; color: #86909c; text-align: center;">借阅 {{ book.borrowCount }} 次</div>
            </div>
          </div>
        </el-col>
      </el-row>
      <div style="text-align: center; margin-top: 8px;">
        <el-button type="primary" plain @click="$router.push('/books')">查看全部图书 →</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()

const stats = ref([
  { label: '📚 藏书总数', value: 0 },
  { label: '👥 注册用户', value: 0 },
  { label: '📖 总借阅次数', value: 0 }
])
const topBooks = ref([])

const getFirstLetter = (bookName) => {
  if (!bookName) return '?'
  return bookName.charAt(0).toUpperCase()
}

const loadHomeData = async () => {
  try {
    const statRes = await request.get('/api/stat/overview')
    if (statRes.code === 200) {
      stats.value[0].value = statRes.data.bookCount || 0
      stats.value[1].value = statRes.data.userCount || 0
      stats.value[2].value = statRes.data.borrowCount || 0
    }
    const topRes = await request.get('/api/stat/top-books')
    if (topRes.code === 200) {
      topBooks.value = topRes.data || []
    }
  } catch (e) {
    console.warn('加载首页数据失败', e)
  }
}

onMounted(loadHomeData)
</script>

<style scoped>
/* 保持之前的样式不变 */
.hot-book-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
  padding: 12px 12px 8px;
  text-align: center;
}
.hot-book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.10);
}
.hot-cover {
  background: #f8f5f0;
  border-radius: 12px;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  border: 1px solid #f0ece6;
}
.hot-icon {
  font-size: 24px;
  color: #d1c4e9;
}
.hot-letter {
  font-size: 32px;
  font-weight: 300;
  color: #2d2a3e;
}
.carousel-item {
  display: flex;
  align-items: center;
  height: 100%;
  background: #ffffff;
  padding: 0 40px;
  cursor: pointer;
  transition: background 0.3s;
}
.carousel-item:hover {
  background: #faf8fc;
}
.carousel-cover {
  flex: 0 0 35%;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
.book-cover-art {
  width: 160px;
  height: 200px;
  background: #f8f5f0;
  border-radius: 12px;
  box-shadow: 4px 8px 20px rgba(0, 0, 0, 0.06), -1px -1px 0 #e8e4de inset, 1px 1px 0 #ffffff inset;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 1px solid #e8e4de;
  transition: transform 0.3s;
}
.book-cover-art:hover {
  transform: scale(1.02);
}
.book-icon {
  font-size: 28px;
  color: #d1c4e9;
  margin-bottom: 4px;
}
.book-letter {
  font-size: 48px;
  font-weight: 300;
  color: #2d2a3e;
  line-height: 1.2;
}
.book-sub {
  font-size: 12px;
  color: #b0b0a8;
  margin-top: 6px;
  letter-spacing: 2px;
  border-top: 1px solid #e8e4de;
  padding-top: 6px;
  width: 60%;
  text-align: center;
}
.carousel-info {
  flex: 1;
  padding-left: 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
  overflow: hidden;
}
.info-title {
  font-size: 24px;
  font-weight: 700;
  color: #2d2a3e;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.info-author {
  font-size: 16px;
  color: #86909c;
  margin-bottom: 8px;
}
.info-desc {
  font-size: 14px;
  color: #3c3a47;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: 44px;
  margin-bottom: 12px;
}
.info-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.info-action {
  display: flex;
  align-items: center;
}
.info-action .el-button {
  border-radius: 20px;
}
</style>