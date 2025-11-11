<script setup lang="ts">
import { GithubOutlined } from '@ant-design/icons-vue'
</script>

<template>
  <div class="global-footer">
    <span class="global-footer__text">凌犀零代码平台 by 梁懿豪</span>
    <a
      class="global-footer__link"
      href="https://github.com/fearlesslyh"
      target="_blank"
      rel="noopener noreferrer"
    >
      <GithubOutlined />
    </a>
  </div>
</template>

<style scoped>
.global-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding-block: 12px;
  color: #6f7782;
  font-size: 14px;
}

.global-footer__text {
  text-align: center;
}

.global-footer__link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: inherit;
  font-size: 18px;
  transition: color 0.2s ease;
}

.global-footer__link:hover {
  color: #1677ff;
}

@media (max-width: 768px) {
  .global-footer {
    flex-direction: column;
    gap: 8px;
    font-size: 12px;
  }

  .global-footer__link {
    font-size: 16px;
  }
}
</style>
