<script setup lang="ts">
interface ReminderStats {
  total: number
  pending: number
  sent: number
  read: number
  dismissed: number
  expired: number
}

const props = defineProps<{
  currentStatus: string;
  stats: ReminderStats;
}>();

const emit = defineEmits<{
  (e: 'statusChange', status: string): void;
}>();

const statusOptions = [
  { value: 'ALL', label: '全部' },
  { value: 'PENDING', label: '待处理' },
  { value: 'SENT', label: '已发送' },
  { value: 'READ', label: '已读' },
  { value: 'DISMISSED', label: '已忽略' },
  { value: 'EXPIRED', label: '已过期' }
];

const getStatusCount = (status: string): number => {
  switch(status) {
    case 'PENDING': return props.stats.pending;
    case 'SENT': return props.stats.sent;
    case 'READ': return props.stats.read;
    case 'DISMISSED': return props.stats.dismissed;
    case 'EXPIRED': return props.stats.expired;
    default: return 0;
  }
};
</script>

<template>
  <div class="filter-bar">
    <div class="filter-group">
      <span class="filter-label">状态筛选：</span>
      <div class="filter-tabs">
        <button 
          v-for="option in statusOptions" 
          :key="option.value"
          :class="['filter-tab', { active: currentStatus === option.value }]"
          @click="emit('statusChange', option.value)"
        >
          {{ option.label }}
          <span v-if="currentStatus === 'ALL' && getStatusCount(option.value) > 0" class="badge">{{ getStatusCount(option.value) }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 
    0 6px 24px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  margin-bottom: 20px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-label {
  font-size: 14px;
  color: #6B7280;
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tab {
  padding: 6px 14px;
  background: #E5E7EB;
  border: 1px solid #D1D5DB;
  border-radius: 20px;
  font-size: 13px;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-tab:hover {
  background: #D1D5DB;
}

.filter-tab.active {
  background: var(--primary);
  border-color: var(--primary);
  color: white;
}

.badge {
  padding: 2px 6px;
  background: rgba(255,255,255,0.3);
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

@media (max-width: 991.98px) {
  .filter-bar {
    padding: 12px 14px;
    margin-bottom: 16px;
  }
  
  .filter-label {
    font-size: 13px;
  }
  
  .filter-tab {
    padding: 5px 12px;
    font-size: 12px;
  }
}

@media (max-width: 575px) {
  .filter-bar {
    flex-direction: column;
    padding: 10px 12px;
    border-radius: 12px;
    margin-bottom: 14px;
  }

  .filter-group {
    width: 100%;
  }

  .filter-tabs {
    flex-wrap: wrap;
    gap: 4px;
  }

  .filter-tab {
    padding: 5px 10px;
    font-size: 11px;
  }

  .filter-label {
    font-size: 12px;
  }
}
</style>