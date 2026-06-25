<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageLayout from '../../components/PageLayout.vue'
import Loading from '../../components/Loading.vue'
import { messageApi } from '../../api/messageApi'

interface MessageItem {
  id: number
  senderId: number
  recipientId: number
  title: string
  content: string
  messageType: number
  isRead: number
  createdTime: string
  updatedTime: string
}

const messages = ref<MessageItem[]>([])
const loading = ref(false)
const expandedId = ref<number | null>(null)

const messageTypeMap: Record<number, { label: string; icon: string; cls: string }> = {
  1: { label: '系统通知', icon: '📢', cls: 'type-system' },
  2: { label: '管理员私信', icon: '✉️', cls: 'type-private' }
}

const fetchMessages = async () => {
  loading.value = true
  try {
    const res = await messageApi.getMyMessages()
    messages.value = res.data.data
  } catch {
    ElMessage.error('获取消息列表失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleString('zh-CN')
}

const toggleMessage = async (msg: MessageItem) => {
  if (expandedId.value === msg.id) {
    expandedId.value = null
    return
  }
  expandedId.value = msg.id

  if (msg.isRead === 0) {
    const prevIsRead = msg.isRead
    msg.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    try {
      await messageApi.markAsRead(msg.id)
    } catch {
      msg.isRead = prevIsRead
      unreadCount.value += 1
    }
  }
}

const markAllRead = async () => {
  const prevUnread = unreadCount.value
  const prevReadStates = messages.value.map(m => m.isRead)
  messages.value.forEach(m => m.isRead = 1)
  unreadCount.value = 0
  try {
    await messageApi.markAllAsRead()
    ElMessage.success('已全部标记为已读')
  } catch {
    messages.value.forEach((m, i) => m.isRead = prevReadStates[i])
    unreadCount.value = prevUnread
    ElMessage.error('操作失败')
  }
}

const unreadCount = ref(0)
const fetchUnreadCount = async () => {
  try {
    const res = await messageApi.getUnreadCount()
    unreadCount.value = res.data?.data?.count || 0
  } catch {}
}

let pollTimer: ReturnType<typeof setInterval> | null = null

onMounted(() => {
  fetchMessages()
  fetchUnreadCount()
  pollTimer = setInterval(fetchUnreadCount, 60000)
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
})
</script>

<template>
  <PageLayout title="消息中心" subtitle="查看管理员和系统发送的消息">
    <div class="message-page">
      <div class="message-header">
        <div class="header-left">
          <span v-if="unreadCount > 0" class="unread-info">{{ unreadCount }} 条未读</span>
        </div>
        <el-button size="small" type="primary" plain @click="markAllRead" :disabled="unreadCount === 0">
          ✓ 全部已读
        </el-button>
      </div>

      <div class="message-body">
        <Loading v-if="loading" text="加载中..." />

        <template v-else>
          <div v-if="messages.length === 0" class="empty-state">
            <div class="empty-icon"></div>
            <div class="empty-title">暂无消息</div>
            <div class="empty-desc">管理员发送的消息会在这里显示</div>
          </div>

          <div v-else class="message-list">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="message-card"
              :class="{ 'is-unread': msg.isRead === 0, 'is-expanded': expandedId === msg.id }"
              @click="toggleMessage(msg)"
            >
              <div class="message-top">
                <span class="message-type-badge" :class="messageTypeMap[msg.messageType]?.cls">
                  {{ messageTypeMap[msg.messageType]?.icon }}
                  {{ messageTypeMap[msg.messageType]?.label || '消息' }}
                </span>
                <span v-if="msg.isRead === 0" class="unread-dot"></span>
                <span class="message-time">{{ formatDate(msg.createdTime) }}</span>
              </div>
              <div class="message-title-row">
                <h3 class="message-title">{{ msg.title || '无标题' }}</h3>
                <svg
                  class="expand-icon"
                  :class="{ rotated: expandedId === msg.id }"
                  width="16" height="16" viewBox="0 0 16 16" fill="none"
                >
                  <path d="M4 6L8 10L12 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
              <p v-if="expandedId !== msg.id" class="message-preview">
                {{ msg.content.length > 80 ? msg.content.slice(0, 80) + '...' : msg.content }}
              </p>
              <div v-else class="message-content">
                <p>{{ msg.content }}</p>
              </div>
            </div>
          </div>
        </template>
        <div class="message-body-spacer"></div>
      </div>
    </div>
  </PageLayout>
</template>

<style scoped>
.message-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-width: 1200px;
  margin: 0 auto;
}

.message-header {
  flex-shrink: 0;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.unread-info {
  font-size: 13px;
  color: #B91C1C;
  font-weight: 600;
}

.message-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.message-body::-webkit-scrollbar {
  width: 5px;
}

.message-body::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.message-body::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.message-body::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--primary-rgb), 0.45);
}

.message-body-spacer {
  height: 8px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.message-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 6px 24px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-left: 4px solid #D1D5DB;
  transition: all 0.2s;
  cursor: pointer;
}

.message-card:hover {
  transform: translateY(-2px);
  box-shadow:
    0 8px 30px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.message-card.is-unread {
  border-left-color: var(--primary);
  background: rgba(238, 242, 255, 0.92);
}

.message-card.is-expanded {
  border-left-color: var(--primary);
}

.message-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.message-type-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.message-type-badge.type-system {
  background: #fef3c7;
  color: #d97706;
}

.message-type-badge.type-private {
  background: #eef2ff;
  color: var(--primary);
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #B91C1C;
  flex-shrink: 0;
}

.message-time {
  margin-left: auto;
  font-size: 12px;
  color: #9CA3AF;
}

.message-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.message-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.expand-icon {
  flex-shrink: 0;
  color: #9CA3AF;
  transition: transform 0.2s;
}

.expand-icon.rotated {
  transform: rotate(180deg);
}

.message-preview {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.6;
  margin: 8px 0 0;
  white-space: pre-wrap;
}

.message-content {
  margin-top: 12px;
  padding: 12px 16px;
  background: rgba(99, 102, 241, 0.04);
  border-radius: 10px;
  border-left: 3px solid var(--primary);
}

.message-content p {
  margin: 0;
  font-size: 14px;
  color: #475569;
  line-height: 1.8;
  white-space: pre-wrap;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 6px 24px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  color: #6B7280;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #9CA3AF;
}

@media (max-width: 767px) {
  .message-card {
    padding: 14px;
  }

  .empty-state {
    padding: 40px 16px;
  }
}

@media (max-width: 575px) {
  .message-page {
    max-width: 100%;
  }
}
</style>
