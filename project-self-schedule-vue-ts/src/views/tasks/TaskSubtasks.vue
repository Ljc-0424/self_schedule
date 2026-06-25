<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { SubtaskVO } from '../../types'
import { subtaskApi } from '../../api/subtaskApi'

const props = defineProps<{
  taskId: number
}>()

const subtasks = ref<SubtaskVO[]>([])
const newSubtaskTitle = ref('')
const isAdding = ref(false)

const loadSubtasks = async () => {
  try {
    const response = await subtaskApi.getByTaskId(props.taskId)
    if (response.data.code === 200) {
      subtasks.value = response.data.data
    }
  } catch (e) {
    ElMessage.error('加载子任务失败')
  }
}

const addSubtask = async () => {
  if (!newSubtaskTitle.value.trim()) return
  
  const title = newSubtaskTitle.value.trim()
  newSubtaskTitle.value = ''
  isAdding.value = false

  const tempId = -Date.now()
  const optimistic: SubtaskVO = { id: tempId, taskId: props.taskId, title, status: 1, sortOrder: subtasks.value.length, createdTime: new Date().toISOString(), completedTime: null }
  subtasks.value.push(optimistic)

  try {
    const response = await subtaskApi.create(props.taskId, title)
    if (response.data.code === 200) {
      const idx = subtasks.value.findIndex(s => s.id === tempId)
      if (idx !== -1) subtasks.value[idx] = response.data.data
    } else {
      subtasks.value = subtasks.value.filter(s => s.id !== tempId)
      ElMessage.error('添加子任务失败')
    }
  } catch (e) {
    subtasks.value = subtasks.value.filter(s => s.id !== tempId)
    ElMessage.error('添加子任务失败')
  }
}

const toggleComplete = async (subtaskId: number) => {
  const index = subtasks.value.findIndex(s => s.id === subtaskId)
  if (index === -1) return

  const prevStatus = subtasks.value[index].status
  const newStatus = prevStatus === 2 ? 1 : 2
  subtasks.value[index] = { ...subtasks.value[index], status: newStatus }

  try {
    const response = await subtaskApi.complete(subtaskId)
    if (response.data.code === 200) {
      subtasks.value[index] = response.data.data
    } else {
      subtasks.value[index] = { ...subtasks.value[index], status: prevStatus }
    }
  } catch (e) {
    subtasks.value[index] = { ...subtasks.value[index], status: prevStatus }
    ElMessage.error('操作失败')
  }
}

const deleteSubtask = async (subtaskId: number) => {
  if (!confirm('确定删除这个子任务吗？')) return

  const prev = [...subtasks.value]
  subtasks.value = subtasks.value.filter(s => s.id !== subtaskId)

  try {
    const response = await subtaskApi.delete(subtaskId)
    if (response.data.code !== 200) {
      subtasks.value = prev
    }
  } catch (e) {
    subtasks.value = prev
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadSubtasks()
})
</script>

<template>
  <div class="subtasks-container">
    <!-- 添加子任务 -->
    <div v-if="!isAdding" class="add-subtask" @click="isAdding = true">
      <span class="add-icon"></span>
      <span class="add-text">添加子任务</span>
    </div>
    
    <div v-else class="add-subtask-form">
      <input 
        v-model="newSubtaskTitle" 
        type="text" 
        class="subtask-input" 
        placeholder="输入子任务标题..."
        @keyup.enter="addSubtask"
        autofocus
      />
      <div class="form-actions">
        <button class="btn btn-sm btn-primary" @click="addSubtask">添加</button>
        <button class="btn btn-sm btn-secondary" @click="isAdding = false; newSubtaskTitle = ''">取消</button>
      </div>
    </div>
    
    <!-- 子任务列表 -->
    <div v-if="subtasks.length > 0" class="subtasks-list">
      <div v-for="subtask in subtasks" :key="subtask.id" class="subtask-item">
        <button 
          class="subtask-checkbox" 
          :class="{ completed: subtask.status === 2 }"
          @click="toggleComplete(subtask.id)"
        >
          {{ subtask.status === 2 ? '✓' : '' }}
        </button>
        <span :class="{ completed: subtask.status === 2 }" class="subtask-title">
          {{ subtask.title }}
        </span>
        <button class="subtask-delete" @click="deleteSubtask(subtask.id)">
        </button>
      </div>
    </div>
    
    <div v-else-if="!isAdding" class="empty-subtasks">
      <p>暂无子任务</p>
    </div>
  </div>
</template>

<style scoped>
.subtasks-container {
  padding: 15px 15px 15px 35px;
  background-color: rgba(0, 0, 0, 0.02);
  border-top: 1px solid #D1D5DB;
}

.add-subtask {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background-color: white;
  border-radius: 8px;
  border: 1px dashed #D1D5DB;
  cursor: pointer;
  transition: all 0.2s ease;
}

.add-subtask:hover {
  border-color: var(--primary);
  background-color: rgba(var(--primary-rgb), 0.05);
}

.add-icon {
  font-size: 14px;
  color: #9CA3AF;
}

.add-text {
  font-size: 13px;
  color: #6B7280;
}

.add-subtask-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.subtask-input {
  padding: 10px 14px;
  border: 2px solid var(--primary);
  border-radius: 8px;
  font-size: 13px;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.subtask-input:focus {
  outline: none;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.btn {
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-sm {
  padding: 5px 12px;
  font-size: 12px;
}

.btn-primary {
  background: var(--primary);
  color: white;
}

.btn-secondary {
  background: #E5E7EB;
  color: #374151;
}

.subtasks-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.subtask-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background-color: white;
  border-radius: 8px;
  border: 1px solid #D1D5DB;
}

.subtask-checkbox {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid #D1D5DB;
  background-color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: white;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.subtask-checkbox:hover {
  border-color: var(--primary);
}

.subtask-checkbox.completed {
  background-color: #3F6212;
  border-color: #3F6212;
}

.subtask-title {
  flex: 1;
  font-size: 13px;
  color: #111827;
  transition: all 0.2s ease;
}

.subtask-title.completed {
  text-decoration: line-through;
  color: #9CA3AF;
}

.subtask-delete {
  width: 26px;
  height: 26px;
  border: none;
  border-radius: 5px;
  background-color: transparent;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.2s ease;
}

.subtask-item:hover .subtask-delete {
  opacity: 1;
}

.subtask-delete:hover {
  background-color: #fee2e2;
}

.empty-subtasks {
  text-align: center;
  padding: 20px;
  color: #9CA3AF;
  font-size: 13px;
}
</style>
