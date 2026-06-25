import api from './base'

export const feedbackApi = {
  create: (data: { category: string; title: string; content: string; contact?: string }) =>
    api.post('/feedback', data),

  getMyFeedbacks: () =>
    api.get('/feedback/my'),

  getUnreadCount: () =>
    api.get('/feedback/unread-count'),

  markAllAsRead: () =>
    api.put('/feedback/mark-read')
}
