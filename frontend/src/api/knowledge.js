import request from './request'

export const knowledgeApi = {
  listBases(params) {
    return request.get('/knowledge-base/list', { params })
  },
  baseDetail(id) {
    return request.get(`/knowledge-base/detail/${id}`)
  },
  addBase(data) {
    return request.post('/knowledge-base/add', data)
  },
  updateBase(data) {
    return request.put('/knowledge-base/update', data)
  },
  deleteBase(id) {
    return request.delete(`/knowledge-base/delete/${id}`)
  },
  listDocuments(params) {
    return request.get('/knowledge-document/list', { params })
  },
  documentDetail(id) {
    return request.get(`/knowledge-document/detail/${id}`)
  },
  addDocument(data) {
    return request.post('/knowledge-document/add', data)
  },
  updateDocument(data) {
    return request.put('/knowledge-document/update', data)
  },
  deleteDocument(id) {
    return request.delete(`/knowledge-document/delete/${id}`)
  },
  chunkDocument(id) {
    return request.post(`/knowledge-document/chunk/${id}`)
  },
  listChunks(documentId) {
    return request.get(`/knowledge-document/chunks/${documentId}`)
  }
}
