<template>
  <div class="page page-stack">
    <PageHeader
      title="全部题目"
      subtitle="筛选适合自己的编程题，进入详情页完成提交和 AI 诊断"
      eyebrow="Practice"
      :icon="Reading"
    />

    <div class="toolbar">
      <el-input v-model="query.title" clearable placeholder="搜索题目标题" :prefix-icon="Search" style="width: 260px" @keyup.enter="loadProblems" />
      <el-select v-model="query.bankId" clearable placeholder="所属题库" style="width: 220px">
        <el-option v-for="bank in banks" :key="bank.id" :label="bank.name" :value="bank.id" />
      </el-select>
      <el-select v-model="query.subjectId" clearable placeholder="学科" style="width: 140px">
        <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
      </el-select>
      <el-select v-model="query.questionType" clearable placeholder="题型" style="width: 150px">
        <el-option label="编程题" value="PROGRAMMING" />
        <el-option label="选择题" value="CHOICE" />
        <el-option label="填空题" value="FILL_BLANK" />
        <el-option label="简答题" value="SHORT_ANSWER" />
      </el-select>
      <el-select v-model="query.difficulty" clearable placeholder="难度" style="width: 150px">
        <el-option label="简单" value="EASY" />
        <el-option label="中等" value="MEDIUM" />
        <el-option label="困难" value="HARD" />
      </el-select>
      <el-input v-model="query.knowledgeTags" clearable placeholder="知识点" style="width: 180px" @keyup.enter="loadProblems" />
      <el-button type="primary" :icon="Search" :loading="loading" @click="loadProblems">筛选</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />
    <div v-else-if="problems.length" class="card-grid">
      <ProblemCard v-for="problem in problems" :key="problem.id" :problem="problem" @start="openProblem" />
    </div>
    <EmptyState v-else title="没有匹配的题目" description="调整关键词、难度或知识点后重新筛选" :icon="Reading" />

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        background
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadProblems"
      />
    </div>
  </div>
</template>

<script setup>
import { Reading, Search } from '@element-plus/icons-vue'
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { problemApi } from '../../api/problem'
import { problemBankApi } from '../../api/problemBank'
import { subjectApi } from '../../api/subject'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import ProblemCard from '../../components/ProblemCard.vue'

const router = useRouter()
const loading = ref(false)
const problems = ref([])
const banks = ref([])
const subjects = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 9,
  title: '',
  difficulty: '',
  knowledgeTags: '',
  bankId: '',
  subjectId: '',
  questionType: '',
  status: 1
})

async function loadProblems() {
  loading.value = true
  try {
    const data = await problemApi.list(query)
    problems.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function loadBanks() {
  const data = await problemBankApi.list({ pageNum: 1, pageSize: 100, status: 1 })
  banks.value = data.records || []
}

async function loadSubjects() {
  const data = await subjectApi.list({ pageNum: 1, pageSize: 100, status: 1 })
  subjects.value = data.records || []
}

function openProblem(problem) {
  router.push(`/student/problem/${problem.id}`)
}

watch(() => [query.bankId, query.subjectId, query.questionType, query.difficulty], () => {
  query.pageNum = 1
  loadProblems()
})

onMounted(() => {
  loadBanks()
  loadSubjects()
  loadProblems()
})
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: center;
}
</style>
