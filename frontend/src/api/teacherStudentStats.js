import request from './request'

export const teacherStudentStatsApi = {
  overview() {
    return request.get('/teacher/student-stats/overview')
  },
  list(params) {
    return request.get('/teacher/student-stats', { params })
  },
  profile(studentId) {
    return request.get(`/teacher/students/${studentId}/profile`)
  }
}
