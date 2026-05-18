import request from './request'

export const aiApi = {
  feedback(submitId) {
    return request.post(`/ai/feedback/${submitId}`)
  },
  detail(submitId) {
    return request.get(`/ai/feedback/${submitId}`)
  }
}
