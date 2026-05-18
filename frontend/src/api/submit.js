import request from './request'

export const submitApi = {
  submitCode(data) {
    return request.post('/submit/code', data)
  },
  submitAnswer(data) {
    return request.post('/submission/submit', data)
  },
  my(params) {
    return request.get('/submit/my', { params })
  },
  detail(id) {
    return request.get(`/submit/detail/${id}`)
  },
  byProblem(problemId, params) {
    return request.get(`/submit/problem/${problemId}`, { params })
  },
  list(params) {
    return request.get('/submit/list', { params })
  }
}
