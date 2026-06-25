import api from './base'
import type { UserStatsVO } from '../types'

/** 用户API */
export const userApi = {
  getInfo: () => api.get('/user/info'),
  
  updateInfo: (data: { 
    nickname?: string; 
    email?: string; 
    avatarUrl?: string; 
    settings?: string;
    occupation?: string;
    birthday?: string;
    phone?: string;
    gender?: string;
    city?: string;
    hobbies?: string;
    bio?: string;
    weChatWebhookUrl?: string;
    aiApiKey?: string;
    dailyFocusGoal?: number;
    minEffectiveDuration?: number;
  }) =>
    api.put('/user/info', data),

  getStats: () => api.get<{ code: number; data: UserStatsVO }>('/user/stats'),

  getFocusTimeByCategory: (params: { range: string; startDate?: string; endDate?: string }) =>
    api.get('/user/stats/focus-by-category', { params })
}