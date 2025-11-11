<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

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
  router.push('/login')
}
</script>

<template>
  <div class="global-header">
    <div class="global-header__left" @click="() => router.push('/')">
      <img
        src="/logo.png"
        alt="凌犀零代码平台"
        class="global-header__logo"
      />
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
      <a-button type="primary" @click="handleLoginClick">登录</a-button>
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
