<script setup lang="ts">
import { reminderForm, advanceMinuteOptions, toggleAdvanceMinute, addCustomAdvanceMinute, customAdvanceMinutes, reminderError } from './useTasks'
</script>

<template>
  <div class="reminder-settings">
    <div class="option-group">
      <label>提前提醒时间</label>
      <div class="checkbox-group">
        <label 
          v-for="minute in advanceMinuteOptions" 
          :key="minute.value" 
          class="checkbox-item"
          :class="{ active: reminderForm.advanceMinutes.includes(minute.value) }"
        >
          <input 
            type="checkbox" 
            :checked="reminderForm.advanceMinutes.includes(minute.value)" 
            @change="toggleAdvanceMinute(minute.value)"
          />
          <span>{{ minute.label }}</span>
        </label>
      </div>
      
      <!-- 自定义提前时间输入 -->
      <div class="custom-input-group">
        <input 
          v-model.number="customAdvanceMinutes" 
          type="number" 
          min="1" 
          max="43200"
          placeholder="自定义分钟数（1-43200，即30天）"
          class="custom-input"
          @keyup.enter="addCustomAdvanceMinute"
        />
        <button @click="addCustomAdvanceMinute" class="custom-add-btn">
          + 添加
        </button>
      </div>
      
      <!-- 错误提示 -->
      <div v-if="reminderError" class="error-message">
        {{ reminderError }}
      </div>
      
      <div class="hint">在提醒时间前提前通知的时间，可多选。未选择则仅在提醒时间点通知</div>
    </div>
  </div>
</template>

<style scoped>
.reminder-settings {
  background: #F9FAFB;
  padding: 16px;
  border-radius: 8px;
}

.option-group {
  margin-bottom: 12px;
}

.option-group:last-child {
  margin-bottom: 0;
}

.option-group label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 8px;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: white;
  border: 1px solid #D1D5DB;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
}

.checkbox-item.active {
  border-color: var(--primary);
  background-color: #EEF2FF;
}

.hint {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 6px;
}

.error-message {
  font-size: 12px;
  color: #B91C1C;
  margin-top: 8px;
  padding: 8px 12px;
  background: #fef2f2;
  border-radius: 4px;
  border: 1px solid #fee2e2;
}

.custom-input-group {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.custom-input {
  flex: 1;
  max-width: 150px;
  padding: 6px 12px;
  border: 1px solid #D1D5DB;
  border-radius: 4px;
  font-size: 12px;
  outline: none;
  transition: border-color 0.2s;
}

.custom-input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.1);
}

.custom-add-btn {
  padding: 6px 16px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.custom-add-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(var(--primary-rgb), 0.3);
}
</style>