<template>
  <div style="padding: 0;">
    <!-- 搜索栏 + 筛选 -->
    <div style="margin-bottom: 20px; display: flex; align-items: center; flex-wrap: wrap; gap: 10px;">
      <el-input
        v-model="searchName"
        placeholder="输入书名搜索"
        style="width: 200px;"
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
        <el-option
          v-for="item in categoryList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" @click="loadBooks">搜索</el-button>
      <el-button
        v-if="userRole === 'ADMIN'"
        type="success"
        @click="handleAdd"
      >
        + 新增图书
      </el-button>
      <el-button
        v-if="userRole === 'ADMIN'"
        type="info"
        size="small"
        @click="router.push('/admin/categories')"
      >
        分类管理
      </el-button>
      <!-- 批量操作按钮 -->
      <el-button
        v-if="userRole === 'ADMIN'"
        type="danger"
        size="small"
        :disabled="selectedIds.length === 0"
        @click="batchDelete"
      >
        批量删除 ({{ selectedIds.length }})
      </el-button>
      <el-button
        v-if="userRole === 'ADMIN'"
        type="primary"
        plain
        @click="handleExport"
      >
        📥 导出当前页
      </el-button>
      <el-button
        v-if="userRole === 'ADMIN'"
        type="primary"
        plain
        @click="exportAll"
      >
        📥 导出全部
      </el-button>
      <el-button
        v-if="userRole === 'ADMIN'"
        type="primary"
        plain
        @click="handleImport"
      >
        📤 导入 Excel
      </el-button>
      <input type="file" ref="fileInput" accept=".xlsx,.xls" style="display:none" @change="handleFileChange" />
    </div>

    <!-- 图书表格（增加多选列） -->
    <el-table
      :data="tableData"
      border
      stripe
      style="width: 100%;"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="bookName" label="书名" />
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="categoryName" label="分类" width="130">
        <template #default="scope">
          <span
            v-if="scope.row.categoryName"
            class="category-tag"
            :style="{
              backgroundColor: getCategoryColor(scope.row.categoryId, scope.row.categoryName).bg,
              color: getCategoryColor(scope.row.categoryId, scope.row.categoryName).text,
              borderColor: getCategoryColor(scope.row.categoryId, scope.row.categoryName).border
            }"
          >
            {{ scope.row.categoryName }}
          </span>
          <span v-else style="color: #ccc; font-size: 13px;">未分类</span>
        </template>
      </el-table-column>
      <el-table-column prop="price" label="价格" width="120">
        <template #default="scope">￥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column label="操作" width="350" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.stock > 0"
            type="primary"
            size="small"
            @click="handleBorrow(scope.row.id)"
          >借书</el-button>
          <el-button v-else type="info" size="small" disabled>无库存</el-button>
          <el-button
            type="success"
            size="small"
            @click="handleReturn(scope.row.id)"
            style="margin-left: 5px;"
          >还书</el-button>
          <el-button
            v-if="userRole === 'ADMIN'"
            type="warning"
            size="small"
            @click="handleEdit(scope.row)"
            style="margin-left: 5px;"
          >编辑</el-button>
          <el-button
            v-if="userRole === 'ADMIN'"
            type="danger"
            size="small"
            @click="handleDelete(scope.row.id)"
            style="margin-left: 5px;"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
      <el-pagination
        @current-change="handlePageChange"
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        hide-on-single-page
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="书名">
          <el-input v-model="formData.bookName" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="formData.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" clearable style="width: 100%;">
            <el-option
              v-for="item in categoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="formData.price" :precision="2" :step="1" :min="0" controls-position="right" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="formData.stock" :step="1" :min="0" controls-position="right" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '用户')
const userRole = ref(localStorage.getItem('role') || 'USER')

// ---------- 表格数据 ----------
const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(5)
const searchName = ref('')
const searchCategoryId = ref(null)

// ---------- 分类列表 ----------
const categoryList = ref([])

// ---------- 选中项 ----------
const selectedIds = ref([])

// ---------- 莫兰迪色板（20种） ----------
const MORANDI_PALETTE = [
  { bg: '#f5e8ea', text: '#b0888f', border: '#e6d0d4' },
  { bg: '#e7f0e7', text: '#7fa080', border: '#cde0ce' },
  { bg: '#e5eaf2', text: '#7a8fa8', border: '#cdd6e6' },
  { bg: '#f2ede6', text: '#b09a82', border: '#e3d9cd' },
  { bg: '#f0e8f0', text: '#b08ab0', border: '#e0cde0' },
  { bg: '#f2ede6', text: '#b09a82', border: '#e3d9cd' },
  { bg: '#e6f2ef', text: '#7aa09a', border: '#cde4df' },
  { bg: '#f2e6e6', text: '#b08080', border: '#e3cdcd' },
  { bg: '#e5edf2', text: '#7a8aa8', border: '#cdd6e3' },
  { bg: '#eff2e6', text: '#8aa080', border: '#dde3cd' },
  { bg: '#f5ece6', text: '#b09480', border: '#e8d9cd' },
  { bg: '#e8edf0', text: '#889aa0', border: '#d0dae0' },
  { bg: '#f0ece6', text: '#a89880', border: '#e0d8cd' },
  { bg: '#e6f0ed', text: '#7a9e94', border: '#cde0da' },
  { bg: '#f2e6ed', text: '#b0889a', border: '#e3cdd6' },
  { bg: '#edf0e6', text: '#8e9e80', border: '#dae0cd' },
  { bg: '#f0e8ed', text: '#a88aa0', border: '#e0d0da' },
  { bg: '#e6edf0', text: '#7a8e9e', border: '#cddae0' },
  { bg: '#f2ebe6', text: '#b08a80', border: '#e3d5cd' },
  { bg: '#edf0ed', text: '#8e9e8e', border: '#dae0da' },
]

const categoryColorMap = new Map()
const getCategoryColor = (categoryId, categoryName) => {
  if (!categoryId) return MORANDI_PALETTE[0]
  if (categoryColorMap.has(categoryId)) return categoryColorMap.get(categoryId)

  const usedIndices = new Set()
  for (const color of categoryColorMap.values()) {
    const index = MORANDI_PALETTE.indexOf(color)
    if (index !== -1) usedIndices.add(index)
  }
  let nextIndex = categoryColorMap.size % MORANDI_PALETTE.length
  while (usedIndices.has(nextIndex)) {
    nextIndex = (nextIndex + 1) % MORANDI_PALETTE.length
  }
  const color = MORANDI_PALETTE[nextIndex]
  categoryColorMap.set(categoryId, color)
  return color
}

const loadCategories = async () => {
  try {
    const res = await request.get('/api/categories/all')
    if (res.code === 200) {
      const categories = res.data || []
      categories.sort((a, b) => a.id - b.id)
      categoryList.value = categories

      let colorIndex = 0
      const usedIndices = new Set()
      for (const color of categoryColorMap.values()) {
        const idx = MORANDI_PALETTE.indexOf(color)
        if (idx !== -1) usedIndices.add(idx)
      }
      for (const cat of categories) {
        if (!categoryColorMap.has(cat.id)) {
          while (usedIndices.has(colorIndex)) {
            colorIndex = (colorIndex + 1) % MORANDI_PALETTE.length
          }
          categoryColorMap.set(cat.id, MORANDI_PALETTE[colorIndex])
          usedIndices.add(colorIndex)
          colorIndex = (colorIndex + 1) % MORANDI_PALETTE.length
        }
      }
    }
  } catch {
    console.warn('加载分类失败')
  }
}

const loadBooks = async () => {
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
    } else {
      ElMessage.error(res.msg || '加载数据失败')
    }
  } catch {
    ElMessage.error('获取数据异常，请检查网络')
  }
}

const handlePageChange = (val) => {
  pageNum.value = val
  loadBooks()
}

// ---------- 借书 ----------
const handleBorrow = async (bookId) => {
  try {
    const res = await request.post(`/api/borrow/${bookId}`)
    if (res.code === 200) {
      ElMessage.success('借书成功！')
      loadBooks()
    } else {
      ElMessage.error(res.msg || '借书失败')
    }
  } catch {
    ElMessage.error('借书异常，请稍后重试')
  }
}

// ---------- 还书 ----------
const handleReturn = async (bookId) => {
  try {
    const res = await request.put(`/api/borrow/return/${bookId}`)
    if (res.code === 200) {
      ElMessage.success('还书成功！')
      loadBooks()
    } else {
      ElMessage.error(res.msg || '还书失败')
    }
  } catch {
    ElMessage.error('还书异常，请稍后重试')
  }
}

// ---------- 新增/编辑弹窗 ----------
const dialogVisible = ref(false)
const dialogTitle = ref('新增图书')
const formData = ref({ id: null, bookName: '', author: '', price: 0, stock: 0, categoryId: null })

const handleAdd = () => {
  dialogTitle.value = '新增图书'
  formData.value = { id: null, bookName: '', author: '', price: 0, stock: 0, categoryId: null }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑图书'
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.value.bookName) {
    ElMessage.warning('书名不能为空')
    return
  }
  try {
    let res
    if (formData.value.id) {
      res = await request.put('/api/books', formData.value)
    } else {
      res = await request.post('/api/books', formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(res.msg || '操作成功')
      dialogVisible.value = false
      loadBooks()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    ElMessage.error('保存异常，请检查后端服务')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该图书吗？该操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/api/books/${id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadBooks()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch {
      ElMessage.error('删除异常')
    }
  }).catch(() => {})
}

// ---------- 多选处理 ----------
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// ---------- 批量删除 ----------
const batchDelete = async () => {
  if (selectedIds.value.length === 0) return
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 本图书吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete('/api/books/batch', { data: selectedIds.value })
      if (res.code === 200) {
        ElMessage.success(res.msg)
        selectedIds.value = []
        loadBooks()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch {
      ElMessage.error('删除异常')
    }
  }).catch(() => {})
}

// ---------- 导出当前页 ----------
const handleExport = () => {
  window.open('/api/books/export', '_blank')
}

// ---------- 导出全部 ----------
const exportAll = () => {
  window.open('/api/books/export-all', '_blank')
}

// ---------- Excel 导入 ----------
const fileInput = ref(null)
const handleImport = () => {
  fileInput.value.click()
}
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)

  try {
    const res = await request.post('/api/books/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    if (res.code === 200) {
      ElMessage.success(res.msg)
      loadBooks()
    } else {
      ElMessage.error(res.msg || '导入失败')
    }
  } catch {
    ElMessage.error('导入失败，请检查文件格式')
  } finally {
    fileInput.value.value = '' // 重置 input
  }
}

// ---------- 生命周期 ----------
onMounted(() => {
  loadCategories()
  loadBooks()
})
</script>

<style scoped>
.category-tag {
  display: inline-block;
  padding: 2px 14px;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid;
  transition: all 0.2s ease;
  letter-spacing: 0.3px;
  line-height: 1.6;
}
.category-tag:hover {
  transform: scale(1.04);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
</style>