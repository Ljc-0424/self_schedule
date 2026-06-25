import api from './base'

/** 头像API */
export const avatarApi = {
  upload: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/avatar/upload', formData)
  },
  
  delete: () => api.delete('/avatar/delete')
}