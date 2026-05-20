<template>
  <div class="page student-detail-page" v-loading="loading">
    <PageHeader
      title="学生学习详情"
      subtitle="查看该学生在你创建题目范围内的练习、错误类型和近期提交"
      eyebrow="Student Profile"
      :icon="User"
    >
      <template #actions>
        <el-button @click="router.push('/teacher/students')">返回列表</el-button>
        <el-button type="primary" @click="router.push(`/teacher/submissions?studentId=${studentId}`)">查看提交明细</el-button>
      </template>
    </PageHeader>

    <section v-if="profile" class="profile-card">
      <div class="student-hero">
        <el-avatar :size="58">{{ (profile.studentName || profile.username || '?').slice(0, 1) }}</el-avatar>
        <div>
          <h2>{{ profile.studentName || profile.username }}</h2>
          <p>ID {{ profile.studentId }} · {{ profile.username }}</p>
        </div>
      </div>
      <div class="summary-grid">
        <StatCard :icon="TrendCharts" title="提交次数" :value="profile.submitCount || 0" type="blue" />
        <StatCard :icon="CircleCheck" title="通过次数" :value="profile.acceptedCount || 0" type="green" />
        <StatCard :icon="CircleClose" title="错误次数" :value="profile.wrongCount || 0" type="red" />
        <StatCard :icon="DataAnalysis" title="正确率" :value="`${profile.accuracyRate || 0}%`" type="cyan" />
        <StatCard :icon="ChatDotRound" title="AI诊断" :value="profile.aiFeedbackCount || 0" type="purple" />
        <StatCard :icon="Coin" title="缓存命中" :value="profile.cacheHitCount || 0" type="orange" />
      </div>
    </section>

    <section class="detail-grid">
      <div class="surface-card">
        <h3>薄弱知识点</h3>
        <div v-if="profile?.weakKnowledgeTags?.length" class="tag-list">
          <el-tag v-for="tag in profile.weakKnowledgeTags" :key="tag" round>{{ tag }}</el-tag>
        </div>
        <EmptyState v-else title="暂无明显薄弱点" description="该学生暂未产生足够错误样本。" :icon="DataAnalysis" />
      </div>
      <div class="surface-card">
        <h3>错误类型分布</h3>
        <el-table v-if="profile?.errorTypeDistribution?.length" :data="profile.errorTypeDistribution" size="small">
          <el-table-column prop="errorType" label="错误类型" />
          <el-table-column prop="count" label="次数" width="90" />
        </el-table>
        <EmptyState v-else title="暂无错误类型" description="该学生当前没有错误诊断数据。" :icon="CircleCheck" />
      </div>
    </section>

    <section class="surface-card">
      <h3>最近提交</h3>
      <el-table :data="profile?.recentSubmissions || []" size="large">
        <el-table-column prop="problemTitle" label="题目" min-width="220" />
        <el-table-column prop="bankName" label="题库" min-width="160" />
        <el-table-column prop="judgeStatus" label="状态" width="160">
          <template #default="{ row }"><StatusTag :status="row.judgeStatus" /></template>
        </el-table-column>
        <el-table-column label="通过用例" width="110">
          <template #default="{ row }">{{ row.passCount || 0 }} / {{ row.totalCount || 0 }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="190" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button text type="primary" @click="router.push(`/teacher/submissions?studentId=${studentId}&problemId=${row.problemId}`)">查看明细</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无最近提交" description="该学生还没有提交当前教师创建的题目。" :icon="TrendCharts" />
        </template>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { ChatDotRound, CircleCheck, CircleClose, Coin, DataAnalysis, TrendCharts, User } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { teacherStudentStatsApi } from '../../api/teacherStudentStats'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatCard from '../../components/StatCard.vue'
import StatusTag from '../../components/StatusTag.vue'

const route = useRoute()
const router = useRouter()
const studentId = route.params.studentId
const loading = ref(false)
const profile = ref(null)

async function loadProfile() {
  loading.value = true
  try {
    profile.value = await teacherStudentStatsApi.profile(studentId)
  } finally {
    loading.value = false
  }
}

onMounted(loadProfile)
</script>

<style scoped>
.student-detail-page {
  display: grid;
  gap: 22px;
  padding: 24px;
  background: #f8fafc;
}

.profile-card,
.surface-card {
  padding: 20px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 18px 44px rgba(15, 23, 42, 0.06);
}

.student-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}

.student-hero h2 {
  margin: 0;
  color: #0f172a;
  font-size: 24px;
  font-weight: 950;
}

.student-hero p {
  margin: 4px 0 0;
  color: #64748b;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 14px;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1.1fr);
  gap: 18px;
}

.surface-card h3 {
  margin: 0 0 14px;
  color: #0f172a;
  font-size: 18px;
  font-weight: 950;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
