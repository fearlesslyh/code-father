<script setup lang="ts">
import { onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()

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
  <div class="app-layout">
    <GlobalHeader />
    <main class="app-content">
      <RouterView />
    </main>
    <GlobalFooter />
  </div>
</template>

<style scoped>
/* 使用全局样式，这里不需要额外的样式 */
/* 布局结构由全局CSS变量和类名控制 */
</style>
