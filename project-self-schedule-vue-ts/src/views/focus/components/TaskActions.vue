<template>
  <div class="task-actions">
    <button class="btn-secondary" @click="$emit('toggle-task-search')">
      关联任务
    </button>
    <button v-if="selectedTask" class="btn-danger" @click="$emit('clear-selected-task')">
      取消关联
    </button>
  </div>
</template>

<script setup lang="ts">
interface Props {
  selectedTask: any
}

defineProps<Props>()
defineEmits<{
  'toggle-task-search': []
  'clear-selected-task': []
}>()
</script>

<style scoped>
.task-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 25px;
}

.btn-secondary {
  padding: 11px 26px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(var(--primary-rgb), 0.3);
}

.btn-secondary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(var(--primary-rgb), 0.4);
}

.btn-danger {
  padding: 11px 26px;
  background: rgba(239, 68, 68, 0.08);
  color: #B91C1C;
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-danger:hover {
  background: rgba(239, 68, 68, 0.15);
}
</style>