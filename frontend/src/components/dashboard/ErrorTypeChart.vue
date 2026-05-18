<template>
  <section class="chart-card">
    <div class="card-head">
      <div>
        <h3>错误类型分布</h3>
        <p>从 AI 诊断和评测结果中识别常见问题。</p>
      </div>
    </div>

    <div v-if="chartData.length" ref="chartRef" class="chart"></div>
    <EmptyState v-else description="暂无错误类型数据" />
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import EmptyState from '../EmptyState.vue'

const props = defineProps({
  items: { type: Array, default: () => [] }
})

const chartRef = ref(null)
let chart

const chartData = computed(() => props.items
  .map((item) => ({
    name: item.errorType || item.name || '未分类错误',
    value: Number(item.count || item.value || 0)
  }))
  .filter((item) => item.value > 0))

function renderChart() {
  if (!chartRef.value || !chartData.value.length) return
  chart = chart || echarts.init(chartRef.value)
  chart.setOption({
    color: ['#2563EB', '#10B981', '#F59E0B', '#7C3AED', '#EF4444', '#06B6D4'],
    tooltip: { trigger: 'item' },
    legend: {
      orient: 'vertical',
      right: 0,
      top: 'middle',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: '#475569', fontSize: 12 }
    },
    series: [
      {
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['34%', '50%'],
        avoidLabelOverlap: true,
        label: { show: false },
        labelLine: { show: false },
        data: chartData.value
      }
    ]
  })
}

function resize() {
  chart?.resize()
}

onMounted(async () => {
  await nextTick()
  renderChart()
  window.addEventListener('resize', resize)
})

watch(chartData, async () => {
  await nextTick()
  renderChart()
}, { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.chart-card {
  min-width: 0;
  padding: 22px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.card-head {
  margin-bottom: 12px;
}

.card-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

.card-head p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
}

.chart {
  width: 100%;
  height: 260px;
}
</style>
