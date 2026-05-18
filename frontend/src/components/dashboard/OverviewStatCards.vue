<template>
  <section class="overview-grid">
    <article v-for="item in stats" :key="item.key" class="stat-card" @click="go(item.path)">
      <span class="stat-icon" :class="item.className">
        <el-icon><component :is="item.icon" /></el-icon>
      </span>
      <div>
        <p>{{ item.label }}</p>
        <strong>{{ item.value }}</strong>
        <span>{{ item.subText }}</span>
      </div>
    </article>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ChatLineRound, CircleCheck, DataAnalysis, EditPen, Warning } from '@element-plus/icons-vue'

const props = defineProps({
  overview: { type: Object, default: () => ({}) }
})

const router = useRouter()

const stats = computed(() => {
  const submitCount = Number(props.overview.submitCount || 0)
  const acceptedCount = Number(props.overview.acceptedCount || 0)
  const wrongCount = Number(props.overview.wrongQuestionCount ?? props.overview.wrongCount ?? 0)
  return [
    {
      key: 'submit',
      label: '总练习次数',
      value: submitCount,
      subText: `今日 ${props.overview.todaySubmitCount || 0} 次`,
      icon: EditPen,
      className: 'blue',
      path: '/student/submissions'
    },
    {
      key: 'accepted',
      label: '正确题数',
      value: acceptedCount,
      subText: '累计通过',
      icon: CircleCheck,
      className: 'green',
      path: '/student/submissions'
    },
    {
      key: 'rate',
      label: '正确率',
      value: `${props.overview.accuracyRate || 0}%`,
      subText: '按提交统计',
      icon: DataAnalysis,
      className: 'orange',
      path: '/student/report'
    },
    {
      key: 'ai',
      label: 'AI 诊断次数',
      value: props.overview.aiFeedbackCount || 0,
      subText: '已生成反馈',
      icon: ChatLineRound,
      className: 'purple',
      path: '/student/report'
    },
    {
      key: 'wrong',
      label: '错题数',
      value: wrongCount,
      subText: '建议复习',
      icon: Warning,
      className: 'red',
      path: '/student/submissions'
    }
  ]
})

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(190px, 1fr));
  gap: 14px;
}

.stat-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
  min-height: 116px;
  min-width: 0;
  padding: 20px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 38px rgba(37, 99, 235, 0.12);
}

.stat-icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon.blue {
  background: linear-gradient(135deg, #2563eb, #60a5fa);
}

.stat-icon.green {
  background: linear-gradient(135deg, #10b981, #34d399);
}

.stat-icon.orange {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.stat-icon.purple {
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
}

.stat-icon.red {
  background: linear-gradient(135deg, #ef4444, #fb7185);
}

.stat-card p,
.stat-card span {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.45;
  word-break: keep-all;
  overflow-wrap: normal;
}

.stat-card strong {
  display: block;
  margin: 7px 0 6px;
  color: #0f172a;
  font-size: 26px;
  font-weight: 900;
  line-height: 1;
  white-space: nowrap;
}

@media (max-width: 1280px) {
  .overview-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
