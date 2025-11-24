<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  addApp,
  listMyAppVoByPage,
  listGoodAppVoByPage,
  deleteApp,
  updateAppByAdmin
} from '@/api/codeMother/appController'
import type {
  AppAddRequest,
  AppQueryRequest,
  AppVO,
  DeleteRequest,
  AppAdminUpdateRequest
} from '@/api/codeMother/typings'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const { userState } = storeToRefs(loginUserStore)

// 用户提示词输入
const promptInput = ref('')
const isCreatingApp = ref(false)

// 我的应用列表
const myApps = ref<AppVO[]>([])
const myAppsLoading = ref(false)
const myAppsPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
})

// 精选应用列表
const featuredApps = ref<AppVO[]>([])
const featuredAppsLoading = ref(false)
const featuredAppsPagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
})

// 搜索关键词
const myAppsSearchKeyword = ref('')
const featuredAppsSearchKeyword = ref('')
const searchTags = ref<string[]>([])

// 根据描述生成应用名称（简化版，实际应该调用AI接口）
const generateAppName = (prompt: string): string => {
  const trimmed = prompt.trim()
  if (!trimmed) return '未命名应用'
  
  // 提取关键词生成名称
  const keywords = trimmed.match(/创建|生成|制作|开发|设计|搭建|构建|实现|做一个|做一个|帮我|想要|需要/i)
  if (keywords) {
    const afterKeyword = trimmed.substring(trimmed.indexOf(keywords[0]) + keywords[0].length).trim()
    const firstSentence = afterKeyword.split(/[，。！？\n]/)[0]
    if (firstSentence.length > 0 && firstSentence.length <= 20) {
      return firstSentence.replace(/的|一个|网站|应用|系统|平台|工具|软件/gi, '').trim() || '未命名应用'
    }
  }
  
  // 如果提取失败，使用前20个字符
  if (trimmed.length <= 20) {
    return trimmed
  }
  
  return trimmed.substring(0, 20) + '...'
}

// 创建应用
const createApp = async () => {
  if (!promptInput.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!userState.value.user) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  try {
    isCreatingApp.value = true
    
    // 生成应用名称（前端生成，创建后可以通过更新接口设置）
    const generatedName = generateAppName(promptInput.value)
    
    const addRequest: AppAddRequest = {
      initPrompt: promptInput.value.trim()
      // 注意：后端AppAddRequest目前不支持appName字段，需要在创建后通过updateApp接口设置
    }
    const response = await addApp(addRequest)

    if (response.data) {
      // 创建成功后，尝试设置生成的应用名称
      // 注意：这需要后端支持，或者在前端创建后立即调用updateApp
      // 目前先跳转，应用名称可以在编辑页面手动设置
      
      message.success('应用创建成功，正在跳转到对话页面...')
      // 跳转到对话页面 - 确保使用 string 类型避免精度丢失
      router.push(`/app/chat/${String(response.data)}`)
    } else {
      message.error('创建应用失败')
    }
  } catch (error) {
    console.error('创建应用失败:', error)
    message.error('创建应用失败')
  } finally {
    isCreatingApp.value = false
  }
}

// 获取我的应用列表
const fetchMyApps = async (page = 1, keyword = '') => {
  if (!userState.value.user) return

  try {
    myAppsLoading.value = true
    const queryRequest: AppQueryRequest = {
      pageNum: page,
      pageSize: myAppsPagination.value.pageSize,
      appName: keyword || undefined,
    }
    const response = await listMyAppVoByPage(queryRequest)

    if (response.data?.records) {
      myApps.value = response.data.records
      myAppsPagination.value.current = page
      myAppsPagination.value.total = response.data.totalRow || 0
    }
  } catch (error) {
    console.error('获取我的应用列表失败:', error)
    message.error('获取我的应用列表失败')
  } finally {
    myAppsLoading.value = false
  }
}

// 获取精选应用列表（只显示公开应用）
const fetchFeaturedApps = async (page = 1, keyword = '') => {
  try {
    featuredAppsLoading.value = true
    const queryRequest: AppQueryRequest = {
      pageNum: page,
      pageSize: featuredAppsPagination.value.pageSize,
      appName: keyword || undefined,
      sortField: 'priority', // 按优先级排序，置顶(999)和精选(99)会排在前面
      sortOrder: 'descend',
      visibility: 'public', // 只显示公开应用
      tags: searchTags.value.length > 0 ? searchTags.value : undefined, // 标签筛选
    }
    const response = await listGoodAppVoByPage(queryRequest)

    if (response.data?.records) {
      featuredApps.value = response.data.records
      featuredAppsPagination.value.current = page
      featuredAppsPagination.value.total = response.data.totalRow || 0
    }
  } catch (error) {
    console.error('获取精选应用列表失败:', error)
    message.error('获取精选应用列表失败')
  } finally {
    featuredAppsLoading.value = false
  }
}

// 处理我的应用分页变化
const handleMyAppsPageChange = (page: number) => {
  fetchMyApps(page, myAppsSearchKeyword.value)
}

// 处理精选应用分页变化
const handleFeaturedAppsPageChange = (page: number) => {
  fetchFeaturedApps(page, featuredAppsSearchKeyword.value)
}

// 处理我的应用搜索
const handleMyAppsSearch = () => {
  fetchMyApps(1, myAppsSearchKeyword.value)
}

// 处理精选应用搜索
const handleFeaturedAppsSearch = () => {
  fetchFeaturedApps(1, featuredAppsSearchKeyword.value)
}

// 跳转到应用详情页
const goToAppDetail = (appId: string) => {
  router.push(`/app/detail/${appId}`)
}

// 查看作品（打开部署地址）
const viewApp = (deployKey: string) => {
  if (!deployKey) return
  const deployDomain = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost'
  const deployUrl = `${deployDomain}/${deployKey}`
  window.open(deployUrl, '_blank')
}

// 跳转到应用对话页
const goToAppChat = (appId: string) => {
  router.push(`/app/chat/${appId}`)
}

// 跳转到应用详情页（带view参数，不自动发送消息）
const goToAppDetailFromCard = (appId: string) => {
  router.push(`/app/chat/${appId}?view=1`)
}

// 编辑应用
const editApp = (appId: string) => {
  router.push(`/app/edit/${appId}`)
}

// 删除应用
const deleteAppHandler = async (appId: string) => {
  try {
    const deleteRequest: DeleteRequest = {
      id: appId
    }
    
    const response = await deleteApp(deleteRequest)
    
    if (response.data) {
      message.success('应用删除成功')
      // 刷新我的应用列表
      fetchMyApps(myAppsPagination.value.current, myAppsSearchKeyword.value)
    } else {
      message.error('应用删除失败')
    }
  } catch (error) {
    console.error('删除应用失败:', error)
    message.error('删除应用失败')
  }
}

// 设置应用为精选（仅管理员可用）
const setAppAsFeatured = async (appId: string) => {
  if (!isAdmin.value) {
    message.warning('只有管理员可以设置精选应用')
    return
  }
  
  try {
    const updateRequest: AppAdminUpdateRequest = {
      id: appId,
      priority: 99
    }
    
    const response = await updateAppByAdmin(updateRequest)
    
    if (response.data) {
      message.success('应用已设置为精选')
      // 刷新应用列表
      fetchMyApps(myAppsPagination.value.current, myAppsSearchKeyword.value)
      fetchFeaturedApps(featuredAppsPagination.value.current, featuredAppsSearchKeyword.value)
    } else {
      message.error('设置精选失败')
    }
  } catch (error) {
    console.error('设置精选失败:', error)
    message.error('设置精选失败')
  }
}

// 计算是否为管理员
const isAdmin = computed(() => userState.value.user?.userRole === 'admin')

// 快捷提示词示例
const quickPrompts = [
  {
    title: '个人博客网站',
    text: '帮我创建一个个人博客网站，包含首页展示最新文章列表、文章详情页支持Markdown渲染、文章分类和标签功能、关于我页面介绍个人背景，整体风格简洁现代，支持响应式设计，适配手机和电脑端。'
  },
  {
    title: '企业官网',
    text: '帮我创建一个企业官网，包含首页展示公司介绍和核心业务、产品服务页面展示详细的产品信息、团队介绍页面、联系我们页面包含表单提交功能，整体风格专业大气，使用蓝色和白色作为主色调。'
  },
  {
    title: '在线商城',
    text: '帮我创建一个在线商城网站，包含商品列表页支持分类筛选和搜索、商品详情页展示商品图片和详细信息、购物车功能可以添加和删除商品、订单结算页面，整体风格现代时尚，支持移动端购物体验。'
  },
  {
    title: '作品集网站',
    text: '帮我创建一个作品集展示网站，包含首页展示精选作品、作品详情页展示项目介绍和技术栈、技能展示页面、联系方式页面，整体风格创意个性，支持图片轮播和动画效果，突出视觉冲击力。'
  }
]

// 页面加载时获取数据
onMounted(() => {
  fetchMyApps()
  fetchFeaturedApps()
})
</script>

<template>
  <div class="home-page app-content tech-bg tech-particles">
    <div class="home-container">
      <!-- 网站标题和提示词输入区域 -->
      <section class="hero-section">
        <div class="hero-content">
          <h1 class="hero-title">
            <span class="title-text">凌犀零代码平台</span>
          </h1>
          <p class="hero-subtitle">
            无需编码，即可快速构建应用。让每一位创作者都能把想法转化为现实，释放你的无限想象力。
          </p>

          <div class="prompt-input-container">
            <a-textarea
              v-model:value="promptInput"
              placeholder="帮我创建个人博客网站"
              :rows="3"
              class="prompt-input"
              :maxlength="500"
            />
            <a-button
              type="primary"
              size="large"
              class="create-app-btn tech-button"
              :loading="isCreatingApp"
              @click="createApp"
            >
              <template #icon>
                <PlusOutlined />
              </template>
              创建应用
            </a-button>
          </div>
          
          <!-- 快捷提示词示例 -->
          <div class="quick-prompts">
            <a-button
              v-for="(prompt, index) in quickPrompts"
              :key="index"
              size="small"
              class="quick-prompt-btn"
              @click="promptInput = prompt.text"
            >
              {{ prompt.title }}
            </a-button>
          </div>
        </div>
      </section>

      <!-- 我的应用区域 -->
      <section class="my-apps-section">
        <div class="section-header">
          <h2 class="section-title">我的应用</h2>
          <div class="search-container">
            <a-input-search
              v-model:value="myAppsSearchKeyword"
              placeholder="搜索应用名称"
              style="width: 250px"
              @search="handleMyAppsSearch"
            />
          </div>
        </div>

        <div v-if="myAppsLoading" class="loading-container">
          <a-spin size="large" />
        </div>

        <div v-else-if="myApps.length === 0" class="empty-container">
          <a-empty description="暂无应用，快创建第一个应用吧！" />
        </div>

        <div v-else class="apps-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            :show-actions="true"
          />
        </div>

        <div v-if="myApps.length > 0" class="pagination-container">
          <a-pagination
            v-model:current="myAppsPagination.current"
            :total="myAppsPagination.total"
            :page-size="myAppsPagination.pageSize"
            @change="handleMyAppsPageChange"
            show-size-changer
            show-quick-jumper
          />
        </div>
      </section>

      <!-- 精选应用区域 -->
      <section class="featured-apps-section">
        <div class="section-header">
          <h2 class="section-title">精选应用</h2>
          <div class="search-container">
            <a-space>
              <a-input-search
                v-model:value="featuredAppsSearchKeyword"
                placeholder="搜索应用名称、描述或标签"
                style="width: 300px"
                @search="handleFeaturedAppsSearch"
                allow-clear
              />
              <a-select
                v-model:value="searchTags"
                mode="tags"
                placeholder="选择标签"
                style="width: 200px"
                :max-tag-count="3"
                allow-clear
                @change="handleFeaturedAppsSearch"
              >
              </a-select>
            </a-space>
          </div>
        </div>

        <div v-if="featuredAppsLoading" class="loading-container">
          <a-spin size="large" />
        </div>

        <div v-else-if="featuredApps.length === 0" class="empty-container">
          <a-empty description="暂无精选应用" />
        </div>

        <div v-else class="apps-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :show-actions="true"
          />
        </div>

        <div v-if="featuredApps.length > 0" class="pagination-container">
          <a-pagination
            v-model:current="featuredAppsPagination.current"
            :total="featuredAppsPagination.total"
            :page-size="featuredAppsPagination.pageSize"
            @change="handleFeaturedAppsPageChange"
            show-size-changer
            show-quick-jumper
          />
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  min-height: 100vh;
  padding: var(--spacing-xl);
  position: relative;
  overflow-y: auto;
  background: transparent; /* 使用 BasicLayout 的渐变背景 */
}

.home-container {
  max-width: 1200px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

/* 英雄区域 - 网站标题和提示词输入 */
.hero-section {
  margin-bottom: var(--spacing-2xl);
  text-align: center;
}

.hero-content {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-2xl);
  background: rgba(255, 255, 255, 0.9);
  border-radius: var(--radius-xl);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

[data-theme='dark'] .hero-content {
  background: rgba(30, 41, 59, 0.8);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(102, 126, 234, 0.3);
}

.hero-title {
  font-size: 42px;
  font-weight: var(--font-weight-bold);
  color: var(--text-highlight);
  margin: 0 0 var(--spacing-lg) 0;
  position: relative;
  display: inline-block;
  letter-spacing: -0.5px;
}

.title-text {
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
  opacity: 0.3;
  filter: blur(15px);
  z-index: 1;
}

.hero-subtitle {
  font-size: 18px;
  color: var(--text-secondary);
  margin: 0 0 var(--spacing-xl) 0;
  line-height: 1.6;
  font-weight: var(--font-weight-normal);
}

.prompt-input-container {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.prompt-input {
  width: 100%;
  background: var(--bg-primary);
  border: 1px solid var(--border-primary);
  border-radius: var(--radius-lg);
  color: var(--text-primary);
  font-size: 16px;
  transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
}

.prompt-input:focus {
  border-color: var(--border-focus);
  box-shadow: var(--shadow-glow-hover);
}

.quick-prompts {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-sm);
  justify-content: center;
  margin-top: var(--spacing-lg);
}

.quick-prompt-btn {
  background: var(--bg-primary);
  border: 1px solid var(--border-primary);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  transition: all 200ms cubic-bezier(0.4, 0, 0.2, 1);
}

.quick-prompt-btn:hover {
  background: var(--bg-secondary);
  border-color: var(--border-hover);
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.create-app-btn {
  align-self: center;
  min-width: 200px;
  height: 48px;
  font-size: 16px;
  font-weight: var(--font-weight-semibold);
}

/* 应用列表区域通用样式 */
.my-apps-section,
.featured-apps-section {
  margin-bottom: var(--spacing-2xl);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-xl);
}

.section-title {
  font-size: 28px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0;
  text-shadow: 0 0 20px rgba(120, 219, 255, 0.5);
}

.search-container {
  display: flex;
  align-items: center;
}

/* 加载和空状态 */
.loading-container,
.empty-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 应用卡片网格 */
.apps-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-xl);
}

.app-card {
  background: var(--bg-glass);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all var(--transition-normal);
  cursor: pointer;
  border: 1px solid var(--border-primary);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4);
  border-color: var(--border-focus);
}

.app-card.featured {
  border-color: rgba(255, 215, 0, 0.5);
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.2);
}

.app-cover {
  height: 160px;
  position: relative;
  overflow: hidden;
  background: var(--bg-secondary);
}

.app-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform var(--transition-normal);
}

.app-card:hover .app-cover img {
  transform: scale(1.05);
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: var(--text-tertiary);
}

.featured-badge {
  position: absolute;
  top: var(--spacing-sm);
  right: var(--spacing-sm);
  background: rgba(255, 215, 0, 0.9);
  color: #000;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.app-info {
  padding: var(--spacing-lg);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.app-name {
  font-size: 18px;
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-sm) 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-desc {
  font-size: 14px;
  color: var(--text-tertiary);
  margin: 0 0 var(--spacing-md) 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.app-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.app-type,
.app-author {
  font-size: 12px;
  color: var(--text-quaternary);
  background: var(--bg-tertiary);
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

.app-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: var(--radius-sm);
}

.app-status.deployed {
  background: rgba(76, 175, 80, 0.2);
  color: #4caf50;
}

.app-status.draft {
  background: rgba(255, 152, 0, 0.2);
  color: #ff9800;
}

.app-actions {
  padding: var(--spacing-md) var(--spacing-lg);
  display: flex;
  gap: var(--spacing-sm);
  border-top: 1px solid var(--border-secondary);
}

/* 分页容器 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-xl);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-page {
    padding: var(--spacing-lg);
  }

  .hero-content {
    padding: var(--spacing-xl);
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .section-title {
    font-size: 24px;
  }

  .section-header {
    flex-direction: column;
    gap: var(--spacing-md);
    align-items: flex-start;
  }

  .apps-grid {
    grid-template-columns: 1fr;
  }

  .app-actions {
    flex-wrap: wrap;
  }
}

@media (max-width: 480px) {
  .home-page {
    padding: var(--spacing-md);
  }

  .hero-content {
    padding: var(--spacing-lg);
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 14px;
  }

  .section-title {
    font-size: 20px;
  }

  .search-container {
    width: 100%;
  }

  .app-info {
    padding: var(--spacing-md);
  }

  .app-actions {
    padding: var(--spacing-sm) var(--spacing-md);
  }
}

/* 日间模式调整 */
[data-theme='light'] .hero-title,
[data-theme='light'] .section-title {
  text-shadow: none;
}

[data-theme='light'] .app-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

[data-theme='light'] .app-card.featured {
  box-shadow: 0 0 15px rgba(255, 215, 0, 0.3);
}
</style>
