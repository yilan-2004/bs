<template>
  <div class="student-dashboard-page">
    <DashboardTopBar
      :student-name="studentName"
      :reminder-count="reminders.length"
      @open-reminders="scrollToReminders"
    />

    <div v-loading="pageLoading" class="dashboard-shell">
      <main class="dashboard-main">
        <HeroLearningCard />
        <OverviewStatCards :overview="overview" />
        <SubjectProgressCards :subjects="subjects" />

        <div class="dashboard-bottom">
          <RecentPracticeTable :records="recentSubmissions" />
          <ErrorTypeChart :items="errorTypes" />
        </div>
      </main>

      <aside class="dashboard-sidebar">
        <StudyCalendar />
        <TodayTaskList ref="taskRef" :tasks="reminders" />
        <WeakKnowledgeList :items="weakKnowledge" />
        <LearningRanking :ranking="ranking" />
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { dashboardApi } from '../../api/dashboard'
import { reportApi } from '../../api/report'
import DashboardTopBar from '../../components/dashboard/DashboardTopBar.vue'
import ErrorTypeChart from '../../components/dashboard/ErrorTypeChart.vue'
import HeroLearningCard from '../../components/dashboard/HeroLearningCard.vue'
import LearningRanking from '../../components/dashboard/LearningRanking.vue'
import OverviewStatCards from '../../components/dashboard/OverviewStatCards.vue'
import RecentPracticeTable from '../../components/dashboard/RecentPracticeTable.vue'
import StudyCalendar from '../../components/dashboard/StudyCalendar.vue'
import SubjectProgressCards from '../../components/dashboard/SubjectProgressCards.vue'
import TodayTaskList from '../../components/dashboard/TodayTaskList.vue'
import WeakKnowledgeList from '../../components/dashboard/WeakKnowledgeList.vue'
import { useAuthStore } from '../../store/auth'

const auth = useAuthStore()
const pageLoading = ref(false)
const overview = ref({})
const subjects = ref([])
const reminders = ref([])
const recentSubmissions = ref([])
const ranking = ref({ list: [] })
const weakKnowledge = ref([])
const errorTypes = ref([])
const taskRef = ref(null)

const studentName = computed(() => auth.userInfo?.realName || auth.userInfo?.username || '同学')

async function loadDashboard() {
  pageLoading.value = true
  try {
    const [overviewRes, subjectsRes, remindersRes, recentRes, rankingRes, weakRes, errorRes] = await Promise.all([
      dashboardApi.studentOverview(),
      dashboardApi.studentSubjects(),
      dashboardApi.studentReminders(),
      dashboardApi.studentRecentSubmissions(),
      dashboardApi.studentRanking({ range: 'WEEK' }),
      reportApi.studentKnowledge(),
      reportApi.studentErrorTypes()
    ])
    overview.value = overviewRes || {}
    subjects.value = subjectsRes || []
    reminders.value = remindersRes || []
    recentSubmissions.value = recentRes || []
    ranking.value = rankingRes || { list: [] }
    weakKnowledge.value = (weakRes || []).filter((item) => Number(item.wrongCount || item.wrongRate || 0) > 0).slice(0, 6)
    errorTypes.value = errorRes || []
  } finally {
    pageLoading.value = false
  }
}

function scrollToReminders() {
  taskRef.value?.scrollIntoView?.()
}

onMounted(loadDashboard)
</script>

<style scoped>
.student-dashboard-page {
  min-height: 100%;
  padding: 24px;
  overflow-x: hidden;
  background: #f8fafc;
}

.dashboard-shell {
  display: grid;
  grid-template-columns: minmax(720px, 1fr) 340px;
  gap: 24px;
  max-width: 1440px;
  margin: 0 auto;
}

.dashboard-main,
.dashboard-sidebar {
  display: grid;
  align-content: start;
  gap: 24px;
  min-width: 0;
}

.dashboard-sidebar {
  width: 340px;
}

.dashboard-bottom {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(300px, 0.75fr);
  gap: 24px;
  min-width: 0;
}

:deep(.el-icon),
:deep(.el-icon svg) {
  width: 1em;
  height: 1em;
}

@media (max-width: 1280px) {
  .dashboard-shell {
    grid-template-columns: 1fr;
  }

  .dashboard-sidebar {
    width: auto;
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 920px) {
  .dashboard-bottom,
  .dashboard-sidebar {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .student-dashboard-page {
    padding: 16px;
  }
}
</style>
