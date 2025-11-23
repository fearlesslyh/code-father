<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { 
  getAppVoById, 
  updateApp,
  updateAppByAdmin
} from '@/api/codeMother/appController'
import type { 
  AppVO,
  AppUpdateRequest,
  AppAdminUpdateRequest
} from '@/api/codeMother/typings'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const { userState } = storeToRefs(loginUserStore)

// 应用ID - 保持为 string 避免精度丢失
const appId = computed(() => route.params.id as string)

// 应用信息
const appInfo = ref<AppVO | null>(null)
const appInfoLoading = ref(true)

// 表单数据
const formData = ref({
  appName: '',
  cover: '',
  priority: 0,
  visibility: 'public' as 'public' | 'private',
  tags: [] as string[]
})

// 表单验证
const formErrors = ref({
  appName: ''
})

// 提交状态
const isSubmitting = ref(false)

// 获取应用信息
const fetchAppInfo = async () => {
  try {
    appInfoLoading.value = true
    const response = await getAppVoById({ id: appId.value })
    
    if (response.data) {
      appInfo.value = response.data
      
      // 填充表单数据
      formData.value.appName = response.data.appName || ''
      formData.value.cover = response.data.cover || ''
      formData.value.priority = response.data.priority || 0
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

// 验证表单
const validateForm = () => {
  formErrors.value.appName = ''
  
  if (!formData.value.appName.trim()) {
    formErrors.value.appName = '请输入应用名称'
    return false
  }
  
  if (formData.value.appName.trim().length > 50) {
    formErrors.value.appName = '应用名称不能超过50个字符'
    return false
  }
  
  return true
}

// 提交表单
const submitForm = async () => {
  if (!validateForm()) return
  
  if (!appInfo.value) return
  
  try {
    isSubmitting.value = true
    
    // 判断是普通用户还是管理员
    const isAdmin = userState.value.user?.userRole === 'admin'
    
    if (isAdmin) {
      // 管理员可以编辑更多字段
      const adminUpdateRequest: AppAdminUpdateRequest = {
        id: appId.value,
        appName: formData.value.appName.trim(),
        cover: formData.value.cover.trim(),
        priority: formData.value.priority
      }
      
      const response = await updateAppByAdmin(adminUpdateRequest)
      
      if (response.data) {
        message.success('应用信息更新成功')
        router.push(`/app/detail/${appId.value}`)
      } else {
        message.error('应用信息更新失败')
      }
    } else {
      // 普通用户只能编辑应用名称
      const updateRequest: AppUpdateRequest = {
        id: appId.value,
        appName: formData.value.appName.trim()
      }
      
      const response = await updateApp(updateRequest)
      
      if (response.data) {
        message.success('应用信息更新成功')
        router.push(`/app/detail/${appId.value}`)
      } else {
        message.error('应用信息更新失败')
      }
    }
  } catch (error) {
    console.error('更新应用信息失败:', error)
    message.error('更新应用信息失败')
  } finally {
    isSubmitting.value = false
  }
}

// 判断是否为管理员
const isAdmin = computed(() => userState.value.user?.userRole === 'admin')

// 判断是否为应用所有者或管理员
const canEdit = computed(() => {
  if (!appInfo.value || !userState.value.user) return false
  return String(appInfo.value.userId) === String(userState.value.user.id) || isAdmin.value
})

// 页面加载时获取应用信息
onMounted(() => {
  fetchAppInfo()
})
</script>

<template>
  <div class="app-edit-page app-content">
    <!-- 顶部栏 -->
    <header class="edit-header">
      <div class="header-left">
        <a-button type="text" @click="() => router.back()">
          <template #icon>
            <ArrowLeftOutlined />
          </template>
        </a-button>
        <h1 class="page-title">编辑应用</h1>
      </div>
      
      <div class="header-right">
        <a-button
          type="primary"
          :loading="isSubmitting"
          @click="submitForm"
          v-if="canEdit"
        >
          <template #icon>
            <SaveOutlined />
          </template>
          保存
        </a-button>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="edit-main" v-if="!appInfoLoading">
      <div class="edit-container">
        <a-form
          layout="vertical"
          :model="formData"
          class="edit-form"
        >
          <!-- 应用名称 -->
          <a-form-item
            label="应用名称"
            :validate-status="formErrors.appName ? 'error' : ''"
            :help="formErrors.appName"
          >
            <a-input
              v-model:value="formData.appName"
              placeholder="请输入应用名称"
              :maxlength="50"
              show-count
            />
          </a-form-item>
          
          <!-- 应用封面 - 仅管理员可见 -->
          <a-form-item
            label="应用封面"
            v-if="isAdmin"
          >
            <a-input
              v-model:value="formData.cover"
              placeholder="请输入应用封面URL"
            />
            <div class="form-help-text">
              请输入图片URL，建议尺寸为800x600像素
            </div>
          </a-form-item>
          
          <!-- 应用可见范围 -->
          <a-form-item label="可见范围">
            <a-radio-group v-model:value="formData.visibility">
              <a-radio value="public">公开</a-radio>
              <a-radio value="private">私有</a-radio>
            </a-radio-group>
            <div class="form-help-text">
              公开：其他用户可以看到你的应用；私有：只有你自己和管理员可以看到
            </div>
          </a-form-item>
          
          <!-- 应用标签 -->
          <a-form-item label="应用标签">
            <a-select
              v-model:value="formData.tags"
              mode="tags"
              placeholder="输入标签后按回车添加，最多10个"
              :max-tag-count="10"
              style="width: 100%"
            >
            </a-select>
            <div class="form-help-text">
              添加标签可以帮助其他用户更容易找到你的应用
            </div>
          </a-form-item>
          
          <!-- 应用优先级 - 仅管理员可见 -->
          <a-form-item
            label="应用优先级"
            v-if="isAdmin"
          >
            <a-input-number
              v-model:value="formData.priority"
              :min="0"
              :max="999"
              style="width: 100%"
            />
            <div class="form-help-text">
              数值越大，应用在精选列表中的排序越靠前，设置为99可设为精选应用，设置为999可置顶
            </div>
          </a-form-item>
          
          <!-- 应用创建信息 - 只读 -->
          <a-form-item label="应用ID">
            <a-input :value="appInfo?.id" disabled />
          </a-form-item>
          
          <a-form-item label="创建者">
            <a-input :value="appInfo?.user?.userName || '未知用户'" disabled />
          </a-form-item>
          
          <a-form-item label="创建时间">
            <a-input :value="appInfo?.createTime ? new Date(appInfo.createTime).toLocaleString() : '未知'" disabled />
          </a-form-item>
          
          <a-form-item label="应用类型">
            <a-input :value="appInfo?.codeGenType || 'Web'" disabled />
          </a-form-item>
          
          <a-form-item label="初始提示词">
            <a-textarea
              :value="appInfo?.initPrompt || ''"
              :rows="4"
              disabled
            />
          </a-form-item>
        </a-form>
      </div>
    </main>

    <!-- 加载状态 -->
    <div v-if="appInfoLoading" class="loading-container">
      <a-spin size="large" />
    </div>
    
    <!-- 无权限提示 -->
    <div v-if="!appInfoLoading && !canEdit" class="no-permission-container">
      <a-result
        status="403"
        title="无权限编辑"
        sub-title="您没有权限编辑此应用，只有应用创建者和管理员可以编辑应用信息。"
      >
        <template #extra>
          <a-button type="primary" @click="() => router.back()">
            返回
          </a-button>
        </template>
      </a-result>
    </div>
  </div>
</template>

<style scoped>
.app-edit-page {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 顶部栏 */
.edit-header {
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
.edit-main {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-lg);
}

.edit-container {
  max-width: 800px;
  margin: 0 auto;
}

.edit-form {
  background: var(--bg-glass);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  border: 1px solid var(--border-primary);
}

.form-help-text {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: var(--spacing-xs);
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

/* 无权限提示 */
.no-permission-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  padding: var(--spacing-lg);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .edit-header {
    padding: var(--spacing-sm) var(--spacing-md);
  }
  
  .page-title {
    font-size: 18px;
  }
  
  .edit-main {
    padding: var(--spacing-md);
  }
  
  .edit-form {
    padding: var(--spacing-lg);
  }
}

@media (max-width: 480px) {
  .edit-header {
    padding: var(--spacing-sm);
  }
  
  .edit-main {
    padding: var(--spacing-sm);
  }
  
  .edit-form {
    padding: var(--spacing-md);
  }
}
</style>