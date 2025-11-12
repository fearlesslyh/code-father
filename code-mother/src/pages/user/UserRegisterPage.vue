<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { message } from 'ant-design-vue'
import { userRegister } from '@/api/codeMother/userController'
import type { UserRegisterRequest, BaseResponseLong } from '@/api/codeMother/typings'

const router = useRouter()
const formRef = ref<FormInstance>()

const formState = reactive<UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const loading = ref(false)

const passwordValidator = (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject('请再次输入密码')
  }
  if (value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const rules: Record<string, Rule[]> = {
  userAccount: [
    { required: true, message: '请输入账号' },
    {
      min: 4,
      message: '账号长度不能少于 4 位',
    },
  ],
  userPassword: [
    { required: true, message: '请输入密码' },
    {
      min: 8,
      message: '密码长度不能少于 8 位',
    },
  ],
  checkPassword: [
    { required: true, validator: passwordValidator },
  ],
}

const handleSubmit = async () => {
  if (!formRef.value) {
    return
  }
  try {
    loading.value = true
    await formRef.value.validate()
    const response = await userRegister(formState)
    const parsed = response as BaseResponseLong
    if (parsed.code !== 0) {
      message.error(parsed.message ?? '注册失败')
      return
    }
    message.success('注册成功，请登录')
    await router.replace('/user/login')
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    }
  } finally {
    loading.value = false
  }
}

const goLogin = () => {
  router.push('/user/login')
}
</script>

<template>
  <div class="app-content tech-bg tech-particles">
    <div class="auth-container">
      <div class="auth-card tech-card">
        <div class="auth-header">
          <div class="auth-icon">🚀</div>
          <h2 class="auth-title">注册凌犀零代码平台</h2>
          <div class="auth-subtitle">创建账号，开启零代码开发新体验</div>
        </div>
        
        <a-form 
          ref="formRef" 
          :model="formState" 
          :rules="rules" 
          layout="vertical" 
          @finish="handleSubmit"
          class="auth-form"
        >
          <a-form-item label="账号" name="userAccount" class="tech-form-item">
            <span class="tech-input-wrapper">
              <a-input 
                v-model:value="formState.userAccount" 
                placeholder="请输入账号" 
                allow-clear 
                size="large"
                class="tech-input"
              />
            </span>
          </a-form-item>
          
          <a-form-item label="密码" name="userPassword" class="tech-form-item">
            <span class="tech-input-wrapper">
              <a-input-password 
                v-model:value="formState.userPassword" 
                placeholder="请输入密码" 
                allow-clear 
                size="large"
                class="tech-input"
              />
            </span>
          </a-form-item>
          
          <a-form-item label="确认密码" name="checkPassword" class="tech-form-item">
            <span class="tech-input-wrapper">
              <a-input-password 
                v-model:value="formState.checkPassword" 
                placeholder="请再次输入密码" 
                allow-clear 
                size="large"
                class="tech-input"
              />
            </span>
          </a-form-item>
          
          <a-button 
            type="primary" 
            html-type="submit" 
            block 
            :loading="loading"
            size="large"
            class="tech-button auth-submit-btn"
          >
            <span v-if="!loading">注 册</span>
            <span v-else>注册中...</span>
          </a-button>
        </a-form>
        
        <div class="auth-extra">
          <span class="extra-text">已有账号？</span>
          <a @click.prevent="goLogin" class="tech-link">立即登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 认证页面容器 */
.auth-container {
  min-height: var(--content-min-height);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-lg);
  position: relative;
  z-index: 1;
}

/* 认证卡片 */
.auth-card {
  width: 100%;
  max-width: 420px;
  padding: var(--spacing-xl);
  position: relative;
  z-index: 2;
  animation: fadeInUp 0.6s ease-out;
}

/* 头部区域 */
.auth-header {
  text-align: center;
  margin-bottom: var(--spacing-xl);
}

.auth-icon {
  font-size: 48px;
  margin-bottom: var(--spacing-md);
  filter: drop-shadow(0 0 20px rgba(120, 219, 255, 0.8));
  animation: pulse 2s infinite;
}

.auth-title {
  font-size: 24px;
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin: 0 0 var(--spacing-sm) 0;
  text-shadow: 0 0 25px rgba(120, 219, 255, 0.8);
  letter-spacing: 1px;
  line-height: 1.2;
}

.auth-subtitle {
  font-size: 14px;
  color: var(--text-tertiary);
  margin: 0;
  font-weight: var(--font-weight-normal);
}

/* 表单样式 */
.auth-form {
  margin-bottom: var(--spacing-lg);
}

.tech-form-item :deep(.ant-form-item-label > label) {
  color: var(--text-secondary);
  font-weight: var(--font-weight-medium);
  font-size: 14px;
}

.tech-form-item :deep(.ant-form-item-explain-error) {
  color: var(--border-error);
  font-size: 12px;
  margin-top: var(--spacing-xs);
}

.tech-form-item.tech-error :deep(.ant-input),
.tech-form-item.tech-error :deep(.ant-input-affix-wrapper),
.tech-form-item.tech-error :deep(.ant-input-password) {
  border-color: var(--border-error);
  box-shadow: 0 0 15px rgba(255, 107, 107, 0.3);
}

/* 表单控件样式 - 与span元素保持一致 */
.tech-input,
.tech-input .ant-input,
.tech-input .ant-input-affix-wrapper,
.tech-input .ant-input-password {
  background: transparent;
  border: 1px solid var(--border-primary);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  transition: all var(--transition-normal);
  height: 44px;
  font-size: 14px;
  line-height: 1.5;
  padding: 8px 12px;
  width: 100%;
  box-sizing: border-box;
}

/* 大尺寸输入框与span元素保持一致 */
.tech-input .ant-input-lg,
.tech-input .ant-input-affix-wrapper-lg,
.tech-input .ant-input-password-lg {
  height: 44px;
  font-size: 14px;
  line-height: 1.5;
  padding: 8px 12px;
}

.tech-input .ant-input::placeholder,
.tech-input .ant-input-affix-wrapper input::placeholder,
.tech-input .ant-input-password input::placeholder {
  color: var(--text-quaternary);
}

.tech-input .ant-input:focus,
.tech-input .ant-input-affix-wrapper:focus,
.tech-input .ant-input-password:focus,
.tech-input .ant-input-affix-wrapper-focused,
.tech-input .ant-input-password-focused {
  border-color: var(--border-focus);
  box-shadow: var(--shadow-glow);
  background: rgba(255, 255, 255, 0.05);
}

.tech-input .ant-input-suffix {
  color: var(--text-tertiary);
}

/* 提交按钮 */
.auth-submit-btn {
  height: 48px;
  font-size: 16px;
  font-weight: var(--font-weight-semibold);
  letter-spacing: 1px;
  margin-top: var(--spacing-md);
}

/* 底部额外信息 */
.auth-extra {
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-xs);
  flex-wrap: wrap;
}

.extra-text {
  color: var(--text-tertiary);
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .auth-container {
    padding: var(--spacing-md);
  }
  
  .auth-card {
    padding: var(--spacing-lg);
  }
  
  .auth-icon {
    font-size: 40px;
  }
  
  .auth-title {
    font-size: 20px;
  }
  
  .auth-subtitle {
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .auth-container {
    padding: var(--spacing-sm);
  }
  
  .auth-card {
    padding: var(--spacing-md);
    max-width: none;
  }
  
  .auth-icon {
    font-size: 36px;
  }
  
  .auth-title {
    font-size: 18px;
  }
  
  .auth-submit-btn {
    height: 44px;
    font-size: 15px;
  }
  
  .auth-extra {
    font-size: 13px;
  }
}
</style>
