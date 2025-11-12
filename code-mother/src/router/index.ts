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
        requiresAuth: true,
      },
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../pages/AboutPage.vue'),
      meta: {
        requiresAuth: true,
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
      },
    },
  ],
})

router.beforeEach(async (to, _from, next) => {
  const requiresAuth = to.meta.requiresAuth === true
  if (!requiresAuth) {
    next()
    return
  }
  const { useLoginUserStore } = await import('@/stores/loginUser')
  const loginUserStore = useLoginUserStore()
  const loggedIn = await loginUserStore.fetchLoginUser()
  if (!loggedIn) {
    next({ path: '/user/login', query: { redirect: to.fullPath } })
    return
  }
  if (to.path === '/user/login') {
    next({ path: to.query.redirect?.toString() ?? '/' })
    return
  }
  next()
})

export default router
