import api from './base'

export const appealApi = {
  getBanInfo: (username: string) =>
    api.get('/appeal/ban-info', { params: { username } }),

  submitAppeal: (data: { username: string; content: string }) =>
    api.post('/appeal/submit', data)
}
