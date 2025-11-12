<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, Rule } from 'ant-design-vue/es/form'
import { message } from 'ant-design-vue'
import { userLogin } from '@/api/codeMother/userController'
import type { UserLoginRequest, BaseResponseLoginUserVO } from '@/api/codeMother/typings'
import { useLoginUserStore } from '@/stores/loginUser'

const router = useRouter()
const loginUserStore = useLoginUserStore()
const formRef = ref<FormInstance>()

const formState = reactive<UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const loading = ref(false)

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
}

const handleSubmit = async () => {
  if (!formRef.value) {
    return
  }
  try {
    loading.value = true
    await formRef.value.validate()
    const response = await userLogin(formState)
    const parsed = response as BaseResponseLoginUserVO
    if (parsed.code !== 0 || !parsed.data) {
      message.error(parsed.message ?? '登录失败')
      return
    }
    loginUserStore.setUser({
      id: String(parsed.data.id ?? ''),
      nickname: parsed.data.userName ?? parsed.data.userAccount ?? '未命名用户',
      avatar: parsed.data.userAvatar,
    })
    await loginUserStore.updateHeartbeat()
    message.success('登录成功')
    const redirect = (router.currentRoute.value.query.redirect as string) ?? '/'
    await router.replace(redirect)
  } catch (error) {
    if (error instanceof Error) {
      message.error(error.message)
    }
  } finally {
    loading.value = false
  }
}

const goRegister = () => {
  router.push('/user/register')
}
</script>

<template>
  <div class="auth-page">
    <a-card class="auth-card" title="登录凌犀零代码平台">
      <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical" @finish="handleSubmit">
        <a-form-item label="账号" name="userAccount">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" allow-clear />
        </a-form-item>
        <a-form-item label="密码" name="userPassword">
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" allow-clear />
        </a-form-item>
        <a-button type="primary" html-type="submit" block :loading="loading">登 录</a-button>
      </a-form>
      <div class="auth-extra">
        如果未注册，请先 <a @click.prevent="goRegister">注册</a>
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
  background: linear-gradient(135deg, #f6f9ff 0%, #ffffff 100%);
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
