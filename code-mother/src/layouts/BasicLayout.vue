<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import {healthCheck} from '@/api/codeMother/healthController'

healthCheck().then(res=>{

  console.log('healthCheck',res)

})
</script>

<template>
  <a-layout class="basic-layout">
    <a-layout-header class="basic-layout__header">
      <GlobalHeader />
    </a-layout-header>
    <a-layout-content class="basic-layout__content">
      <div class="basic-layout__content-inner">
        <RouterView />
      </div>
    </a-layout-content>
    <a-layout-footer class="basic-layout__footer">
      <GlobalFooter />
    </a-layout-footer>
  </a-layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.basic-layout__header {
  background-color: #ffffff;
  padding-inline: 0;
}

.basic-layout__content {
  flex: 1;
  padding: 24px;
  background-color: #f5f5f5;
}

.basic-layout__content-inner {
  min-height: 100%;
  background-color: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.basic-layout__footer {
  background-color: #ffffff;
  padding: 16px 24px;
}

@media (max-width: 768px) {
  .basic-layout__content {
    padding: 16px;
  }

  .basic-layout__content-inner {
    padding: 16px;
  }
}
</style>
