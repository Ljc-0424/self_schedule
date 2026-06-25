import api from './base'

/** 认证API */
export const authApi = {
  login: (data: { account: string; password: string }) =>
    api.post('/auth/login', data),
  
  register: (data: { username: string; password: string; email: string }) =>
    api.post('/auth/register', data),
  
  logout: () => api.post('/auth/logout'),
  
  checkUsername: (username: string) =>
    api.get('/auth/check-username', { params: { username } }),
  
  checkEmail: (email: string) =>
    api.get('/auth/check-email', { params: { email } }),

  forgotPassword: (data: { email?: string; username?: string }) =>
    api.post('/auth/forgot-password', data),

  verifyCode: (data: { email: string; code: string }) =>
    api.post('/auth/verify-code', data),

  resetPassword: (data: { email: string; code: string; newPassword: string }) =>
    api.post('/auth/reset-password', data),

  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    api.post('/auth/change-password', data)
}