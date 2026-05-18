import request from './request'

export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  },
  info() {
    return request.get('/auth/info')
  },
  logout() {
    return request.post('/auth/logout')
  }
}
