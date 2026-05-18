import request from './request'

export const subjectApi = {
  list(params) {
    return request.get('/subject/list', { params })
  },

  detail(id) {
    return request.get(`/subject/detail/${id}`)
  },

  add(data) {
    return request.post('/subject/add', data)
  },

  update(data) {
    return request.put('/subject/update', data)
  },

  delete(id) {
    return request.delete(`/subject/delete/${id}`)
  }
}
