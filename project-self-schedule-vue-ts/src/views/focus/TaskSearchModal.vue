<script setup lang="ts">
import { useStore } from 'vuex'
import Pagination from '../../components/Pagination.vue'
import { 
  showTaskSearch, 
  searchKeyword, 
  tasks, 
  toggleTaskSearch, 
  searchTasks,
  useFocus,
  taskSearchCurrentPage,
  taskSearchTotalPages,
  taskSearchTotalItems,
  taskSearchChangePage
} from './useFocus'

const store = useStore()
const { selectedTask, selectTask } = useFocus(store)

const formatEstimatedTime = (seconds: number | undefined): string => {
  if (!seconds || seconds <= 0) return '暂无预估'
  
  const days = Math.floor(seconds / (24 * 60 * 60))
  const remainingAfterDays = seconds % (24 * 60 * 60)
  const hours = Math.floor(remainingAfterDays / (60 * 60))
  const remainingAfterHours = remainingAfterDays % (60 * 60)
  const minutes = Math.floor(remainingAfterHours / 60)
  const secs = remainingAfterHours % 60
  
  const parts: string[] = []
  if (days > 0) parts.push(`${days}天`)
  if (hours > 0) parts.push(`${hours}时`)
  if (minutes > 0) parts.push(`${minutes}分`)
  if (secs > 0) parts.push(`${secs}秒`)
  
  return parts.join('') || '暂无预估'
}

const handleSearch = () => {
  searchTasks(1)
}

const handlePageChange = (page: number) => {
  taskSearchChangePage(page)
}
</script>

<template>
  <div class="modal-overlay" v-if="showTaskSearch">
    <div class="modal-content">
      <div class="modal-header">
        <h2>选择任务</h2>
        <button class="close-btn" @click="toggleTaskSearch">
          关闭
        </button>
      </div>
      
      <div class="search-box">
        <input 
          v-model="searchKeyword" 
          @input="handleSearch" 
          type="text" 
          placeholder="搜索任务..." 
          class="search-input"
        />
      </div>
      
      <div class="task-list-container">
        <div class="task-list">
          <div 
            v-for="task in tasks" 
            :key="task.id" 
            @click="selectTask(task)" 
            class="task-item"
            :class="{ selected: selectedTask()?.id === task.id }"
          >
            <div class="task-title">{{ task.title }}</div>
            <div class="task-meta">预估时间: {{ formatEstimatedTime(task.estimatedSeconds) }}</div>
            <div v-if="task.deadline" class="task-deadline">
              截止时间: {{ new Date(task.deadline).toLocaleString('zh-CN') }}
            </div>
          </div>
          <div v-if="tasks.length === 0" class="empty-state">
            暂无任务
          </div>
        </div>
      </div>
      
      <div class="pagination-wrapper">
        <Pagination 
          :current-page="taskSearchCurrentPage"
          :total-pages="taskSearchTotalPages"
          :total-items="taskSearchTotalItems"
          @page-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 24px;
  border-radius: 12px;
  width: 500px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  color: #111827;
}

.close-btn {
  padding: 6px 12px;
  background-color: #E5E7EB;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: #6B7280;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #D1D5DB;
}

.search-box {
  margin-bottom: 16px;
}

.search-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: var(--primary);
}

.task-list-container {
  flex: 1;
  max-height: 300px;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 6px;
  scrollbar-width: thin;
  scrollbar-color: #D1D5DB #E5E7EB;
}

.task-list-container::-webkit-scrollbar {
  width: 6px;
}

.task-list-container::-webkit-scrollbar-track {
  background: rgba(241, 245, 249, 0.5);
  border-radius: 3px;
}

.task-list-container::-webkit-scrollbar-thumb {
  background: #D1D5DB;
  border-radius: 3px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.task-item {
  padding: 14px;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background-color: transparent;
}

.task-item:hover {
  background-color: #f0f9ff;
}

.task-item.selected {
  background-color: #e3f2fd;
  border-color: #93c5fd;
}

.task-title {
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
}

.task-meta {
  font-size: 13px;
  color: #6B7280;
}

.task-deadline {
  font-size: 12px;
  color: var(--primary);
  margin-top: 4px;
}

.empty-state {
  text-align: center;
  color: #9CA3AF;
  padding: 30px 20px;
}

.pagination-wrapper {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #D1D5DB;
}
</style>
