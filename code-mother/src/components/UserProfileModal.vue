<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { message } from 'ant-design-vue'
import { updateUser } from '@/api/codeMother/userController'
import type { UserUpdateRequest, BaseResponseBoolean } from '@/api/codeMother/typings'
import { useLoginUserStore } from '@/stores/loginUser'

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}>()

const loginUserStore = useLoginUserStore()
const formRef = ref()
const loading = ref(false)

// 表单数据
const formState = reactive<UserUpdateRequest & { 
  id: number
  currentPassword?: string
  newPassword?: string
  confirmPassword?: string
}>({
  id: 0,
  userName: '',
  userAvatar: '',
  userProfile: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 监听visible变化，打开时初始化表单
watch(() => props.visible, (newVal) => {
  if (newVal) {
    const user = loginUserStore.userState.user
    if (user) {
      formState.id = Number(user.id)
      formState.userName = user.nickname
      formState.userAvatar = user.avatar || ''
      formState.userProfile = ''
      formState.currentPassword = ''
      formState.newPassword = ''
      formState.confirmPassword = ''
    }
  }
})

// 验证规则
const validatePassword = (_rule: unknown, value: string) => {
  if (!value) {
    return Promise.resolve()
  }
  if (value.length < 8) {
    return Promise.reject('密码长度不能少于 8 位')
  }
  return Promise.resolve()
}

const validateConfirmPassword = (_rule: unknown, value: string) => {
  if (!value && !formState.newPassword) {
    return Promise.resolve()
  }
  if (value !== formState.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const formRules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符', trigger: 'blur' },
  ],
  newPassword: [
    { validator: validatePassword, trigger: 'blur' },
  ],
  confirmPassword: [
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true

    // 构建更新数据
    const updateData: UserUpdateRequest = {
      id: formState.id,
      userName: formState.userName,
      userAvatar: formState.userAvatar,
      userProfile: formState.userProfile,
    }

    const response = await updateUser(updateData)
    const parsed = response as BaseResponseBoolean

    if (parsed.code === 0 && parsed.data) {
      message.success('个人信息更新成功')
      
      // 更新本地用户信息（保持登录状态）
      const currentUser = loginUserStore.userState.user
      if (currentUser) {
        loginUserStore.setUser({
          id: currentUser.id,
          nickname: formState.userName || '未命名用户',
          avatar: formState.userAvatar,
          userRole: currentUser.userRole, // 保持原有角色
        })
      }
      
      emit('success')
      handleCancel()
    } else {
      message.error(parsed.message || '更新失败')
    }
  } catch (error) {
    if (error instanceof Error && error.message) {
      // 表单验证错误不显示消息
      if (!error.message.includes('validate')) {
        message.error('更新失败，请检查网络连接')
      }
    }
  } finally {
    loading.value = false
  }
}

// 取消操作
const handleCancel = () => {
  formRef.value?.resetFields()
  emit('update:visible', false)
}

// 头像上传（这里只是示例，实际需要对接图片上传服务）
// const handleAvatarChange = (e: Event) => {
//   const target = e.target as HTMLInputElement
//   const file = target.files?.[0]
//   if (file) {
//     // 这里应该上传到服务器并获取URL
//     // 暂时使用本地预览
//     const reader = new FileReader()
//     reader.onload = (event) => {
//       formState.userAvatar = event.target?.result as string
//     }
//     reader.readAsDataURL(file)
//     message.info('头像上传功能待完善，请直接输入图片URL')
//   }
// }
</script>

<template>
  <a-modal
    :open="visible"
    title="编辑个人资料"
    :confirm-loading="loading"
    :width="600"
    @ok="handleSubmit"
    @cancel="handleCancel"
    class="user-profile-modal"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="formRules"
      layout="vertical"
      class="profile-form"
    >
      <a-form-item label="用户名" name="userName">
        <a-input 
          v-model:value="formState.userName" 
          placeholder="请输入用户名"
          size="large"
        />
      </a-form-item>

      <a-form-item label="头像URL" name="userAvatar">
        <a-input 
          v-model:value="formState.userAvatar" 
          placeholder="请输入头像图片URL"
          size="large"
        />
        <div v-if="formState.userAvatar" class="avatar-preview">
          <a-avatar :src="formState.userAvatar" :size="64">
            {{ formState.userName?.charAt(0) }}
          </a-avatar>
        </div>
      </a-form-item>

      <a-form-item label="个人简介" name="userProfile">
        <a-textarea
          v-model:value="formState.userProfile"
          placeholder="请输入个人简介（可选）"
          :rows="4"
          :maxlength="200"
          show-count
          size="large"
        />
      </a-form-item>

      <a-divider>修改密码（可选）</a-divider>

      <a-form-item label="新密码" name="newPassword">
        <a-input-password
          v-model:value="formState.newPassword"
          placeholder="如需修改密码请输入新密码（至少8位）"
          size="large"
        />
      </a-form-item>

      <a-form-item label="确认新密码" name="confirmPassword">
        <a-input-password
          v-model:value="formState.confirmPassword"
          placeholder="请再次输入新密码"
          size="large"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<style scoped>
.avatar-preview {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.profile-form {
  margin-top: 24px;
}

.profile-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
}

.profile-form :deep(.ant-divider) {
  margin: 24px 0 16px;
  border-color: var(--border-secondary);
}

.profile-form :deep(.ant-divider-inner-text) {
  color: var(--text-secondary);
  font-size: 13px;
}

/* 暗色主题样式 */
[data-theme='dark'] .user-profile-modal :deep(.ant-modal-content) {
  background: linear-gradient(135deg, #1a1f3a 0%, #0a0e27 100%);
  border: 1px solid rgba(120, 119, 198, 0.3);
}

[data-theme='dark'] .user-profile-modal :deep(.ant-modal-header) {
  background: transparent;
  border-bottom: 1px solid rgba(120, 119, 198, 0.2);
}

[data-theme='dark'] .user-profile-modal :deep(.ant-modal-title) {
  color: #fff;
  font-weight: 600;
  text-shadow: 0 0 15px rgba(120, 219, 255, 0.6);
}

[data-theme='dark'] .user-profile-modal :deep(.ant-form-item-label > label) {
  color: rgba(255, 255, 255, 0.9);
}

[data-theme='dark'] .user-profile-modal :deep(.ant-input),
[data-theme='dark'] .user-profile-modal :deep(.ant-input-password),
[data-theme='dark'] .user-profile-modal :deep(.ant-textarea) {
  background: rgba(26, 31, 58, 0.8);
  border: 1px solid rgba(120, 119, 198, 0.3);
  color: #fff;
}

[data-theme='dark'] .user-profile-modal :deep(.ant-input::placeholder),
[data-theme='dark'] .user-profile-modal :deep(.ant-textarea::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

[data-theme='dark'] .user-profile-modal :deep(.ant-input:focus),
[data-theme='dark'] .user-profile-modal :deep(.ant-input-password:focus),
[data-theme='dark'] .user-profile-modal :deep(.ant-textarea:focus) {
  border-color: #78dbff;
  box-shadow: 0 0 15px rgba(120, 219, 255, 0.3);
}
</style>
