<template>
  <div class="page student-stats-page">
    <PageHeader
      title="学生学习统计"
      subtitle="查看学生练习次数、正确率、AI反馈和最近学习情况"
      eyebrow="Student Analytics"
      :icon="User"
    />

    <section class="stats-grid">
      <StatCard :icon="User" title="参与学生数" :value="overview.studentCount || 0" sub-text="有提交记录的学生" type="blue" />
      <StatCard :icon="TrendCharts" title="总提交次数" :value="overview.submitCount || 0" sub-text="当前教师题目范围" type="cyan" />
      <StatCard :icon="CircleCheck" title="平均正确率" :value="`${overview.averageAccuracyRate || 0}%`" sub-text="按总体提交计算" type="green" />
      <StatCard :icon="ChatDotRound" title="AI诊断次数" :value="overview.aiFeedbackCount || 0" sub-text="已生成反馈" type="purple" />
      <StatCard :icon="Coin" title="缓存命中次数" :value="overview.cacheHitCount || 0" sub-text="复用相似错误反馈" type="orange" />
    </section>

    <section class="surface-card filter-card">
      <el-select v-model="query.bankId" clearable placeholder="题库筛选" @change="handleBankChange">
        <el-option v-for="bank in banks" :key="bank.id" :label="bank.name" :value="bank.id" />
      </el-select>
      <el-select v-model="query.problemId" clearable placeholder="题目筛选">
        <el-option v-for="problem in filteredProblems" :key="problem.id" :label="problem.title" :value="problem.id" />
      </el-select>
      <el-input v-model="query.keyword" clearable placeholder="学生ID / 姓名 / 用户名" @keyup.enter="search" />
      <el-select v-model="query.activeStatus" clearable placeholder="学习状态">
        <el-option label="活跃" value="ACTIVE" />
        <el-option label="低活跃" value="LOW_ACTIVITY" />
        <el-option label="近期未练习" value="NO_RECENT_ACTIVITY" />
      </el-select>
      <el-input-number v-model="query.minAccuracy" :min="0" :max="100" placeholder="最低正确率" />
      <el-input-number v-model="query.maxAccuracy" :min="0" :max="100" placeholder="最高正确率" />
      <el-button type="primary" :icon="Search" :loading="loading" @click="search">查询</el-button>
    </section>

    <section class="surface-card">
      <el-table v-loading="loading" :data="records" size="large">
        <el-table-column prop="studentId" label="学生ID" width="110" />
        <el-table-column label="学生" min-width="150">
          <template #default="{ row }">
            <div class="student-cell">
              <el-avatar :size="34">{{ (row.studentName || row.username || '?').slice(0, 1) }}</el-avatar>
              <div>
                <strong>{{ row.studentName || row.username || '-' }}</strong>
                <span>{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="submitCount" label="提交次数" width="105" />
        <el-table-column prop="acceptedCount" label="通过次数" width="105" />
        <el-table-column prop="wrongCount" label="错误次数" width="105" />
        <el-table-column label="正确率" width="180">
          <template #default="{ row }">
            <div class="accuracy-cell">
              <el-progress :percentage="Number(row.accuracyRate || 0)" :stroke-width="8" :show-text="false" />
              <b>{{ row.accuracyRate || 0 }}%</b>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="aiFeedbackCount" label="AI诊断" width="105" />
        <el-table-column prop="cacheHitCount" label="缓存命中" width="105" />
        <el-table-column prop="lastSubmitTime" label="最近提交时间" width="180" />
        <el-table-column label="学习状态" width="120">
          <template #default="{ row }">
            <el-tag :type="activeTagType(row.activeStatus)" round>{{ activeText(row.activeStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="router.push(`/teacher/students/${row.studentId}`)">查看详情</el-button>
            <el-button text type="primary" @click="router.push(`/teacher/submissions?studentId=${row.studentId}`)">查看提交</el-button>
            <el-button text type="primary" @click="router.push(`/teacher/students/${row.studentId}`)">查看报告</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无学生学习数据" description="学生提交你创建的题目后，会在这里汇总展示。" :icon="User" />
        </template>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          background
          layout="total, sizes, prev, pager, next"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          @size-change="search"
          @current-change="loadStats"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ChatDotRound, CircleCheck, Coin, Search, TrendCharts, User } from '@element-plus/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { problemApi } from '../../api/problem'
import { problemBankApi } from '../../api/problemBank'
import { teacherStudentStatsApi } from '../../api/teacherStudentStats'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatCard from '../../components/StatCard.vue'

const router = useRouter()
const loading = ref(false)
const overview = ref({})
const records = ref([])
const total = ref(0)
const banks = ref([])
const problems = ref([])

const query = reactive({
  page: 1,
  pageSize: 10,
  bankId: '',
  problemId: '',
  keyword: '',
  activeStatus: '',
  minAccuracy: undefined,
  maxAccuracy: undefined
})

const filteredProblems = computed(() => {
  if (!query.bankId) return problems.value
  return problems.value.filter(item => item.bankId === query.bankId)
})

function cleanParams() {
  return Object.fromEntries(Object.entries(query).filter(([, value]) => value !== '' && value !== undefined && value !== null))
}

function activeText(status) {
  return {
    ACTIVE: '活跃',
    LOW_ACTIVITY: '低活跃',
    NO_RECENT_ACTIVITY: '近期未练习'
  }[status] || '未知'
}

function activeTagType(status) {
  return {
    ACTIVE: 'success',
    LOW_ACTIVITY: 'warning',
    NO_RECENT_ACTIVITY: 'info'
  }[status] || 'info'
}

function handleBankChange() {
  if (query.problemId && !filteredProblems.value.some(item => item.id === query.problemId)) {
    query.problemId = ''
  }
}

async function loadOptions() {
  const [bankData, problemData] = await Promise.all([
    problemBankApi.list({ pageNum: 1, pageSize: 500 }),
    problemApi.list({ pageNum: 1, pageSize: 500 })
  ])
  banks.value = bankData.records || []
  problems.value = problemData.records || []
}

async function loadOverview() {
  overview.value = await teacherStudentStatsApi.overview()
}

async function loadStats() {
  loading.value = true
  try {
    const data = await teacherStudentStatsApi.list(cleanParams())
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  loadStats()
}

onMounted(async () => {
  await Promise.all([loadOptions(), loadOverview(), loadStats()])
})
</script>

<style scoped>
.student-stats-page {
  display: grid;
  gap: 22px;
  padding: 24px;
  background: #f8fafc;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.filter-card {
  display: grid;
  grid-template-columns: 180px 220px minmax(180px, 1fr) 160px 150px 150px auto;
  gap: 12px;
  align-items: center;
}

.surface-card {
  padding: 18px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.06);
}

.student-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.student-cell strong,
.student-cell span {
  display: block;
}

.student-cell strong {
  color: #0f172a;
  font-weight: 900;
}

.student-cell span {
  margin-top: 2px;
  color: #64748b;
  font-size: 12px;
}

.accuracy-cell {
  display: grid;
  grid-template-columns: minmax(80px, 1fr) 48px;
  align-items: center;
  gap: 10px;
}

.accuracy-cell b {
  color: #0f172a;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

@media (max-width: 1280px) {
  .stats-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .filter-card {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
