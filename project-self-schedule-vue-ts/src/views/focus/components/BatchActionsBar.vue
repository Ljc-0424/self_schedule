<template>
  <div v-if="isBatchDeleting" class="batch-actions">
    <label class="select-all-label">
      <input type="checkbox" :checked="isAllSelected" @change="$emit('toggle-select-all')" />
      全选 ({{ selectedCount }})
    </label>
    <button 
      class="batch-btn batch-delete-btn" 
      @click="$emit('batch-delete')" 
      :disabled="selectedCount === 0"
    >
      删除选中 ({{ selectedCount }})
    </button>
    <button class="batch-btn batch-cancel-btn" @click="$emit('cancel-batch')">
      取消
    </button>
  </div>
</template>

<script setup lang="ts">
interface Props {
  isBatchDeleting: boolean
  isAllSelected: boolean
  selectedCount: number
}

defineProps<Props>()
defineEmits<{
  'toggle-select-all': []
  'batch-delete': []
  'cancel-batch': []
}>()
</script>

<style scoped>
.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  margin-bottom: 12px;
  background-color: rgba(248, 250, 252, 0.85);
  border-radius: 10px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.select-all-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #334155;
  font-weight: 500;
  cursor: pointer;
}

.select-all-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
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

.batch-cancel-btn {
  background-color: rgba(100, 116, 139, 0.15);
  color: #6B7280;
}

.batch-cancel-btn:hover {
  background-color: rgba(100, 116, 139, 0.25);
}
</style>