import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import PublicLayout from '@/layout/PublicLayout.vue'
import Home from '@/views/Home.vue'
import BookListPublic from '@/views/BookListPublic.vue'
import BookDetail from '@/views/BookDetail.vue'
import MyBorrow from '@/views/MyBorrow.vue'
import Profile from '@/views/Profile.vue'
import MyFavorites from '@/views/MyFavorites.vue'
import AdminLayout from '@/layout/AdminLayout.vue'
import Dashboard from '@/views/Dashboard.vue'
import BookList from '@/views/BookList.vue'  // ✅ 直接使用现有的 BookList.vue
import CategoryManage from '@/views/CategoryManage.vue'
// import OperationLog from '@/views/admin/OperationLog.vue'  // 暂时注释，如需请创建该文件
import { ElMessage } from 'element-plus'

const routes = [
    // 公开路由
    {
        path: '/',
        component: PublicLayout,
        children: [
            { path: '', component: Home },
            { path: 'books', component: BookListPublic },
            { path: 'book/:id', component: BookDetail },
            { path: 'login', component: Login },
            { path: 'register', component: Register }
        ]
    },
    // 用户路由（需登录）
    {
        path: '/user',
        component: PublicLayout,
        meta: { requiresAuth: true },
        children: [
            { path: 'my-borrow', component: MyBorrow },
            { path: 'profile', component: Profile },
            { path: 'favorites', component: MyFavorites }
        ]
    },
    // 管理员路由
    {
        path: '/admin',
        component: AdminLayout,
        meta: { requiresAuth: true, requiresAdmin: true },
        children: [
            { path: '', redirect: '/admin/dashboard' },
            { path: 'dashboard', component: Dashboard },
            { path: 'books', component: BookList },  // ✅ 指向 BookList.vue
            { path: 'categories', component: CategoryManage }
            // { path: 'logs', component: OperationLog }  // 暂时注释
        ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const role = localStorage.getItem('role') || 'USER'

    if (to.meta.requiresAuth) {
        if (!token) {
            ElMessage.warning('请先登录')
            return next('/login')
        }
        if (to.meta.requiresAdmin && role !== 'ADMIN') {
            ElMessage.error('权限不足，需要管理员账号')
            return next('/')
        }
    }

    if (token && (to.path === '/login' || to.path === '/register')) {
        return next('/')
    }

    next()
})

export default router