import api from './base'

export const messageApi = {
  getMyMessages: () =>
    api.get('/messages'),

  getUnreadCount: () =>
    api.get('/messages/unread-count'),

  markAsRead: (id: number) =>
    api.put(`/messages/${id}/read`),

  markAllAsRead: () =>
    api.put('/messages/mark-all-read')
}
