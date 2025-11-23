import { createRouter, createWebHistory } from 'vue-router'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import HomePage from '../pages/HomePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '主页',
      component: HomePage,
      meta: {
        requiresAuth: false, // 所有用户均可访问
      },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../pages/AboutPage.vue'),
      meta: {
        requiresAuth: false, // 所有用户均可访问
      },
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('@/pages/user/UserRegisterPage.vue'),
    },
    {
      path: '/admin/userManager',
      name: '用户管理',
      component: UserManagePage,
      meta: {
        requiresAuth: true,
        requiresAdmin: true, // 需要管理员权限
      },
    },
    {
      path: '/admin/appManager',
      name: '应用管理',
      component: () => import('@/pages/admin/AppManagePage.vue'),
      meta: {
        requiresAuth: true,
        requiresAdmin: true, // 需要管理员权限
      },
    },
    {
      path: '/app/detail/:id',
      name: '应用详情',
      component: () => import('@/pages/AppDetailPage.vue'),
      meta: {
        requiresAuth: true, // 需要登录
      },
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: () => import('@/pages/AppChatPage.vue'),
      meta: {
        requiresAuth: true, // 需要登录
      },
    },
    {
      path: '/app/edit/:id',
      name: '应用编辑',
      component: () => import('@/pages/AppEditPage.vue'),
      meta: {
        requiresAuth: true, // 需要登录
      },
    },
    {
      path: '/noAuth',
      name: '无权限',
      component: () => import('@/pages/NoAuthPage.vue'),
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  const { useLoginUserStore } = await import('@/stores/loginUser')
  const loginUserStore = useLoginUserStore()

  // 对于不需要认证的页面，直接放行
  const requiresAuth = to.meta.requiresAuth === true
  if (!requiresAuth) {
    next()
    return
  }

  // 检查用户登录状态
  const loggedIn = await loginUserStore.fetchLoginUser()
  if (!loggedIn) {
    next({ path: '/user/login', query: { redirect: to.fullPath } })
    return
  }

  // 检查是否需要管理员权限
  const requiresAdmin = to.meta.requiresAdmin === true
  if (requiresAdmin) {
    const userRole = loginUserStore.userState.user?.userRole
    if (userRole !== 'admin') {
      // 普通用户访问管理员页面，跳转到无权限页面
      next('/noAuth')
      return
    }
  }

  next()
})

export default router
