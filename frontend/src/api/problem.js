import request from './request'

export const problemApi = {
  list(params) {
    return request.get('/problem/list', { params })
  },
  detail(id) {
    return request.get(`/problem/detail/${id}`)
  },
  add(data) {
    return request.post('/problem/add', data)
  },
  update(data) {
    return request.put('/problem/update', data)
  },
  delete(id) {
    return request.delete(`/problem/delete/${id}`)
  }
}
