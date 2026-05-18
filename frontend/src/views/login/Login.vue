<template>
  <div ref="reactRoot" class="react-auth-host"></div>
</template>

<script setup>
import React from 'react'
import { createRoot } from 'react-dom/client'
import { ElMessage } from 'element-plus'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import AnimatedAuthPage from '../../react-auth/AnimatedAuthPage.jsx'
import { useAuthStore } from '../../store/auth'

const router = useRouter()
const auth = useAuthStore()
const reactRoot = ref(null)
const loading = ref(false)
const errorMessage = ref('')
let root = null

async function handleLogin(username, password) {
  if (loading.value) return
  loading.value = true
  errorMessage.value = ''
  try {
    const data = await auth.login({ username, password })
    ElMessage.success('登录成功')
    router.push(data.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard')
  } catch (error) {
    const message = error?.message || '登录失败，请检查账号或密码'
    errorMessage.value = message
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

function renderReact() {
  if (!root) return
  root.render(React.createElement(AnimatedAuthPage, {
    mode: 'login',
    loading: loading.value,
    errorMessage: errorMessage.value,
    onLogin: handleLogin,
    onRegister: () => router.push('/register')
  }))
}

onMounted(async () => {
  await nextTick()
  root = createRoot(reactRoot.value)
  renderReact()
})

watch([loading, errorMessage], renderReact)

onBeforeUnmount(() => {
  root?.unmount()
  root = null
})
</script>

<style scoped>
.react-auth-host {
  min-height: 100vh;
}
</style>
