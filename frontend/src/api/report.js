import request from './request'

export const reportApi = {
  studentOverview: () => request.get('/report/student/overview'),
  studentTrend: () => request.get('/report/student/trend'),
  studentAccuracyTrend: () => request.get('/report/student/accuracy-trend'),
  studentKnowledgeMastery: () => request.get('/report/student/knowledge-mastery'),
  studentKnowledge: () => request.get('/report/student/knowledge'),
  studentErrors: () => request.get('/report/student/errors'),
  studentErrorTypes: () => request.get('/report/student/error-types'),
  studentBankProgress: () => request.get('/report/student/bank-progress'),
  studentRecent: () => request.get('/report/student/recent'),
  studentRecentSubmissions: () => request.get('/report/student/recent-submissions'),
  studentCalendar: (params) => request.get('/report/student/calendar', { params }),
  studentNotifications: () => request.get('/report/student/notifications'),
  studentRanking: () => request.get('/report/student/ranking'),
  studentAiAnalysis: () => request.get('/report/student/ai-analysis'),
  teacherOverview: () => request.get('/report/teacher/overview'),
  teacherQuestionRank: () => request.get('/report/teacher/question-rank'),
  teacherKnowledgeWeakness: () => request.get('/report/teacher/knowledge-weakness'),
  teacherErrorTypes: () => request.get('/report/teacher/error-types'),
  teacherStudentRank: () => request.get('/report/teacher/student-rank'),
  teacherAiUsage: () => request.get('/report/teacher/ai-usage'),
  teacherRecentSubmissions: () => request.get('/report/teacher/recent-submissions'),
  teacherProblemStats: () => request.get('/report/teacher/problem-stats'),
  teacherErrorStats: () => request.get('/report/teacher/error-stats')
}
