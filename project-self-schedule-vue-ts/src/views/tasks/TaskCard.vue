<script setup lang="ts">
import { computed } from 'vue'
import type { TaskVO } from '../../types'
import {
  expandedTasks,
  getStatusLabel,
  getTaskTags,
  getPriorityInfo,
  formatDeadline,
  formatRemindTime,
  formatTimeOnly,
  formatEstimatedTime,
  toggleExpand,
  isTaskBatchDeleting,
  selectedTaskIds,
  toggleTaskSelection,
  undoTask,
  getRecurrenceLabel
} from './useTasks'
import TaskActions from './TaskActions.vue'
import TaskSubtasks from './TaskSubtasks.vue'
import { useTaskFocus } from '../../composables/useTaskFocus'
import router from '../../router'

const props = defineProps<{
  task: TaskVO
  index?: number
}>()

const { startFocusFromTask } = useTaskFocus()

const isSelected = () => selectedTaskIds.value.has(props.task.id)

const isOverdue = computed(() => {
  const t = props.task
  if (!t || t.status === 2 || t.status === 3 || t.status === 4) return false
  const now = new Date()
  if (t.deadline && new Date(t.deadline) < now) return true
  if (t.remindTime && new Date(t.remindTime) < now) return true
  return false
})

const handleStartFocus = async () => {
  const success = await startFocusFromTask(props.task)
  if (success) {
    router.push('/focus')
  }
}
</script>

<template>
  <div class="task-card" :class="{ completed: task.status === 2, archived: task.status === 3 || task.status === 4, selected: isSelected(), overdue: isOverdue }">
    <div class="task-main">
      <!-- 序号 -->
      <div class="task-number">{{ index !== undefined ? index + 1 : '' }}</div>
      
      <!-- 批量删除模式下显示勾选框 -->
      <div v-if="isTaskBatchDeleting" class="checkbox-wrapper">
        <input 
          type="checkbox" 
          :checked="isSelected()" 
          @change="toggleTaskSelection(task.id)"
          class="task-checkbox"
        />
      </div>
      
      <button class="expand-btn" @click="toggleExpand(task.id)">
        {{ expandedTasks.has(task.id) ? '▼' : '▶' }}
      </button>
      
      <div class="task-content">
        <div class="task-header">
          <h3 class="task-title">{{ task.title }}</h3>
          <div class="task-labels">
            <span v-if="isOverdue" class="overdue-label">已过期</span>
            <span class="status-label" :class="`status-${task.status}`">
              {{ getStatusLabel(task.status) }}
            </span>
            <span class="priority-label" :style="{ backgroundColor: getPriorityInfo(task.priority).color }">
              {{ getPriorityInfo(task.priority).label }}
            </span>
            <span v-if="task.category" class="category-label">
              {{ task.category }}
            </span>
            <span v-if="task.recurrenceRule" class="recurrence-label">
              {{ getRecurrenceLabel(task.recurrenceRule) }}
            </span>
          </div>
        </div>
        
        <p v-if="task.description" class="task-desc">{{ task.description }}</p>
        
        <div class="task-meta">
          <!-- 截止时间 -->
          <span v-if="task.deadline" class="meta-item" :class="{ urgent: new Date(task.deadline) < new Date() && task.status !== 2 && task.status !== 3 && task.status !== 4 }">
            <span class="meta-label">截止日期:</span>
            <span class="meta-value">{{ formatDeadline(task.deadline) }} {{ formatTimeOnly(task.deadline) }}</span>
          </span>

          <!-- 提醒时间 -->
          <span v-if="task.remindTime" class="meta-item" :class="{ urgent: new Date(task.remindTime) < new Date() && task.status !== 2 && task.status !== 3 && task.status !== 4 }">
            <span class="meta-label">提醒时间:</span>
            <span class="meta-value">{{ formatRemindTime(task.remindTime) }}</span>
          </span>

          <!-- 预估时长 -->
          <span v-if="task.estimatedSeconds" class="meta-item success">
            <span class="meta-label">预估时长:</span>
            <span class="meta-value">{{ formatEstimatedTime(task.estimatedSeconds) }}</span>
          </span>

          <!-- 实际用时 -->
          <span v-if="task.actualSeconds" class="meta-item accent">
            <span class="meta-label">实际用时:</span>
            <span class="meta-value">{{ formatEstimatedTime(task.actualSeconds) }}</span>
          </span>
          
          <span v-if="getTaskTags(task).length > 0 && (task.deadline || task.remindTime || task.estimatedSeconds || task.actualSeconds)" class="meta-divider">|</span>
          
          <template v-if="getTaskTags(task).length > 0">
            <span v-for="(tag, index) in getTaskTags(task)" :key="index" class="tag-item">
              #{{ tag }}
            </span>
          </template>
        </div>
      </div>
      
      <!-- 非批量删除模式下显示操作按钮 -->
      <div v-if="!isTaskBatchDeleting" class="task-actions-wrapper">
        <TaskActions :task="task" />
        
        <button
          v-if="task.estimatedSeconds && task.status !== 2 && task.status !== 4"
          class="action-btn focus-btn"
          @click="handleStartFocus"
          title="开始专注"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="5 3 19 12 5 21 5 3"/></svg>
        </button>
      </div>
    </div>
    
    <TaskSubtasks v-if="expandedTasks.has(task.id)" :task-id="task.id" />
  </div>
</template>

<style scoped>
.task-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 
    0 6px 24px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 30px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.task-card.overdue {
  border-color: rgba(239, 68, 68, 0.3);
  box-shadow: 
    0 6px 24px rgba(239, 68, 68, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.task-card.overdue:hover {
  border-color: rgba(239, 68, 68, 0.5);
  box-shadow: 
    0 8px 30px rgba(239, 68, 68, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.task-card.selected {
  border-color: var(--primary);
  background-color: rgba(var(--primary-rgb), 0.08);
  box-shadow: 
    0 6px 24px rgba(var(--primary-rgb), 0.15),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.task-card.completed {
  opacity: 0.85;
}

.task-card.archived {
  opacity: 0.7;
}

.task-main {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 16px;
}

.task-number {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary);
  color: white;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(var(--primary-rgb), 0.3);
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.task-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: var(--primary);
}

.expand-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 12px;
  color: #9CA3AF;
  padding: 4px;
  flex-shrink: 0;
  transition: all 0.2s ease;
}

.expand-btn:hover {
  color: var(--primary);
  transform: scale(1.2);
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.task-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  flex: 1;
  min-width: 120px;
}

.task-card.completed .task-title,
.task-card.archived .task-title {
  text-decoration: line-through;
  color: #9CA3AF;
}

.task-labels {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.overdue-label {
  background: rgba(239, 68, 68, 0.1);
  color: #B91C1C;
  border: 1px solid rgba(239, 68, 68, 0.3);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  flex-shrink: 0;
}

.status-label {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.status-label.status-0 {
  background: #f3f4f6;
  color: #4b5563;
}

.status-label.status-1 {
  background: #dbeafe;
  color: var(--primary-darker);
}

.status-label.status-2 {
  background: #dcfce7;
  color: #166534;
}

.status-label.status-3 {
  background: #fee2e2;
  color: #991b1b;
}

.status-label.status-4 {
  background: #e9d5ff;
  color: #6b21a8;
}

.priority-label {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.category-label {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  background: #e0e7ff;
  color: #4338ca;
}

.recurrence-label {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  background: #d1fae5;
  color: #065f46;
}

.task-desc {
  margin: 0 0 10px 0;
  color: #6B7280;
  font-size: 13px;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-item {
  font-size: 12px;
  color: #6B7280;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-label {
  color: #9CA3AF;
  font-weight: 500;
}

.meta-value {
  color: #374151;
  font-weight: 500;
}

.meta-item.urgent {
  color: #B91C1C;
  font-weight: 600;
}

.meta-item.success {
  color: #3F6212;
}

.meta-item.accent {
  color: var(--primary);
}

.meta-divider {
  color: #D1D5DB;
}

.tag-item {
  background: #dbeafe;
  color: var(--primary-darker);
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
}



.task-actions-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  transform: translateY(-2px);
}

.focus-btn {
  background-color: transparent;
  color: var(--primary, #3B82F6);
}

.focus-btn:hover {
  background-color: var(--primary-lighter, #EFF6FF);
}

@media (max-width: 575px) {
  .task-card {
    border-radius: 10px;
  }

  .task-main {
    padding: 10px 12px;
    gap: 4px;
    flex-wrap: wrap;
  }

  .task-number {
    display: none;
  }

  .expand-btn {
    display: none;
  }

  .task-content {
    width: 100%;
    padding-left: 0;
  }

  .task-header {
    gap: 6px;
    flex-wrap: wrap;
  }

  .task-title {
    font-size: 14px;
    min-width: 100%;
    word-break: normal;
    overflow: visible;
    white-space: normal;
    line-height: 1.4;
  }

  .task-labels {
    gap: 3px;
    width: 100%;
  }

  .task-labels span {
    font-size: 10px;
    padding: 2px 6px;
  }

  .task-desc {
    font-size: 12px;
    margin-bottom: 4px;
  }

  .task-meta {
    gap: 4px;
    flex-wrap: wrap;
  }

  .meta-item {
    font-size: 11px;
    white-space: nowrap;
  }

  .meta-label {
    display: none;
  }

  .task-actions-wrapper {
    width: 100%;
    justify-content: flex-end;
    margin-top: 6px;
    border-top: 1px solid rgba(0,0,0,0.1);
    padding-top: 8px;
  }

  .action-btn {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
}
</style>