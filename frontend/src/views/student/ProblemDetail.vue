<template>
  <div class="page practice-page" v-loading="loading">
    <div v-if="problem" class="practice-layout">
      <aside class="problem-panel">
        <div class="problem-toolbar">
          <el-button text :icon="ArrowLeft" @click="router.back()">返回</el-button>
          <span v-if="problem.bankName" class="bank-pill">{{ problem.bankName }}</span>
        </div>

        <div class="problem-title-row">
          <h1>{{ problem.title }}</h1>
          <span class="difficulty" :class="difficultyClass">{{ difficultyText }}</span>
        </div>
        <div class="meta-row">
          <span class="meta-pill">{{ questionTypeText }}</span>
          <span v-if="problem.subjectName" class="meta-pill">{{ problem.subjectName }}</span>
        </div>
        <ProblemTag :tags="problem.knowledgeTags" />

        <section class="problem-section">
          <h3>题目描述</h3>
          <p>{{ problem.description || '暂无题目描述' }}</p>
        </section>

        <section v-if="isProgramming" class="problem-section">
          <h3>输入说明</h3>
          <p>{{ problem.inputDescription || '无特殊输入说明' }}</p>
        </section>

        <section v-if="isProgramming" class="problem-section">
          <h3>输出说明</h3>
          <p>{{ problem.outputDescription || '无特殊输出说明' }}</p>
        </section>

        <section v-if="isProgramming" class="sample-grid">
          <div class="problem-section">
            <h3>样例输入</h3>
            <pre class="sample-code">{{ problem.sampleInput || sampleCase?.inputData || '无输入' }}</pre>
          </div>
          <div class="problem-section">
            <h3>样例输出</h3>
            <pre class="sample-code">{{ problem.sampleOutput || sampleCase?.expectedOutput || '无输出' }}</pre>
          </div>
        </section>
      </aside>

      <main class="coding-panel">
        <CodeEditor
          v-if="isProgramming"
          v-model="code"
          title="Python 代码编辑器"
          height="580px"
          show-submit
          show-reset
          :loading="submitting"
          submit-text="提交代码"
          loading-text="评测中..."
          @submit="handleSubmit"
          @reset="resetCode"
        />

        <section v-else-if="isChoice" class="answer-panel">
          <div class="answer-header">
            <div>
              <h2>选择题作答</h2>
              <p>请选择一个你认为正确的选项，提交后系统会立即评测。</p>
            </div>
            <el-button type="primary" size="large" :loading="submitting" :disabled="submitting || !choiceAnswer" @click="handleSubmit">
              {{ submitting ? '评测中...' : '提交答案' }}
            </el-button>
          </div>
          <el-radio-group v-model="choiceAnswer" class="choice-list">
            <label v-for="option in problem.options || []" :key="option.id || option.optionKey" class="choice-card">
              <el-radio :label="option.optionKey">
                <strong>{{ option.optionKey }}</strong>
                <span>{{ option.optionContent }}</span>
              </el-radio>
            </label>
          </el-radio-group>
        </section>

        <section v-else-if="isFillBlank" class="answer-panel">
          <div class="answer-header">
            <div>
              <h2>填空题作答</h2>
              <p>输入你的答案。系统会忽略首尾空格，并与标准答案进行匹配。</p>
            </div>
            <el-button type="primary" size="large" :loading="submitting" :disabled="submitting || !fillAnswer.trim()" @click="handleSubmit">
              {{ submitting ? '评测中...' : '提交答案' }}
            </el-button>
          </div>
          <el-input v-model="fillAnswer" type="textarea" :rows="8" placeholder="请输入答案" />
        </section>

        <section v-else-if="isShortAnswer" class="answer-panel">
          <div class="answer-header">
            <div>
              <h2>简答题作答</h2>
              <p>请输入你的理解和分析。提交后 AI 助教会根据参考答案和评分要点给出语义评分与修改建议。</p>
            </div>
            <el-button type="primary" size="large" :loading="submitting" :disabled="submitting || !shortAnswer.trim()" @click="handleSubmit">
              {{ submitting ? 'AI 批改中...' : '提交批改' }}
            </el-button>
          </div>
          <el-input v-model="shortAnswer" type="textarea" :rows="12" placeholder="请输入简答题答案，建议写出核心概念、原因说明和必要例子。" />
        </section>

        <section v-else class="answer-panel">
          <h2>当前题型暂未开放在线评测</h2>
          <p>该题型暂未接入评测流程。</p>
        </section>
      </main>
    </div>

    <JudgeResultCard v-if="judgeResult" :result="judgeResult" />

    <section v-if="showAiAction" class="ai-action-card">
      <div class="ai-action-copy">
        <div class="ai-action-kicker">AI Diagnosis</div>
        <h3>需要 AI 助教帮你定位问题吗？</h3>
        <p>系统会基于失败用例、错误信息和关键代码片段生成错因诊断、知识讲解、修改建议和学习评价。</p>
      </div>
      <el-button class="ai-button" size="large" :icon="MagicStick" :loading="aiLoading" :disabled="aiLoading" @click="handleAi">
        {{ aiLoading ? '生成中...' : '生成 AI 诊断' }}
      </el-button>
    </section>

    <AiFeedbackCard v-if="feedback" :feedback="feedback" />
  </div>
</template>

<script setup>
import { ArrowLeft, MagicStick } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { aiApi } from '../../api/ai'
import { problemApi } from '../../api/problem'
import { submitApi } from '../../api/submit'
import { testCaseApi } from '../../api/testcase'
import AiFeedbackCard from '../../components/AiFeedbackCard.vue'
import CodeEditor from '../../components/CodeEditor.vue'
import JudgeResultCard from '../../components/JudgeResultCard.vue'
import ProblemTag from '../../components/ProblemTag.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const aiLoading = ref(false)
const problem = ref(null)
const samples = ref([])
const judgeResult = ref(null)
const feedback = ref(null)
const defaultCode = 'a, b = map(int, input().split())\nprint(a + b)'
const code = ref(defaultCode)
const choiceAnswer = ref('')
const fillAnswer = ref('')
const shortAnswer = ref('')

const sampleCase = computed(() => samples.value[0])
const showAiAction = computed(() => judgeResult.value && judgeResult.value.judgeStatus !== 'ACCEPTED' && !feedback.value)
const questionType = computed(() => problem.value?.questionType || 'PROGRAMMING')
const isProgramming = computed(() => questionType.value === 'PROGRAMMING')
const isChoice = computed(() => questionType.value === 'CHOICE')
const isFillBlank = computed(() => questionType.value === 'FILL_BLANK')
const isShortAnswer = computed(() => questionType.value === 'SHORT_ANSWER')
const questionTypeText = computed(() => ({
  PROGRAMMING: '编程题',
  CHOICE: '选择题',
  FILL_BLANK: '填空题',
  SHORT_ANSWER: '简答题'
})[questionType.value] || questionType.value)

const difficultyMap = {
  EASY: { text: '简单', className: 'difficulty-easy' },
  MEDIUM: { text: '中等', className: 'difficulty-medium' },
  HARD: { text: '困难', className: 'difficulty-hard' }
}
const difficultyInfo = computed(() => difficultyMap[problem.value?.difficulty] || { text: problem.value?.difficulty || '未分级', className: 'difficulty-mixed' })
const difficultyText = computed(() => difficultyInfo.value.text)
const difficultyClass = computed(() => difficultyInfo.value.className)

async function loadProblem() {
  loading.value = true
  try {
    const id = route.params.id
    const [detail, sampleData] = await Promise.all([
      problemApi.detail(id),
      testCaseApi.sample(id).catch(() => [])
    ])
    problem.value = detail
    samples.value = Array.isArray(sampleData) ? sampleData : (sampleData.records || [])
  } finally {
    loading.value = false
  }
}

function resetCode() {
  code.value = defaultCode
}

async function handleSubmit() {
  if (submitting.value) return
  submitting.value = true
  feedback.value = null
  try {
    if (isProgramming.value) {
      judgeResult.value = await submitApi.submitCode({
        problemId: Number(route.params.id),
        language: 'python',
        code: code.value
      })
    } else {
      judgeResult.value = await submitApi.submitAnswer({
        problemId: Number(route.params.id),
        questionType: questionType.value,
        answerContent: getAnswerContent()
      })
      if (isShortAnswer.value && judgeResult.value?.aiFeedback) {
        feedback.value = judgeResult.value.aiFeedback
      }
    }
    ElMessage.success('评测完成')
  } finally {
    submitting.value = false
  }
}

function getAnswerContent() {
  if (isChoice.value) return choiceAnswer.value
  if (isFillBlank.value) return fillAnswer.value
  if (isShortAnswer.value) return shortAnswer.value
  return ''
}

async function handleAi() {
  if (!judgeResult.value?.submitId || aiLoading.value) return
  aiLoading.value = true
  try {
    feedback.value = await aiApi.feedback(judgeResult.value.submitId)
    ElMessage.success((feedback.value.fromCache === true || feedback.value.fromCache === 1) ? '已复用相似错误反馈' : 'AI 诊断已生成')
  } finally {
    aiLoading.value = false
  }
}

onMounted(loadProblem)
</script>

<style scoped>
.practice-page {
  display: grid;
  gap: 22px;
}

.practice-layout {
  display: grid;
  grid-template-columns: minmax(360px, 0.42fr) minmax(0, 0.58fr);
  gap: 22px;
  align-items: start;
}

.problem-panel {
  position: sticky;
  top: 100px;
  max-height: calc(100vh - 124px);
  padding: 24px;
  overflow: auto;
  border: 1px solid rgba(148, 163, 184, 0.20);
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 16px 42px rgba(15, 23, 42, 0.07);
}

.problem-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.bank-pill {
  max-width: 220px;
  padding: 7px 12px;
  overflow: hidden;
  border-radius: 999px;
  color: #1d4ed8;
  background: #dbeafe;
  font-size: 12px;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.problem-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin: 14px 0 12px;
}

.problem-title-row h1 {
  margin: 0;
  color: #111827;
  font-size: 28px;
  font-weight: 900;
  line-height: 1.25;
}

.meta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: -4px 0 12px;
}

.meta-pill {
  padding: 6px 10px;
  border-radius: 999px;
  color: #2563eb;
  background: #eff6ff;
  font-size: 12px;
  font-weight: 900;
}

.difficulty {
  flex: 0 0 auto;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}

.problem-section {
  margin-top: 24px;
}

.problem-section h3 {
  margin: 0 0 10px;
  color: #111827;
  font-size: 17px;
  font-weight: 900;
}

.problem-section p {
  margin: 0;
  color: #4b5563;
  line-height: 1.9;
  white-space: pre-wrap;
}

.sample-grid {
  display: grid;
  gap: 14px;
}

.sample-code {
  min-height: 70px;
  margin: 0;
  padding: 14px 16px;
  overflow: auto;
  border: 1px solid rgba(148, 163, 184, 0.20);
  border-radius: 14px;
  color: #dbeafe;
  background: linear-gradient(135deg, #0f172a, #172554);
  font-family: Consolas, "JetBrains Mono", monospace;
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
}

.coding-panel {
  min-width: 0;
}

.answer-panel {
  min-height: 420px;
  padding: 24px;
  border: 1px solid rgba(148, 163, 184, 0.20);
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 16px 42px rgba(15, 23, 42, 0.07);
}

.answer-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 22px;
}

.answer-header h2 {
  margin: 0;
  color: #111827;
  font-size: 22px;
  font-weight: 900;
}

.answer-header p,
.answer-panel > p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.7;
}

.choice-list {
  display: grid;
  gap: 14px;
}

.choice-card {
  display: block;
  padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.24);
  border-radius: 16px;
  background: #f8fafc;
  cursor: pointer;
  transition: all .18s ease;
}

.choice-card:hover {
  border-color: rgba(37, 99, 235, 0.38);
  transform: translateY(-1px);
}

.choice-card :deep(.el-radio) {
  width: 100%;
  height: auto;
  align-items: flex-start;
  white-space: normal;
}

.choice-card strong {
  display: inline-flex;
  width: 30px;
  height: 30px;
  align-items: center;
  justify-content: center;
  margin-right: 10px;
  border-radius: 10px;
  color: #2563eb;
  background: #dbeafe;
}

.choice-card span {
  color: #334155;
  line-height: 1.7;
}

.ai-action-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 22px;
  border: 1px solid rgba(124, 58, 237, 0.18);
  border-radius: 22px;
  background:
    radial-gradient(circle at 94% 0%, rgba(124, 58, 237, 0.12), transparent 30%),
    linear-gradient(135deg, #fff, #eff6ff);
  box-shadow: 0 18px 42px rgba(124, 58, 237, 0.10);
}

.ai-action-copy {
  min-width: 0;
}

.ai-action-kicker {
  color: #7c3aed;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.ai-action-card h3 {
  margin: 5px 0 0;
  color: #111827;
  font-size: 20px;
  font-weight: 900;
}

.ai-action-card p {
  margin: 8px 0 0;
  color: #64748b;
  line-height: 1.7;
}

@media (max-width: 1100px) {
  .practice-layout {
    grid-template-columns: 1fr;
  }

  .problem-panel {
    position: static;
    max-height: none;
  }

  .ai-action-card {
    align-items: flex-start;
    flex-direction: column;
  }

  .answer-header {
    flex-direction: column;
  }
}
</style>
