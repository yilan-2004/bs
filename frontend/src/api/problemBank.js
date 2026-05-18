import request from './request'

export const problemBankApi = {
  list(params) {
    return request.get('/problem-bank/list', { params })
  },
  detail(id) {
    return request.get(`/problem-bank/detail/${id}`)
  },
  problems(bankId, params) {
    return request.get(`/problem-bank/problems/${bankId}`, { params })
  },
  add(data) {
    return request.post('/problem-bank/add', data)
  },
  update(data) {
    return request.put('/problem-bank/update', data)
  },
  delete(id) {
    return request.delete(`/problem-bank/delete/${id}`)
  }
}
