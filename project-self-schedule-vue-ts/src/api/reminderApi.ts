import request from './base'
import type { ReminderRecordVO } from '../types'

export const reminderApi = {
  /**
   * 获取待处理提醒列表
   */
  getPendingReminders: () => {
    return request.get('/reminders/pending')
  },

  /**
   * 获取所有提醒记录（分页）
   */
  getAllReminders: (page: number = 1, pageSize: number = 10) => {
    return request.get('/reminders/all', {
      params: { page, pageSize }
    })
  },

  /**
   * 获取提醒记录（支持状态筛选，分页）
   */
  getRemindersByStatus: (status: string, page: number = 1, pageSize: number = 10) => {
    return request.get('/reminders', {
      params: { status, page, pageSize }
    })
  },

  /**
   * 更新提醒状态
   */
  updateReminderStatus: (id: number, status: string) => {
    return request.put(`/reminders/${id}/status`, null, {
      params: { status }
    })
  },

  /**
   * 批量标记所有提醒为已读
   */
  markAllAsRead: () => {
    return request.put('/reminders/read-all')
  },

  /**
   * 删除提醒记录
   */
  deleteReminder: (id: number) => {
    return request.delete(`/reminders/${id}`)
  },

  /**
   * 获取提醒统计信息
   */
  getReminderStats: () => {
    return request.get('/reminders/stats')
  },

  /**
   * 手动触发提醒扫描
   */
  triggerScan: () => {
    return request.post('/reminders/scan')
  },

  /**
   * 批量清理已完成的提醒记录
   */
  cleanupCompleted: () => {
    return request.delete('/reminders/cleanup')
  },

  /**
   * 标记所有超时提醒为已过期
   */
  markAsExpired: () => {
    return request.put('/reminders/expire')
  }
}
