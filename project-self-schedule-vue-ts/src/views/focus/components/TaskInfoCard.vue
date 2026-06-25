<template>
  <div class="task-info">
    <div v-if="selectedTask" class="task-card">
      <div class="task-icon"></div>
      <div class="task-details">
        <div class="task-title">{{ selectedTask.title }}</div>
        <div class="task-estimate">预估时间: {{ formatEstimatedTime(selectedTask.estimatedSeconds) }}</div>
      </div>
    </div>
    <div v-else class="no-task">
      <div class="no-task-icon"></div>
      <div class="no-task-text">未关联任务</div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  selectedTask: any
  formatEstimatedTime: (seconds: number | undefined) => string
}

defineProps<Props>()
</script>

<style scoped>
.task-info {
  text-align: center;
  margin-bottom: 25px;
}

.task-card {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  background: rgba(var(--primary-rgb), 0.06);
  padding: 15px 25px;
  border-radius: 12px;
  border: 1px solid #D1D5DB;
}

.task-icon {
  font-size: 24px;
}

.task-details {
  text-align: left;
}

.task-title {
  color: #111827;
  font-weight: 500;
  font-size: 15px;
  margin-bottom: 4px;
}

.task-estimate {
  color: #6B7280;
  font-size: 13px;
}

.no-task {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #9CA3AF;
}

.no-task-icon {
  font-size: 32px;
}

.no-task-text {
  font-size: 14px;
}
@media (max-width: 575px) {
  .task-info {
    margin-bottom: 14px;
  }

  .task-card {
    padding: 10px 16px;
    gap: 8px;
    border-radius: 10px;
  }

  .task-icon {
    font-size: 20px;
  }

  .task-title {
    font-size: 13px;
  }

  .task-estimate {
    font-size: 11px;
  }

  .no-task-icon {
    font-size: 26px;
  }
}
</style>