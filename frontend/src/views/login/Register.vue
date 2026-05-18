<template>
  <div ref="reactRoot" class="react-auth-host"></div>
</template>

<script setup>
import React from 'react'
import { createRoot } from 'react-dom/client'
import { ElMessage } from 'element-plus'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../../api/auth'
import AnimatedAuthPage from '../../react-auth/AnimatedAuthPage.jsx'

const router = useRouter()
const reactRoot = ref(null)
const loading = ref(false)
const errorMessage = ref('')
let root = null

async function handleRegister(payload) {
  if (loading.value) return
  loading.value = true
  errorMessage.value = ''
  try {
    await authApi.register({
      username: payload.username,
      password: payload.password,
      realName: payload.realName,
      email: payload.email,
      phone: payload.phone
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    const message = error?.message || '注册失败，请稍后重试'
    errorMessage.value = message
    ElMessage.error(message)
  } finally {
    loading.value = false
  }
}

function renderReact() {
  if (!root) return
  root.render(React.createElement(AnimatedAuthPage, {
    mode: 'register',
    loading: loading.value,
    errorMessage: errorMessage.value,
    onRegisterSubmit: handleRegister,
    onBackToLogin: () => router.push('/login')
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
