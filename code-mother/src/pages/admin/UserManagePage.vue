<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue';
import { listUserVOByPage, deleteUser, addUser, updateUser } from '@/api/codeMother/userController';
import type { UserVO, UserQueryRequest, BaseResponsePageUserVO, BaseResponseBoolean, UserAddRequest, UserUpdateRequest } from '@/api/codeMother/typings';
import { message, Modal } from 'ant-design-vue';
import { h } from 'vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';

const users = ref<UserVO[]>([]);
const loading = ref(false);
const searchText = ref('');

// 表单相关状态
const addModalVisible = ref(false);
const editModalVisible = ref(false);
const addFormRef = ref();
const editFormRef = ref();

// 新增用户表单
const addForm = reactive<UserAddRequest>({
  userName: '',
  userAccount: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
});

// 编辑用户表单
const editForm = reactive<UserUpdateRequest & { id: number }>({
  id: 0,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: 'user',
});

const currentEditUser = ref<UserVO | null>(null);

// 表单验证规则
const formRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符', trigger: 'blur' },
  ],
  userAccount: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '账号只能包含字母、数字和下划线', trigger: 'blur' },
  ],
  userRole: [
    { required: true, message: '请选择用户角色', trigger: 'change' },
  ],
};

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  pageSizeOptions: ['10', '20', '50', '100'],
});

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 180 },
  { title: '用户名', dataIndex: 'userName', key: 'userName' },
  { title: '账号', dataIndex: 'userAccount', key: 'userAccount' },
  { title: '头像', dataIndex: 'userAvatar', key: 'avatar', slots: { customRender: 'avatar' } },
  { title: '简介', dataIndex: 'userProfile', key: 'userProfile', ellipsis: true },
  { title: '角色', dataIndex: 'userRole', key: 'userRole' },
  { 
    title: '创建时间', 
    dataIndex: 'createTime', 
    key: 'createTime', 
    sorter: (a: UserVO, b: UserVO) => {
      const timeA = a.createTime ? new Date(a.createTime).getTime() : 0;
      const timeB = b.createTime ? new Date(b.createTime).getTime() : 0;
      return timeA - timeB;
    }
  },
  { title: '操作', key: 'action', width: 150, slots: { customRender: 'action' } },
];

const fetchUsers = async (query: UserQueryRequest = {}) => {
  loading.value = true;
  try {
    const params: UserQueryRequest = {
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      userName: searchText.value || undefined,
      ...query,
    };
    const response = await listUserVOByPage(params);
    const parsed = response as BaseResponsePageUserVO;
    if (parsed.code === 0 && parsed.data) {
      users.value = parsed.data.records ?? [];
      pagination.value.total = parsed.data.totalRow ?? 0;
    } else {
      message.error(parsed.message || '获取用户列表失败');
    }
  } catch {
    message.error('请求失败');
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchUsers();
});

watch(searchText, () => {
  fetchUsers({ userName: searchText.value });
});

interface TablePagination {
  current: number;
  pageSize: number;
}

interface TableSorter {
  field?: string;
  order?: 'ascend' | 'descend';
}

const handleTableChange = (pager: TablePagination, _filters: Record<string, never>, sorter: TableSorter) => {
  pagination.value.current = pager.current;
  pagination.value.pageSize = pager.pageSize;
  let sortField: string | undefined;
  let sortOrder: 'ascend' | 'descend' | undefined;

  if (sorter.field && sorter.order) {
    sortField = sorter.field;
    sortOrder = sorter.order === 'ascend' ? 'ascend' : 'descend';
  }

  fetchUsers({
    sortField,
    sortOrder,
  });
};

const handleSearch = (value: string) => {
  searchText.value = value;
};

const handleEdit = (record: UserVO) => {
  currentEditUser.value = record;
  editForm.id = record.id ?? 0;
  editForm.userName = record.userName || '';
  editForm.userAvatar = record.userAvatar || '';
  editForm.userProfile = record.userProfile || '';
  editForm.userRole = record.userRole || 'user';
  editModalVisible.value = true;
};

const handleDelete = (record: UserVO) => {
  Modal.confirm({
    title: `确认删除用户 "${record.userName}"?`,
    icon: h(ExclamationCircleOutlined),
    content: '此操作不可恢复，将记录操作日志。',
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const response = await deleteUser({ id: record.id });
        const parsed = response as BaseResponseBoolean;
        if (parsed.code === 0 && parsed.data) {
          message.success('删除成功');
          console.log(`用户 ${record.id} (${record.userName}) 已被删除。操作员：[当前管理员ID]`);
          fetchUsers(); // Refresh the list
        } else {
          message.error(parsed.message || '删除失败');
        }
      } catch {
        message.error('请求删除失败');
      }
    },
  });
};

const handleAddUser = () => {
  addModalVisible.value = true;
};

// 重置新增表单
const resetAddForm = () => {
  addForm.userName = '';
  addForm.userAccount = '';
  addForm.userAvatar = '';
  addForm.userProfile = '';
  addForm.userRole = 'user';
};

// 重置编辑表单
const resetEditForm = () => {
  editForm.id = 0;
  editForm.userName = '';
  editForm.userAvatar = '';
  editForm.userProfile = '';
  editForm.userRole = 'user';
  currentEditUser.value = null;
};

// 处理新增用户
const handleAddSubmit = async () => {
  try {
    await addFormRef.value.validate();
    const response = await addUser(addForm);
    if (response.data) {
      message.success('新增用户成功');
      addModalVisible.value = false;
      resetAddForm();
      fetchUsers(); // 刷新用户列表
    } else {
      message.error(response.message || '新增用户失败');
    }
  } catch {
    message.error('新增用户失败');
  }
};

// 处理编辑用户
const handleEditSubmit = async () => {
  try {
    await editFormRef.value.validate();
    const response = await updateUser(editForm);
    if (response.data) {
      message.success('更新用户成功');
      editModalVisible.value = false;
      resetEditForm();
      fetchUsers(); // 刷新用户列表
    } else {
      message.error(response.message || '更新用户失败');
    }
  } catch {
    message.error('更新用户失败');
  }
};

// 处理取消操作
const handleAddCancel = () => {
  addModalVisible.value = false;
  resetAddForm();
};

const handleEditCancel = () => {
  editModalVisible.value = false;
  resetEditForm();
};

</script>

<template>
  <div class="user-manage-page">
    <a-card title="用户管理" :bordered="false">
      <div class="header-bar">
        <div class="search-area">
          <a-input-search
            v-model:value="searchText"
            placeholder="输入用户名关键词"
            enter-button="查询"
            @search="handleSearch"
          />
          <a-button @click="() => (searchText = '')">重置</a-button>
        </div>
        <div class="actions-area">
          <a-button type="primary" @click="handleAddUser">新增用户</a-button>
        </div>
      </div>

      <a-table
        :columns="columns"
        :dataSource="users"
        :row-key="(record: UserVO) => record.id"
        :pagination="pagination"
        :loading="loading"
        @change="handleTableChange"
        responsive
      >
        <template #avatar="{ text: url }">
          <a-avatar :src="url" />
        </template>
        <template #action="{ record }">
          <a-space>
            <a-button type="link" @click="handleEdit(record)">编辑</a-button>
            <a-button type="link" danger @click="handleDelete(record)">删除</a-button>
          </a-space>
        </template>
      </a-table>
    </a-card>

    <!-- 新增用户对话框 -->
    <a-modal
      v-model:open="addModalVisible"
      title="新增用户"
      @ok="handleAddSubmit"
      @cancel="handleAddCancel"
      :confirm-loading="loading"
      width="600px"
    >
      <a-form
        ref="addFormRef"
        :model="addForm"
        :rules="formRules"
        layout="vertical"
        style="margin-top: 20px;"
      >
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="addForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="addForm.userAccount" placeholder="请输入账号" />
        </a-form-item>
        
        <a-form-item label="用户角色" name="userRole">
          <a-select v-model:value="addForm.userRole" placeholder="请选择用户角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="头像URL" name="userAvatar">
          <a-input v-model:value="addForm.userAvatar" placeholder="请输入头像URL（可选）" />
        </a-form-item>
        
        <a-form-item label="个人简介" name="userProfile">
          <a-textarea 
            v-model:value="addForm.userProfile" 
            placeholder="请输入个人简介（可选）" 
            :rows="3"
            :maxlength="200"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 编辑用户对话框 -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑用户"
      @ok="handleEditSubmit"
      @cancel="handleEditCancel"
      :confirm-loading="loading"
      width="600px"
    >
      <a-form
        ref="editFormRef"
        :model="editForm"
        :rules="formRules"
        layout="vertical"
        style="margin-top: 20px;"
      >
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        
        <a-form-item label="用户角色" name="userRole">
          <a-select v-model:value="editForm.userRole" placeholder="请选择用户角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item label="头像URL" name="userAvatar">
          <a-input v-model:value="editForm.userAvatar" placeholder="请输入头像URL（可选）" />
        </a-form-item>
        
        <a-form-item label="个人简介" name="userProfile">
          <a-textarea 
            v-model:value="editForm.userProfile" 
            placeholder="请输入个人简介（可选）" 
            :rows="3"
            :maxlength="200"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
/* 夜间模式样式 */
[data-theme='dark'] .user-manage-page {
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
  min-height: calc(59vh - 60px);
  padding: 24px;
  position: relative;
  overflow: hidden;
}

[data-theme='dark'] .user-manage-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.2) 0%, transparent 50%);
  animation: backgroundAnimation 20s ease-in-out infinite;
  pointer-events: none;
}

@keyframes backgroundAnimation {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 0.8; }
}

/* 白天模式样式 */
[data-theme='light'] .user-manage-page {
  background: #f0f2f5;
  min-height: calc(59vh - 60px);
  padding: 24px;
  position: relative;
  overflow: hidden;
}

[data-theme='light'] .user-manage-page::before {
  display: none;
}

[data-theme='light'] .user-manage-page :deep(.ant-card) {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

[data-theme='light'] .user-manage-page :deep(.ant-card-head) {
  border-bottom: 1px solid #e8e8e8;
}

[data-theme='light'] .user-manage-page :deep(.ant-card-head-title) {
  color: rgba(0, 0, 0, 0.85);
  text-shadow: none;
}

[data-theme='light'] .user-manage-page :deep(.ant-card-head-title)::before {
  background: linear-gradient(180deg, #1890ff 0%, #096dd9 100%);
  box-shadow: 0 0 6px rgba(24, 144, 255, 0.4);
}

[data-theme='light'] .header-bar {
  background: rgba(24, 144, 255, 0.06);
  border: 1px solid rgba(24, 144, 255, 0.15);
}

[data-theme='light'] .search-area :deep(.ant-input),
[data-theme='light'] .search-area :deep(.ant-input-affix-wrapper),
[data-theme='light'] .actions-area :deep(.ant-btn) {
  background: #ffffff;
  border: 1px solid #d9d9d9;
  color: rgba(0, 0, 0, 0.85);
}

[data-theme='light'] .search-area :deep(.ant-input::placeholder) {
  color: rgba(0, 0, 0, 0.25);
}

[data-theme='light'] .search-area :deep(.ant-input:focus),
[data-theme='light'] .search-area :deep(.ant-input-affix-wrapper:focus),
[data-theme='light'] .actions-area :deep(.ant-btn:hover) {
  border-color: #40a9ff;
  box-shadow: 0 0 8px rgba(24, 144, 255, 0.2);
  background: #ffffff;
}

[data-theme='light'] .search-area :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

[data-theme='light'] .user-manage-page :deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
  color: rgba(0, 0, 0, 0.85);
}

[data-theme='light'] .user-manage-page :deep(.ant-table-tbody > tr) {
  border-bottom: 1px solid #f0f0f0;
}

[data-theme='light'] .user-manage-page :deep(.ant-table-tbody > tr > td) {
  color: rgba(0, 0, 0, 0.85);
}

[data-theme='light'] .user-manage-page :deep(.ant-pagination-item) {
  background: #ffffff;
  border: 1px solid #d9d9d9;
}

[data-theme='light'] .user-manage-page :deep(.ant-pagination-item:hover) {
  border-color: #40a9ff;
  background: rgba(24, 144, 255, 0.06);
}

[data-theme='light'] .user-manage-page :deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  border-color: transparent;
}

[data-theme='light'] .user-manage-page :deep(.ant-pagination-item a) {
  color: rgba(0, 0, 0, 0.85);
}

[data-theme='light'] .user-manage-page :deep(.ant-pagination-item-active a) {
  color: #ffffff;
}

[data-theme='light'] .user-manage-page :deep(.ant-btn-link) {
  color: #1890ff;
  text-shadow: none;
}

[data-theme='light'] .user-manage-page :deep(.ant-btn-link:hover) {
  color: #40a9ff;
  background: rgba(24, 144, 255, 0.06);
}

[data-theme='light'] .user-manage-page :deep(.ant-btn-link.danger) {
  color: #ff4d4f;
  text-shadow: none;
}

[data-theme='light'] .user-manage-page :deep(.ant-btn-link.danger:hover) {
  color: #ff7875;
  background: rgba(255, 77, 79, 0.06);
}

[data-theme='light'] .user-manage-page :deep(.ant-avatar) {
  border: 2px solid rgba(24, 144, 255, 0.3);
  box-shadow: 0 0 8px rgba(24, 144, 255, 0.15);
}

/* 模态框白天模式 */
[data-theme='light'] .user-manage-page :deep(.ant-modal-content) {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

[data-theme='light'] .user-manage-page :deep(.ant-modal-header) {
  background: #fafafa;
  border-bottom: 1px solid #e8e8e8;
}

[data-theme='light'] .user-manage-page :deep(.ant-modal-title) {
  color: rgba(0, 0, 0, 0.85);
  text-shadow: none;
}

[data-theme='light'] .user-manage-page :deep(.ant-form-item-label > label) {
  color: rgba(0, 0, 0, 0.85);
}

[data-theme='light'] .user-manage-page :deep(.ant-input),
[data-theme='light'] .user-manage-page :deep(.ant-input-affix-wrapper),
[data-theme='light'] .user-manage-page :deep(.ant-select-selector),
[data-theme='light'] .user-manage-page :deep(.ant-textarea) {
  background: #ffffff;
  border: 1px solid #d9d9d9;
  color: rgba(0, 0, 0, 0.85);
}

[data-theme='light'] .user-manage-page :deep(.ant-input::placeholder),
[data-theme='light'] .user-manage-page :deep(.ant-textarea::placeholder) {
  color: rgba(0, 0, 0, 0.25);
}

[data-theme='light'] .user-manage-page :deep(.ant-input:focus),
[data-theme='light'] .user-manage-page :deep(.ant-input-affix-wrapper:focus),
[data-theme='light'] .user-manage-page :deep(.ant-select-focused .ant-select-selector),
[data-theme='light'] .user-manage-page :deep(.ant-textarea:focus) {
  border-color: #40a9ff;
  box-shadow: 0 0 8px rgba(24, 144, 255, 0.15);
  background: #ffffff;
}

[data-theme='light'] .user-manage-page :deep(.ant-select-arrow) {
  color: rgba(0, 0, 0, 0.45);
}

[data-theme='light'] .user-manage-page :deep(.ant-select-dropdown) {
  background: #ffffff;
  border: 1px solid #d9d9d9;
}

[data-theme='light'] .user-manage-page :deep(.ant-select-item) {
  color: rgba(0, 0, 0, 0.85);
}

[data-theme='light'] .user-manage-page :deep(.ant-select-item-option-selected) {
  background: rgba(24, 144, 255, 0.1);
}

[data-theme='light'] .user-manage-page :deep(.ant-select-item-option-active) {
  background: rgba(24, 144, 255, 0.06);
}

[data-theme='light'] .user-manage-page :deep(.ant-modal-footer) {
  background: #fafafa;
  border-top: 1px solid #e8e8e8;
}

[data-theme='light'] .user-manage-page :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
}

[data-theme='light'] .user-manage-page :deep(.ant-btn-primary:hover) {
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

[data-theme='light'] .user-manage-page :deep(.ant-btn-default) {
  background: #ffffff;
  border: 1px solid #d9d9d9;
  color: rgba(0, 0, 0, 0.85);
}

[data-theme='light'] .user-manage-page :deep(.ant-btn-default:hover) {
  border-color: #40a9ff;
  background: rgba(24, 144, 255, 0.06);
  color: #1890ff;
}

[data-theme='light'] .user-manage-page :deep(.ant-spin-dot-item) {
  background-color: #1890ff;
}

[data-theme='light'] .user-manage-page :deep(.ant-form-item-extra) {
  color: rgba(0, 0, 0, 0.45);
}

/* 夜间模式保持原样式 */
[data-theme='dark'] .user-manage-page :deep(.ant-card) {
  background: rgba(26, 31, 58, 0.8);
  border: 1px solid rgba(120, 119, 198, 0.3);
  border-radius: 16px;
  backdrop-filter: blur(20px);
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 1;
}

.user-manage-page :deep(.ant-card-head) {
  border-bottom: 1px solid rgba(120, 119, 198, 0.2);
  background: transparent;
}

.user-manage-page :deep(.ant-card-head-title) {
  color: #fff;
  font-size: 20px;
  font-weight: 600;
  text-shadow: 0 0 20px rgba(120, 119, 198, 0.8);
  position: relative;
}

.user-manage-page :deep(.ant-card-head-title)::before {
  content: '';
  position: absolute;
  left: -20px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 24px;
  background: linear-gradient(180deg, #78dbff 0%, #7877c6 100%);
  border-radius: 2px;
  box-shadow: 0 0 10px rgba(120, 219, 255, 0.8);
}

[data-theme='dark'] .header-bar,
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  gap: 16px;
  flex-wrap: wrap;
  padding: 20px;
  background: rgba(120, 119, 198, 0.1);
  border-radius: 12px;
  border: 1px solid rgba(120, 119, 198, 0.2);
  backdrop-filter: blur(10px);
}

.search-area {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  max-width: 400px;
}

.actions-area {
  display: flex;
  align-items: center;
  gap: 12px;
}

[data-theme='dark'] .search-area :deep(.ant-input),
[data-theme='dark'] .search-area :deep(.ant-input-affix-wrapper),
[data-theme='dark'] .actions-area :deep(.ant-btn),
.search-area :deep(.ant-input),
.search-area :deep(.ant-input-affix-wrapper),
.actions-area :deep(.ant-btn) {
  background: rgba(26, 31, 58, 0.8);
  border: 1px solid rgba(120, 119, 198, 0.3);
  color: #fff;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.search-area :deep(.ant-input::placeholder) {
  color: rgba(255, 255, 255, 0.5);
}

.search-area :deep(.ant-input:focus),
.search-area :deep(.ant-input-affix-wrapper:focus),
.actions-area :deep(.ant-btn:hover) {
  border-color: #78dbff;
  box-shadow: 0 0 15px rgba(120, 219, 255, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

.search-area :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #78dbff 0%, #7877c6 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(120, 219, 255, 0.4);
}

.search-area :deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(120, 219, 255, 0.6);
}

.user-manage-page :deep(.ant-table) {
  background: transparent;
  border-radius: 12px;
  overflow: hidden;
}

.user-manage-page :deep(.ant-table-thead > tr > th) {
  background: rgba(120, 119, 198, 0.15);
  border-bottom: 1px solid rgba(120, 119, 198, 0.3);
  color: #fff;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-size: 12px;
  padding: 16px;
}

.user-manage-page :deep(.ant-table-tbody > tr) {
  border-bottom: 1px solid rgba(120, 119, 198, 0.1);
  transition: all 0.3s ease;
}

.user-manage-page :deep(.ant-table-tbody > tr:hover) {
  background: transparent;
  transform: none;
}

.user-manage-page :deep(.ant-table-tbody > tr > td) {
  border-bottom: none;
  color: rgba(255, 255, 255, 0.9);
  padding: 16px;
}

.user-manage-page :deep(.ant-table-pagination) {
  margin-top: 24px;
}

.user-manage-page :deep(.ant-pagination) {
  color: #fff;
}

.user-manage-page :deep(.ant-pagination-item) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(120, 119, 198, 0.3);
  border-radius: 6px;
  transition: all 0.3s ease;
}

.user-manage-page :deep(.ant-pagination-item:hover) {
  border-color: #78dbff;
  background: rgba(120, 219, 255, 0.1);
}

.user-manage-page :deep(.ant-pagination-item-active) {
  background: linear-gradient(135deg, #78dbff 0%, #7877c6 100%);
  border-color: transparent;
}

.user-manage-page :deep(.ant-pagination-item a) {
  color: #fff;
}

.user-manage-page :deep(.ant-pagination-item-active a) {
  color: #fff;
}

.user-manage-page :deep(.ant-btn-link) {
  color: #78dbff;
  text-shadow: 0 0 8px rgba(120, 219, 255, 0.6);
  transition: all 0.3s ease;
  border: none;
  background: transparent;
  padding: 4px 8px;
  border-radius: 4px;
}

.user-manage-page :deep(.ant-btn-link:hover) {
  color: #7877c6;
  text-shadow: 0 0 12px rgba(120, 119, 198, 0.8);
  background: rgba(120, 119, 198, 0.1);
  transform: scale(1.05);
}

.user-manage-page :deep(.ant-btn-link.danger) {
  color: #ff6b6b;
  text-shadow: 0 0 8px rgba(255, 107, 107, 0.6);
}

.user-manage-page :deep(.ant-btn-link.danger:hover) {
  color: #ff4757;
  text-shadow: 0 0 12px rgba(255, 71, 87, 0.8);
  background: rgba(255, 107, 107, 0.1);
}

.user-manage-page :deep(.ant-avatar) {
  border: 2px solid rgba(120, 219, 255, 0.5);
  box-shadow: 0 0 15px rgba(120, 219, 255, 0.3);
}

/* 模态框样式 */
.user-manage-page :deep(.ant-modal-content) {
  background: linear-gradient(135deg, #1a1f3a 0%, #0a0e27 100%);
  border: 1px solid rgba(120, 119, 198, 0.3);
  border-radius: 16px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
}

.user-manage-page :deep(.ant-modal-header) {
  background: transparent;
  border-bottom: 1px solid rgba(120, 119, 198, 0.2);
  border-radius: 16px 16px 0 0;
}

.user-manage-page :deep(.ant-modal-title) {
  color: #fff;
  font-weight: 600;
  text-shadow: 0 0 15px rgba(120, 219, 255, 0.6);
}

.user-manage-page :deep(.ant-modal-body) {
  background: transparent;
}

.user-manage-page :deep(.ant-form-item-label > label) {
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.user-manage-page :deep(.ant-input),
.user-manage-page :deep(.ant-input-affix-wrapper),
.user-manage-page :deep(.ant-select-selector),
.user-manage-page :deep(.ant-textarea) {
  background: rgba(26, 31, 58, 0.8);
  border: 1px solid rgba(120, 119, 198, 0.3);
  border-radius: 8px;
  color: #fff;
  transition: all 0.3s ease;
  width: 100%;
  box-sizing: border-box;
}

.user-manage-page :deep(.ant-input::placeholder),
.user-manage-page :deep(.ant-textarea::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

.user-manage-page :deep(.ant-input:focus),
.user-manage-page :deep(.ant-input-affix-wrapper:focus),
.user-manage-page :deep(.ant-select-focused .ant-select-selector),
.user-manage-page :deep(.ant-textarea:focus) {
  border-color: #78dbff;
  box-shadow: 0 0 15px rgba(120, 219, 255, 0.3);
  background: rgba(26, 31, 58, 0.95);
}

.user-manage-page :deep(.ant-select-arrow) {
  color: rgba(255, 255, 255, 0.6);
}

.user-manage-page :deep(.ant-select-dropdown) {
  background: rgba(26, 31, 58, 0.95);
  border: 1px solid rgba(120, 119, 198, 0.3);
  border-radius: 8px;
  backdrop-filter: blur(10px);
}

.user-manage-page :deep(.ant-select-item) {
  color: rgba(255, 255, 255, 0.9);
}

.user-manage-page :deep(.ant-select-item-option-selected) {
  background: rgba(120, 219, 255, 0.2);
}

.user-manage-page :deep(.ant-select-item-option-active) {
  background: rgba(120, 219, 255, 0.1);
}

.user-manage-page :deep(.ant-modal-footer) {
  background: transparent;
  border-top: 1px solid rgba(120, 119, 198, 0.2);
}

.user-manage-page :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #78dbff 0%, #7877c6 100%);
  border: none;
  border-radius: 8px;
  font-weight: 500;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.user-manage-page :deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(120, 219, 255, 0.4);
}

.user-manage-page :deep(.ant-btn-default) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(120, 119, 198, 0.3);
  color: rgba(255, 255, 255, 0.9);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-manage-page :deep(.ant-btn-default:hover) {
  border-color: #78dbff;
  background: rgba(120, 219, 255, 0.1);
  color: #fff;
}

/* 加载状态样式 */
.user-manage-page :deep(.ant-spin-dot-item) {
  background-color: #78dbff;
}

/* 字符计数样式 */
.user-manage-page :deep(.ant-form-item-extra) {
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .user-manage-page {
    padding: 16px;
  }
  
  .header-bar {
    flex-direction: column;
    gap: 16px;
    padding: 16px;
  }
  
  .search-area {
    max-width: 100%;
    width: 100%;
  }
  
  .actions-area {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
