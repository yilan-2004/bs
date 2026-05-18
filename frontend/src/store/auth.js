import { defineStore } from 'pinia'
import { authApi } from '../api/auth'
import { getStoredUser, getToken, removeStoredUser, removeToken, setStoredUser, setToken } from '../utils/storage'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    userInfo: getStoredUser(),
    role: getStoredUser()?.role || ''
  }),
  actions: {
    async login(payload) {
      const data = await authApi.login(payload)
      this.token = data.token
      this.userInfo = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        role: data.role
      }
      this.role = data.role
      setToken(data.token)
      setStoredUser(this.userInfo)
      return data
    },
    async loadUserInfo() {
      const data = await authApi.info()
      this.userInfo = data
      this.role = data.role
      setStoredUser(data)
      return data
    },
    async logout() {
      try {
        await authApi.logout()
      } finally {
        this.clearAuth()
      }
    },
    clearAuth() {
      this.token = ''
      this.userInfo = null
      this.role = ''
      removeToken()
      removeStoredUser()
    }
  }
})
