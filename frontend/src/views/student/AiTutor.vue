<template>
  <div class="student-page">
    <PageHeader
      title="AI 助教"
      subtitle="和虚拟学习助教交流学习方法、错题复盘与知识点理解问题。"
      eyebrow="AI Tutor"
      :icon="ChatDotRound"
    />

    <section class="tutor-layout">
      <div class="avatar-card">
        <Live2dAvatar :status="avatarStatus" :message="avatarMessage" />
      </div>

      <div class="chat-card">
        <div class="chat-head">
          <div>
            <h3>学习问答</h3>
            <p>这里会调用后端 `/agent/student/ask` 接口，由后端统一接入 DeepSeek。</p>
          </div>
          <el-tag round :type="statusTagType">{{ statusText }}</el-tag>
        </div>

        <div ref="chatBodyRef" class="chat-body">
          <div v-for="item in chatMessages" :key="item.id" class="message" :class="item.role">
            <strong>{{ item.role === 'user' ? '我' : 'AI 助教' }}</strong>
            <p>{{ item.content }}</p>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="question"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="输入你的学习问题，例如：为什么我的循环题总是超时？"
            @keydown.ctrl.enter.prevent="sendQuestion"
          />
          <el-button type="primary" :loading="asking" :disabled="asking || !question.trim()" @click="sendQuestion">
            {{ asking ? '思考中...' : '发送问题' }}
          </el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, nextTick, ref } from 'vue'
import { agentApi } from '../../api/agent'
import Live2dAvatar from '../../components/ai-tutor/Live2dAvatar.vue'
import PageHeader from '../../components/PageHeader.vue'

const avatarStatus = ref('idle')
const question = ref('')
const asking = ref(false)
const chatBodyRef = ref(null)
const chatMessages = ref([
  {
    id: Date.now(),
    role: 'assistant',
    content: '你好，我是你的虚拟学习助教。可以问我学习方法、错题复盘、知识点理解或编程调试思路。'
  }
])

const avatarMessage = computed(() => {
  const map = {
    idle: '准备好一起学习了。',
    thinking: '我正在分析你的问题...',
    speaking: '这是我的学习建议。',
    error: '刚才出了一点小问题。'
  }
  return map[avatarStatus.value] || map.idle
})

const statusText = computed(() => {
  const map = {
    idle: '待命',
    thinking: '思考中',
    speaking: '回答中',
    error: '异常'
  }
  return map[avatarStatus.value] || '待命'
})

const statusTagType = computed(() => {
  if (avatarStatus.value === 'error') return 'danger'
  if (avatarStatus.value === 'thinking') return 'warning'
  if (avatarStatus.value === 'speaking') return 'success'
  return 'info'
})

async function sendQuestion() {
  const content = question.value.trim()
  if (!content || asking.value) return

  chatMessages.value.push({ id: Date.now(), role: 'user', content })
  question.value = ''
  asking.value = true
  avatarStatus.value = 'thinking'
  await scrollChat()

  try {
    const data = await agentApi.ask({ question: content })
    const answer = data?.answer || '我已经收到你的问题，但暂时没有生成有效回复。'
    chatMessages.value.push({ id: Date.now() + 1, role: 'assistant', content: answer })
    avatarStatus.value = 'speaking'
    await scrollChat()
    window.setTimeout(() => {
      if (!asking.value) avatarStatus.value = 'idle'
    }, 1500)
  } catch (error) {
    avatarStatus.value = 'error'
    ElMessage.error('AI 问答暂时不可用，请稍后重试')
    window.setTimeout(() => {
      if (!asking.value) avatarStatus.value = 'idle'
    }, 3000)
  } finally {
    asking.value = false
  }
}

async function scrollChat() {
  await nextTick()
  if (chatBodyRef.value) {
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  }
}
</script>

<style scoped>
.student-page {
  display: grid;
  gap: 24px;
  padding: 24px;
}

.tutor-layout {
  display: grid;
  grid-template-columns: 360px minmax(0, 1fr);
  gap: 24px;
  align-items: stretch;
}

.avatar-card,
.chat-card {
  border: 1px solid #e5eaf2;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.avatar-card {
  display: grid;
  justify-content: center;
  padding: 18px 10px;
  overflow: hidden;
}

.chat-card {
  display: grid;
  grid-template-rows: auto minmax(360px, 1fr) auto;
  gap: 16px;
  padding: 24px;
}

.chat-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.chat-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 900;
}

.chat-head p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.chat-body {
  display: grid;
  align-content: start;
  gap: 12px;
  min-height: 360px;
  padding: 16px;
  overflow-y: auto;
  border: 1px solid #edf2f7;
  border-radius: 18px;
  background: #f8fafc;
}

.message {
  max-width: 82%;
  padding: 13px 15px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.04);
}

.message.user {
  justify-self: end;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
}

.message strong {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  opacity: 0.78;
}

.message p {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.chat-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 132px;
  align-items: end;
  gap: 12px;
}

.chat-input :deep(.el-button) {
  height: 72px;
  border: 0;
  border-radius: 16px;
  font-weight: 900;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
}

@media (max-width: 1180px) {
  .tutor-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .chat-input {
    grid-template-columns: 1fr;
  }

  .chat-input :deep(.el-button) {
    height: 44px;
  }
}
</style>
