<script setup lang="ts">
import { computed } from 'vue';
import Loading from '../../../components/Loading.vue'

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

const props = defineProps<{
  reminders: ReminderVO[];
  isLoading: boolean;
  currentStatus: string;
  formatDateTime: (dateStr: string) => string;
  getDisplayTime: (reminder: ReminderVO) => string;
}>();

const emit = defineEmits<{
  (e: 'updateStatus', id: number, status: string): void;
  (e: 'delete', id: number): void;
}>();

const statusOptions = [
  { value: 'ALL', label: '全部' },
  { value: 'PENDING', label: '待处理' },
  { value: 'SENT', label: '已发送' },
  { value: 'READ', label: '已读' },
  { value: 'DISMISSED', label: '已忽略' },
  { value: 'EXPIRED', label: '已过期' }
];

const currentStatusLabel = computed(() => {
  return statusOptions.find(o => o.value === props.currentStatus)?.label || '';
});
</script>

<template>
  <div class="reminder-container">
    <Loading v-if="isLoading" text="加载中..." />

    <div v-else-if="reminders.length === 0" class="empty-state">
      <div class="empty-icon"></div>
      <p>暂无{{ currentStatus === 'ALL' ? '' : currentStatusLabel }}提醒记录</p>
      <p class="empty-hint">创建带有提醒时间的任务后，这里会显示提醒</p>
    </div>

    <div v-else class="reminder-list">
      <div v-for="reminder in reminders" :key="reminder.id" class="reminder-card" :class="[reminder.status.toLowerCase()]">
        <div class="reminder-header">
          <span class="type-badge" :class="reminder.reminderType.toLowerCase()">{{ reminder.reminderTypeLabel }}</span>
          <span class="status-badge" :class="reminder.status.toLowerCase()">{{ reminder.statusLabel }}</span>
          <span class="time-ago">{{ getDisplayTime(reminder) }}</span>
        </div>
        <h3 class="reminder-title">{{ reminder.taskTitle }}</h3>
        <p class="reminder-message">{{ reminder.message }}</p>
        <div class="reminder-meta">
          <span class="meta-item">触发时间：{{ formatDateTime(reminder.triggerTime) }}</span>
          <span class="meta-item">任务时间：{{ formatDateTime(reminder.taskTime) }}</span>
        </div>
        <div class="reminder-actions">
          <button v-if="reminder.status === 'PENDING'" class="action-btn small" @click="emit('updateStatus', reminder.id, 'READ')">✓ 标为已读</button>
          <button v-if="reminder.status !== 'DISMISSED'" class="action-btn small secondary" @click="emit('updateStatus', reminder.id, 'DISMISSED')">忽略</button>
          <button class="action-btn small danger" @click="emit('delete', reminder.id)">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reminder-container {
  padding-right: 4px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255,255,255,0.95);
  border-radius: 12px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-state p {
  margin: 0 0 8px 0;
  color: #6B7280;
}

.empty-hint {
  font-size: 13px !important;
  color: #9CA3AF !important;
}

.reminder-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reminder-card {
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
}

.reminder-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 30px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.reminder-card.pending {
  border-left-color: var(--primary);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 
    0 8px 30px rgba(var(--primary-rgb), 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.reminder-card.read {
  background: rgba(248, 250, 252, 0.75);
  opacity: 0.85;
}

.reminder-header {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.type-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.type-badge.remind_time {
  background: #dbeafe;
  color: var(--primary-darker);
}

.type-badge.deadline {
  background: #fef3c7;
  color: #d97706;
}

.status-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fee2e2;
  color: #B91C1C;
}

.status-badge.read {
  background: #dcfce7;
  color: #166534;
}

.status-badge.dismissed {
  background: #f3f4f6;
  color: #6b7280;
}

.reminder-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.reminder-message {
  font-size: 14px;
  color: #6B7280;
  margin: 0 0 12px 0;
  line-height: 1.5;
}

.reminder-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.meta-item {
  font-size: 12px;
  color: #9CA3AF;
}

.reminder-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #E5E7EB;
}

.action-btn {
  padding: 8px 16px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(var(--primary-rgb), 0.35);
}

.action-btn.secondary {
  background: #E5E7EB;
  color: #6B7280;
}

.action-btn.secondary:hover {
  background: #D1D5DB;
  box-shadow: none;
}

.action-btn.small {
  padding: 6px 12px;
  font-size: 12px;
}

.action-btn.danger {
  background: #fee2e2;
  color: #B91C1C;
}

.action-btn.danger:hover {
  background: #fecaca;
  box-shadow: none;
}

@media (max-width: 1199.98px) {
  .reminder-container {
    max-height: 420px;
  }
}

@media (max-width: 991.98px) {
  .reminder-container {
    max-height: 380px;
  }
  
  .reminder-card {
    padding: 16px;
  }
  
  .reminder-title {
    font-size: 14px;
  }
  
  .reminder-message {
    font-size: 13px;
  }
  
  .meta-item {
    font-size: 11px;
  }
  
  .empty-state {
    padding: 40px 16px;
  }
  
  .empty-icon {
    font-size: 40px;
    margin-bottom: 12px;
  }
}

@media (max-width: 767.98px) {
  .reminder-container {
    max-height: 320px;
    padding-bottom: 12px;
  }
  
  .reminder-card {
    padding: 14px;
    border-radius: 12px;
  }
  
  .reminder-header {
    flex-wrap: wrap;
    gap: 6px;
    margin-bottom: 10px;
  }
  
  .type-badge,
  .status-badge {
    padding: 2px 8px;
    font-size: 10px;
  }
  
  .reminder-title {
    font-size: 13px;
    margin-bottom: 6px;
  }
  
  .reminder-message {
    font-size: 12px;
    margin-bottom: 10px;
  }
  
  .reminder-meta {
    margin-bottom: 10px;
  }
  
  .meta-item {
    font-size: 10px;
  }
  
  .reminder-actions {
    padding-top: 10px;
    gap: 6px;
  }
  
  .action-btn.small {
    padding: 5px 10px;
    font-size: 11px;
  }
  
  .empty-state {
    padding: 30px 12px;
    border-radius: 10px;
  }
  
  .empty-icon {
    font-size: 32px;
  }
  
  .empty-state p {
    font-size: 13px;
  }
  
  .empty-hint {
    font-size: 11px !important;
  }
}

@media (max-width: 575.98px) {
  .reminder-container {
    max-height: 260px;
    padding-bottom: 10px;
  }
  
  .reminder-card {
    padding: 12px;
    border-radius: 10px;
  }
  
  .reminder-header {
    gap: 4px;
    margin-bottom: 8px;
  }
  
  .type-badge,
  .status-badge {
    padding: 2px 6px;
    font-size: 9px;
  }
  
  .time-ago {
    font-size: 11px;
  }
  
  .reminder-title {
    font-size: 12px;
    margin-bottom: 5px;
  }
  
  .reminder-message {
    font-size: 11px;
    margin-bottom: 8px;
    line-height: 1.4;
  }
  
  .reminder-meta {
    margin-bottom: 8px;
  }
  
  .meta-item {
    font-size: 9px;
  }
  
  .reminder-actions {
    padding-top: 8px;
    gap: 4px;
  }
  
  .action-btn.small {
    padding: 4px 8px;
    font-size: 10px;
  }
  
  .empty-state {
    padding: 20px 10px;
  }
  
  .empty-icon {
    font-size: 28px;
    margin-bottom: 10px;
  }
  
  .empty-state p {
    font-size: 12px;
  }
}

@media (max-height: 600px) {
  .reminder-container {
    max-height: 220px;
  }
  
  .empty-state {
    padding: 20px;
  }
}

@media (max-height: 500px) {
  .reminder-container {
    max-height: 180px;
  }
}
</style>