import request from './request'

export const dashboardApi = {
  studentOverview() {
    return request.get('/dashboard/student/overview')
  },
  studentSubjects() {
    return request.get('/dashboard/student/subjects')
  },
  studentCalendar(params) {
    return request.get('/dashboard/student/calendar', { params })
  },
  studentDayRecords(params) {
    return request.get('/dashboard/student/day-records', { params })
  },
  studentReminders() {
    return request.get('/dashboard/student/reminders')
  },
  studentRecentSubmissions() {
    return request.get('/dashboard/student/recent-submissions')
  },
  studentRanking(params) {
    return request.get('/dashboard/student/ranking', { params })
  }
}
