<template>
  <div class="page page-stack">
    <PageHeader
      title="提交记录"
      subtitle="查看学生提交、评测状态、AI 反馈和缓存命中情况"
      eyebrow="Submissions"
      :icon="Document"
    />

    <div class="toolbar submit-toolbar">
      <el-select v-model="filterBankId" clearable placeholder="题库筛选">
        <el-option v-for="bank in banks" :key="bank.id" :label="bank.name" :value="bank.id" />
      </el-select>
      <el-select v-model="query.problemId" clearable placeholder="题目筛选">
        <el-option v-for="problem in filteredProblems" :key="problem.id" :label="problem.title" :value="problem.id" />
      </el-select>
      <el-input v-model="query.userId" clearable placeholder="学生 ID" />
      <el-select v-model="query.judgeStatus" clearable placeholder="评测状态">
        <el-option label="Accepted" value="ACCEPTED" />
        <el-option label="Wrong Answer" value="WRONG_ANSWER" />
        <el-option label="Runtime Error" value="RUNTIME_ERROR" />
        <el-option label="Compile Error" value="COMPILE_ERROR" />
        <el-option label="Time Limit" value="TIME_LIMIT_EXCEEDED" />
      </el-select>
      <el-select v-model="localFilter.ai" clearable placeholder="AI反馈">
        <el-option label="需要诊断" value="need" />
        <el-option label="无需诊断" value="none" />
      </el-select>
      <el-select v-model="localFilter.cache" clearable placeholder="缓存命中">
        <el-option label="命中" value="hit" />
        <el-option label="未命中" value="miss" />
      </el-select>
      <el-button type="primary" :icon="Search" :loading="loading" @click="loadSubmits">查询</el-button>
    </div>

    <section class="surface-card">
      <el-table v-loading="loading" :data="displayRecords" size="large">
        <el-table-column prop="username" label="学生" width="130">
          <template #default="{ row }">{{ row.username || row.userId }}</template>
        </el-table-column>
        <el-table-column label="题库" min-width="160">
          <template #default="{ row }">{{ bankName(row.problemId) }}</template>
        </el-table-column>
        <el-table-column label="题目" min-width="200">
          <template #default="{ row }">{{ row.problemTitle || problemMap[row.problemId]?.title || row.problemId }}</template>
        </el-table-column>
        <el-table-column prop="judgeStatus" label="状态" width="170">
          <template #default="{ row }"><StatusTag :status="row.judgeStatus" /></template>
        </el-table-column>
        <el-table-column label="通过用例" width="120">
          <template #default="{ row }">{{ row.passCount || 0 }} / {{ row.totalCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="运行时间" width="110">
          <template #default="{ row }">{{ row.runTime || 0 }} ms</template>
        </el-table-column>
        <el-table-column label="AI反馈" width="110">
          <template #default="{ row }">
            <el-tag :type="row.needAiFeedback ? 'warning' : 'success'" round>{{ row.needAiFeedback ? '可诊断' : '无需诊断' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="缓存命中" width="110">
          <template #default="{ row }">
            <el-tag :type="row.fromCache || row.cacheHit ? 'success' : 'info'" round>{{ row.fromCache || row.cacheHit ? '命中' : '未命中' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="190" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="View" @click="openDetail(row)">详情</el-button>
            <el-button text type="primary" :disabled="!row.needAiFeedback && row.judgeStatus === 'ACCEPTED'" @click="openFeedback(row)">AI反馈</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无提交记录" description="学生提交代码后会在这里展示评测结果" :icon="Document" />
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          background
          layout="total, prev, pager, next"
          :total="total"
          @current-change="loadSubmits"
        />
      </div>
    </section>

    <el-dialog v-model="detailVisible" title="提交详情" width="880px">
      <div v-if="detail" class="detail-box">
        <div class="detail-meta">
          <StatusTag :status="detail.judgeStatus" />
          <span>{{ detail.passCount || 0 }} / {{ detail.totalCount || 0 }} 用例通过</span>
          <span>{{ detail.runTime || 0 }} ms</span>
        </div>
        <pre class="mono-block">{{ detail.code || '无代码内容' }}</pre>
        <JudgeResultCard :result="detail" />
      </div>
    </el-dialog>

    <el-dialog v-model="feedbackVisible" title="AI反馈详情" width="920px">
      <AiFeedbackCard v-if="feedback" :feedback="feedback" />
      <EmptyState v-else title="暂无AI反馈" description="学生生成AI诊断后，教师可在这里查看" :icon="Document" />
    </el-dialog>
  </div>
</template>

<script setup>
import { Document, Search, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { aiApi } from '../../api/ai'
import { problemApi } from '../../api/problem'
import { problemBankApi } from '../../api/problemBank'
import { submitApi } from '../../api/submit'
import AiFeedbackCard from '../../components/AiFeedbackCard.vue'
import EmptyState from '../../components/EmptyState.vue'
import JudgeResultCard from '../../components/JudgeResultCard.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'

const route = useRoute()
const loading = ref(false)
const records = ref([])
const total = ref(0)
const problems = ref([])
const banks = ref([])
const filterBankId = ref('')
const detailVisible = ref(false)
const feedbackVisible = ref(false)
const detail = ref(null)
const feedback = ref(null)
const query = reactive({
  pageNum: 1,
  pageSize: 10,
  problemId: route.query.problemId ? Number(route.query.problemId) : '',
  userId: '',
  judgeStatus: ''
})
const localFilter = reactive({ ai: '', cache: '' })

const problemMap = computed(() => Object.fromEntries(problems.value.map(item => [item.id, item])))
const bankMap = computed(() => Object.fromEntries(banks.value.map(item => [item.id, item])))
const filteredProblems = computed(() => {
  if (!filterBankId.value) return problems.value
  return problems.value.filter(item => item.bankId === filterBankId.value)
})
const displayRecords = computed(() => records.value.filter(item => {
  const problem = problemMap.value[item.problemId]
  if (filterBankId.value && problem?.bankId !== filterBankId.value) return false
  if (localFilter.ai === 'need' && !item.needAiFeedback) return false
  if (localFilter.ai === 'none' && item.needAiFeedback) return false
  const cacheHit = item.fromCache || item.cacheHit
  if (localFilter.cache === 'hit' && !cacheHit) return false
  if (localFilter.cache === 'miss' && cacheHit) return false
  return true
}))

function bankName(problemId) {
  const problem = problemMap.value[problemId]
  return bankMap.value[problem?.bankId]?.name || problem?.bankName || '-'
}

async function loadOptions() {
  const [problemData, bankData] = await Promise.all([
    problemApi.list({ pageNum: 1, pageSize: 100 }),
    problemBankApi.list({ pageNum: 1, pageSize: 100 })
  ])
  problems.value = problemData.records || []
  banks.value = bankData.records || []
}

async function loadSubmits() {
  loading.value = true
  try {
    const params = { ...query }
    if (!params.problemId) delete params.problemId
    if (!params.userId) delete params.userId
    if (!params.judgeStatus) delete params.judgeStatus
    const data = await submitApi.list(params)
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  detail.value = await submitApi.detail(row.id)
  detailVisible.value = true
}

async function openFeedback(row) {
  feedback.value = null
  feedbackVisible.value = true
  try {
    feedback.value = await aiApi.detail(row.id)
  } catch (error) {
    ElMessage.warning('该提交暂未生成AI反馈')
  }
}

watch(filterBankId, () => {
  if (query.problemId && !filteredProblems.value.some(item => item.id === query.problemId)) {
    query.problemId = ''
  }
})

onMounted(() => {
  loadOptions()
  loadSubmits()
})
</script>

<style scoped>
.submit-toolbar {
  display: grid;
  grid-template-columns: 180px 220px 120px 180px 140px 140px auto;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

.detail-box {
  display: grid;
  gap: 16px;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  color: #64748b;
  font-weight: 800;
}

@media (max-width: 1100px) {
  .submit-toolbar {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
