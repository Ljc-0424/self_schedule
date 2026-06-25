<template>
  <div 
    class="record-item"
    :class="{ selected: selected }"
    @click="handleClick"
  >
    <!-- 批量删除模式下显示勾选框 -->
    <div v-if="isBatchDeleting" class="checkbox-wrapper">
      <input 
        type="checkbox" 
        :checked="selected" 
        @change.stop="$emit('toggle-selection', record.id)"
        class="record-checkbox"
      />
    </div>
    
    <div class="record-info">
      <div class="record-title">{{ record.taskTitle || '无关联任务' }}</div>
      <div class="record-tags">
        <span class="tag tag-duration">
          {{ formatDuration(record.duration) }}
        </span>
        <span :class="['tag', `tag-status-${record.status}`]">
          {{ getStatusLabel(record.status) }}
        </span>
        <span class="tag tag-date">
          {{ formatDateTime(record.startTime) }}
        </span>
      </div>
      <div v-if="record.notes" class="record-notes">
        {{ record.notes }}
      </div>
    </div>
    
    <!-- 非批量删除模式下显示删除按钮 -->
    <button 
      v-if="!isBatchDeleting"
      @click.stop="$emit('delete-record', record.id)" 
      class="delete-btn"
    >
      删除
    </button>
  </div>
</template>

<script setup lang="ts">
interface Props {
  record: any
  isBatchDeleting: boolean
  selected: boolean
  formatDuration: (duration: number) => string
  formatDateTime: (dateTime: string) => string
  getStatusLabel: (status: number) => string
}

const emit = defineEmits<{
  'toggle-selection': [id: number]
  'delete-record': [id: number]
  'view-detail': [id: number]
}>()

const props = defineProps<Props>()

const handleClick = () => {
  if (props.isBatchDeleting) {
    emit('toggle-selection', props.record.id)
  } else {
    emit('view-detail', props.record.id)
  }
}
</script>

<style scoped>
.record-item {
  padding: 12px 16px;
  background-color: #fff;
  border-radius: 8px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
}

.record-item:hover {
  background-color: #f8fafc;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.record-item.selected {
  border-color: var(--primary);
  background-color: rgba(var(--primary-rgb), 0.05);
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.record-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: var(--primary);
}

.record-info {
  flex: 1;
}

.record-title {
  font-weight: 500;
  color: #111827;
  margin-bottom: 6px;
}

.record-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.tag {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.tag-duration {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.tag-status-1 {
  background-color: #e8f5e9;
  color: #2e7d32;
}

.tag-status-2 {
  background-color: #ffebee;
  color: #c62828;
}

.tag-status-3, .tag-status-4 {
  background-color: #f5f5f5;
  color: #666;
}

.tag-date {
  background-color: #e3f2fd;
  color: #1565c0;
}

.record-notes {
  margin-top: 5px;
  font-size: 13px;
  color: #6B7280;
}

.delete-btn {
  padding: 5px 10px;
  background-color: #B91C1C;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  margin-left: 15px;
  transition: background-color 0.2s;
}

.delete-btn:hover {
  background-color: #B91C1C;
}
</style>