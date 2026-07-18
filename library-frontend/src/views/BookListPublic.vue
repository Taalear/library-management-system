<template>
  <div>
    <div style="margin-bottom: 24px;">
      <h2 style="font-size: 22px; color: #3c3a47; margin: 0;">📖 全部图书</h2>
      <p style="color: #86909c; font-size: 14px; margin-top: 4px;">共 {{ total }} 本图书</p>
    </div>

    <div style="margin-bottom: 24px; display: flex; align-items: center; flex-wrap: wrap; gap: 12px;">
      <el-input
        v-model="searchName"
        placeholder="输入书名搜索"
        style="width: 220px;"
        clearable
        @keyup.enter="loadBooks"
      />
      <el-select
        v-model="searchCategoryId"
        placeholder="全部分类"
        clearable
        style="width: 150px;"
        @change="loadBooks"
      >
        <el-option v-for="item in categoryList" :key="item.id" :label="item.name" :value="item.id" />
      </el-select>
      <el-button type="primary" @click="loadBooks">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-row :gutter="24">
      <template v-if="loading">
        <el-col :span="6" v-for="n in 8" :key="'skeleton-' + n" style="margin-bottom: 24px;">
          <div class="skeleton-card">
            <el-skeleton style="padding: 16px;" animated>
              <template #template>
                <div style="display: flex; flex-direction: column; align-items: center;">
                  <el-skeleton-item variant="image" style="width: 80px; height: 100px; border-radius: 12px;" />
                  <el-skeleton-item variant="text" style="width: 80%; margin-top: 12px;" />
                  <el-skeleton-item variant="text" style="width: 60%;" />
                  <el-skeleton-item variant="text" style="width: 40%;" />
                </div>
              </template>
            </el-skeleton>
          </div>
        </el-col>
      </template>

      <template v-else>
        <el-col :span="6" v-for="book in tableData" :key="book.id" style="margin-bottom: 24px;">
          <div class="book-card" @click="$router.push(`/book/${book.id}`)">
            <div class="book-cover" :style="{ background: getBookBg(book.bookName) }">
              <div class="cover-icon">📖</div>
              <div class="cover-letter">{{ getFirstLetter(book.bookName) }}</div>
              <div class="cover-stock" :class="book.stock > 0 ? 'in-stock' : 'out-of-stock'">
                {{ book.stock > 0 ? `库存 ${book.stock}` : '已售罄' }}
              </div>
              <div class="cover-category">{{ book.categoryName || '未分类' }}</div>
            </div>
            <div class="book-info">
              <div class="book-title">{{ book.bookName }}</div>
              <div class="book-author">{{ book.author }}</div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px;">
                <span class="book-price">¥{{ book.price }}</span>
              </div>
              <div style="margin-top: 12px;">
                <!-- 🔥 立即借阅（防重复点击） -->
                <el-button
                  v-if="isLoggedIn && book.stock > 0"
                  type="primary"
                  size="small"
                  plain
                  :loading="borrowingIds.includes(book.id)"
                  @click.stop="handleQuickBorrow(book.id)"
                  style="width: 100%;"
                >
                  {{ borrowingIds.includes(book.id) ? '借阅中...' : '立即借阅' }}
                </el-button>
                <el-button
                  v-if="!isLoggedIn"
                  size="small"
                  @click.stop="$router.push('/login')"
                  style="width: 100%;"
                >
                  登录借阅
                </el-button>
                <el-button
                  v-if="isLoggedIn && book.stock <= 0"
                  size="small"
                  disabled
                  style="width: 100%;"
                >
                  已售罄
                </el-button>
              </div>
            </div>
          </div>
        </el-col>
      </template>
    </el-row>

    <el-empty v-if="!loading && tableData.length === 0" description="📚 没有找到匹配的图书，试试其他关键词吧" />

    <div style="margin-top: 20px; display: flex; justify-content: center;">
      <el-pagination
        @current-change="handlePageChange"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        hide-on-single-page
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const searchName = ref('')
const searchCategoryId = ref(null)
const loading = ref(false)
const categoryList = ref([])
const borrowingIds = ref([]) // 正在借阅的图书ID列表

// ===== 书封背景 =====
const BOOK_BG_PALETTE = [
  'linear-gradient(145deg, #f7f3ee 0%, #ede8e2 100%)',
  'linear-gradient(145deg, #f2efe9 0%, #e8e3db 100%)',
  'linear-gradient(145deg, #f5f0ea 0%, #eae4dd 100%)',
  'linear-gradient(145deg, #f0ece5 0%, #e6dfd7 100%)',
  'linear-gradient(145deg, #f8f4ef 0%, #efe8e0 100%)',
  'linear-gradient(145deg, #f4efe8 0%, #e8e1d9 100%)',
  'linear-gradient(145deg, #f6f2ec 0%, #ece4dc 100%)',
  'linear-gradient(145deg, #f3eee7 0%, #e7dfd6 100%)'
]

const getBookBg = (bookName) => {
  if (!bookName) return BOOK_BG_PALETTE[0]
  let hash = 0
  for (let i = 0; i < bookName.length; i++) {
    hash = bookName.charCodeAt(i) + ((hash << 5) - hash)
  }
  return BOOK_BG_PALETTE[Math.abs(hash) % BOOK_BG_PALETTE.length]
}

const getFirstLetter = (bookName) => {
  if (!bookName) return '?'
  return bookName.charAt(0).toUpperCase()
}

// ===== 加载分类 =====
const loadCategories = async () => {
  try {
    const res = await request.get('/api/categories/all')
    if (res.code === 200) categoryList.value = res.data || []
  } catch { categoryList.value = [] }
}

// ===== 加载图书 =====
const loadBooks = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/books', {
      params: {
        pageNum: pageNum.value,
        pageSize: pageSize.value,
        bookName: searchName.value,
        categoryId: searchCategoryId.value
      }
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ===== 立即借阅（防重复） =====
const handleQuickBorrow = async (bookId) => {
  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  // 防止重复点击
  if (borrowingIds.value.includes(bookId)) return

  borrowingIds.value.push(bookId) // 标记为加载中
  try {
    const res = await request.post(`/api/borrow/${bookId}`)
    if (res.code === 200) {
      ElMessage.success('借书成功！')
      loadBooks()
    } else {
      ElMessage.error(res.msg || '借书失败')
    }
  } catch {
    ElMessage.error('借书异常')
  } finally {
    borrowingIds.value = borrowingIds.value.filter(id => id !== bookId) // 移除标记
  }
}

const handlePageChange = (val) => {
  pageNum.value = val
  loadBooks()
}

const resetFilters = () => {
  searchName.value = ''
  searchCategoryId.value = null
  pageNum.value = 1
  loadBooks()
}

const initFromQuery = () => {
  if (route.query.keyword) searchName.value = route.query.keyword
}

onMounted(() => {
  initFromQuery()
  loadCategories()
  loadBooks()
})
</script>

<style scoped>
.book-card {
  background: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.book-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.10);
}

.book-cover {
  position: relative;
  padding: 20px 0 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 160px;
  border-bottom: 1px solid rgba(0,0,0,0.04);
}
.cover-icon {
  font-size: 28px;
  color: rgba(0,0,0,0.15);
  margin-bottom: 2px;
}
.cover-letter {
  font-size: 44px;
  font-weight: 300;
  color: #2d2a3e;
  line-height: 1;
}
.cover-stock {
  position: absolute;
  top: 12px;
  right: 12px;
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 20px;
  font-weight: 500;
  background: rgba(255,255,255,0.7);
  backdrop-filter: blur(4px);
  color: #3c3a47;
}
.cover-stock.in-stock {
  background: rgba(232, 245, 233, 0.9);
  color: #2e7d32;
}
.cover-stock.out-of-stock {
  background: rgba(252, 228, 236, 0.9);
  color: #c62828;
}
.cover-category {
  font-size: 12px;
  color: rgba(0,0,0,0.3);
  margin-top: 4px;
  letter-spacing: 1px;
}

.book-info {
  padding: 16px 16px 18px;
  flex: 1;
  display: flex;
  flex-direction: column;
}
.book-title {
  font-weight: 600;
  font-size: 16px;
  color: #2d2a3e;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 44px;
}
.book-author {
  font-size: 13px;
  color: #86909c;
  margin-top: 4px;
}
.book-price {
  font-weight: 700;
  font-size: 16px;
  color: #b39ddb;
}
.skeleton-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 12px;
  height: 100%;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}
</style>