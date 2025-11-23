<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { AppVO } from '@/api/codeMother/typings'
import { MessageOutlined, EyeOutlined, StarOutlined } from '@ant-design/icons-vue'
import { getCodeGenTypeName } from '@/constants/codeGenType'

interface Props {
  app: AppVO
  showActions?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showActions: true
})

const router = useRouter()

// 部署域名
const deployDomain = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost'

// 跳转到对话页面
const goToChat = (appId: string) => {
  router.push(`/app/chat/${appId}?view=1`)
}

// 查看作品
const viewApp = (deployKey: string) => {
  if (!deployKey) return
  const deployUrl = `${deployDomain}/${deployKey}`
  window.open(deployUrl, '_blank')
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}
</script>

<template>
  <div class="app-card tech-card" :class="{ featured: app.priority === 99 }">
    <!-- 封面图 -->
    <div class="app-card-cover">
      <img 
        v-if="app.cover" 
        :src="app.cover" 
        :alt="app.appName"
        class="cover-image"
      />
      <div v-else class="cover-placeholder">
        <span class="cover-icon">&lt;/&gt;</span>
      </div>
      <!-- 精选标识 -->
      <div v-if="app.priority === 99" class="featured-badge">
        <StarOutlined />
      </div>
    </div>
    
    <!-- 应用信息 -->
    <div class="app-card-info">
      <div class="app-card-header">
        <span class="app-icon">&lt;/&gt;</span>
        <h3 class="app-name">{{ app.appName || '未命名应用' }}</h3>
      </div>
      <div class="app-card-footer">
        <span class="app-author">{{ app.user?.userName || '匿名用户' }}</span>
      </div>
    </div>
    
    <!-- 悬浮按钮 -->
    <div class="app-card-hover-actions" v-if="showActions">
      <a-button type="primary" size="small" @click.stop="goToChat(String(app.id!))">
        <template #icon>
          <MessageOutlined />
        </template>
        查看对话
      </a-button>
      <a-button 
        v-if="app.deployKey" 
        size="small" 
        @click.stop="viewApp(app.deployKey!)"
      >
        <template #icon>
          <EyeOutlined />
        </template>
        查看作品
      </a-button>
    </div>
  </div>
</template>

<style scoped>
.app-card {
  position: relative;
  display: flex;
  flex-direction: column;
  transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  overflow: hidden;
  height: 100%;
}

.app-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.app-card:hover .app-card-cover .cover-image {
  filter: brightness(1.05);
}

.app-card:hover .app-card-hover-actions {
  opacity: 1;
  visibility: visible;
}

/* 封面图区域 */
.app-card-cover {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background: var(--bg-tertiary);
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: filter 200ms cubic-bezier(0.4, 0, 0.2, 1);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
}

[data-theme='dark'] .cover-placeholder {
  background: linear-gradient(135deg, #1c2128 0%, #161b22 100%);
}

.cover-icon {
  font-size: 48px;
  color: var(--text-tertiary);
  font-weight: 300;
  font-family: 'Courier New', monospace;
}

/* 应用信息区域 */
.app-card-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.app-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.app-icon {
  font-size: 16px;
  color: var(--primary-color);
  font-weight: 300;
  font-family: 'Courier New', monospace;
  flex-shrink: 0;
}

.app-name {
  font-size: 16px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.app-card-footer {
  display: flex;
  align-items: center;
}

.app-author {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* 悬浮按钮 */
.app-card-hover-actions {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  gap: 8px;
  opacity: 0;
  visibility: hidden;
  transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 10;
  background: var(--bg-card);
  padding: 8px;
  border-radius: var(--radius-md);
  backdrop-filter: blur(10px);
  box-shadow: var(--shadow-lg);
}

/* 精选标识 */
.featured-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 215, 0, 0.95);
  color: #000;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  z-index: 5;
  box-shadow: var(--shadow-sm);
}

.app-card.featured {
  border-color: rgba(255, 215, 0, 0.3);
}
</style>

