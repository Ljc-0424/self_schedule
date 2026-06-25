<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { reminderApi } from '../api'
import { ElNotification } from 'element-plus'
import type { ReminderRecordVO } from '../types'

const notifications = ref<ReminderRecordVO[]>([])
const isVisible = ref(false)
let checkInterval: number | null = null
let eventSource: EventSource | null = null
let reconnectTimer: number | null = null
const isMuted = ref(localStorage.getItem('reminder-muted') === 'true')
let audioCtx: AudioContext | null = null
const tokenRef = ref(localStorage.getItem('token'))

watch(tokenRef, (newToken, oldToken) => {
  if (newToken !== oldToken) {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    if (newToken) {
      connectSSE()
    }
  }
})

window.addEventListener('storage', (e) => {
  if (e.key === 'token') {
    tokenRef.value = e.newValue
  }
})

const shownReminderIds = new Set<number>()
const dismissedIds = new Set<number>()

const toggleMute = () => {
  isMuted.value = !isMuted.value
  localStorage.setItem('reminder-muted', String(isMuted.value))
}

const playNotificationSound = () => {
  if (isMuted.value) return
  try {
    if (!audioCtx) {
      audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)()
    }
    const ctx = audioCtx
    const now = ctx.currentTime

    const notes = [880, 1108.73, 1318.51]
    notes.forEach((freq, i) => {
      const osc = ctx.createOscillator()
      const gain = ctx.createGain()
      osc.type = 'sine'
      osc.frequency.value = freq
      gain.gain.setValueAtTime(0, now + i * 0.15)
      gain.gain.linearRampToValueAtTime(0.3, now + i * 0.15 + 0.02)
      gain.gain.exponentialRampToValueAtTime(0.001, now + i * 0.15 + 0.25)
      osc.connect(gain)
      gain.connect(ctx.destination)
      osc.start(now + i * 0.15)
      osc.stop(now + i * 0.15 + 0.25)
    })
  } catch {}
}

const isLoggedIn = () => {
  return localStorage.getItem('token') !== null
}

const fetchPendingReminders = async () => {
  if (!isLoggedIn()) {
    return
  }

  try {
    const response = await reminderApi.getPendingReminders()
    if (response.data.code === 200 && response.data.data) {
      const now = new Date()
      const freshReminders = response.data.data.filter((r: ReminderRecordVO) => {
        const triggerTime = new Date(r.triggerTime!)
        const diffMinutes = (triggerTime.getTime() - now.getTime()) / (1000 * 60)
        const withinWindow = diffMinutes >= -2 && diffMinutes <= 5
        const notDismissed = !dismissedIds.has(r.id!)
        const notAlreadyShown = !shownReminderIds.has(r.id!)
        return withinWindow && notDismissed && notAlreadyShown
      })

      if (freshReminders.length > 0) {
        freshReminders.forEach((r: ReminderRecordVO) => shownReminderIds.add(r.id!))
        notifications.value = [...notifications.value, ...freshReminders]
        isVisible.value = true
        playNotificationSound()
      }
    }
  } catch (error: any) {
    if (error.response?.status === 401) {
      if (checkInterval) {
        clearInterval(checkInterval)
        checkInterval = null
      }
    }
  }
}

const closeNotification = async () => {
  const idsToRead = notifications.value.map(n => n.id!)
  for (const id of idsToRead) {
    dismissedIds.add(id)
  }
  isVisible.value = false
  notifications.value = []

  for (const id of idsToRead) {
    try {
      await reminderApi.updateReminderStatus(id, 'READ')
      shownReminderIds.delete(id)
    } catch {}
  }
}

const markAsRead = async (reminderId: number) => {
  try {
    await reminderApi.updateReminderStatus(reminderId, 'READ')
    dismissedIds.add(reminderId)
    shownReminderIds.delete(reminderId)
    notifications.value = notifications.value.filter(n => n.id !== reminderId)
    if (notifications.value.length === 0) {
      isVisible.value = false
    }
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const markAllAsRead = async () => {
  const ids = notifications.value.map(n => n.id!)
  ids.forEach(id => {
    dismissedIds.add(id)
    shownReminderIds.delete(id)
  })
  notifications.value = []
  isVisible.value = false

  try {
    await reminderApi.markAllAsRead()
  } catch (error) {
    console.error('批量标记已读失败:', error)
  }
}

const connectSSE = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }

  const token = localStorage.getItem('token')
  if (!token) return

  try {
    eventSource = new EventSource(`/api/sse/connect?token=${encodeURIComponent(token)}`)

    eventSource.addEventListener('reminder', (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.id && !shownReminderIds.has(data.id) && !dismissedIds.has(data.id)) {
          shownReminderIds.add(data.id)
          const reminder: ReminderRecordVO = {
            id: data.id,
            taskId: data.taskId,
            taskTitle: data.taskTitle,
            triggerTime: data.triggerTime,
            taskTime: data.taskTime,
            reminderType: data.reminderType,
            message: data.message,
            status: 'SENT',
            statusLabel: '已发送'
          }
          notifications.value = [...notifications.value, reminder]
          isVisible.value = true
          playNotificationSound()
        }
      } catch (e) {
        console.error('解析SSE提醒数据失败:', e)
      }
    })

    eventSource.addEventListener('ai_task_created', (event) => {
      try {
        const data = JSON.parse(event.data)
        const taskTitles = (data.tasks || []).map((t: any) => t.title).filter(Boolean)
        ElNotification({
          title: 'AI 任务创建成功',
          message: `已创建 ${data.taskCount || 0} 个任务${taskTitles.length > 0 ? '：' + taskTitles.join('、') : ''}`,
          type: 'success',
          duration: 5000,
          position: 'top-right'
        })
      } catch (e) {
        console.error('解析AI任务创建通知失败:', e)
      }
    })

    eventSource.onerror = () => {
      eventSource?.close()
      eventSource = null
      if (!reconnectTimer && localStorage.getItem('token')) {
        reconnectTimer = window.setTimeout(() => {
          reconnectTimer = null
          connectSSE()
        }, 5000)
      }
    }
  } catch {
    startPollingFallback()
  }
}

const startPollingFallback = () => {
  fetchPendingReminders()
  if (!checkInterval) {
    checkInterval = window.setInterval(fetchPendingReminders, 30000)
  }
}

onMounted(() => {
  connectSSE()
  fetchPendingReminders()
  checkInterval = window.setInterval(fetchPendingReminders, 60000)
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
  if (checkInterval) {
    clearInterval(checkInterval)
    checkInterval = null
  }
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
})
</script>

<template>
  <Transition name="slide-down">
    <div v-if="isVisible && notifications.length > 0" class="reminder-notification">
      <div class="notification-header">
        <div class="notification-title">
          <span class="bell-icon"></span>
          <span>提醒通知</span>
          <span class="badge">{{ notifications.length }}</span>
        </div>
        <button class="mute-btn" @click="toggleMute" :title="isMuted ? '开启提示音' : '关闭提示音'">
          {{ isMuted ? '' : '' }}
        </button>
        <button class="close-btn" @click="closeNotification">×</button>
      </div>
      
      <div class="notification-content">
        <div 
          v-for="notification in notifications" 
          :key="notification.id" 
          class="notification-item"
        >
          <div class="notification-type">
            {{ notification.reminderType === 'DEADLINE' ? '截止提醒' : '任务提醒' }}
          </div>
          <div class="notification-message">{{ notification.message }}</div>
          <div class="notification-time">
            {{ new Date(notification.triggerTime!).toLocaleString('zh-CN') }}
          </div>
          <button class="mark-read-btn" @click="markAsRead(notification.id!)">
            知道了
          </button>
        </div>
      </div>
      
      <div class="notification-footer">
        <button class="mark-all-btn" @click="markAllAsRead">
          全部标为已读
        </button>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.3s ease;
}

.slide-down-enter-from,
.slide-down-leave-to {
  transform: translateY(-100%);
  opacity: 0;
}

.reminder-notification {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  background: var(--primary);
  color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  border-radius: 0 0 16px 16px;
  overflow: hidden;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.bell-icon {
  font-size: 20px;
}

.badge {
  background: rgba(255, 255, 255, 0.3);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  opacity: 1;
}

.mute-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  opacity: 0.8;
  transition: opacity 0.2s;
  padding: 0;
  line-height: 1;
  margin-right: 8px;
}

.mute-btn:hover {
  opacity: 1;
}

.notification-content {
  padding: 16px 20px;
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 14px;
  margin-bottom: 12px;
  position: relative;
}

.notification-item:last-child {
  margin-bottom: 0;
}

.notification-type {
  font-size: 12px;
  opacity: 0.8;
  margin-bottom: 6px;
}

.notification-message {
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.4;
}

.notification-time {
  font-size: 12px;
  opacity: 0.7;
  margin-bottom: 10px;
}

.mark-read-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.mark-read-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.notification-footer {
  padding: 12px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  text-align: right;
}

.mark-all-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.mark-all-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
