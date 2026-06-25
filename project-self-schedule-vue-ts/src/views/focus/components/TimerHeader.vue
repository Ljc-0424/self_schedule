<template>
  <div class="timer-header">
    <h2>专注倒计时</h2>
    <div v-if="isFocusing" :class="['focus-status', isPaused ? 'paused' : 'active']">
      <span class="status-dot"></span>
      {{ isPaused ? '已暂停' : '专注中' }}
      <span v-if="interruptions > 0" class="interruptions-badge">
        {{ interruptions }}次中断
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  isFocusing: boolean
  isPaused: boolean
  interruptions: number
}

defineProps<Props>()
</script>

<style scoped>
.timer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.timer-header h2 {
  color: #111827;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.focus-status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background-color: rgba(148, 163, 184, 0.1);
  border-radius: 20px;
  font-size: 13px;
  color: #6B7280;
}

.focus-status.active {
  background-color: rgba(16, 185, 129, 0.15);
  color: #3F6212;
}

.focus-status.paused {
  background-color: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
}

.focus-status.paused .status-dot {
  background-color: #f59e0b;
  animation: none;
}

.interruptions-badge {
  margin-left: 8px;
  padding: 2px 8px;
  background-color: rgba(239, 68, 68, 0.1);
  color: #B91C1C;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.status-dot {
  width: 8px;
  height: 8px;
  background-color: #3F6212;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>