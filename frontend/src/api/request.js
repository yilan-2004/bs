import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useAuthStore } from '../store/auth'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_PREFIX || '/api',
  timeout: 20000
})

const isAuthExpiredMessage = (message = '') => {
  return /token|Token|未登录|登录|无效|失效|过期|无法读取/i.test(String(message))
}

const redirectToLogin = () => {
  const auth = useAuthStore()
  auth.clearAuth()
  if (router.currentRoute.value.path !== '/login') {
    router.push('/login')
  }
}

service.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    config.headers.satoken = auth.token
  }
  return config
})

service.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data && typeof data.code !== 'undefined' && data.code !== 0) {
      const message = data.message || '请求失败'
      ElMessage.error(message)
      if (data.code === 401 || isAuthExpiredMessage(message)) {
        redirectToLogin()
      }
      return Promise.reject(new Error(message))
    }
    return data?.data ?? data
  },
  (error) => {
    const rawMessage = error.response?.data?.message || error.response?.data || error.message || '网络异常'
    const message = error.response?.status === 500 && String(rawMessage).includes('Request failed')
      ? '后端服务异常或未启动，请检查 8080 服务'
      : rawMessage
    ElMessage.error(message)
    if (error.response?.status === 401 || isAuthExpiredMessage(message)) {
      redirectToLogin()
    }
    return Promise.reject(error)
  }
)

export default service
