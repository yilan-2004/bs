<template>
  <div class="student-page">
    <PageHeader
      title="个人中心"
      subtitle="查看账号信息、学习概览和最近学习时间。"
      eyebrow="Profile"
      :icon="User"
    />

    <section class="profile-grid" v-loading="loading">
      <div class="surface-card profile-card">
        <el-avatar :size="78" class="big-avatar">{{ avatarText }}</el-avatar>
        <h2>{{ displayName }}</h2>
        <p>{{ auth.userInfo?.username }}</p>
        <el-tag type="primary" round>{{ auth.role === 'STUDENT' ? '学生' : auth.role }}</el-tag>
        <el-button class="logout-button" :icon="SwitchButton" @click="logout">退出登录</el-button>
      </div>

      <div class="stat-grid">
        <article v-for="item in stats" :key="item.label" class="stat-card">
          <span class="stat-icon" :class="item.color">
            <el-icon><component :is="item.icon" /></el-icon>
          </span>
          <div>
            <p>{{ item.label }}</p>
            <strong>{{ item.value }}</strong>
            <span>{{ item.sub }}</span>
          </div>
        </article>
      </div>
    </section>

    <section class="surface-card">
      <div class="section-head">
        <h3>最近练习</h3>
        <el-button text type="primary" @click="router.push('/student/submissions')">查看全部</el-button>
      </div>
      <el-table :data="recent" size="large">
        <el-table-column prop="problemTitle" label="题目" min-width="180" />
        <el-table-column prop="bankName" label="题库" min-width="150" />
        <el-table-column label="状态" width="150">
          <template #default="{ row }"><StatusTag :status="row.judgeStatus" /></template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无练习记录" description="开始练习后会在这里显示最近动态。" :icon="Tickets" />
        </template>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ChatLineRound, CircleCheck, DataAnalysis, SwitchButton, Tickets, User, Warning } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardApi } from '../../api/dashboard'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatusTag from '../../components/StatusTag.vue'
import { useAuthStore } from '../../store/auth'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)
const overview = ref({})
const recent = ref([])

const displayName = computed(() => auth.userInfo?.realName || auth.userInfo?.username || '同学')
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
const stats = computed(() => [
  { label: '总练习次数', value: overview.value.submitCount || 0, sub: '累计提交', icon: Tickets, color: 'blue' },
  { label: '正确题数', value: overview.value.acceptedCount || 0, sub: '已通过', icon: CircleCheck, color: 'green' },
  { label: '正确率', value: `${overview.value.accuracyRate || 0}%`, sub: '按提交统计', icon: DataAnalysis, color: 'orange' },
  { label: 'AI 诊断', value: overview.value.aiFeedbackCount || 0, sub: '反馈次数', icon: ChatLineRound, color: 'purple' },
  { label: '错题数', value: overview.value.wrongQuestionCount || 0, sub: '建议复习', icon: Warning, color: 'red' }
])

async function loadData() {
  loading.value = true
  try {
    const [overviewRes, recentRes] = await Promise.all([
      dashboardApi.studentOverview(),
      dashboardApi.studentRecentSubmissions()
    ])
    overview.value = overviewRes || {}
    recent.value = (recentRes || []).slice(0, 6)
  } finally {
    loading.value = false
  }
}

async function logout() {
  await auth.logout()
  router.replace('/login')
}

function formatTime(value) {
  return value ? String(value).replace('T', ' ').slice(0, 16) : '-'
}

onMounted(loadData)
</script>

<style scoped>
.student-page {
  display: grid;
  gap: 24px;
  padding: 24px;
}

.profile-grid {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 24px;
}

.surface-card,
.stat-card {
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.profile-card {
  display: grid;
  place-items: center;
  padding: 28px;
  text-align: center;
}

.big-avatar {
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  font-size: 30px;
  font-weight: 900;
}

.profile-card h2 {
  margin: 16px 0 4px;
  color: #0f172a;
  font-size: 24px;
}

.profile-card p {
  margin: 0 0 14px;
  color: #64748b;
}

.logout-button {
  margin-top: 20px;
  border-radius: 12px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
  min-height: 118px;
  padding: 20px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #fff;
  font-size: 24px;
}

.stat-icon.blue { background: #2563eb; }
.stat-icon.green { background: #10b981; }
.stat-icon.orange { background: #f59e0b; }
.stat-icon.purple { background: #7c3aed; }
.stat-icon.red { background: #ef4444; }

.stat-card p,
.stat-card span {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.stat-card strong {
  display: block;
  margin: 8px 0 6px;
  color: #0f172a;
  font-size: 26px;
}

.surface-card {
  padding: 22px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.section-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
}

@media (max-width: 1100px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
