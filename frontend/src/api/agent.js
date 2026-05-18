import request from './request'

export const agentApi = {
  ask(data) {
    return request.post('/agent/student/ask', data)
  }
}
