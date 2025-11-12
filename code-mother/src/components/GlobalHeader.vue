<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { userLogout } from '@/api/codeMother/userController'
import type { BaseResponseBoolean } from '@/api/codeMother/typings'

const menuItems = ref([
  {
    key: 'home',
    label: '首页',
    path: '/',
  },
  {
    key: 'about',
    label: '关于我们',
    path: '/about',
  },
])

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const { isLoggedIn, userState } = storeToRefs(loginUserStore)

const selectedKeys = computed(() => {
  const currentItem = menuItems.value.find((item) => item.path === route.path)
  return currentItem ? [currentItem.key] : []
})

const handleMenuClick = ({ key }: { key: string }) => {
  const targetItem = menuItems.value.find((item) => item.key === key)
  if (!targetItem) {
    return
  }
  router.push(targetItem.path)
}

const handleLoginClick = () => {
  router.push('/user/login')
}

const handleLogout = async () => {
  try {
    // 先清除本地状态
    loginUserStore.logout()
    message.success('正在退出登录...')
    
    // 尝试调用后端退出接口
    try {
      const response = await userLogout()
      const parsed = response as BaseResponseBoolean
      if (parsed.code !== 0) {
        console.warn('退出登录接口调用失败:', parsed.message)
      }
    } catch (apiError) {
      console.warn('退出登录接口调用异常:', apiError)
      // 即使接口调用失败，也继续退出流程
    }
    
    // 跳转到登录页面
    await router.push('/user/login')
    
    // 强制刷新页面以清除所有缓存数据和状态
    setTimeout(() => {
      window.location.reload()
    }, 100)
  } catch (error) {
    message.error('退出登录过程中发生错误')
    // 如果发生严重错误，也尝试刷新页面
    setTimeout(() => {
      window.location.reload()
    }, 100)
  }
}

const userDropdownItems = [
  {
    key: 'profile',
    label: '个人资料',
    icon: '👤',
  },
  {
    key: 'settings',
    label: '设置',
    icon: '⚙️',
  },
  {
    type: 'divider',
  },
  {
    key: 'logout',
    label: '退出登录',
    icon: '🚪',
    danger: true,
  },
]

const handleUserMenuClick = ({ key }: { key: string }) => {
  if (key === 'logout') {
    handleLogout()
  } else if (key === 'profile') {
    // TODO: 跳转到个人资料页面
    message.info('个人资料功能开发中')
  } else if (key === 'settings') {
    // TODO: 跳转到设置页面
    message.info('设置功能开发中')
  }
}
</script>

<template>
  <div class="global-header">
    <div class="global-header__left" @click="() => router.push('/')">
      <img src="/logo.png" alt="凌犀零代码平台" class="global-header__logo" />
      <span class="global-header__title">凌犀零代码平台</span>
    </div>
    <div class="global-header__center">
      <a-menu
        mode="horizontal"
        :selectedKeys="selectedKeys"
        :items="menuItems"
        class="global-header__menu"
        @click="handleMenuClick"
      />
    </div>
    <div class="global-header__right">
      <template v-if="isLoggedIn">
        <a-dropdown 
          :trigger="['hover']" 
          placement="bottomRight"
        >
          <div class="global-header__user-container">
            <a-avatar :src="userState.user?.avatar" size="large">
              {{ userState.user?.nickname?.charAt(0) ?? '访客' }}
            </a-avatar>
            <div class="global-header__user-info">
              <div class="global-header__user-name">{{ userState.user?.nickname ?? '未命名用户' }}</div>
              <div class="global-header__user-id">ID: {{ userState.user?.id }}</div>
            </div>
          </div>
          <template #overlay>
            <a-menu :items="userDropdownItems" @click="handleUserMenuClick" />
          </template>
        </a-dropdown>
      </template>
      <template v-else>
        <a-button type="primary" @click="handleLoginClick">登录</a-button>
      </template>
    </div>
  </div>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-inline: 24px;
  height: 64px;
}

.global-header__left {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.global-header__logo {
  width: 36px;
  height: 36px;
  object-fit: cover;
}

.global-header__title {
  font-size: 18px;
  font-weight: 600;
  color: #111a2c;
  white-space: nowrap;
}

.global-header__center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.global-header__menu {
  border-bottom: none;
}

.global-header__right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.global-header__user-container {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

.global-header__user-container:hover {
  background-color: rgba(0, 0, 0, 0.06);
}

.global-header__user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.global-header__user-name {
  font-weight: 600;
  color: #111a2c;
}

.global-header__user-id {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

@media (max-width: 768px) {
  .global-header {
    flex-wrap: wrap;
    height: auto;
    padding-block: 12px;
  }

  .global-header__center {
    order: 3;
    width: 100%;
    justify-content: flex-start;
  }

  .global-header__menu {
    width: 100%;
  }
}
</style>
