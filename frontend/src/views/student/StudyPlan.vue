<template>
  <div class="student-page">
    <PageHeader
      title="学习计划"
      subtitle="根据今日练习、薄弱知识点和最近错误提交，生成可执行的复习任务。"
      eyebrow="Study Plan"
      :icon="Notebook"
    />

    <section class="plan-grid" v-loading="loading">
      <article v-for="task in planTasks" :key="task.key" class="plan-card" @click="router.push(task.path)">
        <span class="task-icon" :class="task.color">
          <el-icon><component :is="task.icon" /></el-icon>
        </span>
        <div class="task-copy">
          <h3>{{ task.title }}</h3>
          <p>{{ task.description }}</p>
          <el-progress :percentage="task.progress" :stroke-width="9" :color="task.progressColor" />
        </div>
        <el-button type="primary" plain>开始</el-button>
      </article>
    </section>

    <section class="content-grid">
      <div class="surface-card">
        <div class="section-head">
          <h3>学科练习安排</h3>
          <p>点击学科进入对应题库继续训练。</p>
        </div>
        <div v-if="subjects.length" class="subject-list">
          <button v-for="subject in subjects" :key="subject.subjectId" class="subject-row" @click="goSubject(subject.subjectId)">
            <div>
              <strong>{{ subject.subjectName }}</strong>
              <span>已练习 {{ subject.practicedQuestionCount || 0 }} 题，正确率 {{ subject.accuracyRate || 0 }}%</span>
            </div>
            <el-progress :percentage="subject.accuracyRate || 0" :stroke-width="8" />
          </button>
        </div>
        <EmptyState v-else-if="!loading" title="暂无学科计划" description="先完成几道题，系统会自动形成计划。" :icon="Notebook" />
      </div>

      <div class="surface-card">
        <div class="section-head">
          <h3>待处理提醒</h3>
          <p>来自真实提交和 AI 反馈状态。</p>
        </div>
        <div v-if="reminders.length" class="reminder-list">
          <button v-for="item in reminders" :key="`${item.type}-${item.title}`" class="reminder-row" @click="router.push(item.targetUrl || '/student/banks')">
            <strong>{{ item.title }}</strong>
            <span>{{ item.content }}</span>
          </button>
        </div>
        <EmptyState v-else-if="!loading" title="暂无待办提醒" description="今天状态不错，继续保持学习节奏。" :icon="CircleCheck" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ChatLineRound, CircleCheck, Collection, Notebook, Warning } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardApi } from '../../api/dashboard'
import { reportApi } from '../../api/report'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'

const router = useRouter()
const loading = ref(false)
const overview = ref({})
const subjects = ref([])
const weakKnowledge = ref([])
const reminders = ref([])

const planTasks = computed(() => {
  const todaySubmitCount = Number(overview.value.todaySubmitCount || 0)
  const wrongQuestionCount = Number(overview.value.wrongQuestionCount || 0)
  const aiFeedbackCount = Number(overview.value.aiFeedbackCount || 0)
  const weak = weakKnowledge.value[0]
  return [
    {
      key: 'practice',
      title: '完成今日练习',
      description: `今日已练习 ${todaySubmitCount} 次，建议至少完成 3 次有效练习。`,
      progress: Math.min(100, Math.round((todaySubmitCount / 3) * 100)),
      progressColor: '#2563eb',
      icon: Collection,
      color: 'blue',
      path: '/student/banks'
    },
    {
      key: 'wrong',
      title: '复习错题本',
      description: `当前有 ${wrongQuestionCount} 道错题需要复盘。`,
      progress: wrongQuestionCount ? 35 : 100,
      progressColor: '#f59e0b',
      icon: Warning,
      color: 'orange',
      path: '/student/submissions'
    },
    {
      key: 'weak',
      title: weak ? `专项突破：${weak.knowledgeTag}` : '专项突破薄弱点',
      description: weak ? `该知识点错误 ${weak.wrongCount || 0} 次，建议进行题库筛选练习。` : '完成更多练习后，系统会给出薄弱知识点。',
      progress: weak ? Math.max(10, 100 - wrongRate(weak)) : 0,
      progressColor: '#10b981',
      icon: Notebook,
      color: 'green',
      path: weak ? `/student/banks?knowledgeTag=${encodeURIComponent(weak.knowledgeTag)}` : '/student/banks'
    },
    {
      key: 'ai',
      title: '查看 AI 诊断',
      description: `已生成 ${aiFeedbackCount} 次诊断，错误提交可继续生成解析。`,
      progress: aiFeedbackCount ? 70 : 0,
      progressColor: '#7c3aed',
      icon: ChatLineRound,
      color: 'purple',
      path: '/student/ai-tutor'
    }
  ]
})

function wrongRate(item) {
  const submitCount = Number(item.submitCount || 0)
  const wrongCount = Number(item.wrongCount || 0)
  return submitCount ? Math.round((wrongCount * 100) / submitCount) : 0
}

function goSubject(subjectId) {
  router.push(subjectId ? `/student/banks?subjectId=${subjectId}` : '/student/banks')
}

async function loadData() {
  loading.value = true
  try {
    const [overviewRes, subjectsRes, weakRes, remindersRes] = await Promise.all([
      dashboardApi.studentOverview(),
      dashboardApi.studentSubjects(),
      reportApi.studentKnowledge(),
      dashboardApi.studentReminders()
    ])
    overview.value = overviewRes || {}
    subjects.value = subjectsRes || []
    weakKnowledge.value = (weakRes || []).filter((item) => Number(item.wrongCount || 0) > 0)
    reminders.value = remindersRes || []
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.student-page {
  display: grid;
  gap: 24px;
  padding: 24px;
}

.plan-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.plan-card,
.surface-card {
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.plan-card {
  display: grid;
  gap: 14px;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.18s ease;
}

.plan-card:hover {
  transform: translateY(-3px);
}

.task-icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #fff;
  font-size: 24px;
}

.task-icon.blue { background: #2563eb; }
.task-icon.orange { background: #f59e0b; }
.task-icon.green { background: #10b981; }
.task-icon.purple { background: #7c3aed; }

.task-copy h3 {
  margin: 0;
  color: #0f172a;
  font-size: 17px;
  font-weight: 900;
}

.task-copy p {
  min-height: 42px;
  margin: 8px 0 14px;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 24px;
}

.surface-card {
  padding: 22px;
}

.section-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

.section-head p {
  margin: 6px 0 16px;
  color: #64748b;
  font-size: 13px;
}

.subject-list,
.reminder-list {
  display: grid;
  gap: 12px;
}

.subject-row,
.reminder-row {
  display: grid;
  gap: 10px;
  width: 100%;
  padding: 14px;
  border: 1px solid #edf2f7;
  border-radius: 14px;
  background: #f8fafc;
  cursor: pointer;
  text-align: left;
}

.subject-row strong,
.reminder-row strong {
  color: #0f172a;
  font-size: 14px;
}

.subject-row span,
.reminder-row span {
  color: #64748b;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .plan-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .plan-grid {
    grid-template-columns: 1fr;
  }
}
</style>
