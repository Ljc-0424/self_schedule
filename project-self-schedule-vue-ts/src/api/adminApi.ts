import api from './base'

export const adminApi = {
  getFeedbacks: (params: { pageNum?: number; pageSize?: number; status?: number | null }) =>
    api.get('/admin/feedbacks', { params }),

  replyFeedback: (id: number, data: { status?: number; adminReply?: string }) =>
    api.put(`/admin/feedbacks/${id}/reply`, data),

  getUserOnline: () =>
    api.get('/admin/users/online'),

  getUsers: (params: { keyword?: string; pageNum?: number; pageSize?: number }) =>
    api.get('/admin/users', { params }),

  getFeedbackStats: () =>
    api.get('/admin/feedbacks/stats'),

  getPendingFeedbackCount: () =>
    api.get('/admin/feedbacks/pending-count'),

  banUser: (id: number, data: { banType: number; banReason: string; banEndTime?: string }) =>
    api.post(`/admin/users/${id}/ban`, data),

  unbanUser: (id: number) =>
    api.put(`/admin/users/${id}/unban`),

  getAppeals: (params: { pageNum?: number; pageSize?: number; status?: number | null }) =>
    api.get('/appeal/admin/list', { params }),

  auditAppeal: (id: number, data: { status: number; auditNote?: string }) =>
    api.put(`/appeal/admin/audit/${id}`, data),

  getPendingAppealCount: () =>
    api.get('/appeal/admin/pending-count'),

  sendMessage: (data: { recipientId: number; title?: string; content: string }) =>
    api.post('/admin/messages/send', data),

  broadcastMessage: (data: { title?: string; content: string }) =>
    api.post('/admin/messages/broadcast', data)
}
