<template>
  <div class="report-page">
    <PageHeader
      title="学习报告"
      subtitle="用真实作答数据追踪学习趋势、知识点掌握情况和 AI 诊断效果。"
      eyebrow="Learning Analytics"
      :icon="TrendCharts"
    >
      <template #actions>
        <el-button type="primary" :icon="MagicStick" :loading="aiLoading" @click="generateAiAnalysis">
          {{ aiAnalysis ? '重新生成 AI 学情分析' : '生成 AI 学情分析' }}
        </el-button>
      </template>
    </PageHeader>

    <section class="surface-card ai-analysis-card">
      <div class="ai-analysis-head">
        <div>
          <h3><el-icon><MagicStick /></el-icon>AI 学情分析</h3>
          <p>手动触发 DeepSeek，基于学习概览、薄弱知识点、错误类型和题库进度生成分析。</p>
        </div>
        <el-tag :type="aiAnalysis?.generatedByAi ? 'success' : 'info'" round>
          {{ aiAnalysis?.generatedByAi ? `DeepSeek · ${aiAnalysis.model || ''}` : '未生成' }}
        </el-tag>
      </div>

      <div v-if="aiAnalysis" class="ai-analysis-grid">
        <article>
          <span>总体结论</span>
          <p>{{ aiAnalysis.summary }}</p>
        </article>
        <article>
          <span>优势表现</span>
          <p>{{ aiAnalysis.strengths }}</p>
        </article>
        <article>
          <span>主要短板</span>
          <p>{{ aiAnalysis.weaknesses }}</p>
        </article>
        <article>
          <span>改进建议</span>
          <p>{{ aiAnalysis.recommendations }}</p>
        </article>
        <article class="wide">
          <span>未来一周计划</span>
          <p>{{ aiAnalysis.nextWeekPlan }}</p>
        </article>
      </div>

      <EmptyState
        v-else
        title="暂未生成 AI 学情分析"
        description="点击右上角按钮后，系统会调用 DeepSeek 结合真实学习数据生成总结和计划。"
        :icon="MagicStick"
      />
    </section>

    <section class="stat-grid" v-loading="loading">
      <StatCard :icon="Tickets" title="总提交次数" :value="overview.submitCount || 0" type="blue" />
      <StatCard :icon="CircleCheck" title="通过次数" :value="overview.acceptedCount || 0" type="green" />
      <StatCard :icon="PieChart" title="通过率" :value="`${overview.acceptedRate || 0}%`" type="cyan" />
      <StatCard :icon="MagicStick" title="AI 诊断" :value="overview.aiFeedbackCount || 0" type="purple" />
      <StatCard :icon="Coin" title="缓存命中" :value="overview.cacheHitCount || 0" type="orange" />
    </section>

    <section class="chart-grid">
      <div class="surface-card chart-card" v-loading="loading">
        <div class="section-head">
          <h3><el-icon><TrendCharts /></el-icon>最近练习趋势</h3>
          <span>近 30 天</span>
        </div>
        <div v-show="hasTrendData" ref="trendChartRef" class="chart-box"></div>
        <EmptyState v-if="!hasTrendData && !loading" title="暂无练习趋势" description="完成作答后会生成每日练习曲线。" :icon="TrendCharts" />
      </div>

      <div class="surface-card chart-card" v-loading="loading">
        <div class="section-head">
          <h3><el-icon><PieChart /></el-icon>正确率变化</h3>
          <span>近 30 天</span>
        </div>
        <div v-show="hasAccuracyData" ref="accuracyChartRef" class="chart-box"></div>
        <EmptyState v-if="!hasAccuracyData && !loading" title="暂无正确率数据" description="多完成几道题后会看到变化趋势。" :icon="PieChart" />
      </div>
    </section>

    <section class="chart-grid">
      <div class="surface-card chart-card" v-loading="loading">
        <div class="section-head">
          <h3><el-icon><Reading /></el-icon>知识点掌握</h3>
          <span>按知识标签统计</span>
        </div>
        <div v-show="knowledgeMastery.length" ref="knowledgeChartRef" class="chart-box"></div>
        <EmptyState v-if="!knowledgeMastery.length && !loading" title="暂无知识点统计" description="题目配置知识点后会自动聚合掌握情况。" :icon="Reading" />
      </div>

      <div class="surface-card chart-card" v-loading="loading">
        <div class="section-head">
          <h3><el-icon><Warning /></el-icon>错误类型分布</h3>
          <span>来自 AI 诊断</span>
        </div>
        <div v-show="errorTypes.length" ref="errorChartRef" class="chart-box"></div>
        <EmptyState v-if="!errorTypes.length && !loading" title="暂无错误类型" description="生成 AI 诊断后会沉淀错误类型分布。" :icon="Warning" />
      </div>
    </section>

    <section class="surface-card" v-loading="loading">
      <div class="section-head">
        <h3><el-icon><Collection /></el-icon>题库完成进度</h3>
        <span>按题库统计已通过题目</span>
      </div>
      <div v-if="bankProgress.length" class="bank-progress-list">
        <div v-for="item in bankProgress" :key="item.bankId" class="bank-progress-row">
          <div>
            <strong>{{ item.bankName || '未归属题库' }}</strong>
            <p>已完成 {{ item.completedProblems || 0 }} / {{ item.totalProblems || 0 }} 题，提交 {{ item.submitCount || 0 }} 次</p>
          </div>
          <el-progress :percentage="item.progressRate || 0" :stroke-width="12" />
        </div>
      </div>
      <EmptyState v-else-if="!loading" title="暂无题库进度" description="从题库进入练习后会展示完成进度。" :icon="Collection" />
    </section>

    <section class="surface-card" v-loading="loading">
      <div class="section-head">
        <h3><el-icon><Tickets /></el-icon>最近作答记录</h3>
        <span>最近 10 条</span>
      </div>
      <el-table :data="recent" size="large">
        <el-table-column prop="problemTitle" label="题目" min-width="180" />
        <el-table-column prop="bankName" label="题库" min-width="140" />
        <el-table-column prop="judgeStatus" label="状态" width="170">
          <template #default="{ row }"><StatusTag :status="row.judgeStatus" /></template>
        </el-table-column>
        <el-table-column label="通过用例" width="120">
          <template #default="{ row }">{{ row.passCount || 0 }} / {{ row.totalCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="190">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无作答记录" description="完成一次作答后会显示在这里。" :icon="Tickets" />
        </template>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { CircleCheck, Coin, Collection, MagicStick, PieChart, Reading, Tickets, TrendCharts, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { reportApi } from '../../api/report'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatCard from '../../components/StatCard.vue'
import StatusTag from '../../components/StatusTag.vue'

const loading = ref(false)
const aiLoading = ref(false)
const aiAnalysis = ref(null)
const overview = ref({})
const trend = ref([])
const accuracyTrend = ref([])
const knowledgeMastery = ref([])
const errorTypes = ref([])
const bankProgress = ref([])
const recent = ref([])

const hasTrendData = computed(() => trend.value.some(item => (item.submitCount || 0) > 0))
const hasAccuracyData = computed(() => accuracyTrend.value.some(item => (item.submitCount || 0) > 0))

const trendChartRef = ref(null)
const accuracyChartRef = ref(null)
const knowledgeChartRef = ref(null)
const errorChartRef = ref(null)
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
  chartOf(trendChartRef)?.setOption({
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['提交', '通过', '未通过'] },
    grid: { left: 34, right: 18, top: 48, bottom: 30 },
    xAxis: { type: 'category', data: trend.value.map(item => item.date.slice(5)) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '提交', type: 'line', smooth: true, data: trend.value.map(item => item.submitCount || 0), areaStyle: { opacity: 0.12 } },
      { name: '通过', type: 'line', smooth: true, data: trend.value.map(item => item.acceptedCount || 0) },
      { name: '未通过', type: 'line', smooth: true, data: trend.value.map(item => item.wrongCount || 0) }
    ],
    color: ['#2563eb', '#10b981', '#ef4444']
  })

  chartOf(accuracyChartRef)?.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br />正确率：{c}%' },
    grid: { left: 34, right: 18, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: accuracyTrend.value.map(item => item.date.slice(5)) },
    yAxis: { type: 'value', min: 0, max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{ type: 'line', smooth: true, data: accuracyTrend.value.map(item => item.acceptedRate || 0), areaStyle: { opacity: 0.18 } }],
    color: ['#7c3aed']
  })

  chartOf(knowledgeChartRef)?.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 58, right: 18, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: knowledgeMastery.value.map(item => item.knowledgeTag), axisLabel: { rotate: 25 } },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
    series: [{ name: '掌握度', type: 'bar', data: knowledgeMastery.value.map(item => item.masteryRate || 0), barWidth: 18, itemStyle: { borderRadius: [8, 8, 0, 0] } }],
    color: ['#06b6d4']
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
}

async function loadReport() {
  loading.value = true
  try {
    const [overviewData, trendData, accuracyData, masteryData, errorData, bankData, recentData] = await Promise.all([
      reportApi.studentOverview(),
      reportApi.studentTrend(),
      reportApi.studentAccuracyTrend(),
      reportApi.studentKnowledgeMastery(),
      reportApi.studentErrorTypes(),
      reportApi.studentBankProgress(),
      reportApi.studentRecentSubmissions()
    ])
    overview.value = overviewData || {}
    trend.value = trendData || []
    accuracyTrend.value = accuracyData || []
    knowledgeMastery.value = masteryData || []
    errorTypes.value = errorData || []
    bankProgress.value = bankData || []
    recent.value = recentData || []
    await nextTick()
    renderCharts()
  } finally {
    loading.value = false
  }
}

async function generateAiAnalysis() {
  aiLoading.value = true
  try {
    aiAnalysis.value = await reportApi.studentAiAnalysis()
    ElMessage.success(aiAnalysis.value?.generatedByAi ? 'DeepSeek 学情分析已生成' : '已生成本地学情分析')
  } finally {
    aiLoading.value = false
  }
}

function resizeCharts() {
  charts.forEach(chart => chart.resize())
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

onMounted(() => {
  loadReport()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  charts.forEach(chart => chart.dispose())
})
</script>

<style scoped>
.report-page {
  display: grid;
  gap: 24px;
  padding: 24px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 22px;
}

.surface-card {
  padding: 22px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.ai-analysis-card {
  background:
    radial-gradient(circle at 90% 0%, rgba(124, 58, 237, 0.08), transparent 28%),
    linear-gradient(180deg, #fff, #f8fbff);
}

.ai-analysis-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.ai-analysis-head h3,
.section-head h3 {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

.ai-analysis-head p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.ai-analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.ai-analysis-grid article {
  padding: 16px;
  border: 1px solid #e8eef7;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.82);
}

.ai-analysis-grid article.wide {
  grid-column: 1 / -1;
}

.ai-analysis-grid span {
  color: #2563eb;
  font-size: 13px;
  font-weight: 900;
}

.ai-analysis-grid p {
  margin: 8px 0 0;
  color: #334155;
  font-size: 14px;
  line-height: 1.8;
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

.bank-progress-list {
  display: grid;
  gap: 14px;
}

.bank-progress-row {
  display: grid;
  grid-template-columns: minmax(220px, 0.4fr) minmax(0, 1fr);
  align-items: center;
  gap: 18px;
  padding: 16px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 16px;
  background: #f8fafc;
}

.bank-progress-row strong {
  color: #111827;
  font-weight: 900;
}

.bank-progress-row p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

@media (max-width: 1180px) {
  .stat-grid,
  .chart-grid,
  .ai-analysis-grid {
    grid-template-columns: 1fr;
  }

  .bank-progress-row {
    grid-template-columns: 1fr;
  }
}
</style>
