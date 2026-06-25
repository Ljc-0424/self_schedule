<template>
  <div class="date-filter-bar">
    <div class="date-filter">
      <input 
        type="date" 
        v-model="localFilterDate" 
        @change="$emit('filter-date-change', localFilterDate)"
        class="date-input"
        placeholder="选择日期"
      />
      <button 
        v-if="filterDate" 
        class="clear-btn" 
        @click="$emit('clear-filter')"
        title="清除筛选"
      >
      </button>
    </div>
    <button 
      v-if="!isBatchDeleting && hasRecords" 
      class="batch-btn batch-delete-btn" 
      @click="$emit('toggle-batch-delete')"
    >
      批量删除
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  filterDate: string
  isBatchDeleting: boolean
  hasRecords: boolean
}

const props = defineProps<Props>()
defineEmits<{
  'filter-date-change': [date: string]
  'clear-filter': []
  'toggle-batch-delete': []
}>()

const localFilterDate = ref(props.filterDate)

watch(() => props.filterDate, (newVal) => {
  localFilterDate.value = newVal
})
</script>

<style scoped>
.date-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 16px;
  margin-bottom: 12px;
  background-color: rgba(248, 250, 252, 0.85);
  border-radius: 10px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.date-filter {
  display: flex;
  align-items: center;
  gap: 4px;
}

.date-input {
  padding: 6px 10px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-size: 13px;
  background-color: white;
  cursor: pointer;
}

.date-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.1);
}

.clear-btn {
  padding: 4px 6px;
  border: none;
  background-color: rgba(148, 163, 184, 0.2);
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  color: #6B7280;
}

.clear-btn:hover {
  background-color: rgba(148, 163, 184, 0.3);
}

.batch-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.batch-delete-btn {
  background-color: rgba(220, 38, 38, 0.15);
  color: #B91C1C;
}

.batch-delete-btn:hover:not(:disabled) {
  background-color: rgba(220, 38, 38, 0.25);
}

.batch-delete-btn:disabled {
  background-color: #E5E7EB;
  color: #9CA3AF;
  cursor: not-allowed;
}
</style>