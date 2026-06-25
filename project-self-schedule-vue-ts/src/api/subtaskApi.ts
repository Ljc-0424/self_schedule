import api from './base'
import type { SubtaskVO } from '../types'

export const subtaskApi = {
  create(taskId: number, title: string) {
    const params = new URLSearchParams()
    params.append('taskId', taskId.toString())
    params.append('title', title)
    return api.post('/subtasks', params)
  },
  
  update(id: number, title: string) {
    return api.put(`/subtasks/${id}`, { title })
  },
  
  delete(id: number) {
    return api.delete(`/subtasks/${id}`)
  },
  
  complete(id: number) {
    return api.put(`/subtasks/${id}/complete`)
  },
  
  getByTaskId(taskId: number) {
    return api.get(`/subtasks/task/${taskId}`)
  },
  
  getCount(taskId: number) {
    return api.get(`/subtasks/task/${taskId}/count`)
  }
}

export type { SubtaskVO }
