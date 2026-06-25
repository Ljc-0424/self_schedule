import api from './base'

/** 专注记录API */
export const focusRecordApi = {
  searchTasks: (keyword?: string, pageNum: number = 1, pageSize: number = 10) => api.get('/focus-records/task-search', { params: { keyword, pageNum, pageSize } }),
  
  startFocus: (taskId?: number) => api.post('/focus-records/start', {}, { params: { taskId } }),
  
  createRecord: (data: {
    taskId?: number | null
    startTime: string
    endTime?: string | null
    duration: number
    interruptions?: number
    focusScore?: number | null
    notes?: string
    status: number
  }) => api.post('/focus-records', data),
  
  getRecords: (pageNum: number = 1, params?: {
    taskId?: number
    startTimeAfter?: string
    endTimeBefore?: string
  }) => api.get('/focus-records', { params: { ...params, pageNum, pageSize: 10 } }),
  
  getRecordDetail: (id: number) => api.get(`/focus-records/${id}`),
  
  updateNotes: (id: number, notes: string) => api.put(`/focus-records/${id}/notes`, {}, { params: { notes } }),
  
  deleteRecord: (id: number) => api.delete(`/focus-records/${id}`)
}