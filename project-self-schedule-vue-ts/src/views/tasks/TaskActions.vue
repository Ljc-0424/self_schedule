<script setup lang="ts">
import type { TaskVO } from '../../types'
import {
  completeTask,
  undoTask,
  openEditModal,
  deleteTask,
  expandedTasks,
  toggleExpand
} from './useTasks'

const props = defineProps<{
  task: TaskVO
}>()

const isExpanded = () => expandedTasks.value.has(props.task.id)
</script>

<template>
  <div class="task-actions">
    <!-- 完成 -->
    <button
      v-if="task.status !== 2 && task.status !== 3 && task.status !== 4"
      class="action-btn"
      @click="completeTask(task.id)"
      title="完成任务"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
    </button>
    <!-- 撤销 -->
    <button
      v-if="task.status === 2 || task.status === 3 || task.status === 4"
      class="action-btn"
      @click="undoTask(task.id)"
      :title="task.status === 4 ? '撤销为完成' : '撤销为待办'"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="1 4 1 10 7 10"/><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10"/></svg>
    </button>
    <!-- 编辑 -->
    <button
      v-if="task.status !== 3"
      class="action-btn"
      @click="openEditModal(task)"
      title="编辑任务"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
    </button>
    <!-- 子任务 -->
    <button
      class="action-btn"
      @click="toggleExpand(task.id)"
      :title="isExpanded() ? '收起子任务' : '展开子任务'"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
    </button>
    <!-- 删除 -->
    <button
      class="action-btn btn-delete"
      @click="deleteTask(task.id)"
      title="删除任务"
    >
      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
    </button>
  </div>
</template>

<style scoped>
.task-actions {
  display: flex;
  gap: 2px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #9CA3AF;
  cursor: pointer;
  transition: all 0.15s ease;
}

.action-btn:hover {
  background: #E5E7EB;
  color: #374151;
}

.action-btn:active {
  transform: scale(0.92);
}

.btn-delete:hover {
  background: #FEF2F2;
  color: #DC2626;
}
</style>
