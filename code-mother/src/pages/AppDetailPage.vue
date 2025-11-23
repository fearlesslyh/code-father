<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { 
  getAppVoById, 
  deployApp,
  deleteApp
} from '@/api/codeMother/appController'
import type { 
  AppVO,
  AppDeployRequest,
  DeleteRequest
} from '@/api/codeMother/typings'
import { 
  ArrowLeftOutlined, 
  EditOutlined, 
  DeleteOutlined, 
  DeploymentUnitOutlined,
  MessageOutlined,
  CalendarOutlined,
  UserOutlined,
  GlobalOutlined,
  CodeOutlined,
  HistoryOutlined
} from '@ant-design/icons-vue'
import VersionHistory from '@/components/VersionHistory.vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { userState } = storeToRefs(loginUserStore)

// 应用ID - 保持为 string 避免精度丢失
const appId = computed(() => route.params.id as string)

// 应用信息
const appInfo = ref<AppVO | null>(null)
const appInfoLoading = ref(true)

// 部署相关
const isDeploying = ref(false)
const deployedUrl = ref('')

// 删除相关
const deleteConfirmVisible = ref(false)

// 获取应用信息
const fetchAppInfo = async () => {
  try {
    appInfoLoading.value = true
    const response = await getAppVoById({ id: appId.value })
    
    if (response.data) {
      appInfo.value = response.data
      
      // 如果应用已部署，设置预览URL
      if (response.data.deployKey) {
        const deployDomain = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost'
        deployedUrl.value = `${deployDomain}/${response.data.deployKey}`
      }
    } else {
      message.error('应用不存在或已被删除')
      router.push('/')
    }
  } catch (error: any) {
    console.error('获取应用信息失败:', error)
    
    // 处理特定的错误码
    if (error?.code === 10003 || error?.message?.includes('不存在')) {
      message.error('应用不存在或已被删除')
    } else if (error?.code === 10001 || error?.code === 10002) {
      message.error('登录状态已失效，请重新登录')
    } else {
      message.error(error?.message || '获取应用信息失败')
    }
    
    // 延迟跳转，让用户看到错误提示
    setTimeout(() => {
      router.push('/')
    }, 1500)
  } finally {
    appInfoLoading.value = false
  }
}

// 部署应用
const deployApplication = async () => {
  if (!appInfo.value) return
  
  try {
    isDeploying.value = true
    const deployRequest: AppDeployRequest = {
      appId: appId.value
    }
    
    const response = await deployApp(deployRequest)
    
    if (response.data) {
      // 部署接口返回的是deployKey（如 "LtLDYk"），需要拼接部署域名
      const deployKey = response.data
      const deployDomain = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost'
      // 如果返回的已经是完整URL，直接使用；否则拼接
      const finalUrl = deployKey.startsWith('http') ? deployKey : `${deployDomain}/${deployKey}`
      deployedUrl.value = finalUrl
      
      message.success('应用部署成功！')
      
      // 更新应用信息
      await fetchAppInfo()
    } else {
      message.error('应用部署失败')
    }
  } catch (error) {
    console.error('部署应用失败:', error)
    message.error('部署应用失败')
  } finally {
    isDeploying.value = false
  }
}

// 删除应用
const deleteApplication = async () => {
  if (!appInfo.value) return
  
  try {
    const deleteRequest: DeleteRequest = {
      id: appId.value
    }
    
    const response = await deleteApp(deleteRequest)
    
    if (response.data) {
      message.success('应用删除成功')
      router.push('/')
    } else {
      message.error('应用删除失败')
    }
  } catch (error) {
    console.error('删除应用失败:', error)
    message.error('删除应用失败')
  }
}

// 跳转到编辑页面
const goToEditPage = () => {
  router.push(`/app/edit/${appId.value}`)
}

// 跳转到对话页面
const goToChatPage = () => {
  router.push(`/app/chat/${appId.value}`)
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleString()
}

// 判断是否为应用所有者或管理员
const canEditOrDelete = computed(() => {
  if (!appInfo.value || !userState.value.user) return false
  return String(appInfo.value.userId) === String(userState.value.user.id) || userState.value.user.userRole === 'admin'
})

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})
</script>

<template>
  <div class="app-detail-page app-content">
    <!-- 顶部栏 -->
    <header class="detail-header">
      <div class="header-left">
        <a-button type="text" @click="() => router.back()">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
        </a-button>
        <h1 class="page-title">应用详情</h1>
      </div>
      
      <div class="header-right">
        <a-space>
          <a-button @click="goToChatPage">
            <template #icon>
              <MessageOutlined />
            </template>
            对话
          </a-button>
          
          <a-button v-if="canEditOrDelete" @click="goToEditPage">
            <template #icon>
              <EditOutlined />
            </template>
            编辑
          </a-button>
          
          <a-popconfirm
            v-if="canEditOrDelete"
            title="确定要删除这个应用吗？删除后不可恢复。"
            v-model:open="deleteConfirmVisible"
            @confirm="deleteApplication"
          >
            <a-button danger>
              <template #icon>
                <DeleteOutlined />
              </template>
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="detail-main" v-if="!appInfoLoading">
      <div class="detail-container">
        <!-- 应用基本信息 -->
        <section class="app-info-section">
          <div class="app-cover">
            <img v-if="appInfo?.cover" :src="appInfo.cover" :alt="appInfo.appName" />
            <div v-else class="default-cover">
              <CodeOutlined />
            </div>
          </div>
          
          <div class="app-basic-info">
            <h2 class="app-name">{{ appInfo?.appName || '未命名应用' }}</h2>
            
            <div class="app-meta">
              <div class="meta-item">
                <UserOutlined />
                <span>创建者：{{ appInfo?.user?.userName || '未知用户' }}</span>
              </div>
              
              <div class="meta-item">
                <CalendarOutlined />
                <span>创建时间：{{ formatDate(appInfo?.createTime) }}</span>
              </div>
              
              <div class="meta-item">
                <CalendarOutlined />
                <span>更新时间：{{ formatDate(appInfo?.updateTime) }}</span>
              </div>
              
              <div class="meta-item" v-if="appInfo?.deployedTime">
                <GlobalOutlined />
                <span>部署时间：{{ formatDate(appInfo.deployedTime) }}</span>
              </div>
            </div>
            
            <div class="app-tags">
              <a-tag color="blue">{{ appInfo?.codeGenType || 'Web' }}</a-tag>
              <a-tag v-if="appInfo?.priority && appInfo.priority > 0" color="gold">
                精选
              </a-tag>
            </div>
          </div>
        </section>

        <!-- 应用描述 -->
        <section class="app-description-section">
          <h3 class="section-title">应用描述</h3>
          <div class="description-content">
            {{ appInfo?.initPrompt || '暂无描述' }}
          </div>
        </section>

        <!-- 版本历史 -->
        <section class="app-version-section" v-if="appInfo">
          <h3 class="section-title">版本历史</h3>
          <VersionHistory :app-id="appId" :current-version="appInfo.version || 1" />
        </section>

        <!-- 部署和预览 -->
        <section class="app-deploy-section">
          <h3 class="section-title">部署与预览</h3>
          
          <div class="deploy-status">
            <div class="status-item">
              <span class="status-label">部署状态：</span>
              <a-tag v-if="appInfo?.deployedTime" color="success">已部署</a-tag>
              <a-tag v-else color="warning">未部署</a-tag>
            </div>
            
            <div class="status-item" v-if="deployedUrl">
              <span class="status-label">预览地址：</span>
              <a :href="deployedUrl" target="_blank" class="preview-link">
                {{ deployedUrl }}
              </a>
            </div>
          </div>
          
          <div class="deploy-actions">
            <a-button
              type="primary"
              :loading="isDeploying"
              @click="deployApplication"
            >
              <template #icon>
                <DeploymentUnitOutlined />
              </template>
              {{ appInfo?.deployedTime ? '重新部署' : '部署应用' }}
            </a-button>
            
            <a-button
              v-if="deployedUrl"
              :href="deployedUrl"
              target="_blank"
            >
              <template #icon>
                <GlobalOutlined />
              </template>
              在新窗口打开
            </a-button>
          </div>
        </section>

        <!-- 预览区域 -->
        <section class="app-preview-section" v-if="deployedUrl">
          <h3 class="section-title">应用预览</h3>
          <div class="preview-container">
            <iframe
              :src="deployedUrl"
              class="preview-iframe"
              frameborder="0"
              title="应用预览"
            ></iframe>
          </div>
        </section>
      </div>
    </main>

    <!-- 加载状态 -->
    <div v-if="appInfoLoading" class="loading-container">
      <a-spin size="large" />
    </div>
  </div>
</template>

<style scoped>
.app-detail-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 顶部栏 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) var(--spacing-lg);
  background: var(--bg-glass);
  border-bottom: 1px solid var(--border-primary);
  backdrop-filter: blur(20px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.page-title {
  font-size: 20px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0;
}

/* 主要内容区域 */
.detail-main {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg);
}

.detail-container {
  max-width: 1000px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-xl);
}

/* 应用基本信息 */
.app-info-section {
  display: flex;
  gap: var(--spacing-xl);
  background: var(--bg-glass);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  border: 1px solid var(--border-primary);
}

.app-cover {
  width: 200px;
  height: 200px;
  flex-shrink: 0;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.default-cover {
  font-size: 64px;
  color: var(--text-tertiary);
}

.app-basic-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.app-name {
  font-size: 28px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0;
}

.app-meta {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  color: var(--text-secondary);
}

.app-tags {
  display: flex;
  gap: var(--spacing-sm);
  flex-wrap: wrap;
}

/* 应用描述 */
.app-description-section {
  background: var(--bg-glass);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  border: 1px solid var(--border-primary);
}

.section-title {
  font-size: 20px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-lg) 0;
}

.description-content {
  color: var(--text-secondary);
  line-height: 1.6;
  white-space: pre-wrap;
}

/* 部署和预览 */
.app-deploy-section {
  background: var(--bg-glass);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  border: 1px solid var(--border-primary);
}

.deploy-status {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-lg);
}

.status-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.status-label {
  font-weight: var(--font-weight-medium);
  color: var(--text-secondary);
}

.preview-link {
  color: var(--primary-color);
  word-break: break-all;
}

.deploy-actions {
  display: flex;
  gap: var(--spacing-md);
  flex-wrap: wrap;
}

/* 预览区域 */
.app-preview-section {
  background: var(--bg-glass);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  border: 1px solid var(--border-primary);
}

.preview-container {
  height: 500px;
  border-radius: var(--radius-lg);
  overflow: hidden;
  border: 1px solid var(--border-primary);
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .detail-header {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .detail-main {
    padding: var(--spacing-md);
  }
  
  .app-info-section {
    flex-direction: column;
    gap: var(--spacing-lg);
  }
  
  .app-cover {
    width: 100%;
    height: 200px;
  }
  
  .app-name {
    font-size: 24px;
  }
  
  .deploy-actions {
    flex-direction: column;
  }
  
  .preview-container {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .detail-header {
    padding: var(--spacing-sm);
  }
  
  .header-right .ant-space {
    flex-wrap: wrap;
  }
  
  .detail-main {
    padding: var(--spacing-sm);
  }
  
  .app-info-section,
  .app-description-section,
  .app-deploy-section,
  .app-preview-section {
    padding: var(--spacing-lg);
  }
  
  .app-name {
    font-size: 20px;
  }
  
  .preview-container {
    height: 250px;
  }
}
</style>