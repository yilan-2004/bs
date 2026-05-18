<template>
  <section class="overview-stats">
    <article v-for="item in items" :key="item.label" class="stat-card" @click="router.push(item.target)">
      <div class="stat-icon" :class="item.className">
        <el-icon><component :is="item.icon" /></el-icon>
      </div>
      <div>
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <p>{{ item.hint }}</p>
      </div>
    </article>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { Checked, DataLine, MagicStick, Warning } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  overview: { type: Object, default: () => ({}) }
})
const router = useRouter()

const items = computed(() => [
  {
    label: '总练习次数',
    value: props.overview.submitCount ?? 0,
    hint: `今日 ${props.overview.todaySubmitCount ?? 0} 次`,
    icon: DataLine,
    className: 'blue',
    target: '/student/submissions'
  },
  {
    label: '正确率',
    value: `${props.overview.accuracyRate ?? 0}%`,
    hint: `${props.overview.acceptedCount ?? 0} 次通过`,
    icon: Checked,
    className: 'green',
    target: '/student/report'
  },
  {
    label: 'AI 诊断次数',
    value: props.overview.aiFeedbackCount ?? 0,
    hint: '错因诊断与学习建议',
    icon: MagicStick,
    className: 'purple',
    target: '/student/submissions'
  },
  {
    label: '错题数量',
    value: props.overview.wrongQuestionCount ?? 0,
    hint: `${props.overview.wrongCount ?? 0} 次未通过`,
    icon: Warning,
    className: 'orange',
    target: '/student/banks'
  }
])
</script>

<style scoped>
.overview-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  min-height: 120px;
  padding: 22px;
  border: 1px solid #e8eef8;
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.055);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.stat-card:hover {
  box-shadow: 0 20px 42px rgba(37, 99, 235, 0.12);
  transform: translateY(-3px);
}

.stat-icon {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  font-size: 25px;
}

.stat-icon.blue {
  color: #2563eb;
  background: #dbeafe;
}

.stat-icon.green {
  color: #059669;
  background: #d1fae5;
}

.stat-icon.purple {
  color: #7c3aed;
  background: #ede9fe;
}

.stat-icon.orange {
  color: #f97316;
  background: #ffedd5;
}

.stat-card span {
  color: #718096;
  font-size: 13px;
  font-weight: 700;
}

.stat-card strong {
  display: block;
  margin-top: 5px;
  color: #111827;
  font-size: 28px;
  font-weight: 900;
}

.stat-card p {
  margin: 4px 0 0;
  color: #94a3b8;
  font-size: 12px;
}

@media (max-width: 1180px) {
  .overview-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 680px) {
  .overview-stats {
    grid-template-columns: 1fr;
  }
}
</style>
