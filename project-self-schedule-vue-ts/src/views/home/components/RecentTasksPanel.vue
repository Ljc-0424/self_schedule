<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { taskApi } from '../../../api';
import type { TaskVO } from '../../../types';
import router from '../../../router';

const emit = defineEmits<{
  (e: 'refresh' | 'close'): void;
}>();

const recentTasks = ref<TaskVO[]>([]);

const fetchRecentTasks = async () => {
  try {
    const response = await taskApi.getTasks({
      pageNum: 1,
      pageSize: 6
    });
    if (response.data.data) recentTasks.value = response.data.data.records;
  } catch (error) {
    console.error('获取最近任务失败:', error);
  }
};

const deleteTask = async (taskId: number) => {
  const prev = [...recentTasks.value]
  recentTasks.value = recentTasks.value.filter(task => task.id !== taskId)
  try {
    await taskApi.deleteTask(taskId);
  } catch (error) {
    recentTasks.value = prev
    console.error('删除任务失败:', error);
  }
};

const completeTask = async (taskId: number) => {
  const task = recentTasks.value.find(t => t.id === taskId);
  if (!task || task.status === 2) return
  const prevStatus = task.status
  task.status = 2
  try {
    await taskApi.completeTask(taskId);
  } catch (error) {
    task.status = prevStatus
    console.error('完成任务失败:', error);
  }
};

const goToTasks = () => {
  router.push('/tasks');
};

defineExpose({
  refresh: fetchRecentTasks
});

onMounted(() => {
  fetchRecentTasks();
});
</script>

<template>
  <div class="tasks-wrapper">
    <div class="tasks-card">
      <div class="panel-header">
        <h3>最近任务</h3>
        <div class="header-right">
          <span class="task-count">{{ recentTasks.length }}</span>
          <button class="close-btn" @click="$emit('close')" title="关闭">✕</button>
        </div>
      </div>
      
      <div v-if="recentTasks.length === 0" class="empty-state">
        <div class="empty-icon"></div>
        <p>暂无任务</p>
        <p class="hint">在左侧创建任务后会显示在这里</p>
      </div>
      
      <div v-else class="tasks-list">
        <div 
          v-for="task in recentTasks" 
          :key="task.id" 
          class="task-card"
        >
          <div class="task-content">
            <div class="task-title-row">
              <h4>{{ task.title }}</h4>
              <span :class="['status-tag', task.status === 2 ? 'completed' : 'pending']">
                {{ task.status === 2 ? '✓ 已完成' : '○ 待处理' }}
              </span>
            </div>
            <div class="task-meta">
              <span v-if="task.remindTime" class="meta-item">
                {{ new Date(task.remindTime).toLocaleString('zh-CN') }}
              </span>
              <span v-if="task.deadline" class="meta-item">
                {{ new Date(task.deadline).toLocaleString('zh-CN') }}
              </span>
            </div>
          </div>
          <div class="task-actions">
            <button 
              class="action-btn complete"
              @click="completeTask(task.id!)"
              :disabled="task.status === 2"
              :title="task.status === 2 ? '已完成' : '标记完成'"
            >
              ✓
            </button>
            <button 
              class="action-btn delete"
              @click="deleteTask(task.id!)"
              title="删除任务"
            >
              ✕
            </button>
          </div>
        </div>
      </div>
      
      <div class="panel-footer">
        <button class="view-all-btn" @click="goToTasks">查看全部任务 →</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.tasks-wrapper {
  width: 100%;
}

.tasks-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #E5E7EB;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.panel-header h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.task-count {
  padding: 3px 10px;
  background: var(--primary);
  color: white;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.close-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.12);
  color: #6B7280;
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.12);
  color: #111827;
}

.empty-state {
  padding: 30px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-state p {
  margin: 0 0 6px 0;
  color: #6B7280;
  font-size: 14px;
}

.empty-state .hint {
  font-size: 12px;
  color: #9CA3AF;
}

.tasks-list {
  padding: 10px 14px;
  max-height: 260px;
  overflow-y: auto;
}

.task-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px;
  background: white;
  border-radius: 14px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.03);
  transition: all 0.2s;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.task-content {
  flex: 1;
  min-width: 0;
}

.task-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-title-row h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-tag {
  font-size: 11px;
  font-weight: 500;
  padding: 2px 8px;
  border-radius: 8px;
}

.status-tag.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-tag.completed {
  background: #dcfce7;
  color: #16a34a;
}

.task-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.meta-item {
  font-size: 11px;
  color: #6B7280;
}

.task-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-left: 12px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn.complete {
  background: rgba(34, 197, 94, 0.1);
  color: #3F6212;
}

.action-btn.complete:hover:not(:disabled) {
  background: rgba(34, 197, 94, 0.2);
}

.action-btn.complete:disabled {
  background: #E5E7EB;
  color: #9CA3AF;
  cursor: not-allowed;
}

.action-btn.delete {
  background: rgba(220, 38, 38, 0.1);
  color: #B91C1C;
}

.action-btn.delete:hover {
  background: rgba(220, 38, 38, 0.2);
}

.panel-footer {
  padding: 14px 20px;
  background: rgba(248, 250, 252, 0.8);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.view-all-btn {
  width: 100%;
  padding: 10px;
  background: transparent;
  border: 1px solid rgba(var(--primary-rgb), 0.3);
  border-radius: 12px;
  font-size: 13px;
  color: var(--primary);
  cursor: pointer;
  transition: all 0.2s;
}

.view-all-btn:hover {
  background: rgba(var(--primary-rgb), 0.06);
  border-color: var(--primary);
}

@media (max-width: 1199.98px) {
  .tasks-wrapper {
    width: 300px;
  }
  
  .tasks-list {
    max-height: 300px;
  }
}

@media (max-width: 991.98px) {
  .tasks-wrapper {
    width: 100%;
  }
  
  .tasks-list {
    max-height: 280px;
  }
  
  .panel-header {
    padding: 14px 16px;
  }
}

@media (max-width: 767.98px) {
  .tasks-card {
    border-radius: 16px;
  }
  
  .panel-header {
    padding: 12px 14px;
  }
  
  .panel-header h3 {
    font-size: 14px;
  }
  
  .tasks-list {
    padding: 12px;
    max-height: 250px;
  }
  
  .task-card {
    padding: 12px;
    margin-bottom: 8px;
  }
  
  .task-title-row h4 {
    font-size: 13px;
  }
  
  .meta-item {
    font-size: 10px;
  }
  
  .action-btn {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }
  
  .panel-footer {
    padding: 12px 14px;
  }
  
  .view-all-btn {
    padding: 8px;
    font-size: 12px;
  }
  
  .empty-state {
    padding: 20px 16px;
  }
  
  .empty-icon {
    font-size: 32px;
    margin-bottom: 10px;
  }
  
  .empty-state p {
    font-size: 13px;
  }
  
  .empty-state .hint {
    font-size: 11px;
  }
}

@media (max-width: 575.98px) {
  .tasks-card {
    border-radius: 12px;
  }
  
  .panel-header {
    padding: 10px 12px;
  }
  
  .panel-header h3 {
    font-size: 13px;
  }
  
  .task-count {
    padding: 2px 8px;
    font-size: 11px;
  }
  
  .tasks-list {
    padding: 10px;
    max-height: 200px;
  }
  
  .task-card {
    padding: 10px;
    margin-bottom: 6px;
    border-radius: 10px;
  }
  
  .task-title-row {
    margin-bottom: 6px;
  }
  
  .task-title-row h4 {
    font-size: 12px;
  }
  
  .status-tag {
    font-size: 10px;
    padding: 2px 6px;
  }
  
  .meta-item {
    font-size: 9px;
  }
  
  .task-actions {
    gap: 4px;
    margin-left: 8px;
  }
  
  .action-btn {
    width: 24px;
    height: 24px;
    font-size: 11px;
    border-radius: 8px;
  }
  
  .panel-footer {
    padding: 10px 12px;
  }
  
  .view-all-btn {
    padding: 6px;
    font-size: 11px;
    border-radius: 10px;
  }
  
  .empty-state {
    padding: 16px 12px;
  }
  
  .empty-icon {
    font-size: 28px;
  }
  
  .empty-state p {
    font-size: 12px;
  }
  
  .empty-state .hint {
    font-size: 10px;
  }
}
</style>