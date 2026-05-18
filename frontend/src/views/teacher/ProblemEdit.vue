<template>
  <div class="page page-stack" v-loading="loading">
    <PageHeader
      :title="isEdit ? '编辑题目' : '新增题目'"
      subtitle="完善题面、样例和知识点，让学生能清楚理解输入输出要求"
      eyebrow="Problem Editor"
      :icon="EditPen"
    />

    <section class="editor-layout">
      <el-form class="surface-card problem-form" label-position="top" :model="form">
        <div class="form-section-title">基本信息</div>
        <div class="form-grid">
          <el-form-item label="题目标题" required>
            <el-input v-model="form.title" placeholder="例如：两数之和" />
          </el-form-item>
          <el-form-item label="所属题库">
            <el-select v-model="form.bankId" clearable placeholder="选择题库" style="width: 100%">
              <el-option v-for="bank in banks" :key="bank.id" :label="bank.name" :value="bank.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="所属学科">
            <el-select v-model="form.subjectId" clearable placeholder="选择学科" style="width: 100%">
              <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="题型">
            <el-select v-model="form.questionType" style="width: 100%">
              <el-option label="编程题" value="PROGRAMMING" />
              <el-option label="选择题" value="CHOICE" />
              <el-option label="填空题" value="FILL_BLANK" />
              <el-option label="简答题" value="SHORT_ANSWER" />
            </el-select>
          </el-form-item>
          <el-form-item label="难度">
            <el-select v-model="form.difficulty" style="width: 100%">
              <el-option label="简单" value="EASY" />
              <el-option label="中等" value="MEDIUM" />
              <el-option label="困难" value="HARD" />
            </el-select>
          </el-form-item>
          <el-form-item label="知识点">
            <el-input v-model="form.knowledgeTags" placeholder="多个知识点用逗号分隔，如 数组,循环" />
          </el-form-item>
        </div>

        <div class="form-section-title">题目内容</div>
        <el-form-item label="题目描述" required>
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="描述题目背景、输入条件和需要完成的任务" />
        </el-form-item>

        <div class="form-section-title">输入输出</div>
        <div class="form-grid">
          <el-form-item label="输入说明">
            <el-input v-model="form.inputDescription" type="textarea" :rows="4" placeholder="说明输入格式、范围和多行输入规则" />
          </el-form-item>
          <el-form-item label="输出说明">
            <el-input v-model="form.outputDescription" type="textarea" :rows="4" placeholder="说明输出格式和精度要求" />
          </el-form-item>
        </div>

        <div class="form-section-title">样例</div>
        <div class="form-grid">
          <el-form-item label="样例输入">
            <el-input v-model="form.sampleInput" type="textarea" :rows="5" />
          </el-form-item>
          <el-form-item label="样例输出">
            <el-input v-model="form.sampleOutput" type="textarea" :rows="5" />
          </el-form-item>
        </div>

        <div v-if="form.questionType === 'CHOICE'" class="form-section-title">选择题选项</div>
        <div v-if="form.questionType === 'CHOICE'" class="choice-options">
          <div v-for="(option, index) in form.options" :key="index" class="choice-option-row">
            <el-input v-model="option.optionKey" class="option-key-input" placeholder="A" />
            <el-input v-model="option.optionContent" placeholder="选项内容" />
            <el-radio v-model="correctOptionIndex" :label="index">正确</el-radio>
            <el-button text type="danger" :disabled="form.options.length <= 2" @click="removeOption(index)">删除</el-button>
          </div>
          <el-button plain type="primary" @click="addOption">添加选项</el-button>
          <p class="form-tip">第一版选择题仅支持单选，请设置且只设置一个正确选项。</p>
        </div>

        <div v-if="form.questionType === 'FILL_BLANK'" class="form-section-title">填空题答案</div>
        <el-form-item v-if="form.questionType === 'FILL_BLANK'" label="标准答案">
          <el-input
            v-model="form.standardAnswer"
            type="textarea"
            :rows="5"
            placeholder="支持多个答案，用英文分号 ; 或换行分隔。评测时会忽略首尾空格。"
          />
        </el-form-item>

        <div v-if="form.questionType === 'SHORT_ANSWER'" class="form-section-title">简答题 AI 批改</div>
        <template v-if="form.questionType === 'SHORT_ANSWER'">
          <div class="form-grid">
            <el-form-item label="参考答案" required>
              <el-input
                v-model="form.standardAnswer"
                type="textarea"
                :rows="6"
                placeholder="填写参考答案，AI 会结合学生答案进行语义评分"
              />
            </el-form-item>
            <el-form-item label="评分要点">
              <el-input
                v-model="form.scoringPoints"
                type="textarea"
                :rows="6"
                placeholder="可选：关键概念、条件说明、例子、表达完整性等"
              />
            </el-form-item>
          </div>
          <el-form-item label="满分">
            <el-input-number v-model="form.score" :min="1" :max="1000" :step="5" />
          </el-form-item>
        </template>

        <div class="form-actions">
          <el-button @click="router.push('/teacher/problems')">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveProblem">保存题目</el-button>
        </div>
      </el-form>

      <aside class="surface-card guide-card">
        <div class="guide-icon"><el-icon><EditPen /></el-icon></div>
        <h3>编辑建议</h3>
        <p>清晰的题面会显著减少学生在格式理解上的错误，把注意力留给真正的编程思维。</p>
        <ul>
          <li>标题保持短而明确，避免太口语化。</li>
          <li>知识点建议使用 2-4 个标签。</li>
          <li>样例输入输出应与测试用例格式一致。</li>
          <li>保存后继续配置样例和隐藏测试用例。</li>
        </ul>
      </aside>
    </section>
  </div>
</template>

<script setup>
import { EditPen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { problemApi } from '../../api/problem'
import { problemBankApi } from '../../api/problemBank'
import { subjectApi } from '../../api/subject'
import PageHeader from '../../components/PageHeader.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const banks = ref([])
const subjects = ref([])
const isEdit = computed(() => Boolean(route.params.id))
const form = reactive({
  id: null,
  title: '',
  bankId: '',
  subjectId: '',
  questionType: 'PROGRAMMING',
  description: '',
  inputDescription: '',
  outputDescription: '',
  sampleInput: '',
  sampleOutput: '',
  difficulty: 'EASY',
  knowledgeTags: '',
  standardAnswer: '',
  scoringPoints: '',
  score: 100,
  options: [
    { optionKey: 'A', optionContent: '', isCorrect: 1, sortOrder: 0 },
    { optionKey: 'B', optionContent: '', isCorrect: 0, sortOrder: 1 }
  ]
})
const correctOptionIndex = ref(0)

async function loadData() {
  loading.value = true
  try {
    const bankData = await problemBankApi.list({ pageNum: 1, pageSize: 100, status: 1 })
    const subjectData = await subjectApi.list({ pageNum: 1, pageSize: 100, status: 1 })
    banks.value = bankData.records || []
    subjects.value = subjectData.records || []
    if (isEdit.value) {
      const detail = await problemApi.detail(route.params.id)
      Object.assign(form, {
        id: detail.id,
        title: detail.title || '',
        bankId: detail.bankId || '',
        subjectId: detail.subjectId || '',
        questionType: detail.questionType || 'PROGRAMMING',
        description: detail.description || '',
        inputDescription: detail.inputDescription || '',
        outputDescription: detail.outputDescription || '',
        sampleInput: detail.sampleInput || '',
        sampleOutput: detail.sampleOutput || '',
        difficulty: detail.difficulty || 'EASY',
        knowledgeTags: detail.knowledgeTags || '',
        standardAnswer: detail.standardAnswer || '',
        scoringPoints: detail.scoringPoints || '',
        score: detail.score || 100,
        options: normalizeOptions(detail.options)
      })
      correctOptionIndex.value = Math.max(0, form.options.findIndex(option => option.isCorrect === 1))
    }
  } finally {
    loading.value = false
  }
}

async function saveProblem() {
  if (!form.title?.trim() || !form.description?.trim()) {
    ElMessage.warning('请填写题目标题和题目描述')
    return
  }
  saving.value = true
  try {
    const normalizedOptions = form.questionType === 'CHOICE'
      ? form.options.map((option, index) => ({
          ...option,
          optionKey: option.optionKey?.trim(),
          optionContent: option.optionContent?.trim(),
          isCorrect: index === correctOptionIndex.value ? 1 : 0,
          sortOrder: index
        }))
      : []
    const payload = {
      ...form,
      bankId: form.bankId || null,
      subjectId: form.subjectId || null,
      questionType: form.questionType || 'PROGRAMMING',
      standardAnswer: ['FILL_BLANK', 'SHORT_ANSWER'].includes(form.questionType) ? form.standardAnswer : null,
      scoringPoints: form.questionType === 'SHORT_ANSWER' ? form.scoringPoints : null,
      score: form.questionType === 'SHORT_ANSWER' ? form.score : null,
      options: normalizedOptions
    }
    if (isEdit.value) {
      await problemApi.update(payload)
    } else {
      delete payload.id
      await problemApi.add(payload)
    }
    ElMessage.success('保存成功')
    router.push('/teacher/problems')
  } finally {
    saving.value = false
  }
}

function normalizeOptions(options = []) {
  if (!Array.isArray(options) || options.length < 2) {
    return [
      { optionKey: 'A', optionContent: '', isCorrect: 1, sortOrder: 0 },
      { optionKey: 'B', optionContent: '', isCorrect: 0, sortOrder: 1 }
    ]
  }
  return options.map((option, index) => ({
    optionKey: option.optionKey || String.fromCharCode(65 + index),
    optionContent: option.optionContent || '',
    isCorrect: option.isCorrect === 1 ? 1 : 0,
    sortOrder: option.sortOrder ?? index
  }))
}

function addOption() {
  const index = form.options.length
  form.options.push({
    optionKey: String.fromCharCode(65 + index),
    optionContent: '',
    isCorrect: 0,
    sortOrder: index
  })
}

function removeOption(index) {
  form.options.splice(index, 1)
  form.options.forEach((option, i) => {
    option.sortOrder = i
    option.optionKey = option.optionKey || String.fromCharCode(65 + i)
  })
  if (correctOptionIndex.value >= form.options.length) {
    correctOptionIndex.value = 0
  }
}

watch(() => form.questionType, (type) => {
  if (type === 'CHOICE' && (!form.options || form.options.length < 2)) {
    form.options = normalizeOptions()
  }
})

onMounted(loadData)
</script>

<style scoped>
.editor-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 22px;
  align-items: start;
}

.problem-form {
  display: grid;
  gap: 8px;
}

.form-section-title {
  margin: 4px 0 10px;
  color: #0f172a;
  font-size: 17px;
  font-weight: 900;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 10px;
}

.choice-options {
  display: grid;
  gap: 12px;
}

.choice-option-row {
  display: grid;
  grid-template-columns: 80px minmax(0, 1fr) 90px 70px;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 14px;
  background: #f8fafc;
}

.option-key-input {
  width: 80px;
}

.form-tip {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.guide-card {
  position: sticky;
  top: 100px;
  overflow: hidden;
}

.guide-icon {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  color: #2563eb;
  background: linear-gradient(135deg, #dbeafe, #ede9fe);
  font-size: 28px;
}

.guide-card h3 {
  margin: 16px 0 12px;
  font-size: 20px;
  font-weight: 900;
}

.guide-card p,
.guide-card li {
  color: #64748b;
  line-height: 1.8;
}

.guide-card ul {
  padding-left: 18px;
}

@media (max-width: 1000px) {
  .editor-layout,
  .form-grid,
  .choice-option-row {
    grid-template-columns: 1fr;
  }

  .guide-card {
    position: static;
  }
}
</style>
