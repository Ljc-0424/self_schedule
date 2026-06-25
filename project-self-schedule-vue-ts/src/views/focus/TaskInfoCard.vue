<script setup lang="ts">
import type { TaskVO } from '../../types'
import { formatDateTime } from './useFocus'

defineProps<{
  task: TaskVO
}>()

const getTaskStatusLabel = (status: number) => {
  switch (status) {
    case 0: return { label: '待办', color: '#94a3b8' }
    case 1: return { label: '进行中', color: 'var(--primary)' }
    case 2: return { label: '已完成', color: '#22c55e' }
    case 3: return { label: '已取消', color: '#B91C1C' }
    case 4: return { label: '已存档', color: '#6b7280' }
    default: return { label: '未知', color: '#94a3b8' }
  }
}

const getPriorityLabel = (priority: number) => {
  switch (priority) {
    case 0: return { label: '低', color: '#22c55e' }
    case 1: return { label: '中', color: '#eab308' }
    case 2: return { label: '高', color: '#B91C1C' }
    default: return { label: '未知', color: '#94a3b8' }
  }
}

const formatEstimatedTime = (seconds: number | undefined): string => {
  if (!seconds || seconds <= 0) return '未设置'
  
  const days = Math.floor(seconds / (24 * 60 * 60))
  const remainingAfterDays = seconds % (24 * 60 * 60)
  const hours = Math.floor(remainingAfterDays / (60 * 60))
  const remainingAfterHours = remainingAfterDays % (60 * 60)
  const minutes = Math.floor(remainingAfterHours / 60)
  const secs = remainingAfterHours % 60
  
  const parts: string[] = []
  if (days > 0) parts.push(`${days}天`)
  if (hours > 0) parts.push(`${hours}时`)
  if (minutes > 0) parts.push(`${minutes}分`)
  if (secs > 0) parts.push(`${secs}秒`)
  
  return parts.join('') || '未设置'
}
</script>

<template>
  <div class="section">
    <h3>关联任务</h3>
    
    <div class="task-card">
      <div class="task-title">{{ task.title }}</div>
      
      <div v-if="task.description" class="task-description">
        {{ task.description }}
      </div>
      
      <div class="task-meta">
        <div class="meta-item">
          <span class="meta-label">状态：</span>
          <span 
            class="meta-value status"
            :style="{ color: getTaskStatusLabel(task.status).color }"
          >
            {{ getTaskStatusLabel(task.status).label }}
          </span>
        </div>
        
        <div class="meta-item">
          <span class="meta-label">优先级：</span>
          <span 
            class="meta-value priority"
            :style="{ color: getPriorityLabel(task.priority).color }"
          >
            {{ getPriorityLabel(task.priority).label }}
          </span>
        </div>
        
        <div class="meta-item">
          <span class="meta-label">预估时长：</span>
          <span class="meta-value">{{ formatEstimatedTime(task.estimatedSeconds) }}</span>
        </div>
        
        <div v-if="task.category" class="meta-item">
          <span class="meta-label">分类：</span>
          <span class="meta-value">{{ task.category }}</span>
        </div>
        
        <div v-if="task.deadline" class="meta-item">
          <span class="meta-label">截止日期：</span>
          <span class="meta-value">{{ formatDateTime(task.deadline) }}</span>
        </div>
        
        <div v-if="task.tags && task.tags.length > 0" class="meta-item">
          <span class="meta-label">标签：</span>
          <div class="tags">
            <span v-for="tag in task.tags" :key="tag" class="tag">
              {{ tag }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.section {
  background-color: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.section h3 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #374151;
}

.task-card {
  background-color: white;
  border-radius: 8px;
  padding: 14px;
  border: 1px solid #e5e7eb;
}

.task-title {
  font-weight: 600;
  color: #1f2937;
  font-size: 15px;
  margin-bottom: 8px;
}

.task-description {
  color: #6b7280;
  font-size: 14px;
  margin-bottom: 12px;
  line-height: 1.5;
}

.task-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-label {
  color: #9ca3af;
  font-size: 13px;
}

.meta-value {
  font-size: 13px;
  color: #374151;
}

.meta-value.status,
.meta-value.priority {
  font-weight: 500;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  background-color: #eff6ff;
  color: var(--primary);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}
</style>