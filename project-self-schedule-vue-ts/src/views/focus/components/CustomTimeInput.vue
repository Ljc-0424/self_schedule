<template>
  <div class="custom-time">
    <div class="time-label">⌚ 自定义时长</div>
    <div v-if="error" class="error-hint">
      {{ error }}
    </div>
    <div class="time-inputs">
      <div class="time-block">
        <input 
          v-model.number="localHours" 
          type="number" 
          min="0" 
          max="48" 
          class="time-input"
          placeholder="0"
        />
        <div class="time-unit">时</div>
      </div>
      <span class="time-separator">:</span>
      <div class="time-block">
        <input 
          v-model.number="localMinutes" 
          type="number" 
          min="0" 
          max="59" 
          class="time-input"
          placeholder="0"
        />
        <div class="time-unit">分</div>
      </div>
      <span class="time-separator">:</span>
      <div class="time-block">
        <input 
          v-model.number="localSeconds" 
          type="number" 
          min="0" 
          max="59" 
          class="time-input"
          placeholder="0"
        />
        <div class="time-unit">秒</div>
      </div>
      <button class="btn-set" @click="$emit('set-custom-time', localHours, localMinutes, localSeconds)">
        确认设置
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  customHours: number
  customMinutes: number
  customSeconds: number
  error: string
}

const props = defineProps<Props>()
defineEmits<{
  'set-custom-time': [hours: number, minutes: number, seconds: number]
}>()

const localHours = ref(props.customHours)
const localMinutes = ref(props.customMinutes)
const localSeconds = ref(props.customSeconds)

// 监听父组件传入的值变化
watch(() => props.customHours, (newVal) => {
  localHours.value = newVal
})
watch(() => props.customMinutes, (newVal) => {
  localMinutes.value = newVal
})
watch(() => props.customSeconds, (newVal) => {
  localSeconds.value = newVal
})
</script>

<style scoped>
.custom-time {
  margin-bottom: 25px;
}

.time-label {
  text-align: center;
  margin-bottom: 8px;
  color: #475569;
  font-size: 14px;
  font-weight: 500;
}

.error-hint {
  text-align: center;
  margin-bottom: 12px;
  padding: 10px 15px;
  background-color: rgba(245, 158, 11, 0.1);
  border: 1px solid rgba(245, 158, 11, 0.3);
  border-radius: 8px;
  color: #b45309;
  font-size: 13px;
  font-weight: 500;
  animation: shake 0.3s ease;
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

.time-inputs {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.time-block {
  text-align: center;
}

.time-input {
  width: 75px;
  padding: 13px;
  text-align: center;
  font-size: 19px;
  font-weight: bold;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  background: #F9FAFB;
  color: #111827;
  transition: all 0.3s;
}

.time-input:focus {
  outline: none;
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.time-input::placeholder {
  color: #D1D5DB;
}

.time-unit {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 5px;
}

.time-separator {
  color: #9CA3AF;
  font-size: 26px;
  font-weight: bold;
}

.btn-set {
  padding: 13px 28px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
  margin-left: 15px;
  box-shadow: 0 4px 15px rgba(var(--primary-rgb), 0.3);
}

.btn-set:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(var(--primary-rgb), 0.4);
}

@media (max-width: 575px) {
  .time-inputs {
    flex-wrap: wrap;
    gap: 8px;
  }

  .time-input {
    width: 60px;
    padding: 10px 6px;
    font-size: 16px;
  }

  .time-separator {
    font-size: 20px;
  }

  .btn-set {
    width: 100%;
    margin-left: 0;
    padding: 10px 16px;
    margin-top: 4px;
  }
}
</style>