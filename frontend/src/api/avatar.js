import request from './request'

export const avatarApi = {
  getStudentTutor() {
    return request.get('/avatars/student-tutor')
  }
}
