<template>
  <div style="padding: 0;">
    <div style="margin-bottom: 16px; display: flex; justify-content: space-between; align-items: center;">
      <div>
        <span style="font-size: 14px; color: #86909c;">共 <strong>{{ tableData.length }}</strong> 条借阅记录</span>
        <span style="font-size: 14px; color: #86909c; margin-left: 20px;">
          借出中 <span class="stat-badge borrowing-badge">{{ borrowCount }}</span>
        </span>
        <span style="font-size: 14px; color: #86909c; margin-left: 10px;">
          已归还 <span class="stat-badge returned-badge">{{ returnCount }}</span>
        </span>
      </div>
    </div>

    <el-table v-if="tableData.length > 0" :data="tableData" border stripe style="width: 100%;">
      <el-table-column prop="id" label="记录ID" width="80" />
      <el-table-column prop="bookName" label="书名">
        <template #default="scope">
          <span :style="{ color: scope.row.bookName ? 'inherit' : '#bbb' }">
            {{ scope.row.bookName || '已下架' }}
          </span>
          <el-tag v-if="!scope.row.bookName" size="small" type="info" style="margin-left: 6px;">已删除</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="author" label="作者">
        <template #default="scope">
          {{ scope.row.author || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="borrowTime" label="借书时间" width="180">
        <template #default="scope">{{ formatDate(scope.row.borrowTime) }}</template>
      </el-table-column>
      <el-table-column prop="returnTime" label="还书时间" width="180">
        <template #default="scope">{{ scope.row.returnTime ? formatDate(scope.row.returnTime) : '-' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="130">
        <template #default="scope">
          <span :class="['status-dot', scope.row.status === 0 ? 'borrowing' : 'returned']">
            <span class="dot"></span>
            {{ scope.row.status === 0 ? '借出中' : '已归还' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            type="success"
            size="small"
            @click="handleReturn(scope.row.bookId)"
          >
            还书
          </el-button>
          <span v-else style="color: #ccc; font-size: 13px;">已归还</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 🔥 空状态优化 -->
    <div v-else style="text-align: center; padding: 60px 0;">
      <div style="font-size: 64px; margin-bottom: 16px;">📚</div>
      <h3 style="color: #3c3a47; font-weight: 500; margin-bottom: 8px;">还没有借阅记录</h3>
      <p style="color: #86909c; font-size: 14px; margin-bottom: 20px;">去图书列表看看有没有感兴趣的书吧</p>
      <el-button type="primary" @click="$router.push('/books')">去浏览图书</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const tableData = ref([])

const borrowCount = computed(() => {
  return tableData.value.filter(item => item.status === 0).length
})
const returnCount = computed(() => {
  return tableData.value.filter(item => item.status === 1).length
})

const loadMyBorrows = async () => {
  try {
    const res = await request.get('/api/borrow/my')
    if (res.code === 200) {
      tableData.value = res.data || []
    } else {
      ElMessage.error(res.msg || '加载失败')
    }
  } catch {
    ElMessage.error('加载借阅记录失败，请检查网络')
  }
}

const handleReturn = async (bookId) => {
  try {
    const res = await request.put(`/api/borrow/return/${bookId}`)
    if (res.code === 200) {
      ElMessage.success('还书成功！')
      await loadMyBorrows()
    } else {
      ElMessage.error(res.msg || '还书失败')
    }
  } catch {
    ElMessage.error('还书异常，请稍后重试')
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  if (dateStr.includes('T')) {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', { hour12: false })
  }
  return dateStr
}

onMounted(() => {
  loadMyBorrows()
})
</script>

<style scoped>
.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
}
.dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.borrowing .dot {
  background: #f56c6c;
}
.borrowing {
  color: #f56c6c;
}
.returned .dot {
  background: #67c23a;
}
.returned {
  color: #67c23a;
}
.stat-badge {
  display: inline-block;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}
.borrowing-badge {
  background: #fef0f0;
  color: #f56c6c;
}
.returned-badge {
  background: #f0f9eb;
  color: #67c23a;
}
</style>