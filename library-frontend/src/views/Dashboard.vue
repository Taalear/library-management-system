<template>
  <div>
    <div class="page-header">数据总览</div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6" v-for="(item, index) in statCards" :key="index">
        <el-card shadow="hover" style="text-align: center; padding: 20px 0;">
          <div style="font-size: 14px; color: #86909c;">{{ item.label }}</div>
          <div style="font-size: 32px; font-weight: 600; color: #3c3a47; margin-top: 8px;">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 500;">📈 近7天借阅趋势</span>
          </template>
          <div id="trendChart" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 500;">🏆 热门图书 Top 5</span>
          </template>
          <div style="display: flex; justify-content: flex-end; margin-bottom: 12px;">
            <span style="margin-right: 8px; color: #86909c; font-size: 13px;">时间：</span>
            <el-select v-model="timeRange" size="small" style="width: 120px;" @change="loadTopBooks">
              <el-option label="全部" value="all" />
              <el-option label="本周" value="week" />
              <el-option label="本月" value="month" />
            </el-select>
          </div>
          <div id="topBooksChart" style="width: 100%; height: 260px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近借阅记录 -->
    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 500;">📋 最近借阅动态</span>
          </template>
          <el-table :data="recentBorrows" border stripe style="width: 100%;">
            <el-table-column prop="username" label="用户" width="120"></el-table-column>
            <el-table-column prop="bookName" label="书名"></el-table-column>
            <el-table-column prop="borrowTime" label="借书时间" width="180">
              <template #default="scope">{{ formatDate(scope.row.borrowTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="scope">
                <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'" size="small">
                  {{ scope.row.status === 0 ? '借出中' : '已归还' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import * as echarts from 'echarts'

// ===== 统计卡片 =====
const statCards = ref([
  { label: '📚 图书总数', value: 0 },
  { label: '👥 用户总数', value: 0 },
  { label: '📖 总借阅次数', value: 0 },
  { label: '🔄 当前借出中', value: 0 }
])

// ===== 最近借阅记录 =====
const recentBorrows = ref([])

// ===== 时间范围 =====
const timeRange = ref('all')

// ===== 加载概览数据 =====
const loadStats = async () => {
  try {
    const res = await request.get('/api/stat/overview')
    if (res.code === 200) {
      const d = res.data
      statCards.value[0].value = d.bookCount || 0
      statCards.value[1].value = d.userCount || 0
      statCards.value[2].value = d.borrowCount || 0
      statCards.value[3].value = d.activeBorrowCount || 0
    }
  } catch (e) {
    console.warn('加载统计数据失败', e)
  }
}

// ===== 加载最近借阅记录 =====
const loadRecentBorrows = async () => {
  try {
    const res = await request.get('/api/borrow/recent?limit=5')
    if (res.code === 200) {
      recentBorrows.value = res.data || []
    }
  } catch (e) {
    console.warn('加载最近借阅失败', e)
  }
}

// ===== 格式化日期 =====
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', { hour12: false })
}

// ===== 图表数据 =====
let trendData = []
let topBooksData = []

// ===== 加载热门图书 =====
const loadTopBooks = async () => {
  try {
    const res = await request.get('/api/stat/top-books', { params: { range: timeRange.value } })
    if (res.code === 200) {
      topBooksData = res.data || []
      renderTopBooksChart()
    }
  } catch (e) {
    console.warn('加载热门图书失败', e)
  }
}

// ===== 渲染图表 =====
const renderCharts = async () => {
  // 获取趋势数据
  try {
    const res = await request.get('/api/stat/trend')
    if (res.code === 200) trendData = res.data || []
  } catch (e) {
    console.warn('加载趋势数据失败', e)
    trendData = []
  }

  // 获取热门图书（首次加载使用默认全部）
  await loadTopBooks()

  // 渲染折线图（趋势）
  const trendChart = echarts.init(document.getElementById('trendChart'))
  const dateList = trendData.map(item => item.date)
  const countList = trendData.map(item => item.count)

  const hasTrendData = dateList.length > 0 && countList.some(c => c > 0)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: hasTrendData ? dateList : ['暂无数据'],
      axisLine: { show: false },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      min: hasTrendData ? undefined : 0,
      max: hasTrendData ? undefined : 1
    },
    series: [{
      data: hasTrendData ? countList : [0],
      type: 'line',
      smooth: true,
      lineStyle: { color: '#b39ddb', width: 3 },
      areaStyle: { color: 'rgba(179, 157, 219, 0.15)' },
      symbol: 'circle',
      symbolSize: 8,
      itemStyle: { color: '#b39ddb' }
    }]
  })

  // 窗口自适应
  window.addEventListener('resize', () => {
    trendChart.resize()
    const topChart = echarts.getInstanceByDom(document.getElementById('topBooksChart'))
    if (topChart) topChart.resize()
  })
}

// ===== 渲染柱状图（热门图书） =====
const renderTopBooksChart = () => {
  const topChart = echarts.init(document.getElementById('topBooksChart'))
  const bookNames = topBooksData.map(item => {
    const name = item.bookName || '未知'
    return name.length > 6 ? name.substring(0, 6) + '..' : name
  })
  const bookCounts = topBooksData.map(item => item.borrowCount || 0)

  const hasTopData = bookNames.length > 0 && bookCounts.some(c => c > 0)

  topChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '5%', right: '5%', bottom: '10%', top: '5%', containLabel: true },
    xAxis: {
      type: 'category',
      data: hasTopData ? bookNames : ['暂无数据'],
      axisLabel: { fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      splitLine: { show: false },
      min: hasTopData ? undefined : 0,
      max: hasTopData ? undefined : 1
    },
    series: [{
      data: hasTopData ? bookCounts : [0],
      type: 'bar',
      barWidth: '40%',
      itemStyle: {
        borderRadius: [4, 4, 0, 0],
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#b39ddb' },
          { offset: 1, color: '#d1c4e9' }
        ])
      }
    }]
  })
}

// ===== 生命周期 =====
onMounted(() => {
  loadStats()
  loadRecentBorrows()
  setTimeout(renderCharts, 300)
})
</script>