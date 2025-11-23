<script setup lang="ts">
import { ref, onMounted, nextTick, computed, watch, toRefs, onUnmounted } from 'vue'
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
import { SendOutlined, DeploymentUnitOutlined, LoadingOutlined, ArrowLeftOutlined, RobotOutlined, InfoCircleOutlined, EditOutlined, DeleteOutlined, CheckCircleOutlined, CopyOutlined, CloseOutlined, StopOutlined, PoweroffOutlined } from '@ant-design/icons-vue'
import MarkdownRenderer from '@/components/MarkdownRenderer.vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { userState } = storeToRefs(loginUserStore)

// 应用ID - 保持为 string 避免精度丢失
const appId = computed(() => route.params.id as string)

// 应用信息
const appInfo = ref<AppVO | null>(null)
const appInfoLoading = ref(true)

// 对话相关
const messages = ref<Array<{ role: 'user' | 'assistant', content: string }>>([])
const currentMessage = ref('')
const isLoading = ref(false)
const isStreaming = ref(false)
const eventSource = ref<EventSource | null>(null)

// 网站预览相关
const showPreview = ref(false)
const previewUrl = ref('')
const isDeploying = ref(false)

// 应用详情悬浮窗
const appDetailModalVisible = ref(false)

// 部署成功模态框
const deploySuccessModalVisible = ref(false)
const deploySuccessUrl = ref('')

// 生成状态
const generationStatus = ref<'idle' | 'generating' | 'completed' | 'failed'>('idle')

// 权限相关
const canEdit = computed(() => {
  if (!appInfo.value || !userState.value.user) return false
  return String(appInfo.value.userId) === String(userState.value.user.id) || userState.value.user.userRole === 'admin'
})

const canChat = computed(() => {
  if (!appInfo.value || !userState.value.user) return false
  return String(appInfo.value.userId) === String(userState.value.user.id) || userState.value.user.userRole === 'admin'
})

// 获取应用信息
const fetchAppInfo = async () => {
  try {
    appInfoLoading.value = true
    const response = await getAppVoById({ id: appId.value })
    
    if (response.data) {
      appInfo.value = response.data
      
      // 检查URL查询参数，如果有 view=1 则不自动发送消息
      const viewParam = route.query.view
      const shouldAutoSend = !viewParam || viewParam !== '1'
      
      // 如果是首次进入且需要自动发送，直接发送初始消息（不先添加到消息列表，因为sendMessageToAI会添加）
      if (shouldAutoSend && messages.value.length === 0 && appInfo.value.initPrompt) {
        // 直接发送，不先添加到消息列表
        await sendMessageToAI(appInfo.value.initPrompt)
      }
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败:', error)
    message.error('获取应用信息失败')
    router.push('/')
  } finally {
    appInfoLoading.value = false
  }
}

// 停止AI生成
const stopGeneration = () => {
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
  }
  isStreaming.value = false
  isLoading.value = false
  generationStatus.value = 'idle'
  message.info('已停止生成')
}

// 发送消息给AI
const sendMessageToAI = async (messageContent: string) => {
  if (isStreaming.value) {
    message.warning('正在等待AI回复，请稍后再试')
    return
  }

  try {
    isStreaming.value = true
    isLoading.value = true
    generationStatus.value = 'generating'
    
    // 添加用户消息
    messages.value.push({
      role: 'user',
      content: messageContent
    })
    
    // 添加AI消息占位符
    const assistantMessageIndex = messages.value.length
    messages.value.push({
      role: 'assistant',
      content: ''
    })
    
    // 创建EventSource连接
    // 使用环境变量配置的API地址
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'
    const baseUrl = apiBaseUrl.replace('/api', '')
    const url = `${apiBaseUrl}/app/chat/gen/code?appId=${appId.value}&message=${encodeURIComponent(messageContent)}`
    
    eventSource.value = new EventSource(url, { withCredentials: true })
    
    // 监听普通消息事件
    eventSource.value.onmessage = (event) => {
      try {
        // 后端返回的是JSON格式：{"d": "内容"}
        const data = JSON.parse(event.data)
        const content = data.d || ''
        
        if (content) {
          messages.value[assistantMessageIndex].content += content
        }
      } catch (error) {
        // 如果解析失败，尝试直接使用原始数据
        console.warn('解析SSE消息失败，使用原始数据:', error)
        if (event.data) {
          messages.value[assistantMessageIndex].content += event.data
        }
      }
    }
    
    // 监听 done 事件（后端发送的结束事件）
    eventSource.value.addEventListener('done', async () => {
      eventSource.value?.close()
      isStreaming.value = false
      isLoading.value = false
      generationStatus.value = 'completed'
      
      // 显示预览
      showPreview.value = true
      const codeGenType = appInfo.value?.codeGenType || 'html'
      const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'
      previewUrl.value = `${apiBaseUrl}/static/${codeGenType}_${appId.value}/`
      
      // 更新应用信息（获取最新版本号等）
      await fetchAppInfo()
      
      message.success('代码生成完成')
    })
    
    // 设置生成状态
    generationStatus.value = 'generating'
    
    eventSource.value.onerror = (error) => {
      console.error('EventSource错误:', error)
      
      // 检查连接状态
      if (eventSource.value?.readyState === EventSource.CLOSED) {
        // 连接已关闭，可能是正常结束或错误
        if (!isStreaming.value) {
          // 如果已经处理完成，不显示错误
          return
        }
      }
      
      eventSource.value?.close()
      isStreaming.value = false
      isLoading.value = false
      generationStatus.value = 'failed'
      
      // 如果已经有部分内容，不显示错误，可能是正常结束
      if (messages.value[assistantMessageIndex].content.trim().length === 0) {
        message.error('与AI对话失败，请重试')
      } else {
        // 有部分内容，可能是连接意外中断
        message.warning('连接中断，但已收到部分回复')
        generationStatus.value = 'completed'
      }
    }
    
  } catch (error) {
    console.error('发送消息失败:', error)
    isStreaming.value = false
    isLoading.value = false
    generationStatus.value = 'failed'
    message.error('发送消息失败')
  }
}

// 发送当前消息
const sendMessage = () => {
  if (!currentMessage.value.trim()) {
    message.warning('请输入消息内容')
    return
  }
  
  const messageContent = currentMessage.value.trim()
  currentMessage.value = ''
  sendMessageToAI(messageContent)
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
      previewUrl.value = finalUrl
      
      // 显示部署成功模态框
      deploySuccessModalVisible.value = true
      deploySuccessUrl.value = finalUrl
      
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

// 取消部署（下线应用）
const undeployApplication = async () => {
  if (!appInfo.value) return
  
  try {
    isDeploying.value = true
    // 注意：这里需要后端提供取消部署的接口
    // 目前先通过更新应用信息来清除deployKey
    // 实际应该调用专门的取消部署接口
    message.warning('取消部署功能需要后端支持，请联系管理员')
    // TODO: 调用取消部署接口
    // const response = await undeployApp({ appId: appId.value })
    // if (response.data) {
    //   message.success('应用已取消部署')
    //   await fetchAppInfo()
    // }
  } catch (error) {
    console.error('取消部署失败:', error)
    message.error('取消部署失败')
  } finally {
    isDeploying.value = false
  }
}

// 滚动到消息底部
const scrollToBottom = () => {
  nextTick(() => {
    const messageContainer = document.querySelector('.messages-container')
    if (messageContainer) {
      messageContainer.scrollTop = messageContainer.scrollHeight
    }
  })
}

// 监听消息变化，自动滚动到底部
watch(messages, () => {
  scrollToBottom()
}, { deep: true })

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})

// 删除应用
const handleDeleteApp = async () => {
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

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleString()
}

// 复制部署URL
const copyDeployUrl = () => {
  if (deploySuccessUrl.value) {
    navigator.clipboard.writeText(deploySuccessUrl.value).then(() => {
      message.success('链接已复制到剪贴板')
    }).catch(() => {
      message.error('复制失败')
    })
  }
}

// 访问部署的网站
const visitDeployedSite = () => {
  if (deploySuccessUrl.value) {
    window.open(deploySuccessUrl.value, '_blank')
  }
}

// 页面卸载时关闭EventSource连接
onUnmounted(() => {
  if (eventSource.value) {
    eventSource.value.close()
  }
})
</script>

<template>
  <div class="app-chat-page app-content">
    <!-- 顶部栏 -->
    <header class="chat-header">
      <div class="header-left">
        <a-button type="text" @click="() => router.back()">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
        </a-button>
        <h1 class="app-title">{{ appInfo?.appName || '未命名应用' }}</h1>
      </div>
      
      <div class="header-right">
        <a-space>
          <a-button @click="appDetailModalVisible = true">
            <template #icon>
              <InfoCircleOutlined />
            </template>
            应用详情
          </a-button>
          <a-button
            type="primary"
            :loading="isDeploying"
            @click="deployApplication"
            v-if="showPreview && !appInfo?.deployKey"
          >
            <template #icon>
              <DeploymentUnitOutlined />
            </template>
            部署应用
          </a-button>
          <a-button
            danger
            :loading="isDeploying"
            @click="undeployApplication"
            v-if="showPreview && appInfo?.deployKey"
          >
            <template #icon>
              <PoweroffOutlined />
            </template>
            取消部署
          </a-button>
        </a-space>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="chat-main">
      <!-- 左侧对话区域 -->
      <section class="chat-section">
        <div class="messages-container" ref="messagesContainer">
          <div v-if="messages.length === 0" class="empty-messages">
            <a-empty description="暂无对话记录" />
          </div>
          
          <div
            v-for="(message, index) in messages"
            :key="index"
            class="message-item"
            :class="{ 'user-message': message.role === 'user', 'assistant-message': message.role === 'assistant' }"
          >
            <div class="message-avatar">
              <a-avatar v-if="message.role === 'user'" :src="userState.user?.avatar">
                {{ userState.user?.nickname?.charAt(0) ?? 'U' }}
              </a-avatar>
              <a-avatar v-else :size="40" :src="'/aiAvatar.png'">
                <template #icon>
                  <RobotOutlined />
                </template>
              </a-avatar>
            </div>
            <div class="message-content">
              <div class="message-text" v-if="message.role === 'assistant'">
                <MarkdownRenderer :content="message.content" />
              </div>
              <div class="message-text" v-else v-html="message.content.replace(/\n/g, '<br>')"></div>
              <div v-if="message.role === 'assistant' && isStreaming && index === messages.length - 1" class="typing-indicator">
                <LoadingOutlined /> AI正在思考...
              </div>
            </div>
          </div>
        </div>
        
        <div class="message-input-container">
          <a-tooltip :title="!canChat ? '无法在别人的作品下对话哦~' : ''" :mouseEnterDelay="0.5">
            <a-textarea
              v-model:value="currentMessage"
              placeholder="请描述你想生成的网站，越详细效果越好哦"
              :rows="3"
              :disabled="isLoading || !canChat"
              @keydown.ctrl.enter="sendMessage"
              class="message-input"
            />
          </a-tooltip>
          <a-button
            v-if="!isStreaming"
            type="primary"
            :loading="isLoading"
            :disabled="!currentMessage.trim() || !canChat"
            @click="sendMessage"
            class="send-button"
          >
            <template #icon>
              <SendOutlined />
            </template>
            发送
          </a-button>
          <a-button
            v-else
            danger
            @click="stopGeneration"
            class="stop-button"
          >
            <template #icon>
              <StopOutlined />
            </template>
            停止生成
          </a-button>
        </div>
      </section>

      <!-- 右侧预览区域 -->
      <section class="preview-section" v-if="showPreview">
        <div class="preview-header">
          <h3>网站预览</h3>
          <a-button
            type="primary"
            size="small"
            :href="previewUrl"
            target="_blank"
          >
            在新窗口打开
          </a-button>
        </div>
        <div class="preview-container">
          <iframe
            :src="previewUrl"
            class="preview-iframe"
            frameborder="0"
            title="网站预览"
          ></iframe>
        </div>
      </section>
    </main>
    
    <!-- 应用详情悬浮窗 -->
    <a-modal
      v-model:open="appDetailModalVisible"
      title="应用详情"
      :footer="null"
      width="600px"
    >
      <div class="app-detail-modal">
        <!-- 应用基础信息 -->
        <div class="detail-section">
          <h3 class="section-title">应用基础信息</h3>
          <div class="info-item">
            <span class="info-label">创建者：</span>
            <div class="creator-info">
              <a-avatar :src="appInfo?.user?.userAvatar" size="small">
                {{ appInfo?.user?.userName?.charAt(0) || 'U' }}
              </a-avatar>
              <span class="creator-name">{{ appInfo?.user?.userName || '未知用户' }}</span>
            </div>
          </div>
          <div class="info-item">
            <span class="info-label">创建时间：</span>
            <span>{{ formatDate(appInfo?.createTime) }}</span>
          </div>
        </div>
        
        <!-- 操作栏（仅本人或管理员可见） -->
        <div class="detail-section" v-if="canEdit">
          <h3 class="section-title">操作栏</h3>
          <a-space>
            <a-button @click="goToEditPage">
              <template #icon>
                <EditOutlined />
              </template>
              修改
            </a-button>
            <a-popconfirm
              title="确定要删除这个应用吗？删除后不可恢复。"
              @confirm="handleDeleteApp"
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
      </div>
    </a-modal>
    
    <!-- 部署成功模态框 -->
    <a-modal
      v-model:open="deploySuccessModalVisible"
      :title="null"
      :footer="null"
      :closable="false"
      width="500px"
      class="deploy-success-modal"
    >
      <div class="deploy-success-content">
        <div class="success-header">
          <h2 class="success-title">部署成功</h2>
          <a-button type="text" @click="deploySuccessModalVisible = false" class="close-btn">
            <template #icon>
              <CloseOutlined />
            </template>
          </a-button>
        </div>
        
        <div class="success-icon">
          <CheckCircleOutlined />
        </div>
        
        <div class="success-message">
          <p class="message-text">网站部署成功!</p>
          <p class="message-desc">你的网站已经成功部署,可以通过以下链接访问:</p>
        </div>
        
        <div class="deploy-url-container">
          <a-input
            :value="deploySuccessUrl"
            readonly
            class="deploy-url-input"
          >
            <template #suffix>
              <a-button
                type="text"
                size="small"
                @click="copyDeployUrl"
                class="copy-btn"
              >
                <template #icon>
                  <CopyOutlined />
                </template>
              </a-button>
            </template>
          </a-input>
        </div>
        
        <div class="success-actions">
          <a-button
            type="primary"
            size="large"
            @click="visitDeployedSite"
            class="visit-btn"
          >
            访问网站
          </a-button>
          <a-button
            size="large"
            @click="deploySuccessModalVisible = false"
            class="close-action-btn"
          >
            关闭
          </a-button>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.app-chat-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 顶部栏 */
.chat-header {
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

.app-title {
  font-size: 20px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0;
}

/* 主要内容区域 */
.chat-main {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* 对话区域 */
.chat-section {
  flex: 2;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border-primary);
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 50%, #f1f5f9 100%);
  background-size: 200% 200%;
  animation: gradientShift 10s ease infinite;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

[data-theme='dark'] .chat-section {
  background: linear-gradient(135deg, #0E1117 0%, #161b22 50%, #1c2128 100%);
  background-size: 200% 200%;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg);
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
  background: transparent;
}

.empty-messages {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.message-item {
  display: flex;
  gap: var(--spacing-md);
  max-width: 80%;
}

.user-message {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.assistant-message {
  align-self: flex-start;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  position: relative;
}

.message-text {
  background: #ffffff;
  padding: 12px 16px;
  border-radius: 12px;
  color: #1f2937;
  line-height: 1.6;
  word-break: break-word;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.user-message .message-text {
  background: #1890ff;
  color: #ffffff;
  border: none;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
  margin-top: var(--spacing-sm);
  color: var(--text-tertiary);
  font-size: 12px;
}

/* 消息输入区域 */
.message-input-container {
  display: flex;
  gap: var(--spacing-md);
  padding: var(--spacing-md);
  border-top: 1px solid #e5e7eb;
  background: #ffffff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
}

.message-input {
  flex: 1;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  color: #1f2937;
  resize: none;
}

.message-input:focus {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

.send-button,
.stop-button {
  align-self: flex-end;
  height: 40px;
  min-width: 100px;
}

/* 预览区域 */
.preview-section {
  flex: 3;
  min-width: 400px;
  display: flex;
  flex-direction: column;
  background: var(--bg-glass);
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-sm) var(--spacing-md);
  border-bottom: 1px solid var(--border-primary);
}

.preview-header h3 {
  margin: 0;
  color: var(--text-primary);
  font-size: 16px;
  font-weight: var(--font-weight-semibold);
}

.preview-container {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 部署成功模态框样式 */
:deep(.deploy-success-modal .ant-modal-content) {
  border-radius: 12px;
  padding: 0;
}

.deploy-success-content {
  padding: 32px;
  text-align: center;
}

.success-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.success-title {
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.close-btn {
  color: #6b7280;
}

.close-btn:hover {
  color: #1f2937;
}

.success-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: #10b981;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: white;
}

.success-message {
  margin-bottom: 24px;
}

.message-text {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.message-desc {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.deploy-url-container {
  margin-bottom: 24px;
}

.deploy-url-input {
  width: 100%;
}

.deploy-url-input :deep(.ant-input) {
  font-size: 14px;
  color: #1f2937;
}

.copy-btn {
  color: #6b7280;
}

.copy-btn:hover {
  color: #3b82f6;
}

.success-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.visit-btn {
  min-width: 120px;
}

.close-action-btn {
  min-width: 120px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .preview-section {
    width: 50%;
    min-width: 350px;
  }
}

@media (max-width: 768px) {
  .chat-main {
    flex-direction: column;
  }
  
  .chat-section {
    border-right: none;
    border-bottom: 1px solid var(--border-primary);
  }
  
  .preview-section {
    width: 100%;
    min-width: auto;
    height: 40%;
  }
  
  .message-item {
    max-width: 90%;
  }
}

@media (max-width: 480px) {
  .chat-header {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .app-title {
    font-size: 16px;
  }
  
  .messages-container {
    padding: var(--spacing-md);
  }
  
  .message-input-container {
    padding: var(--spacing-md);
    flex-direction: column;
  }
  
  .send-button {
    width: 100%;
    align-self: stretch;
  }
  
  .message-item {
    max-width: 95%;
  }
}

/* 应用详情悬浮窗样式 */
.app-detail-modal {
  padding: var(--spacing-md);
}

.detail-section {
  margin-bottom: var(--spacing-xl);
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-md) 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: var(--spacing-sm);
  color: var(--text-secondary);
}

.info-label {
  font-weight: var(--font-weight-medium);
  min-width: 80px;
}

.creator-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.creator-name {
  color: var(--text-primary);
}
</style>