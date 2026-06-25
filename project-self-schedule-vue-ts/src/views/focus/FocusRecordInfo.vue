<script setup lang="ts">
import type { FocusRecordVO } from '../../types'
import { formatDuration, formatDateTime } from './useFocus'

defineProps<{
  record: FocusRecordVO
}>()
</script>

<template>
  <div class="section">
    <h3>专注信息</h3>
    
    <div class="info-row">
      <span class="label">专注开始时间：</span>
      <span class="value">{{ formatDateTime(record.startTime) }}</span>
    </div>
    
    <div class="info-row">
      <span class="label">专注时长：</span>
      <span class="value highlight">{{ formatDuration(record.duration) }}</span>
    </div>
    
    <div class="info-row">
      <span class="label">专注状态：</span>
      <span 
        class="status-badge" 
        :style="{ 
          backgroundColor: record.status === 1 ? '#dcfce7' : record.status === 2 ? '#ffebee' : '#f3f4f6',
          color: record.status === 1 ? '#16a34a' : record.status === 2 ? '#B91C1C' : '#4b5563'
        }"
      >
        {{ record.status === 1 ? '✓ 已完成' : record.status === 2 ? '✗ 中途放弃' : '已取消' }}
      </span>
    </div>
    
    <div v-if="record.notes" class="info-row">
      <span class="label">备注：</span>
      <span class="value">{{ record.notes }}</span>
    </div>
    
    <div class="info-row">
      <span class="label">中断次数：</span>
      <span class="value">{{ record.interruptions }} 次</span>
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

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 10px;
}

.info-row:last-child {
  margin-bottom: 0;
}

.label {
  color: #6b7280;
  font-size: 14px;
  min-width: 100px;
}

.value {
  color: #1f2937;
  font-size: 14px;
  flex: 1;
}

.value.highlight {
  font-weight: 600;
  color: #16a34a;
}

.status-badge {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
</style>