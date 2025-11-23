<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { HistoryOutlined, RollbackOutlined, EyeOutlined } from '@ant-design/icons-vue'

interface Props {
  appId: string
  currentVersion?: number
}

const props = defineProps<Props>()

// 版本列表
const versions = ref<Array<{
  version: number
  createTime: string
  description?: string
}>>([])
const loading = ref(false)

// 版本对比
const compareModalVisible = ref(false)
const selectedVersions = ref<[number | null, number | null]>([null, null])

// 获取版本列表
const fetchVersions = async () => {
  try {
    loading.value = true
    // TODO: 调用后端接口获取版本列表
    // const response = await getAppVersions({ appId: props.appId })
    // versions.value = response.data || []
    
    // 模拟数据
    versions.value = [
      { version: 3, createTime: new Date().toISOString(), description: '最新版本' },
      { version: 2, createTime: new Date(Date.now() - 86400000).toISOString(), description: '添加了登录功能' },
      { version: 1, createTime: new Date(Date.now() - 172800000).toISOString(), description: '初始版本' }
    ]
  } catch (error) {
    console.error('获取版本列表失败:', error)
    message.error('获取版本列表失败')
  } finally {
    loading.value = false
  }
}

// 回退到指定版本
const rollbackToVersion = async (version: number) => {
  try {
    // TODO: 调用后端接口回退版本
    // const response = await rollbackAppVersion({ appId: props.appId, version })
    message.success(`已回退到版本 ${version}`)
    await fetchVersions()
  } catch (error) {
    console.error('回退版本失败:', error)
    message.error('回退版本失败')
  }
}

// 查看版本详情
const viewVersion = (version: number) => {
  // TODO: 打开版本详情页面或预览
  message.info(`查看版本 ${version} 的详情`)
}

// 对比版本
const compareVersions = () => {
  if (!selectedVersions.value[0] || !selectedVersions.value[1]) {
    message.warning('请选择两个版本进行对比')
    return
  }
  compareModalVisible.value = true
  // TODO: 调用后端接口获取版本差异
}

onMounted(() => {
  fetchVersions()
})
</script>

<template>
  <div class="version-history">
    <div class="version-header">
      <h3>版本历史</h3>
      <a-button type="primary" size="small" @click="compareVersions">
        <template #icon>
          <EyeOutlined />
        </template>
        版本对比
      </a-button>
    </div>
    
    <a-list
      :data-source="versions"
      :loading="loading"
      size="small"
    >
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <template #title>
              <div class="version-item">
                <span class="version-number">v{{ item.version }}</span>
                <a-tag v-if="item.version === currentVersion" color="blue">当前版本</a-tag>
                <span class="version-time">{{ new Date(item.createTime).toLocaleString() }}</span>
              </div>
            </template>
            <template #description>
              <div class="version-description">{{ item.description || '无描述' }}</div>
              <div class="version-actions">
                <a-button type="link" size="small" @click="viewVersion(item.version)">
                  <template #icon>
                    <EyeOutlined />
                  </template>
                  查看
                </a-button>
                <a-button 
                  v-if="item.version !== currentVersion"
                  type="link" 
                  size="small" 
                  @click="rollbackToVersion(item.version)"
                >
                  <template #icon>
                    <RollbackOutlined />
                  </template>
                  回退
                </a-button>
              </div>
            </template>
          </a-list-item-meta>
        </a-list-item>
      </template>
    </a-list>
    
    <!-- 版本对比模态框 -->
    <a-modal
      v-model:open="compareModalVisible"
      title="版本对比"
      width="800px"
    >
      <div class="version-compare">
        <a-row :gutter="16">
          <a-col :span="12">
            <h4>版本 {{ selectedVersions[0] }}</h4>
            <div class="compare-content">
              <!-- TODO: 显示版本1的内容 -->
            </div>
          </a-col>
          <a-col :span="12">
            <h4>版本 {{ selectedVersions[1] }}</h4>
            <div class="compare-content">
              <!-- TODO: 显示版本2的内容和差异 -->
            </div>
          </a-col>
        </a-row>
      </div>
    </a-modal>
  </div>
</template>

<style scoped>
.version-history {
  padding: 16px;
}

.version-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.version-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.version-number {
  font-weight: 600;
  color: var(--text-primary);
}

.version-time {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-left: auto;
}

.version-description {
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.version-actions {
  display: flex;
  gap: 8px;
}

.version-compare {
  max-height: 600px;
  overflow-y: auto;
}

.compare-content {
  background: var(--bg-secondary);
  padding: 16px;
  border-radius: var(--radius-md);
  min-height: 400px;
}
</style>

