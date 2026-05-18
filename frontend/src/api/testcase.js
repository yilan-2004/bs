import request from './request'

export const testCaseApi = {
  add(data) {
    return request.post('/testcase/add', data)
  },
  update(data) {
    return request.put('/testcase/update', data)
  },
  delete(id) {
    return request.delete(`/testcase/delete/${id}`)
  },
  list(problemId) {
    return request.get(`/testcase/list/${problemId}`)
  },
  sample(problemId) {
    return request.get(`/testcase/sample/${problemId}`)
  }
}
