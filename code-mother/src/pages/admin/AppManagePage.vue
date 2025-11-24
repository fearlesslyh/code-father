<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  listAppVoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin
} from '@/api/codeMother/appController'
import type {
  AppVO,
  AppQueryRequest,
  DeleteRequest,
  AppAdminUpdateRequest
} from '@/api/codeMother/typings'
import {
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  StarOutlined,
  EyeOutlined,
  MessageOutlined,
  PushpinOutlined
} from '@ant-design/icons-vue'
import { getCodeGenTypeName, CodeGenTypeOptions } from '@/constants/codeGenType'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 应用列表数据
const appList = ref<AppVO[]>([])
const loading = ref(false)

// 分页参数
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
})

// 搜索参数
const searchParams = ref<AppQueryRequest>({
  pageNum: 1,
  pageSize: 20,
})

// 删除确认
const deleteConfirmVisible = ref(false)
const deleteAppId = ref<string | null>(null)

// 精选设置
const featuredModalVisible = ref(false)
const featuredAppId = ref<string | null>(null)
const featuredPriority = ref(0)

// 获取应用列表
const fetchAppList = async (params = {}) => {
  try {
    loading.value = true
    const queryParams: AppQueryRequest = {
      ...searchParams.value,
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...params,
    }

    const response = await listAppVoByPageByAdmin(queryParams)

    if (response.data?.records) {
      appList.value = response.data.records
      pagination.value.total = response.data.totalRow || 0
    } else {
      appList.value = []
      pagination.value.total = 0
    }
  } catch (error) {
    console.error('获取应用列表失败:', error)
    message.error('获取应用列表失败')
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handleTableChange = (pag: { current: number; pageSize: number }) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  fetchAppList()
}

// 处理搜索
const handleSearch = () => {
  pagination.value.current = 1
  fetchAppList()
}

// 重置搜索
const resetSearch = () => {
  searchParams.value = {
    pageNum: 1,
    pageSize: pagination.value.pageSize,
  }
  pagination.value.current = 1
  fetchAppList()
}

// 设置精选（优先级为99）
const setFeatured = async (appId: string) => {
  try {
    const updateRequest: AppAdminUpdateRequest = {
      id: appId,
      priority: 99
    }
    
    const response = await updateAppByAdmin(updateRequest)
    
    if (response.data) {
      message.success('应用已设置为精选')
      fetchAppList()
    } else {
      message.error('设置精选失败')
    }
  } catch (error) {
    console.error('设置精选失败:', error)
    message.error('设置精选失败')
  }
}

// 设置置顶（优先级为999）
const setPinned = async (appId: string) => {
  try {
    const updateRequest: AppAdminUpdateRequest = {
      id: appId,
      priority: 999
    }
    
    const response = await updateAppByAdmin(updateRequest)
    
    if (response.data) {
      message.success('应用已置顶')
      fetchAppList()
    } else {
      message.error('置顶失败')
    }
  } catch (error) {
    console.error('置顶失败:', error)
    message.error('置顶失败')
  }
}

// 取消置顶
const unpin = async (appId: string) => {
  try {
    const updateRequest: AppAdminUpdateRequest = {
      id: appId,
      priority: 99 // 取消置顶后设为精选
    }
    
    const response = await updateAppByAdmin(updateRequest)
    
    if (response.data) {
      message.success('已取消置顶')
      fetchAppList()
    } else {
      message.error('取消置顶失败')
    }
  } catch (error) {
    console.error('取消置顶失败:', error)
    message.error('取消置顶失败')
  }
}

// 删除应用
const confirmDelete = (appId: string) => {
  deleteAppId.value = appId
  deleteConfirmVisible.value = true
}

const handleDelete = async () => {
  if (!deleteAppId.value) return

  try {
    const deleteRequest: DeleteRequest = {
      id: deleteAppId.value
    }

    const response = await deleteAppByAdmin(deleteRequest)

    if (response.data) {
      message.success('应用删除成功')
      fetchAppList()
    } else {
      message.error('应用删除失败')
    }
  } catch (error) {
    console.error('删除应用失败:', error)
    message.error('删除应用失败')
  } finally {
    deleteConfirmVisible.value = false
    deleteAppId.value = null
  }
}

// 设置精选（打开模态框用于自定义优先级）
const openFeaturedModal = (app: AppVO) => {
  featuredAppId.value = app.id || null
  featuredPriority.value = app.priority || 0
  featuredModalVisible.value = true
}

// 取消精选（设置优先级为0）
const unsetFeatured = async (appId: string) => {
  try {
    const updateRequest: AppAdminUpdateRequest = {
      id: appId,
      priority: 0
    }

    const response = await updateAppByAdmin(updateRequest)

    if (response.data) {
      message.success('已取消精选')
      fetchAppList()
    } else {
      message.error('取消精选失败')
    }
  } catch (error) {
    console.error('取消精选失败:', error)
    message.error('取消精选失败')
  }
}

// 处理设置优先级（通过模态框）
const handleSetFeatured = async () => {
  if (!featuredAppId.value) return

  try {
    const updateRequest: AppAdminUpdateRequest = {
      id: featuredAppId.value,
      priority: featuredPriority.value
    }

    const response = await updateAppByAdmin(updateRequest)

    if (response.data) {
      message.success('应用优先级设置成功')
      featuredModalVisible.value = false
      fetchAppList()
    } else {
      message.error('应用优先级设置失败')
    }
  } catch (error) {
    console.error('设置应用优先级失败:', error)
    message.error('设置应用优先级失败')
  }
}

// 跳转到应用详情
const goToAppDetail = (appId: string) => {
  router.push(`/app/detail/${appId}`)
}

// 跳转到应用对话
const goToAppChat = (appId: string) => {
  router.push(`/app/chat/${appId}`)
}

// 跳转到应用编辑
const goToAppEdit = (appId: string) => {
  router.push(`/app/edit/${appId}`)
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleString()
}

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 100,
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    ellipsis: true,
    width: 180,
  },
  {
    title: '初始化Prompt',
    dataIndex: 'initPrompt',
    key: 'initPrompt',
    ellipsis: true,
    width: 250,
  },
  {
    title: '创建者',
    dataIndex: ['user', 'userName'],
    key: 'userName',
    width: 120,
  },
  {
    title: '应用类型',
    dataIndex: 'codeGenType',
    key: 'codeGenType',
    width: 130,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 100,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
    customRender: ({ text }: { text: string }) => formatDate(text),
  },
  {
    title: '部署状态',
    key: 'deployStatus',
    width: 100,
    customRender: ({ record }: { record: AppVO }) => (
      record.deployedTime ? '已部署' : '未部署'
    ),
  },
  {
    title: '操作',
    key: 'action',
    width: 260,
    fixed: 'right' as const,
  },
]

// 页面加载时获取应用列表
onMounted(() => {
  fetchAppList()
})
</script>

<template>
  <div class="app-manage-page app-content">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">应用管理</h1>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-form layout="inline" :model="searchParams" class="search-form">
        <a-form-item label="应用名称">
          <a-input
            v-model:value="searchParams.appName"
            placeholder="请输入应用名称"
            style="width: 200px"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="创建者ID">
          <a-input-number
            v-model:value="searchParams.userId"
            placeholder="请输入创建者ID"
            style="width: 150px"
            :min="1"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="应用类型">
          <a-select
            v-model:value="searchParams.codeGenType"
            placeholder="请选择应用类型"
            style="width: 180px"
            allow-clear
          >
            <a-select-option
              v-for="option in CodeGenTypeOptions"
              :key="option.value"
              :value="option.value"
            >
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="部署Key">
          <a-input
            v-model:value="searchParams.deployKey"
            placeholder="请输入部署Key"
            style="width: 200px"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="优先级">
          <a-input-number
            v-model:value="searchParams.priority"
            placeholder="请输入优先级"
            style="width: 120px"
            :min="0"
            :max="99"
            allow-clear
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <SearchOutlined />
              </template>
              搜索
            </a-button>
            <a-button @click="resetSearch">
              重置
            </a-button>
            <a-button @click="fetchAppList">
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </div>

    <!-- 表格区域 -->
    <div class="table-section">
      <a-table
        :columns="columns"
        :data-source="appList"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <!-- 应用名称列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'appName'">
            <div class="app-name-cell">
              <span>{{ record.appName || '未命名应用' }}</span>
              <a-tag v-if="record.priority && record.priority > 0" color="gold" size="small">
                <StarOutlined /> 精选
              </a-tag>
            </div>
          </template>

          <!-- 优先级列 -->
          <template v-else-if="column.key === 'priority'">
            <a-tag v-if="record.priority === 999" color="red">
              <template #icon>
                <PushpinOutlined />
              </template>
              置顶
            </a-tag>
            <a-tag v-else-if="record.priority === 99" color="gold">
              <template #icon>
                <StarOutlined />
              </template>
              精选
            </a-tag>
            <a-tag v-else color="default">
              未精选
            </a-tag>
          </template>

          <!-- 部署状态列 -->
          <template v-else-if="column.key === 'deployStatus'">
            <a-tag v-if="record.deployedTime" color="success">
              已部署
            </a-tag>
            <a-tag v-else color="warning">
              未部署
            </a-tag>
          </template>

          <!-- 应用类型列 -->
          <template v-else-if="column.key === 'codeGenType'">
            <span>{{ getCodeGenTypeName(record.codeGenType) }}</span>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-tooltip title="查看详情">
                <a-button type="text" size="small" @click="goToAppDetail(String(record.id!))">
                  <template #icon>
                    <EyeOutlined />
                  </template>
                </a-button>
              </a-tooltip>

              <a-tooltip title="对话">
                <a-button type="text" size="small" @click="goToAppChat(String(record.id!))">
                  <template #icon>
                    <MessageOutlined />
                  </template>
                </a-button>
              </a-tooltip>

              <a-tooltip title="编辑">
                <a-button type="text" size="small" @click="goToAppEdit(String(record.id!))">
                  <template #icon>
                    <EditOutlined />
                  </template>
                </a-button>
              </a-tooltip>

              <a-tooltip :title="record.priority === 999 ? '取消置顶' : '置顶'">
                <a-button 
                  type="text" 
                  size="small" 
                  @click="record.priority === 999 ? unpin(String(record.id!)) : setPinned(String(record.id!))"
                  :style="record.priority === 999 ? { color: '#ff4d4f' } : {}"
                >
                  <template #icon>
                    <PushpinOutlined />
                  </template>
                </a-button>
              </a-tooltip>
              
              <a-tooltip :title="record.priority === 99 ? '取消精选' : '设置精选'">
                <a-button 
                  type="text" 
                  size="small" 
                  @click="record.priority === 99 ? unsetFeatured(String(record.id!)) : setFeatured(String(record.id!))"
                  :style="record.priority === 99 ? { color: '#faad14' } : {}"
                >
                  <template #icon>
                    <StarOutlined />
                  </template>
                </a-button>
              </a-tooltip>
              
              <a-tooltip title="自定义优先级">
                <a-button type="text" size="small" @click="openFeaturedModal(record)">
                  <template #icon>
                    <EditOutlined />
                  </template>
                </a-button>
              </a-tooltip>

              <a-tooltip title="删除">
                <a-popconfirm
                  title="确定要删除这个应用吗？删除后不可恢复。"
                  @confirm="confirmDelete(String(record.id!))"
                >
                  <a-button type="text" size="small" danger>
                    <template #icon>
                      <DeleteOutlined />
                    </template>
                  </a-button>
                </a-popconfirm>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>

    <!-- 设置精选模态框 -->
    <a-modal
      v-model:open="featuredModalVisible"
      title="设置应用优先级"
      @ok="handleSetFeatured"
      :confirm-loading="loading"
    >
      <a-form layout="vertical">
        <a-form-item label="优先级">
          <a-input-number
            v-model:value="featuredPriority"
            :min="0"
            :max="999"
            style="width: 100%"
          />
          <div class="form-help-text">
            数值越大，应用在精选列表中的排序越靠前。设置为99可设为精选应用，设置为999可置顶显示
          </div>
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 删除确认模态框 -->
    <a-modal
      v-model:open="deleteConfirmVisible"
      title="删除确认"
      @ok="handleDelete"
      :confirm-loading="loading"
      ok-text="删除"
      cancel-text="取消"
      ok-type="danger"
    >
      <p>确定要删除这个应用吗？删除后不可恢复。</p>
    </a-modal>
  </div>
</template>

<style scoped>
.app-manage-page {
  padding: var(--spacing-lg);
}

/* 页面标题 */
.page-header {
  margin-bottom: var(--spacing-lg);
}

.page-title {
  font-size: 24px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0;
}

/* 搜索区域 */
.search-section {
  background: var(--bg-glass);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
  border: 1px solid var(--border-primary);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
}

/* 表格区域 */
.table-section {
  background: var(--bg-glass);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  border: 1px solid var(--border-primary);
}

.app-name-cell {
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.form-help-text {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-top: var(--spacing-xs);
}

/* 夜间模式表格样式修复 */
[data-theme='dark'] .app-manage-page :deep(.ant-table) {
  background: transparent !important;
}

[data-theme='dark'] .app-manage-page :deep(.ant-table-container) {
  background: transparent !important;
}

[data-theme='dark'] .app-manage-page :deep(.ant-table-content) {
  background: transparent !important;
}

[data-theme='dark'] .app-manage-page :deep(.ant-table-thead > tr > th) {
  background: rgba(30, 35, 42, 0.6) !important;
  color: rgba(255, 255, 255, 0.85) !important;
  border-bottom: 1px solid rgba(120, 119, 198, 0.3) !important;
}

[data-theme='dark'] .app-manage-page :deep(.ant-table-tbody > tr > td) {
  background: transparent !important;
  border-bottom: 1px solid rgba(120, 119, 198, 0.2) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

[data-theme='dark'] .app-manage-page :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(120, 119, 198, 0.1) !important;
}

[data-theme='dark'] .app-manage-page :deep(.ant-table-tbody > tr) {
  background: transparent !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-manage-page {
    padding: var(--spacing-md);
  }

  .search-form {
    flex-direction: column;
  }

  .search-form .ant-form-item {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .app-manage-page {
    padding: var(--spacing-sm);
  }

  .search-section,
  .table-section {
    padding: var(--spacing-md);
  }
}
</style>
