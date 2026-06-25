<script setup lang="ts">
import { ref, watch } from 'vue'
import { 
  showDetail, 
  selectedRecord, 
  isLoadingDetail, 
  closeDetail,
  updateFocusRecordNotes
} from './useFocus'
import FocusRecordInfo from './FocusRecordInfo.vue'
import TaskInfoCard from './TaskInfoCard.vue'

const notes = ref('')
const isSaving = ref(false)

watch(() => selectedRecord.value, (record) => {
  if (record) {
    notes.value = record.notes || ''
  }
}, { immediate: true })

const handleNotesChange = async () => {
  if (selectedRecord.value) {
    isSaving.value = true
    await updateFocusRecordNotes(selectedRecord.value.id!, notes.value)
    isSaving.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <div v-if="showDetail" class="modal-overlay" @click.self="closeDetail">
      <div class="modal-content">
        <div class="modal-header">
          <h2>专注记录详情</h2>
          <button class="close-btn" @click="closeDetail">✕</button>
        </div>
        
        <div class="modal-body">
          <div v-if="isLoadingDetail" class="loading">
            加载中...
          </div>
          
          <div v-else-if="selectedRecord" class="detail-content">
            <FocusRecordInfo :record="selectedRecord" />
            
            <TaskInfoCard v-if="selectedRecord.task" :task="selectedRecord.task" />
            
            <div v-else class="section no-task">
              <h3>关联任务</h3>
              <p class="empty-text">该专注记录未关联任何任务</p>
            </div>
            
            <div class="section notes-section">
              <h3>专注笔记</h3>
              <textarea 
                v-model="notes"
                @blur="handleNotesChange"
                class="notes-input"
                placeholder="记录专注期间的想法..."
              ></textarea>
              <div v-if="isSaving" class="saving-text">保存中...</div>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeDetail">关闭</button>
        </div>
      </div>
    </div>
  </Teleport>
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
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #6b7280;
  padding: 5px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background-color: #f3f4f6;
}

.modal-body {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(80vh - 120px);
}

.loading {
  text-align: center;
  padding: 40px;
  color: #6b7280;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section {
  background-color: #fafafa;
  border-radius: 8px;
  padding: 16px;
}

.section h3 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #374151;
}

.empty-text {
  color: #9ca3af;
  font-size: 14px;
  padding: 20px;
  text-align: center;
}

.notes-section {
  margin-top: 16px;
}

.notes-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background-color: #f9fafb;
  color: #374151;
  font-size: 14px;
  resize: vertical;
  min-height: 100px;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.notes-input:focus {
  outline: none;
  border-color: var(--primary);
  background-color: white;
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.notes-input::placeholder {
  color: #9ca3af;
}

.saving-text {
  font-size: 13px;
  color: var(--primary);
  margin-top: 8px;
  text-align: right;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.btn-secondary {
  padding: 8px 16px;
  background-color: #f3f4f6;
  color: #374151;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn-secondary:hover {
  background-color: #e5e7eb;
}
</style>