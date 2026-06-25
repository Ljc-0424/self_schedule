<script setup lang="ts">
import { ref } from 'vue'
import { taskForm, estimatedDays, estimatedHours, estimatedMinutes, estimatedSeconds } from './useTasks'

// 默认隐藏预估完成时间
const showEstimatedTime = ref(false)

const toggleEstimatedTime = () => {
  showEstimatedTime.value = !showEstimatedTime.value
}
</script>

<template>
  <div class="time-settings">
    <div class="section-title">时间设置</div>
    
    <div class="time-item">
      <label>截止时间</label>
      <input 
        v-model="taskForm.deadline" 
        type="datetime-local" 
        class="datetime-input"
        placeholder="有DDL的任务请设置"
      />
      <div class="hint">有期限任务，显示截止倒计时、逾期判断</div>
    </div>
    
    <div class="time-item">
      <label>提醒时间</label>
      <input 
        v-model="taskForm.remindTime" 
        type="datetime-local" 
        class="datetime-input"
        placeholder="纯日程提醒请设置"
      />
      <div class="hint">纯日程提醒，无截止压力，到点弹窗提醒</div>
    </div>
  </div>
  
  <!-- 预估完成时间 - 默认隐藏，通过按钮控制 -->
  <div class="estimated-time-toggle">
    <button 
      @click="toggleEstimatedTime" 
      class="toggle-btn"
      :class="{ expanded: showEstimatedTime }"
    >
      <span class="toggle-icon">{{ showEstimatedTime ? '▼' : '▶' }}</span>
      <span>{{ showEstimatedTime ? '收起' : '设置' }}预估完成时间</span>
    </button>
    
    <div v-if="showEstimatedTime" class="estimated-time-content">
      <div class="time-input-group">
        <div class="time-block">
          <input 
            v-model.number="estimatedDays" 
            type="number" 
            min="0" 
            max="365" 
            class="time-input"
            placeholder="0"
          />
          <div class="time-label">天</div>
        </div>
        <span class="time-separator">:</span>
        <div class="time-block">
          <input 
            v-model.number="estimatedHours" 
            type="number" 
            min="0" 
            max="23" 
            class="time-input"
            placeholder="0"
          />
          <div class="time-label">时</div>
        </div>
        <span class="time-separator">:</span>
        <div class="time-block">
          <input 
            v-model.number="estimatedMinutes" 
            type="number" 
            min="0" 
            max="59" 
            class="time-input"
            placeholder="0"
          />
          <div class="time-label">分</div>
        </div>
        <span class="time-separator">:</span>
        <div class="time-block">
          <input 
            v-model.number="estimatedSeconds" 
            type="number" 
            min="0" 
            max="59" 
            class="time-input"
            placeholder="0"
          />
          <div class="time-label">秒</div>
        </div>
      </div>
      <div class="hint">设置后可用于专注计时的默认时长</div>
    </div>
  </div>
</template>

<style scoped>
.time-settings {
  background: #F9FAFB;
  padding: 16px;
  border-radius: 8px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin-bottom: 12px;
}

.time-item {
  margin-bottom: 12px;
}

.time-item:last-child {
  margin-bottom: 0;
}

.time-item label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.datetime-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-size: 13px;
}

.hint {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 4px;
}

/* 预估完成时间切换区域 */
.estimated-time-toggle {
  margin-top: 16px;
}

.toggle-btn {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: #E5E7EB;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  font-size: 14px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background: #E5E7EB;
}

.toggle-btn.expanded {
  background: #EEF2FF;
  border-color: var(--primary);
  color: var(--primary);
}

.toggle-icon {
  font-size: 12px;
  transition: transform 0.2s;
}

.estimated-time-content {
  margin-top: 12px;
  padding: 16px;
  background: #F9FAFB;
  border-radius: 8px;
  border-left: 3px solid var(--primary);
}

.time-input-group {
  display: flex;
  gap: 6px;
  align-items: center;
}

.time-block {
  flex: 1;
  text-align: center;
}

.time-input {
  width: 100%;
  padding: 10px 6px;
  text-align: center;
  font-size: 14px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
}

.time-label {
  font-size: 12px;
  color: #6B7280;
  margin-top: 4px;
}

.time-separator {
  font-size: 16px;
  font-weight: bold;
  color: #999;
}
</style>