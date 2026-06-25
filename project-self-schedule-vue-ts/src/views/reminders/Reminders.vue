<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import PageLayout from '../../components/PageLayout.vue'
import Pagination from '../../components/Pagination.vue'
import ReminderStatsBar from './components/ReminderStatsBar.vue'
import ReminderFilterBar from './components/ReminderFilterBar.vue'
import ReminderList from './components/ReminderList.vue'
import { reminderApi } from '../../api/reminderApi'

interface ReminderVO {
  id: number
  taskId: number
  taskTitle: string
  triggerTime: string
  taskTime: string
  reminderType: string
  reminderTypeLabel: string
  status: string
  statusLabel: string
  message: string
  createdAt: string
}

interface ReminderStats {
  total: number
  pending: number
  sent: number
  read: number
  dismissed: number
  expired: number
}

const reminders = ref<ReminderVO[]>([])
const isLoading = ref(false)
const stats = ref<ReminderStats>({ total: 0, pending: 0, sent: 0, read: 0, dismissed: 0, expired: 0 })
const currentPage = ref(1)
const totalPages = ref(1)
const totalItems = ref(0)
const currentStatus = ref<string>('ALL')

const statsBarRef = ref<InstanceType<typeof ReminderStatsBar> | null>(null)

const fetchReminders = async (page: number = 1) => {
  isLoading.value = true
  try {
    let response
    if (currentStatus.value === 'ALL') {
      response = await reminderApi.getAllReminders(page, 10)
    } else {
      response = await reminderApi.getRemindersByStatus(currentStatus.value, page, 10)
    }

    if (response.data && response.data.code === 200 && response.data.data) {
      const pageResult = response.data.data
      reminders.value = pageResult.data || []
      currentPage.value = pageResult.pageNum || page
      totalPages.value = pageResult.pages || 1
      totalItems.value = pageResult.total || 0
    }
  } catch (e) {
    ElMessage.error('获取提醒列表失败')
  }
  isLoading.value = false
}

const fetchStats = async () => {
  try {
    const response = await reminderApi.getReminderStats()
    if (response.data && response.data.code === 200 && response.data.data) {
      stats.value = response.data.data
    }
  } catch (error) {
    // silent
  }
}

const cleanupCompleted = async () => {
  if (!confirm('确定要清理所有已读、已忽略和已过期的提醒记录吗？')) return
  try {
    await reminderApi.cleanupCompleted()
    await fetchReminders(1)
    await fetchStats()
    ElMessage.success('清理完成')
  } catch (error) {
    ElMessage.error('清理失败')
  }
}

const changeStatusFilter = (status: string) => {
  currentStatus.value = status
  currentPage.value = 1
  fetchReminders(1)
}

const updateStatus = async (id: number, status: string) => {
  const idx = reminders.value.findIndex(r => r.id === id)
  if (idx === -1) return

  const prevStatus = reminders.value[idx].status
  reminders.value[idx] = { ...reminders.value[idx], status, statusLabel: status === 'READ' ? '已读' : status === 'DISMISSED' ? '已忽略' : reminders.value[idx].statusLabel }

  try {
    await reminderApi.updateReminderStatus(id, status)
    fetchStats()
  } catch (error) {
    reminders.value[idx] = { ...reminders.value[idx], status: prevStatus, statusLabel: prevStatus === 'READ' ? '已读' : prevStatus === 'DISMISSED' ? '已忽略' : reminders.value[idx].statusLabel }
    ElMessage.error('更新状态失败')
  }
}

const markAllAsRead = async () => {
  const prevStatuses = reminders.value.map(r => ({ status: r.status, statusLabel: r.statusLabel }))
  reminders.value.forEach(r => {
    if (r.status !== 'READ') {
      r.status = 'READ'
      r.statusLabel = '已读'
    }
  })

  try {
    await reminderApi.markAllAsRead()
    fetchStats()
  } catch (error) {
    reminders.value.forEach((r, i) => {
      r.status = prevStatuses[i].status
      r.statusLabel = prevStatuses[i].statusLabel
    })
    ElMessage.error('批量标记已读失败')
  }
}

const deleteReminder = async (id: number) => {
  if (!confirm('确定要删除这个提醒吗？')) return

  const prev = [...reminders.value]
  reminders.value = reminders.value.filter(r => r.id !== id)

  try {
    await reminderApi.deleteReminder(id)
    fetchStats()
  } catch (error) {
    reminders.value = prev
    ElMessage.error('删除失败')
  }
}

const triggerScan = async () => {
  try {
    await reminderApi.triggerScan()
    ElMessage.success('提醒扫描已触发')
    await fetchReminders(currentPage.value)
  } catch (error) {
    ElMessage.error('触发扫描失败')
  }
}

const handlePageChange = (page: number) => {
  fetchReminders(page)
}

const formatDateTime = (dateStr: string): string => {
  return new Date(dateStr).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatRelativeTime = (triggerTimeStr: string): string => {
  const now = new Date()
  const triggerTime = new Date(triggerTimeStr)
  const diff = triggerTime.getTime() - now.getTime()

  if (diff >= 0) {
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)
    const weeks = Math.floor(diff / 604800000)

    if (minutes < 1) return '即将触发'
    if (minutes < 60) return `${minutes}分钟后`
    if (hours < 24) return `${hours}小时后`
    if (days < 7) return `${days}天后`
    if (weeks < 4) return `${weeks}周后`
    return triggerTime.toLocaleDateString('zh-CN')
  }

  const absDiff = Math.abs(diff)
  const minutes = Math.floor(absDiff / 60000)
  const hours = Math.floor(absDiff / 3600000)
  const days = Math.floor(absDiff / 86400000)

  if (minutes < 1) return '刚刚触发'
  if (minutes < 60) return `${minutes}分钟前触发`
  if (hours < 24) return `${hours}小时前触发`
  if (days < 7) return `${days}天前触发`
  return triggerTime.toLocaleDateString('zh-CN')
}

const getDisplayTime = (reminder: ReminderVO): string => {
  return formatRelativeTime(reminder.triggerTime)
}

onMounted(() => {
  fetchReminders()
  fetchStats()
})

onUnmounted(() => {
})
</script>

<template>
  <PageLayout title="提醒中心" subtitle="管理您的任务提醒">
    <div class="reminders-page">
      <div class="reminders-header">
        <ReminderStatsBar
          :stats="stats"
          @mark-all-as-read="markAllAsRead"
          @trigger-scan="triggerScan"
          @cleanup-completed="cleanupCompleted"
        />

        <ReminderFilterBar
          :current-status="currentStatus"
          :stats="stats"
          @status-change="changeStatusFilter"
        />
      </div>

      <div class="reminders-body">
        <ReminderList
          :reminders="reminders"
          :is-loading="isLoading"
          :current-status="currentStatus"
          @update-status="updateStatus"
          @delete="deleteReminder"
          :format-date-time="formatDateTime"
          :get-display-time="getDisplayTime"
        />
        <div class="reminders-body-spacer"></div>
      </div>

      <div v-if="totalItems > 0" class="reminders-footer">
        <Pagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :total-items="totalItems"
          @page-change="handlePageChange"
        />
      </div>
    </div>
  </PageLayout>
</template>

<style scoped>
.reminders-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-width: 1200px;
  margin: 0 auto;
}

.reminders-header {
  flex-shrink: 0;
}

.reminders-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.reminders-body::-webkit-scrollbar {
  width: 5px;
}

.reminders-body::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.reminders-body::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.reminders-body::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--primary-rgb), 0.45);
}

.reminders-body-spacer {
  height: 8px;
}

.reminders-footer {
  flex-shrink: 0;
  padding: 12px 0 20px;
}

@media (max-width: 767px) {
  .reminders-footer {
    padding: 10px 0 16px;
  }
}

@media (max-width: 575px) {
  .reminders-page {
    max-width: 100%;
  }

  .reminders-footer {
    padding: 8px 0 12px;
  }
}
</style>
