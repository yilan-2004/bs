<template>
  <div class="page page-stack">
    <PageHeader
      title="教学分析"
      subtitle="围绕教师本人创建的题库、题目、提交和 AI 反馈生成教学数据看板"
      eyebrow="Teaching Analytics"
      :icon="TrendCharts"
    />

    <section class="stat-grid" v-loading="loading">
      <StatCard :icon="Collection" title="题库数量" :value="overview.bankCount || 0" type="blue" />
      <StatCard :icon="Notebook" title="题目数量" :value="overview.problemCount || 0" type="cyan" />
      <StatCard :icon="Tickets" title="提交次数" :value="overview.submitCount || 0" type="purple" />
      <StatCard :icon="CircleCheck" title="平均通过率" :value="`${overview.acceptedRate || 0}%`" type="green" />
      <StatCard :icon="MagicStick" title="AI 诊断" :value="overview.aiFeedbackCount || 0" type="purple" />
      <StatCard :icon="Coin" title="缓存命中" :value="overview.cacheHitCount || 0" type="orange" />
    </section>

    <section class="chart-grid">
      <div class="surface-card chart-card" v-loading="loading">
        <div class="section-head">
          <h3 class="section-title"><el-icon><Notebook /></el-icon>题目正确率排行</h3>
          <span>仅统计我的题目</span>
        </div>
        <div v-show="questionRank.length" ref="questionChartRef" class="chart-box chart-box-tall"></div>
        <EmptyState v-if="!questionRank.length && !loading" title="暂无题目排行" description="学生提交后会生成题目正确率排行" :icon="Notebook" />
      </div>

      <div class="surface-card chart-card" v-loading="loading">
        <div class="section-head">
          <h3 class="section-title"><el-icon><Warning /></el-icon>错误类型分布</h3>
          <span>来自 AI 诊断</span>
        </div>
        <div v-show="errorTypes.length" ref="errorChartRef" class="chart-box"></div>
        <EmptyState v-if="!errorTypes.length && !loading" title="暂无错误类型" description="学生生成 AI 诊断后会沉淀错误类型" :icon="Warning" />
      </div>
    </section>

    <section class="chart-grid">
      <div class="surface-card chart-card" v-loading="loading">
        <div class="section-head">
          <h3 class="section-title"><el-icon><Reading /></el-icon>知识点薄弱排行</h3>
          <span>未通过提交占比</span>
        </div>
        <div v-show="knowledgeWeakness.length" ref="knowledgeChartRef" class="chart-box"></div>
        <EmptyState v-if="!knowledgeWeakness.length && !loading" title="暂无知识点薄弱数据" description="题目配置知识点后会自动聚合学生薄弱项" :icon="Reading" />
      </div>

      <div class="surface-card chart-card" v-loading="loading">
        <div class="section-head">
          <h3 class="section-title"><el-icon><MagicStick /></el-icon>AI 使用情况</h3>
          <span>近 30 天</span>
        </div>
        <div v-show="hasAiUsageData" ref="aiUsageChartRef" class="chart-box"></div>
        <EmptyState v-if="!hasAiUsageData && !loading" title="暂无 AI 使用数据" description="学生请求 AI 诊断后会展示使用趋势" :icon="MagicStick" />
      </div>
    </section>

    <section class="chart-grid">
      <div class="surface-card" v-loading="loading">
        <div class="section-head">
          <h3 class="section-title"><el-icon><UserFilled /></el-icon>学生活跃度排行</h3>
          <span>按提交、通过和 AI 诊断综合计算</span>
        </div>
        <el-table :data="studentRank" size="large">
          <el-table-column type="index" label="#" width="60" />
          <el-table-column label="学生" min-width="140">
            <template #default="{ row }">{{ row.realName || row.username || `学生${row.userId}` }}</template>
          </el-table-column>
          <el-table-column prop="submitCount" label="提交" width="90" />
          <el-table-column prop="acceptedCount" label="通过" width="90" />
          <el-table-column prop="acceptedRate" label="通过率" width="100">
            <template #default="{ row }">{{ row.acceptedRate || 0 }}%</template>
          </el-table-column>
          <el-table-column prop="aiFeedbackCount" label="AI 诊断" width="110" />
          <template #empty>
            <EmptyState title="暂无学生排行" description="学生提交后会生成活跃度排行" :icon="UserFilled" />
          </template>
        </el-table>
      </div>

      <div class="surface-card" v-loading="loading">
        <div class="section-head">
          <h3 class="section-title"><el-icon><Tickets /></el-icon>最近提交记录</h3>
          <span>最近 12 条</span>
        </div>
        <el-table :data="recent" size="large">
          <el-table-column prop="problemTitle" label="题目" min-width="160" />
          <el-table-column prop="bankName" label="题库" min-width="130" />
          <el-table-column prop="judgeStatus" label="状态" width="150">
            <template #default="{ row }"><StatusTag :status="row.judgeStatus" /></template>
          </el-table-column>
          <el-table-column prop="createTime" label="提交时间" width="180" />
          <template #empty>
            <EmptyState title="暂无提交记录" description="学生完成作答后会展示在这里" :icon="Tickets" />
          </template>
        </el-table>
      </div>
    </section>
  </div>
</template>

<script setup>
import { CircleCheck, Coin, Collection, MagicStick, Notebook, Reading, Tickets, TrendCharts, UserFilled, Warning } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { reportApi } from '../../api/report'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatCard from '../../components/StatCard.vue'
import StatusTag from '../../components/StatusTag.vue'

const loading = ref(false)
const overview = ref({})
const questionRank = ref([])
const knowledgeWeakness = ref([])
const errorTypes = ref([])
const studentRank = ref([])
const aiUsage = ref([])
const recent = ref([])

const hasAiUsageData = computed(() => aiUsage.value.some(item => (item.aiFeedbackCount || 0) > 0))

const questionChartRef = ref(null)
const knowledgeChartRef = ref(null)
const errorChartRef = ref(null)
const aiUsageChartRef = ref(null)
const charts = []

function chartOf(elRef) {
  if (!elRef.value) return null
  let chart = echarts.getInstanceByDom(elRef.value)
  if (!chart) {
    chart = echarts.init(elRef.value)
    charts.push(chart)
  }
  return chart
}

function renderCharts() {
  const rankData = [...questionRank.value].reverse()
  chartOf(questionChartRef)?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 116, right: 26, top: 28, bottom: 28 },
    xAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    yAxis: { type: 'category', data: rankData.map(item => item.problemTitle) },
    series: [{ name: '正确率', type: 'bar', data: rankData.map(item => item.acceptedRate || 0), barWidth: 14, itemStyle: { borderRadius: [0, 8, 8, 0] } }],
    color: ['#2563eb']
  })

  chartOf(knowledgeChartRef)?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 56, right: 20, top: 30, bottom: 50 },
    xAxis: { type: 'category', data: knowledgeWeakness.value.map(item => item.knowledgeTag), axisLabel: { rotate: 25 } },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{ name: '薄弱率', type: 'bar', data: knowledgeWeakness.value.map(item => item.weaknessRate || 0), barWidth: 18, itemStyle: { borderRadius: [8, 8, 0, 0] } }],
    color: ['#f97316']
  })

  chartOf(errorChartRef)?.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: 0, top: 'center' },
    series: [{
      name: '错误类型',
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['38%', '52%'],
      data: errorTypes.value.map(item => ({ name: item.errorType || '未知错误', value: item.count || 0 }))
    }],
    color: ['#ef4444', '#f97316', '#8b5cf6', '#f59e0b', '#2563eb', '#06b6d4']
  })

  chartOf(aiUsageChartRef)?.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['AI 诊断', '缓存命中', '命中率'] },
    grid: { left: 38, right: 42, top: 48, bottom: 30 },
    xAxis: { type: 'category', data: aiUsage.value.map(item => item.date.slice(5)) },
    yAxis: [
      { type: 'value', minInterval: 1 },
      { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } }
    ],
    series: [
      { name: 'AI 诊断', type: 'bar', data: aiUsage.value.map(item => item.aiFeedbackCount || 0), barWidth: 12 },
      { name: '缓存命中', type: 'bar', data: aiUsage.value.map(item => item.cacheHitCount || 0), barWidth: 12 },
      { name: '命中率', type: 'line', yAxisIndex: 1, smooth: true, data: aiUsage.value.map(item => item.cacheHitRate || 0) }
    ],
    color: ['#7c3aed', '#06b6d4', '#10b981']
  })
}

async function loadData() {
  loading.value = true
  try {
    const [overviewData, rankData, weaknessData, errorData, studentData, aiData, recentData] = await Promise.all([
      reportApi.teacherOverview(),
      reportApi.teacherQuestionRank(),
      reportApi.teacherKnowledgeWeakness(),
      reportApi.teacherErrorTypes(),
      reportApi.teacherStudentRank(),
      reportApi.teacherAiUsage(),
      reportApi.teacherRecentSubmissions()
    ])
    overview.value = overviewData || {}
    questionRank.value = rankData || []
    knowledgeWeakness.value = weaknessData || []
    errorTypes.value = errorData || []
    studentRank.value = studentData || []
    aiUsage.value = aiData || []
    recent.value = recentData || []
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

function resizeCharts() {
  charts.forEach(chart => chart.resize())
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  charts.forEach(chart => chart.dispose())
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 22px;
}

.chart-card {
  min-height: 390px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
}

.section-head span {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}

.chart-box {
  width: 100%;
  height: 310px;
}

.chart-box-tall {
  height: 360px;
}

@media (max-width: 1180px) {
  .stat-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
