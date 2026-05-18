<template>
  <div class="app-shell" :class="{ 'teacher-shell': auth.role === 'TEACHER' }">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">
          <el-icon><Reading /></el-icon>
        </span>
        <div>
          <strong>{{ auth.role === 'TEACHER' ? 'AgentEdu 教学台' : 'AgentEdu' }}</strong>
          <span>{{ auth.role === 'TEACHER' ? '高校编程教学工作台' : '多学科个性化学习平台' }}</span>
        </div>
      </div>

      <nav class="menu-groups">
        <section v-for="group in groupedMenus" :key="group.title" class="menu-group">
          <p>{{ group.title }}</p>
          <el-menu :default-active="activeMenu" router class="side-menu">
            <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
              <el-icon><component :is="item.icon" /></el-icon>
              <span>{{ item.label }}</span>
            </el-menu-item>
          </el-menu>
        </section>
      </nav>

      <div v-if="auth.role !== 'TEACHER'" class="profile-card">
        <div class="mini-avatar">{{ avatarText }}</div>
        <div>
          <strong>{{ displayName }}</strong>
          <span>学生</span>
        </div>
        <el-progress :percentage="levelProgress" :show-text="false" :stroke-width="7" />
      </div>
    </aside>

    <main class="main-shell">
      <header v-if="showTopbar" class="topbar">
        <div class="hello">
          <strong>你好，{{ displayName }}</strong>
          <span>{{ auth.role === 'TEACHER' ? '管理教学内容，关注学生学习表现' : '坚持学习，未来可期' }}</span>
        </div>
        <div class="user-actions">
          <el-avatar class="avatar">{{ avatarText }}</el-avatar>
          <div class="user-meta">
            <strong>{{ displayName }}</strong>
            <span>{{ roleText }}</span>
          </div>
          <el-button :icon="SwitchButton" round @click="handleLogout">退出</el-button>
        </div>
      </header>

      <section class="content-shell">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup>
import {
  Aim,
  ChatDotRound,
  Collection,
  DataAnalysis,
  DataBoard,
  Document,
  Files,
  List,
  Notebook,
  Reading,
  SwitchButton,
  Tickets,
  TrendCharts,
  User
} from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()

const studentGroups = [
  {
    title: '学习中心',
    items: [
      { path: '/student/dashboard', label: '首页', icon: DataBoard },
      { path: '/student/banks', label: '题库练习', icon: Collection },
      { path: '/student/submissions', label: '错题本', icon: Tickets },
      { path: '/student/report', label: '学习报告', icon: TrendCharts },
      { path: '/student/knowledge-map', label: '知识点图谱', icon: Aim },
      { path: '/student/study-plan', label: '学习计划', icon: Notebook },
      { path: '/student/ai-tutor', label: 'AI 助教', icon: ChatDotRound },
      { path: '/student/profile', label: '个人中心', icon: User }
    ]
  }
]

const teacherGroups = [
  {
    title: '教师专区',
    items: [
      { path: '/teacher/dashboard', label: '教学首页', icon: DataBoard },
      { path: '/teacher/subjects', label: '学科管理', icon: Collection },
      { path: '/teacher/banks', label: '题库管理', icon: Collection },
      { path: '/teacher/problems', label: '题目管理', icon: Notebook },
      { path: '/teacher/testcases', label: '测试用例', icon: List },
      { path: '/teacher/submits', label: '提交记录', icon: Document },
      { path: '/teacher/analysis', label: '教学分析', icon: DataAnalysis },
      { path: '/teacher/knowledge-bases', label: '知识库', icon: Files }
    ]
  }
]

const groupedMenus = computed(() => auth.role === 'TEACHER' ? teacherGroups : studentGroups)
const roleText = computed(() => auth.role === 'TEACHER' ? '教师' : '学生')
const displayName = computed(() => auth.userInfo?.realName || auth.userInfo?.username || '同学')
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
const levelProgress = computed(() => Math.min(100, Number(auth.userInfo?.acceptedCount || 0) * 10 + 30))
const showTopbar = computed(() => !(auth.role === 'STUDENT' && route.path === '/student/dashboard'))
const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/student/bank')) return '/student/banks'
  if (path.startsWith('/student/problem')) return '/student/banks'
  if (path.startsWith('/teacher/testcases')) return '/teacher/testcases'
  if (path.startsWith('/teacher/problem/')) return '/teacher/problems'
  if (path.startsWith('/teacher/knowledge')) return '/teacher/knowledge-bases'
  return path
})

async function handleLogout() {
  await auth.logout()
  router.replace('/login')
}
</script>

<style scoped>
.app-shell {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  min-height: 100vh;
  background: #f8fafc;
}

.sidebar {
  position: sticky;
  top: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 22px 18px;
  border-right: 1px solid #e5eaf2;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 10px 0 36px rgba(15, 23, 42, 0.04);
}

.brand {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  margin-bottom: 26px;
}

.brand-mark {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  font-size: 24px;
  box-shadow: 0 14px 26px rgba(37, 99, 235, 0.24);
}

.brand strong {
  display: block;
  color: #0f172a;
  font-size: 20px;
  font-weight: 900;
}

.brand span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 12px;
}

.menu-groups {
  flex: 1;
  overflow-y: auto;
}

.menu-group + .menu-group {
  margin-top: 20px;
}

.menu-group p {
  margin: 0 0 8px 10px;
  color: #94a3b8;
  font-size: 12px;
  font-weight: 900;
}

.side-menu {
  border-right: 0 !important;
  background: transparent !important;
}

.side-menu :deep(.el-menu-item) {
  height: 46px;
  margin: 6px 0;
  border-radius: 14px;
  color: #475569;
  font-weight: 800;
}

.side-menu :deep(.el-menu-item:hover) {
  color: #2563eb;
  background: #eff6ff;
}

.side-menu :deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.24);
}

.side-menu :deep(.el-icon) {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  font-size: 20px;
}

.profile-card {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 10px;
  padding: 16px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: linear-gradient(180deg, #fff, #f8fbff);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.mini-avatar {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 14px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  font-weight: 900;
}

.profile-card strong,
.profile-card span {
  display: block;
}

.profile-card strong {
  color: #0f172a;
  font-size: 14px;
}

.profile-card span {
  color: #64748b;
  font-size: 12px;
}

.profile-card :deep(.el-progress) {
  grid-column: 1 / -1;
}

.main-shell {
  min-width: 0;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 18px 24px;
  border-bottom: 1px solid #e5eaf2;
  background: rgba(248, 250, 252, 0.88);
  backdrop-filter: blur(18px);
}

.hello strong {
  display: block;
  color: #0f172a;
  font-size: 20px;
  font-weight: 900;
}

.hello span {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  font-weight: 900;
}

.user-meta strong,
.user-meta span {
  display: block;
}

.user-meta strong {
  color: #0f172a;
  font-size: 14px;
}

.user-meta span {
  color: #64748b;
  font-size: 12px;
}

.content-shell {
  min-height: 100vh;
}

@media (max-width: 1080px) {
  .app-shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: relative;
    height: auto;
  }

  .profile-card {
    display: none;
  }
}
</style>
