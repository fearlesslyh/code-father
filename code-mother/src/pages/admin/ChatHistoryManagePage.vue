<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { listAllChatHistoryByPageForAdmin } from '@/api/codeMother/chatHistoryController'
import type {
  ChatHistory,
  ChatHistoryQueryRequest
} from '@/api/codeMother/typings'
import {
  SearchOutlined,
  ReloadOutlined,
  MessageOutlined,
  UserOutlined,
  RobotOutlined
} from '@ant-design/icons-vue'

const router = useRouter()

// 对话历史列表数据
const chatHistoryList = ref<ChatHistory[]>([])
const loading = ref(false)

// 分页参数
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0,
})

// 搜索参数
const searchParams = ref<ChatHistoryQueryRequest>({
  pageNum: 1,
  pageSize: 20,
})

// 获取对话历史列表
const fetchChatHistoryList = async (params = {}) => {
  try {
    loading.value = true
    const queryParams: ChatHistoryQueryRequest = {
      ...searchParams.value,
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      ...params,
    }

    const response = await listAllChatHistoryByPageForAdmin(queryParams)

    if (response.data?.records) {
      chatHistoryList.value = response.data.records
      pagination.value.total = response.data.totalRow || 0
    } else {
      chatHistoryList.value = []
      pagination.value.total = 0
    }
  } catch (error) {
    console.error('获取对话历史列表失败:', error)
    message.error('获取对话历史列表失败')
  } finally {
    loading.value = false
  }
}

// 处理分页变化
const handleTableChange = (pag: { current: number; pageSize: number }) => {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  fetchChatHistoryList()
}

// 处理搜索
const handleSearch = () => {
  pagination.value.current = 1
  fetchChatHistoryList()
}

// 重置搜索
const resetSearch = () => {
  searchParams.value = {
    pageNum: 1,
    pageSize: pagination.value.pageSize,
  }
  pagination.value.current = 1
  fetchChatHistoryList()
}

// 跳转到应用对话
const goToAppChat = (appId?: string) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 格式化日期
const formatDate = (dateString?: string) => {
  if (!dateString) return '未知'
  return new Date(dateString).toLocaleString()
}

// 获取消息类型标签
const getMessageTypeTag = (messageType?: string) => {
  return messageType === 'user' ? { color: 'blue', text: '用户' } : { color: 'green', text: 'AI' }
}

// 表格列定义
const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '消息类型',
    dataIndex: 'messageType',
    key: 'messageType',
    width: 100,
  },
  {
    title: '消息内容',
    dataIndex: 'message',
    key: 'message',
    ellipsis: true,
    width: 300,
  },
  {
    title: '应用ID',
    dataIndex: 'appId',
    key: 'appId',
    width: 100,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    key: 'userId',
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
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right' as const,
  },
]

// 页面加载时获取对话历史列表
onMounted(() => {
  fetchChatHistoryList()
})
</script>

<template>
  <div class="chat-history-manage-page app-content">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">对话管理</h1>
    </div>

    <!-- 搜索区域 -->
    <div class="search-section">
      <a-form layout="inline" :model="searchParams" class="search-form">
        <a-form-item label="消息内容">
          <a-input
            v-model:value="searchParams.message"
            placeholder="请输入消息内容"
            style="width: 200px"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="消息类型">
          <a-select
            v-model:value="searchParams.messageType"
            placeholder="请选择消息类型"
            style="width: 150px"
            allow-clear
          >
            <a-select-option value="user">用户</a-select-option>
            <a-select-option value="assistant">AI</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="应用ID">
          <a-input
            v-model:value="searchParams.appId"
            placeholder="请输入应用ID"
            style="width: 150px"
            allow-clear
          />
        </a-form-item>

        <a-form-item label="用户ID">
          <a-input
            v-model:value="searchParams.userId"
            placeholder="请输入用户ID"
            style="width: 150px"
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
            <a-button @click="fetchChatHistoryList">
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
        :data-source="chatHistoryList"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1200 }"
      >
        <!-- 消息类型列 -->
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'messageType'">
            <a-tag :color="getMessageTypeTag(record.messageType).color">
              <template #icon>
                <UserOutlined v-if="record.messageType === 'user'" />
                <RobotOutlined v-else />
              </template>
              {{ getMessageTypeTag(record.messageType).text }}
            </a-tag>
          </template>

          <!-- 消息内容列 -->
          <template v-else-if="column.key === 'message'">
            <a-tooltip :title="record.message" placement="topLeft">
              <div class="message-content-cell">
                {{ record.message }}
              </div>
            </a-tooltip>
          </template>

          <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a-tooltip title="查看应用对话">
                <a-button 
                  type="text" 
                  size="small" 
                  @click="goToAppChat(String(record.appId!))"
                >
                  <template #icon>
                    <MessageOutlined />
                  </template>
                </a-button>
              </a-tooltip>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<style scoped>
.chat-history-manage-page {
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

.message-content-cell {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 夜间模式表格样式修复 */
[data-theme='dark'] .chat-history-manage-page :deep(.ant-table) {
  background: transparent !important;
}

[data-theme='dark'] .chat-history-manage-page :deep(.ant-table-container) {
  background: transparent !important;
}

[data-theme='dark'] .chat-history-manage-page :deep(.ant-table-content) {
  background: transparent !important;
}

[data-theme='dark'] .chat-history-manage-page :deep(.ant-table-thead > tr > th) {
  background: rgba(30, 35, 42, 0.6) !important;
  color: rgba(255, 255, 255, 0.85) !important;
  border-bottom: 1px solid rgba(120, 119, 198, 0.3) !important;
}

[data-theme='dark'] .chat-history-manage-page :deep(.ant-table-tbody > tr > td) {
  background: transparent !important;
  border-bottom: 1px solid rgba(120, 119, 198, 0.2) !important;
  color: rgba(255, 255, 255, 0.85) !important;
}

[data-theme='dark'] .chat-history-manage-page :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(120, 119, 198, 0.1) !important;
}

[data-theme='dark'] .chat-history-manage-page :deep(.ant-table-tbody > tr) {
  background: transparent !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-history-manage-page {
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
  .chat-history-manage-page {
    padding: var(--spacing-sm);
  }

  .search-section,
  .table-section {
    padding: var(--spacing-md);
  }
}
</style>

