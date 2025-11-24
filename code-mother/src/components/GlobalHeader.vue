<script setup lang="ts">
import { computed, ref, h } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { message } from 'ant-design-vue';
import { useLoginUserStore } from '@/stores/loginUser';
import { useThemeStore } from '@/stores/theme';
import { userLogout } from '@/api/codeMother/userController';
import type { BaseResponseBoolean } from '@/api/codeMother/typings';
import { UserOutlined, BulbOutlined, LogoutOutlined, CrownOutlined, SkinOutlined, ApiOutlined, MessageOutlined } from '@ant-design/icons-vue';
import UserProfileModal from './UserProfileModal.vue';

interface MenuItem {
  key: string;
  label: string;
  icon?: () => unknown;
  type?: string;
  danger?: boolean;
}

const menuItems = computed(() => {
  const baseItems = [
    {
      key: 'home',
      label: '首页',
      path: '/',
    },
    {
      key: 'about',
      label: '关于我',
      path: '/about',
    },
  ]
  
  // 管理员显示管理菜单项
  if (userState.value.user?.userRole === 'admin') {
    baseItems.push({
      key: 'admin-users',
      label: '用户管理',
      path: '/admin/userManager',
    })
    baseItems.push({
      key: 'admin-apps',
      label: '应用管理',
      path: '/admin/appManager',
    })
    baseItems.push({
      key: 'admin-chats',
      label: '对话管理',
      path: '/admin/chatHistoryManager',
    })
  }
  
  return baseItems
})

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()
const themeStore = useThemeStore()
const { isLoggedIn, userState } = storeToRefs(loginUserStore)
const { currentTheme } = storeToRefs(themeStore)

// 个人资料编辑弹窗
const profileModalVisible = ref(false)

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
  } catch {
    message.error('退出登录过程中发生错误')
    // 如果发生严重错误，也尝试刷新页面
    setTimeout(() => {
      window.location.reload()
    }, 100)
  }
}

const userDropdownItems = computed(() => {
  const baseItems: MenuItem[] = [
    {
      key: 'profile',
      label: '个人资料',
      icon: () => h(UserOutlined),
    },
    {
      key: 'theme',
      label: currentTheme.value === 'dark' ? '切换日间模式' : '切换夜间模式',
      icon: () => h(currentTheme.value === 'dark' ? BulbOutlined : SkinOutlined),
    },
  ];

  if (userState.value.user?.userRole === 'admin') {
    baseItems.push({
      key: 'admin-users',
      label: '用户管理',
      icon: () => h(CrownOutlined),
    });
    baseItems.push({
      key: 'admin-apps',
      label: '应用管理',
      icon: () => h(ApiOutlined),
    });
    baseItems.push({
      key: 'admin-chats',
      label: '对话管理',
      icon: () => h(MessageOutlined),
    });
  }

  baseItems.push({
    key: 'divider',
    label: '',
    type: 'divider',
  });

  baseItems.push({
    key: 'logout',
    label: '退出登录',
    icon: () => h(LogoutOutlined),
    danger: true,
  });

  return baseItems;
});

const handleUserMenuClick = ({ key }: { key: string }) => {
  switch (key) {
    case 'logout':
      handleLogout();
      break;
    case 'profile':
      profileModalVisible.value = true;
      break;
    case 'theme':
      themeStore.toggleTheme();
      message.success(`已切换至${currentTheme.value === 'dark' ? '夜间' : '日间'}模式`);
      break;
    case 'admin-users':
      router.push('/admin/userManager');
      break;
    case 'admin-apps':
      router.push('/admin/appManager');
      break;
    case 'admin-chats':
      router.push('/admin/chatHistoryManager');
      break;
  }
};

const handleProfileUpdateSuccess = () => {
  message.success('个人资料已更新');
};
</script>

<template>
  <header class="app-header glass-effect">
    <div class="header-container">
      <div class="header-left" @click="() => router.push('/')">
        <div class="logo-container">
          <img src="/logo.png" alt="凌犀零代码平台" class="logo" />
          <div class="logo-glow"></div>
        </div>
        <h1 class="site-title">
          <span class="title-text">凌犀零代码平台</span>
          <span class="title-glow"></span>
        </h1>
      </div>
      
      <nav class="header-center">
        <a-menu
          mode="horizontal"
          :selectedKeys="selectedKeys"
          :items="menuItems"
          class="nav-menu"
          @click="handleMenuClick"
        />
      </nav>
      
      <div class="header-right">
        <template v-if="isLoggedIn">
          <a-dropdown 
            :trigger="['hover']" 
            placement="bottomRight"
            class="user-dropdown"
          >
            <div class="user-container neon-glow">
              <a-avatar :src="userState.user?.avatar" size="large" class="user-avatar">
                {{ userState.user?.nickname?.charAt(0) ?? '访客' }}
              </a-avatar>
              <div class="user-info">
                <div class="user-name">{{ userState.user?.nickname ?? '未命名用户' }}</div>
                <div class="user-id">ID: {{ userState.user?.id }}</div>
              </div>
            </div>
            <template #overlay>
              <a-menu :items="userDropdownItems" @click="handleUserMenuClick" class="dropdown-menu" />
            </template>
          </a-dropdown>
        </template>
        <template v-else>
          <a-button type="primary" @click="handleLoginClick" class="tech-button login-btn">
            登录
          </a-button>
        </template>
      </div>
    </div>
    
    <!-- 用户个人资料编辑弹窗 -->
    <UserProfileModal 
      v-model:visible="profileModalVisible" 
      @success="handleProfileUpdateSuccess"
    />
  </header>
</template>

<style scoped>
/* 头部容器样式 - 日间模式 */
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  background-image: linear-gradient(to top, #fddb92 0%, #d1fdff 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(20px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 夜间模式 Header 使用紫粉渐变 */
[data-theme='dark'] .app-header {
  background-image: linear-gradient(-225deg, #7742B2 0%, #F180FF 52%, #FD8BD9 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--spacing-md);
  padding: 0 var(--spacing-lg);
  height: var(--header-height);
}

/* 左侧Logo和标题区域 */
.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  cursor: pointer;
  transition: all var(--transition-normal);
  flex-shrink: 0;
}

.header-left:hover .logo-glow {
  opacity: 1;
  transform: scale(1.2);
}

.header-left:hover .title-glow {
  opacity: 1;
  transform: scale(1.05);
}

.logo-container {
  position: relative;
  width: 40px;
  height: 40px;
}

.logo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: var(--radius-md);
  position: relative;
  z-index: 2;
}

.logo-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: var(--gradient-primary);
  border-radius: var(--radius-md);
  opacity: 0.6;
  filter: blur(8px);
  transition: all var(--transition-normal);
  z-index: 1;
}

.site-title {
  margin: 0;
  position: relative;
  display: flex;
  align-items: center;
}

.title-text {
  font-size: 20px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  text-shadow: 0 0 20px rgba(120, 219, 255, 0.6);
  letter-spacing: 1px;
  position: relative;
  z-index: 2;
}

.title-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: var(--gradient-primary);
  opacity: 0;
  filter: blur(12px);
  transition: all var(--transition-normal);
  z-index: 1;
}

/* 中间导航菜单 - 水平居中布局 */
.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
}

.nav-menu {
  background: transparent;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;
  width: auto;
}

.nav-menu :deep(.ant-menu-horizontal) {
  display: flex;
  justify-content: center;
  align-items: center;
  border: none;
  background: transparent;
}

/* 夜间模式导航菜单背景修复 */
[data-theme='dark'] .nav-menu,
[data-theme='dark'] .nav-menu :deep(.ant-menu-horizontal) {
  background: transparent !important;
}

.nav-menu :deep(.ant-menu-horizontal > .ant-menu-item) {
  margin: 0 24px;
  padding: 0 20px;
  height: var(--header-height);
  line-height: var(--header-height);
  display: flex;
  align-items: center;
  transition: all var(--transition-normal);
}

.nav-menu :deep(.ant-menu-horizontal > .ant-menu-item:first-child) {
  margin-left: 12px;
}

.nav-menu :deep(.ant-menu-horizontal > .ant-menu-item:last-child) {
  margin-right: 12px;
}

/* 针对特定菜单项类的间距优化 */
.nav-menu :deep(.ant-menu-overflow-item) {
  margin: 0 24px;
}

.nav-menu :deep(.ant-menu-item) {
  margin: 0 24px;
  padding: 0 20px;
}

.nav-menu :deep(.ant-menu-item-selected) {
  margin: 0 24px;
  padding: 0 20px;
}

.nav-menu :deep(.ant-menu-item-only-child) {
  margin: 0 24px;
  padding: 0 20px;
}

.nav-menu :deep(.ant-menu-item) {
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  position: relative;
  background: transparent !important;
}

.nav-menu :deep(.ant-menu-item:hover) {
  color: var(--primary-color);
  background: var(--primary-light) !important;
}

.nav-menu :deep(.ant-menu-item-selected) {
  color: var(--primary-color);
  background: var(--primary-light) !important;
}

.nav-menu :deep(.ant-menu-item::after) {
  display: none;
}

/* 夜间模式导航菜单项样式 */
[data-theme='dark'] .nav-menu :deep(.ant-menu-item) {
  color: rgba(255, 255, 255, 0.85);
  background: transparent !important;
}

[data-theme='dark'] .nav-menu :deep(.ant-menu-item:hover) {
  color: #78dbff;
  background: rgba(120, 219, 255, 0.15) !important;
}

[data-theme='dark'] .nav-menu :deep(.ant-menu-item-selected) {
  color: #78dbff;
  background: rgba(120, 219, 255, 0.2) !important;
}

/* 右侧用户区域 */
.header-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  flex-shrink: 0;
}

.user-dropdown {
  cursor: pointer;
}

.user-container {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
  cursor: pointer;
}

.user-avatar {
  background: var(--gradient-primary);
  border: 2px solid var(--border-primary);
  box-shadow: 0 0 15px rgba(120, 219, 255, 0.4);
  transition: all var(--transition-normal);
}

.user-container:hover .user-avatar {
  transform: scale(1.05);
  box-shadow: 0 0 25px rgba(120, 219, 255, 0.6);
}

.user-info {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-name {
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  font-size: 14px;
  text-shadow: 0 0 8px rgba(120, 219, 255, 0.4);
}

.user-id {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

/* 登录按钮 */
.login-btn {
  padding: 0 var(--spacing-lg);
  height: 40px;
  font-size: 14px;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 0.5px;
}

/* 下拉菜单样式 */
.dropdown-menu {
  background: var(--bg-glass);
  backdrop-filter: blur(20px);
  border: 1px solid var(--border-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: var(--spacing-sm) 0;
}

.dropdown-menu :deep(.ant-menu-item) {
  color: var(--text-primary) !important;
  padding: var(--spacing-sm) var(--spacing-lg);
  margin: 0 var(--spacing-sm);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  font-weight: var(--font-weight-medium);
}

.dropdown-menu :deep(.ant-menu-item:hover) {
  color: var(--text-primary) !important;
  background: var(--primary-light);
  transform: translateX(4px);
}

.dropdown-menu :deep(.ant-menu-item-selected) {
  color: var(--primary-color) !important;
  background: var(--primary-light);
}

.dropdown-menu :deep(.ant-menu-item-danger) {
  color: var(--text-primary) !important;
}

.dropdown-menu :deep(.ant-menu-item-danger:hover) {
  color: var(--text-primary) !important;
  background: rgba(255, 107, 107, 0.15);
  border-left: 3px solid rgba(255, 107, 107, 0.6);
}

.dropdown-menu :deep(.ant-menu-item-divider) {
  margin: var(--spacing-sm) 0;
  background-color: var(--border-secondary);
}

.dropdown-menu :deep(.ant-menu-item .anticon) {
  color: var(--text-primary) !important;
  transition: all var(--transition-fast);
}

.dropdown-menu :deep(.ant-menu-item:hover .anticon) {
  color: var(--primary-color) !important;
  transform: scale(1.1);
}

.dropdown-menu :deep(.ant-menu-item span) {
  color: var(--text-primary) !important;
  transition: all var(--transition-fast);
}

.dropdown-menu :deep(.ant-menu-item:hover span) {
  color: var(--text-primary) !important;
  text-shadow: 0 0 8px rgba(120, 219, 255, 0.4);
}

/* 夜间模式特定优化 */
[data-theme='dark'] .dropdown-menu {
  background: rgba(26, 31, 58, 0.95);
  border-color: rgba(120, 119, 198, 0.4);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6);
}

[data-theme='dark'] .dropdown-menu :deep(.ant-menu-item) {
  color: #ffffff !important;
}

[data-theme='dark'] .dropdown-menu :deep(.ant-menu-item:hover) {
  color: #ffffff !important;
  background: rgba(120, 219, 255, 0.15);
  box-shadow: 0 0 15px rgba(120, 219, 255, 0.2);
}

[data-theme='dark'] .dropdown-menu :deep(.ant-menu-item .anticon) {
  color: #ffffff !important;
}

[data-theme='dark'] .dropdown-menu :deep(.ant-menu-item span) {
  color: #ffffff !important;
}

/* 覆盖 Ant Design 下拉菜单的字体颜色 */
[data-theme='dark'] .dropdown-menu :deep(.ant-dropdown-menu-title-content) {
  color: #ffffff !important;
}

[data-theme='dark'] .dropdown-menu :deep(.ant-dropdown-menu-item) {
  color: #ffffff !important;
}

[data-theme='dark'] .dropdown-menu :deep(.ant-dropdown-menu-item:hover) {
  color: #ffffff !important;
}

[data-theme='dark'] .dropdown-menu :deep(.ant-dropdown-menu-item .anticon) {
  color: #ffffff !important;
}

/* 顶部栏导航菜单夜间模式悬停样式 */
[data-theme='dark'] .nav-menu :deep(.ant-menu-title-content) {
  color: #ffffff !important;
}

[data-theme='dark'] .nav-menu :deep(.ant-menu-item:hover .ant-menu-title-content) {
  color: #ffffff !important;
}

[data-theme='dark'] .nav-menu :deep(.ant-menu-item-selected .ant-menu-title-content) {
  color: #ffffff !important;
}

/* 响应式间距调整 */
@media (max-width: 1024px) {
  .nav-menu :deep(.ant-menu-horizontal > .ant-menu-item),
  .nav-menu :deep(.ant-menu-overflow-item),
  .nav-menu :deep(.ant-menu-item),
  .nav-menu :deep(.ant-menu-item-selected),
  .nav-menu :deep(.ant-menu-item-only-child) {
    margin: 0 20px;
    padding: 0 16px;
  }
}

@media (max-width: 768px) {
  .nav-menu :deep(.ant-menu-horizontal > .ant-menu-item),
  .nav-menu :deep(.ant-menu-overflow-item),
  .nav-menu :deep(.ant-menu-item),
  .nav-menu :deep(.ant-menu-item-selected),
  .nav-menu :deep(.ant-menu-item-only-child) {
    margin: 0 16px;
    padding: 0 12px;
    font-size: 14px;
  }
}

@media (max-width: 480px) {
  .nav-menu :deep(.ant-menu-horizontal > .ant-menu-item),
  .nav-menu :deep(.ant-menu-overflow-item),
  .nav-menu :deep(.ant-menu-item),
  .nav-menu :deep(.ant-menu-item-selected),
  .nav-menu :deep(.ant-menu-item-only-child) {
    margin: 0 12px;
    padding: 0 8px;
    font-size: 13px;
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .header-container {
    padding: 0 var(--spacing-md);
  }
  
  .site-title .title-text {
    font-size: 18px;
  }
}

@media (max-width: 768px) {
  .header-container {
    padding: 0 var(--spacing-md);
    gap: var(--spacing-sm);
  }
  
  .header-left {
    gap: var(--spacing-sm);
  }
  
  .logo-container {
    width: 32px;
    height: 32px;
  }
  
  .site-title .title-text {
    font-size: 16px;
  }
  
  .header-center {
    order: 3;
    width: 100%;
    max-width: none;
    margin: var(--spacing-md) 0 0;
  }
  
  .nav-menu {
    justify-content: flex-start;
  }
  
  .user-info {
    display: none;
  }
  
  .user-container {
    padding: var(--spacing-sm);
  }
}

@media (max-width: 480px) {
  .header-container {
    padding: 0 var(--spacing-sm);
  }
  
  .site-title .title-text {
    font-size: 14px;
  }
  
  .login-btn {
    padding: 0 var(--spacing-md);
    height: 36px;
    font-size: 12px;
  }
}
</style>
