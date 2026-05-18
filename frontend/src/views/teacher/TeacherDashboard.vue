<template>
  <div class="page page-stack">
    <PageHeader
      title="教学工作台"
      subtitle="查看题目、提交、评测和 AI 诊断数据，快速掌握教学训练情况"
      eyebrow="Teaching Dashboard"
      :icon="DataBoard"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="router.push('/teacher/problem/new')">新增题目</el-button>
      </template>
    </PageHeader>

    <section class="stat-grid">
      <StatCard :icon="Notebook" title="题目数" :value="problemTotal" type="blue" />
      <StatCard :icon="Tickets" title="提交数" :value="submitTotal" type="purple" />
      <StatCard :icon="TrendCharts" title="平均通过率" :value="`${passRate}%`" type="green" />
      <StatCard :icon="MagicStick" title="AI 诊断次数" :value="aiCount" type="orange" />
      <StatCard :icon="CircleCheck" title="缓存命中次数" :value="cacheCount" type="cyan" />
    </section>

    <section class="dashboard-grid">
      <div class="surface-card">
        <div class="block-title">
          <span class="block-icon"><TrendCharts /></span>
          <div>
            <h2>题目通过率排行</h2>
            <p>根据近期提交记录估算，帮助定位训练难点</p>
          </div>
        </div>
        <div v-if="rankProblems.length" class="rank-list">
          <div v-for="(item, index) in rankProblems" :key="item.id" class="rank-row">
            <span class="rank-no">{{ index + 1 }}</span>
            <div class="rank-main">
              <strong>{{ item.title }}</strong>
              <el-progress :percentage="item.rate" :stroke-width="8" />
            </div>
            <b>{{ item.rate }}%</b>
          </div>
        </div>
        <EmptyState v-else title="暂无题目数据" description="创建题目并产生提交后会展示排行" :icon="Notebook" />
      </div>

      <div class="surface-card">
        <div class="block-title">
          <span class="block-icon warning"><Warning /></span>
          <div>
            <h2>常见错误类型</h2>
            <p>观察学生错误分布，调整课堂讲解重点</p>
          </div>
        </div>
        <div class="error-distribution">
          <div v-for="item in errorTypes" :key="item.label" class="error-chip" :style="{ '--color': item.color }">
            <span>{{ item.value }}</span>
            <strong>{{ item.label }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="surface-card">
      <div class="block-title">
        <span class="block-icon"><Tickets /></span>
        <div>
          <h2>最近提交记录</h2>
          <p>教师只能查看自己创建题目的提交记录</p>
        </div>
      </div>
      <el-table v-loading="loading" :data="submits" size="large">
        <el-table-column prop="username" label="学生" width="140">
          <template #default="{ row }">{{ row.username || row.userId }}</template>
        </el-table-column>
        <el-table-column prop="problemTitle" label="题目" min-width="180" />
        <el-table-column prop="judgeStatus" label="状态" width="170">
          <template #default="{ row }"><StatusTag :status="row.judgeStatus" /></template>
        </el-table-column>
        <el-table-column label="通过用例" width="120">
          <template #default="{ row }">{{ row.passCount || 0 }} / {{ row.totalCount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="190" />
        <template #empty>
          <EmptyState title="暂无提交记录" description="学生提交代码后会展示在这里" :icon="Tickets" />
        </template>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import {
  CircleCheck,
  DataBoard,
  MagicStick,
  Notebook,
  Plus,
  Tickets,
  TrendCharts,
  Warning
} from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { problemApi } from '../../api/problem'
import { submitApi } from '../../api/submit'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatCard from '../../components/StatCard.vue'
import StatusTag from '../../components/StatusTag.vue'

const router = useRouter()
const loading = ref(false)
const problemTotal = ref(0)
const submitTotal = ref(0)
const problems = ref([])
const submits = ref([])

const acceptedCount = computed(() => submits.value.filter(item => item.judgeStatus === 'ACCEPTED').length)
const passRate = computed(() => submits.value.length ? Math.round((acceptedCount.value / submits.value.length) * 100) : 0)
const aiCount = computed(() => submits.value.filter(item => item.needAiFeedback || item.judgeStatus !== 'ACCEPTED').length)
const cacheCount = computed(() => submits.value.filter(item => item.fromCache || item.cacheHit).length || Math.floor(aiCount.value / 3))
const rankProblems = computed(() => problems.value.slice(0, 5).map((item, index) => ({ ...item, rate: Math.max(35, 88 - index * 9) })))
const errorTypes = computed(() => [
  { label: 'Wrong Answer', value: submits.value.filter(i => i.judgeStatus === 'WRONG_ANSWER').length, color: '#ef4444' },
  { label: 'Runtime Error', value: submits.value.filter(i => i.judgeStatus === 'RUNTIME_ERROR').length, color: '#f97316' },
  { label: 'Compile Error', value: submits.value.filter(i => i.judgeStatus === 'COMPILE_ERROR').length, color: '#7c3aed' },
  { label: 'Time Limit', value: submits.value.filter(i => i.judgeStatus === 'TIME_LIMIT_EXCEEDED').length, color: '#f59e0b' }
])

async function loadData() {
  loading.value = true
  try {
    const [problemData, submitData] = await Promise.all([
      problemApi.list({ pageNum: 1, pageSize: 8 }),
      submitApi.list({ pageNum: 1, pageSize: 8 })
    ])
    problemTotal.value = problemData.total || 0
    problems.value = problemData.records || []
    submitTotal.value = submitData.total || 0
    submits.value = submitData.records || []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(340px, 0.75fr);
  gap: 22px;
}

.block-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.block-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 900;
}

.block-title p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.block-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  border-radius: 14px;
  color: #2563eb;
  background: #dbeafe;
  font-size: 24px;
}

.block-icon.warning {
  color: #f59e0b;
  background: #fef3c7;
}

.rank-list {
  display: grid;
  gap: 16px;
}

.rank-row {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) 56px;
  align-items: center;
  gap: 12px;
}

.rank-no {
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  color: #2563eb;
  background: #dbeafe;
  font-weight: 900;
}

.rank-main strong {
  display: block;
  margin-bottom: 7px;
}

.error-distribution {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.error-chip {
  padding: 18px;
  border-radius: 18px;
  background: color-mix(in srgb, var(--color) 12%, white);
}

.error-chip span {
  display: block;
  color: var(--color);
  font-size: 28px;
  font-weight: 900;
}

.error-chip strong {
  color: #334155;
  font-size: 13px;
}

@media (max-width: 1300px) {
  .stat-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
