<template>
  <div class="student-page">
    <PageHeader
      title="知识点图谱"
      subtitle="基于你的练习记录和题目知识标签，查看各知识点掌握情况。"
      eyebrow="Knowledge Map"
      :icon="Aim"
    >
      <template #actions>
        <el-button type="primary" @click="router.push('/student/banks')">去题库练习</el-button>
      </template>
    </PageHeader>

    <section class="map-grid" v-loading="loading">
      <div class="surface-card graph-card">
        <div class="section-head">
          <h3>掌握情况图谱</h3>
          <p>颜色越偏橙，代表越需要复习。</p>
        </div>
        <div v-if="knowledgeItems.length" ref="chartRef" class="knowledge-chart"></div>
        <EmptyState v-else-if="!loading" title="暂无知识点数据" description="完成带有知识标签的题目后，这里会自动生成图谱。" :icon="Aim" />
      </div>

      <div class="surface-card weak-panel">
        <div class="section-head">
          <h3>薄弱知识点</h3>
          <p>点击知识点进入题库筛选练习。</p>
        </div>
        <div v-if="weakItems.length" class="weak-list">
          <button v-for="item in weakItems" :key="item.knowledgeTag" class="weak-row" @click="goTag(item.knowledgeTag)">
            <div>
              <strong>{{ item.knowledgeTag }}</strong>
              <span>提交 {{ item.submitCount || 0 }} 次，错误 {{ item.wrongCount || 0 }} 次</span>
            </div>
            <el-progress :percentage="wrongRate(item)" :stroke-width="8" color="#f59e0b" />
          </button>
        </div>
        <EmptyState v-else-if="!loading" title="暂无明显薄弱点" description="继续保持，系统会根据后续练习动态更新。" :icon="CircleCheck" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { Aim, CircleCheck } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { reportApi } from '../../api/report'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'

const router = useRouter()
const loading = ref(false)
const knowledgeItems = ref([])
const chartRef = ref(null)
let chart

const weakItems = computed(() => knowledgeItems.value
  .filter((item) => Number(item.wrongCount || 0) > 0 || Number(item.masteryRate || 100) < 70)
  .sort((a, b) => wrongRate(b) - wrongRate(a))
  .slice(0, 8))

function wrongRate(item) {
  const submitCount = Number(item.submitCount || 0)
  const wrongCount = Number(item.wrongCount || 0)
  if (submitCount > 0) return Math.round((wrongCount * 100) / submitCount)
  return Math.max(0, 100 - Number(item.masteryRate || 0))
}

function goTag(tag) {
  router.push(`/student/banks?knowledgeTag=${encodeURIComponent(tag)}`)
}

function renderChart() {
  if (!chartRef.value || !knowledgeItems.value.length) return
  chart = chart || echarts.init(chartRef.value)
  const nodes = knowledgeItems.value.map((item, index) => ({
    name: item.knowledgeTag || `知识点${index + 1}`,
    value: item.masteryRate ?? Math.max(0, 100 - wrongRate(item)),
    symbolSize: Math.max(34, Math.min(78, 42 + Number(item.submitCount || 0) * 4)),
    itemStyle: { color: wrongRate(item) >= 50 ? '#f59e0b' : wrongRate(item) > 0 ? '#06b6d4' : '#10b981' }
  }))
  chart.setOption({
    tooltip: {
      formatter: (params) => `${params.name}<br/>掌握度：${params.value || 0}%`
    },
    series: [{
      type: 'graph',
      layout: 'force',
      roam: true,
      data: nodes,
      links: nodes.slice(1).map((node) => ({ source: nodes[0].name, target: node.name })),
      force: { repulsion: 150, edgeLength: 80 },
      label: { show: true, color: '#0f172a', fontWeight: 700 },
      lineStyle: { color: '#cbd5e1' }
    }]
  })
}

async function loadData() {
  loading.value = true
  try {
    const [mastery, weak] = await Promise.all([
      reportApi.studentKnowledgeMastery(),
      reportApi.studentKnowledge()
    ])
    const map = new Map()
    ;(mastery || []).forEach((item) => map.set(item.knowledgeTag, { ...item }))
    ;(weak || []).forEach((item) => map.set(item.knowledgeTag, { ...(map.get(item.knowledgeTag) || {}), ...item }))
    knowledgeItems.value = Array.from(map.values()).filter((item) => item.knowledgeTag)
    await nextTick()
    renderChart()
  } finally {
    loading.value = false
  }
}

function resize() {
  chart?.resize()
}

watch(knowledgeItems, async () => {
  await nextTick()
  renderChart()
}, { deep: true })

onMounted(() => {
  loadData()
  window.addEventListener('resize', resize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
})
</script>

<style scoped>
.student-page {
  display: grid;
  gap: 24px;
  padding: 24px;
}

.map-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 24px;
}

.surface-card {
  padding: 22px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.section-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

.section-head p {
  margin: 6px 0 18px;
  color: #64748b;
  font-size: 13px;
}

.knowledge-chart {
  height: 520px;
}

.weak-list {
  display: grid;
  gap: 14px;
}

.weak-row {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid #edf2f7;
  border-radius: 14px;
  background: #f8fafc;
  cursor: pointer;
  text-align: left;
}

.weak-row strong {
  display: block;
  color: #0f172a;
  font-size: 14px;
}

.weak-row span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

@media (max-width: 1100px) {
  .map-grid {
    grid-template-columns: 1fr;
  }
}
</style>
