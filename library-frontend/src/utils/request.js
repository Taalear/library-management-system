import axios from 'axios'
import { ElMessage, ElLoading } from 'element-plus'

let loadingInstance = null
let requestCount = 0

const request = axios.create({
    baseURL: '',  // 必须为空字符串，由 Vite 代理转发
    timeout: 10000
})

request.interceptors.request.use(config => {
    const token = localStorage.getItem('token')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    requestCount++
    if (!loadingInstance) {
        loadingInstance = ElLoading.service({
            fullscreen: true,
            text: '加载中...',
            background: 'rgba(0, 0, 0, 0.5)'
        })
    }
    return config
}, error => {
    return Promise.reject(error)
})

request.interceptors.response.use(
    response => {
        requestCount--
        if (requestCount === 0 && loadingInstance) {
            loadingInstance.close()
            loadingInstance = null
        }
        return response.data
    },
    error => {
        requestCount--
        if (requestCount === 0 && loadingInstance) {
            loadingInstance.close()
            loadingInstance = null
        }
        if (error.response?.status === 401) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('role')
            localStorage.removeItem('username')
            window.location.href = '/login'
        } else if (error.response?.status === 403) {
            // 🔥 403 静默处理，不弹窗，仅打印日志
            console.warn('请求被拒绝(403):', error.config?.url)
        } else {
            ElMessage.error(error.message || '网络异常')
        }
        return Promise.reject(error)
    }
)

export default request