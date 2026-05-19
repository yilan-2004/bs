<template>
  <div class="airi-page">
    <div class="airi-wave" aria-hidden="true"></div>
    <header class="airi-topbar">
      <div class="brand">
        <span class="brand-dot"></span>
        <strong>AIRI</strong>
        <small>AgentEdu 学习助教</small>
      </div>
      <div class="top-actions">
        <span class="status-chip" :class="avatarStatus">{{ statusText }}</span>
        <button class="icon-button" type="button" title="模型说明">i</button>
      </div>
    </header>

    <main class="airi-stage">
      <section class="avatar-zone">
        <AiriStageLive2D :config="airiConfig" :status="avatarStatus" />
      </section>

      <section class="dialog-panel">
        <div class="dialog-head">
          <div>
            <span>学习问答</span>
            <h1>和 AIRI 一起拆解问题</h1>
          </div>
          <span class="model-pill">DeepSeek 后端托管</span>
        </div>

        <div ref="chatBodyRef" class="dialog-body">
          <article
            v-for="item in chatMessages"
            :key="item.id"
            class="chat-bubble"
            :class="item.role"
          >
            <span class="speaker">{{ item.role === 'user' ? '你' : 'AIRI' }}</span>
            <p>{{ item.content }}</p>
          </article>
        </div>

        <form class="dialog-input" @submit.prevent="sendQuestion">
          <textarea
            v-model="question"
            maxlength="500"
            :disabled="asking"
            placeholder="说点什么，例如：为什么我的循环题总是超时？"
            @keydown.ctrl.enter.prevent="sendQuestion"
          ></textarea>
          <div class="input-tools">
            <span>{{ question.length }} / 500</span>
            <button type="submit" :disabled="asking || !question.trim()">
              {{ asking ? '思考中...' : '发送问题' }}
            </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { computed, nextTick, ref } from 'vue'
import { agentApi } from '../../api/agent'
import AiriStageLive2D from '../../components/ai-tutor/AiriStageLive2D.vue'
import airiConfig from '../../assets/airi/airi-stage.config.json'

const avatarStatus = ref('idle')
const question = ref('')
const asking = ref(false)
const chatBodyRef = ref(null)
const chatMessages = ref([
  {
    id: Date.now(),
    role: 'assistant',
    content: '你好，我是 AIRI。可以问我编程学习、错题复盘、知识点理解，也可以让我帮你规划下一步练习。'
  }
])

const statusText = computed(() => {
  const map = {
    idle: '在线',
    thinking: '思考中',
    speaking: '回答中',
    error: '异常'
  }
  return map[avatarStatus.value] || '在线'
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
    const answer = data?.answer || data?.content || '我已经收到你的问题，但这次没有生成有效回答。'
    chatMessages.value.push({ id: Date.now() + 1, role: 'assistant', content: answer })
    avatarStatus.value = 'speaking'
    await scrollChat()
    window.setTimeout(() => {
      if (!asking.value && avatarStatus.value === 'speaking') {
        avatarStatus.value = 'idle'
      }
    }, 1600)
  } catch (error) {
    avatarStatus.value = 'error'
    ElMessage.error('AI 问答暂时不可用，请稍后重试')
    window.setTimeout(() => {
      if (!asking.value && avatarStatus.value === 'error') {
        avatarStatus.value = 'idle'
      }
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
.airi-page {
  position: relative;
  min-height: calc(100vh - 0px);
  overflow: hidden;
  color: #e6fbff;
  background:
    radial-gradient(circle at 76% 8%, rgba(45, 212, 191, 0.14), transparent 24%),
    linear-gradient(180deg, #064758 0 68px, transparent 68px),
    #101415;
}

.airi-page::before {
  position: absolute;
  inset: 76px 0 0;
  content: "";
  opacity: 0.58;
  background-image:
    linear-gradient(rgba(0, 154, 190, 0.26) 2px, transparent 2px),
    linear-gradient(90deg, rgba(0, 154, 190, 0.26) 2px, transparent 2px);
  background-position: 0 0;
  background-size: 52px 52px;
  mask-image: linear-gradient(90deg, rgba(0, 0, 0, 0.95), rgba(0, 0, 0, 0.35));
  pointer-events: none;
}

.airi-wave {
  position: absolute;
  top: 54px;
  left: -4vw;
  width: 108vw;
  height: 68px;
  z-index: 0;
  background: #101415;
  border-radius: 50% 50% 0 0 / 100% 100% 0 0;
  transform: rotate(-1.2deg);
  pointer-events: none;
}

.airi-topbar {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 68px;
  padding: 0 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-dot {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: #7dd3fc;
  box-shadow: 0 0 28px rgba(125, 211, 252, 0.95);
}

.brand strong {
  font-size: 28px;
  letter-spacing: 0;
}

.brand small {
  color: rgba(224, 251, 255, 0.62);
  font-weight: 700;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-chip,
.icon-button,
.model-pill {
  border: 1px solid rgba(111, 229, 255, 0.18);
  color: #d8fbff;
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 10px 26px rgba(0, 0, 0, 0.22);
  backdrop-filter: blur(10px);
}

.status-chip {
  min-width: 92px;
  padding: 10px 16px;
  border-radius: 999px;
  text-align: center;
  font-weight: 900;
}

.status-chip.thinking {
  color: #fde68a;
}

.status-chip.speaking {
  color: #86efac;
}

.status-chip.error {
  color: #fecaca;
}

.icon-button {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  font-size: 18px;
  font-weight: 900;
  cursor: pointer;
}

.airi-stage {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(520px, 1fr) minmax(460px, 38vw);
  min-height: calc(100vh - 68px);
  padding: 26px 28px 28px;
  gap: 24px;
}

.avatar-zone {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  min-width: 0;
}

.dialog-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  min-height: min(820px, calc(100vh - 122px));
  overflow: hidden;
  border: 4px solid rgba(7, 90, 107, 0.92);
  border-radius: 20px;
  background: rgba(4, 44, 55, 0.94);
  box-shadow: 0 28px 90px rgba(0, 0, 0, 0.38);
}

.dialog-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 26px 18px;
}

.dialog-head span {
  color: rgba(181, 240, 250, 0.68);
  font-size: 13px;
  font-weight: 900;
  text-transform: uppercase;
  letter-spacing: 0;
}

.dialog-head h1 {
  margin: 8px 0 0;
  color: #e9fdff;
  font-size: 26px;
  letter-spacing: 0;
}

.model-pill {
  align-self: flex-start;
  padding: 8px 12px;
  border-radius: 999px;
  color: #9ee7f4;
  font-size: 12px;
  font-weight: 900;
}

.dialog-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
  padding: 12px 22px 22px;
  overflow-y: auto;
}

.chat-bubble {
  max-width: 86%;
  padding: 16px 18px;
  border-radius: 18px;
  color: #ddfbff;
  background: rgba(3, 76, 94, 0.9);
  box-shadow: 0 18px 50px rgba(0, 0, 0, 0.16);
}

.chat-bubble.user {
  align-self: flex-end;
  max-width: 74%;
  color: #f8fafc;
  background: rgba(44, 47, 52, 0.92);
}

.speaker {
  display: block;
  margin-bottom: 8px;
  color: rgba(218, 251, 255, 0.66);
  font-size: 13px;
  font-weight: 900;
}

.chat-bubble p {
  margin: 0;
  font-size: 17px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.dialog-input {
  display: grid;
  gap: 12px;
  padding: 18px 22px 22px;
  border-top: 2px solid rgba(111, 229, 255, 0.14);
  background: rgba(6, 72, 86, 0.78);
}

.dialog-input textarea {
  width: 100%;
  min-height: 88px;
  resize: none;
  border: 0;
  outline: none;
  color: #e9fdff;
  background: transparent;
  font-size: 18px;
  line-height: 1.6;
}

.dialog-input textarea::placeholder {
  color: rgba(203, 250, 255, 0.68);
}

.input-tools {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.input-tools span {
  color: rgba(203, 250, 255, 0.54);
  font-size: 13px;
  font-weight: 800;
}

.input-tools button {
  min-width: 132px;
  height: 46px;
  border: 0;
  border-radius: 14px;
  color: #06262e;
  background: linear-gradient(135deg, #8ff3ff, #22d3ee);
  box-shadow: 0 14px 34px rgba(34, 211, 238, 0.18);
  font-weight: 950;
  cursor: pointer;
}

.input-tools button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

@media (max-width: 1180px) {
  .airi-stage {
    grid-template-columns: 1fr;
  }

  .dialog-panel {
    min-height: 620px;
  }
}

@media (max-width: 760px) {
  .airi-topbar {
    padding: 0 16px;
  }

  .brand small,
  .model-pill {
    display: none;
  }

  .airi-stage {
    padding: 16px;
  }

  .dialog-head h1 {
    font-size: 22px;
  }

  .chat-bubble {
    max-width: 96%;
  }
}
</style>
