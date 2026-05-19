import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/login/Login.vue'), meta: { public: true } },
  { path: '/register', component: () => import('../views/login/Register.vue'), meta: { public: true } },
  {
    path: '/student',
    component: () => import('../layout/AppLayout.vue'),
    meta: { role: 'STUDENT' },
    children: [
      { path: 'dashboard', component: () => import('../views/student/StudentDashboard.vue') },
      { path: 'banks', component: () => import('../views/student/BankList.vue') },
      { path: 'bank/:id', component: () => import('../views/student/BankDetail.vue') },
      { path: 'problems', component: () => import('../views/student/ProblemList.vue') },
      { path: 'problem/:id', component: () => import('../views/student/ProblemDetail.vue') },
      { path: 'submissions', component: () => import('../views/student/MySubmissions.vue') },
      { path: 'report', component: () => import('../views/student/StudentReport.vue') },
      { path: 'knowledge-map', component: () => import('../views/student/KnowledgeMap.vue') },
      { path: 'study-plan', component: () => import('../views/student/StudyPlan.vue') },
      { path: 'ai-tutor', component: () => import('../views/student/AiTutor.vue') },
      { path: 'profile', component: () => import('../views/student/ProfileCenter.vue') }
    ]
  },
  {
    path: '/teacher',
    component: () => import('../layout/AppLayout.vue'),
    meta: { role: 'TEACHER' },
    children: [
      { path: 'dashboard', component: () => import('../views/teacher/TeacherDashboard.vue') },
      { path: 'subjects', component: () => import('../views/teacher/SubjectManage.vue') },
      { path: 'banks', component: () => import('../views/teacher/BankManage.vue') },
      { path: 'problems', component: () => import('../views/teacher/ProblemManage.vue') },
      { path: 'problem/new', component: () => import('../views/teacher/ProblemEdit.vue') },
      { path: 'problem/:id/edit', component: () => import('../views/teacher/ProblemEdit.vue') },
      { path: 'testcases', component: () => import('../views/teacher/TeacherTestCaseEntry.vue') },
      { path: 'testcases/:problemId', component: () => import('../views/teacher/TestCaseManage.vue') },
      { path: 'submits', component: () => import('../views/teacher/SubmitManage.vue') },
      { path: 'analysis', component: () => import('../views/teacher/TeacherAnalysis.vue') },
      { path: 'knowledge-bases', component: () => import('../views/teacher/TeacherKnowledgeBases.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    if (auth.token && to.path === '/login') {
      return auth.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard'
    }
    return true
  }
  if (!auth.token) {
    return '/login'
  }
  if (!auth.role) {
    await auth.loadUserInfo()
  }
  const requiredRole = to.matched.find((item) => item.meta.role)?.meta.role
  if (requiredRole && auth.role !== requiredRole) {
    return auth.role === 'TEACHER' ? '/teacher/dashboard' : '/student/dashboard'
  }
  return true
})

export default router
