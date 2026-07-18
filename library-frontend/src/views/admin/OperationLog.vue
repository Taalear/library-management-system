<template>
  <div style="padding: 0;">
    <div style="margin-bottom: 20px;">
      <h3 style="margin: 0;">📋 操作日志</h3>
    </div>

    <!-- 筛选表单 -->
    <el-form :inline="true" :model="filters" style="margin-bottom: 20px;">
      <el-form-item label="操作人">
        <el-input v-model="filters.operator" placeholder="输入操作人" clearable style="width: 150px;" />
      </el-form-item>
      <el-form-item label="操作类型">
        <el-select v-model="filters.operationType" placeholder="全部" clearable style="width: 150px;">
          <el-option label="新增图书" value="ADD_BOOK" />
          <el-option label="修改图书" value="UPDATE_BOOK" />
          <el-option label="删除图书" value="DELETE_BOOK" />
          <el-option label="新增分类" value="ADD_CATEGORY" />
          <el-option label="修改分类" value="UPDATE_CATEGORY" />
          <el-option label="删除分类" value="DELETE_CATEGORY" />
        </el-select>
      </el-form-item>
      <el-form-item label="开始时间">
        <el-date-picker v-model="filters.startDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" />
      </el-form-item>
      <el-form-item label="结束时间">
        <el-date-picker v-model="filters.endDate" type="date" placeholder="选择日期" value-format="yyyy-MM-dd" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadLogs">筛选</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 日志表格 -->
    <el-table :data="tableData" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="operator" label="操作人" width="120" />
      <el-table-column prop="operationType" label="操作类型" width="150">
        <template #default="scope">
          <el-tag :type="getTagType(scope.row.operationType)" size="small">
            {{ getTypeLabel(scope.row.operationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="target" label="操作目标" width="150" />
      <el-table-column prop="details" label="详情" />
      <el-table-column prop="ipAddress" label="IP地址" width="150" />
      <el-table-column prop="createdAt" label="操作时间" width="180">
        <template #default="scope">{{ formatDate(scope.row.createdAt) }}</template>
      </el-table-column>
    </el-table>

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const filters = ref({
  operator: '',
  operationType: '',
  startDate: '',
  endDate: ''
})

const getTagType = (type) => {
  const map = {
    'ADD_BOOK': 'success',
    'UPDATE_BOOK': 'warning',
    'DELETE_BOOK': 'danger',
    'ADD_CATEGORY': 'success',
    'UPDATE_CATEGORY': 'warning',
    'DELETE_CATEGORY': 'danger'
  }
  return map[type] || 'info'
}

const getTypeLabel = (type) => {
  const map = {
    'ADD_BOOK': '新增图书',
    'UPDATE_BOOK': '修改图书',
    'DELETE_BOOK': '删除图书',
    'ADD_CATEGORY': '新增分类',
    'UPDATE_CATEGORY': '修改分类',
    'DELETE_CATEGORY': '删除分类'
  }
  return map[type] || type
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { hour12: false })
}

const loadLogs = async () => {
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      ...filters.value
    }
    const res = await request.get('/api/logs', { params })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch {
    ElMessage.error('加载日志失败')
  }
}

const handlePageChange = (val) => {
  pageNum.value = val
  loadLogs()
}

const resetFilters = () => {
  filters.value = { operator: '', operationType: '', startDate: '', endDate: '' }
  pageNum.value = 1
  loadLogs()
}

onMounted(loadLogs)
</script>