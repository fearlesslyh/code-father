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
  <div class="auth-page">
    <a-card class="auth-card" title="注册">
      <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical" @finish="handleSubmit">
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" allow-clear />
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" allow-clear />
        </a-form-item>
        <a-form-item label="确认密码" name="checkPassword">
          <a-input-password v-model:value="formState.checkPassword" placeholder="请再次输入密码" allow-clear />
        </a-form-item>
        <a-button type="primary" html-type="submit" block :loading="loading">注 册</a-button>
      </a-form>
      <div class="auth-extra">
        已有账号？请<a @click.prevent="goLogin">登录</a>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: calc(100vh - 128px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff5f7 0%, #ffffff 100%);
}

.auth-card {
  width: 420px;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
  border-radius: 16px;
}

.auth-extra {
  margin-top: 16px;
  text-align: center;
  color: rgba(0, 0, 0, 0.45);
}

.auth-extra a {
  margin-left: 4px;
}
</style>
