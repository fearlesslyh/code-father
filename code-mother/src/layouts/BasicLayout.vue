<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { useThemeStore } from '@/stores/theme'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const themeStore = useThemeStore()
const { currentTheme } = storeToRefs(themeStore)

const isDarkMode = computed(() => currentTheme.value === 'dark')

onMounted(async () => {
  const loggedIn = await loginUserStore.fetchLoginUser()
  if (!loggedIn) {
    router.replace('/user/login')
    return
  }
  await loginUserStore.updateHeartbeat()
})
</script>

<template>
  <div class="app-layout" :class="{ 'dark-mode': isDarkMode }">
    <GlobalHeader />
    <main class="app-content">
      <RouterView />
    </main>
    <GlobalFooter />
  </div>
</template>

<style scoped>
.app-layout {
  position: relative;
  min-height: 100vh;
}

/* 日间模式 - 李白的奇迹 (color.oulu.me #036) */
.app-layout:not(.dark-mode) {
  background-image: linear-gradient(to top, #cd9cf2 0%, #f6f3ff 100%);
  background-attachment: fixed;
  background-size: 200% 200%;
  animation: gradientShift 15s ease infinite;
}

/* 夜间模式 - 炫彩渐变 */
.app-layout.dark-mode {
  background-image: linear-gradient(-225deg, #231557 0%, #44107A 29%, #FF1361 67%, #FFF800 100%);
  background-attachment: fixed;
  background-size: 200% 200%;
  animation: gradientShift 20s ease infinite;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 内容区域 */
.app-content {
  position: relative;
  z-index: 1;
}

/* Header 和 Footer 样式 */
.app-layout :deep(.ant-layout-header),
.app-layout :deep(.app-footer) {
  position: relative;
  z-index: 2;
}

/* 日间模式 Header/Footer */
.app-layout:not(.dark-mode) :deep(.ant-layout-header),
.app-layout:not(.dark-mode) :deep(.app-footer) {
  background: rgba(255, 255, 255, 0.8) !important;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(14, 165, 233, 0.2);
}

.app-layout:not(.dark-mode) :deep(.app-footer) {
  border-top: 1px solid rgba(14, 165, 233, 0.2);
  border-bottom: none;
}

/* 夜间模式 Header/Footer */
.app-layout.dark-mode :deep(.ant-layout-header),
.app-layout.dark-mode :deep(.app-footer) {
  background: rgba(15, 23, 42, 0.8) !important;
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(59, 130, 246, 0.2);
}

.app-layout.dark-mode :deep(.app-footer) {
  border-top: 1px solid rgba(59, 130, 246, 0.2);
  border-bottom: none;
}
</style>
