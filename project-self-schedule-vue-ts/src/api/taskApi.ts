import api from './base'

/** 任务API */
export const taskApi = {
  getCategories: () => api.get('/tasks/categories'),

  getTasks: (params?: {
    category?: string
    statusList?: number[]
    priorityList?: number[]
    deadlineBefore?: string
    keyword?: string
    pageNum?: number
    pageSize?: number
  }) => api.get('/tasks', { params }),

  getTask: (id: number) => api.get(`/tasks/${id}`),

  createTask: (data: {
    title: string
    description?: string
    priority?: number
    deadline?: string | null
    remindTime?: string | null
    estimatedSeconds?: number
    category?: string
    tags?: string[]
    reminderConfig?: string | null
  }) => api.post('/tasks', data),

  createTaskByAI: (data: { prompt: string }) =>
    api.post('/ai/parse-task', data),

  updateTask: (id: number, data: {
    title?: string
    description?: string
    priority?: number
    status?: number
    deadline?: string | null
    remindTime?: string | null
    estimatedSeconds?: number
    actualSeconds?: number
    category?: string
    tags?: string[]
    reminderConfig?: string | null
  }) => api.put(`/tasks/${id}`, data),

  completeTask: (id: number) => api.put(`/tasks/${id}/complete`),

  undoTask: (id: number) => api.put(`/tasks/${id}/undo`),

  deleteTask: (id: number) => api.delete(`/tasks/${id}`),

  forceDeleteTask: (id: number) => api.delete(`/tasks/${id}/force`)
}