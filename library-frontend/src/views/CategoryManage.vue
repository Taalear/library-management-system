<template>
  <div style="padding: 0;">
    <!-- 头部 -->
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
      <h3 style="margin: 0;">📂 分类管理</h3>
      <el-button type="primary" @click="handleAdd">+ 新增分类</el-button>
    </div>

    <!-- 分类表格 -->
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button type="warning" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form :model="formData" label-width="80px">
        <el-form-item label="分类名称">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" controls-position="right" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('新增分类')
const formData = ref({ id: null, name: '', sortOrder: 0 })

const loadCategories = async () => {
  try {
    const res = await request.get('/api/categories', {
      params: { pageNum: pageNum.value, pageSize: pageSize.value }
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('加载分类失败')
  }
}

const handlePageChange = (val) => {
  pageNum.value = val
  loadCategories()
}

const handleAdd = () => {
  dialogTitle.value = '新增分类'
  formData.value = { id: null, name: '', sortOrder: 0 }
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  formData.value = { ...row }
  dialogVisible.value = true
}

const handleSave = async () => {
  if (!formData.value.name) {
    ElMessage.warning('分类名称不能为空')
    return
  }
  try {
    let res
    if (formData.value.id) {
      res = await request.put('/api/categories', formData.value)
    } else {
      res = await request.post('/api/categories', formData.value)
    }
    if (res.code === 200) {
      ElMessage.success(res.msg)
      dialogVisible.value = false
      loadCategories()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch {
    ElMessage.error('保存异常')
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该分类吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.delete(`/api/categories/${id}`)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadCategories()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch {
      ElMessage.error('删除异常')
    }
  }).catch(() => {})
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { hour12: false })
}

onMounted(() => {
  loadCategories()
})
</script>