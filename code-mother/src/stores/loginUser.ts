import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { message } from 'ant-design-vue'
import { isAxiosError } from 'axios'
import type { BaseResponseLoginUserVO } from '@/api/codeMother/typings'
import { getLoginUser } from '@/api/codeMother/userController'
import { clearStoredAuthToken } from '@/api/http'

interface LoginUser {
  id: string
  nickname: string
  avatar?: string
  userRole?: string
}

interface LoginUserState {
  user: LoginUser | null
  loading: boolean
  lastHeartbeat: string | null
  initializing: boolean
  hasFetched: boolean
}

const createDefaultState = (): LoginUserState => ({
  user: null,
  loading: false,
  lastHeartbeat: null,
  initializing: false,
  hasFetched: false,
})

export const useLoginUserStore = defineStore('loginUser', () => {
  const userState = ref<LoginUserState>(createDefaultState())

  const isLoggedIn = computed(() => userState.value.user !== null)

  const formattedHeartbeat = computed(() => {
    if (!userState.value.lastHeartbeat) {
      return '未检测'
    }
    return new Date(userState.value.lastHeartbeat).toLocaleString()
  })

  const setUser = (user: LoginUser | null) => {
    userState.value.user = user
    userState.value.hasFetched = true
    userState.value.initializing = false
  }

  const resetUser = () => {
    userState.value = {
      ...createDefaultState(),
      hasFetched: true,
    }
  }

  const fetchLoginUser = async (force = false): Promise<boolean> => {
    if (!force && (userState.value.initializing || userState.value.hasFetched)) {
      return userState.value.user !== null
    }
    try {
      userState.value.initializing = true
      const response = await getLoginUser()
      const parsed = response as BaseResponseLoginUserVO
      if (parsed?.code !== 0 || !parsed?.data) {
        setUser(null)
        if (parsed?.message) {
          message.warning(parsed.message)
        }
        return false
      }
      const data = parsed.data
      setUser({
        id: String(data.id ?? ''),
        nickname: data.userName ?? data.userAccount ?? '未命名用户',
        avatar: data.userAvatar,
        userRole: data.userRole,
      })
      return true
    } catch (error) {
      setUser(null)
      if (isAxiosError(error) && error.response?.status === 401) {
        return false
      }
      if (isAxiosError(error) && error.message) {
        message.error(error.message)
      }
      return false
    } finally {
      userState.value.initializing = false
      userState.value.hasFetched = true
    }
  }

  const updateHeartbeat = async () => {
    if (userState.value.loading) {
      return
    }
    try {
      userState.value.loading = true
      userState.value.lastHeartbeat = new Date().toISOString()
    } catch (error) {
      if (isAxiosError(error)) {
        message.warning('心跳检测失败')
      }
    } finally {
      userState.value.loading = false
    }
  }

  const logout = () => {
    // 清除认证令牌
    clearStoredAuthToken()
    // 重置用户状态
    resetUser()
  }

  return {
    userState,
    isLoggedIn,
    formattedHeartbeat,
    setUser,
    resetUser,
    fetchLoginUser,
    updateHeartbeat,
    logout,
  }
})
