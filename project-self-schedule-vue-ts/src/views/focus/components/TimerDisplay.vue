<template>
  <div class="timer-display">
    <div class="timer-box">
      <div class="timer-text">{{ formattedTime }}</div>
    </div>
    <div v-if="isFocusing" class="progress-bar">
      <div class="progress-fill"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  formattedTime: string
  isFocusing: boolean
}

defineProps<Props>()
</script>

<style scoped>
.timer-display {
  text-align: center;
  margin-bottom: 25px;
}

.timer-box {
  display: inline-block;
  background: rgba(var(--primary-rgb), 0.06);
  backdrop-filter: blur(10px);
  padding: 30px 55px;
  border-radius: 16px;
  border: 1px solid #D1D5DB;
}

.timer-text {
  font-size: 72px;
  font-weight: bold;
  color: #111827;
  font-family: 'SF Mono', 'Courier New', monospace;
  letter-spacing: 8px;
}

.progress-bar {
  width: 80%;
  max-width: 400px;
  height: 6px;
  background-color: rgba(var(--primary-rgb), 0.1);
  border-radius: 3px;
  margin: 20px auto 0;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 3px;
  animation: progress 5s linear infinite;
}

@keyframes progress {
  from { width: 100%; }
  to { width: 0%; }
}

@media (max-width: 575px) {
  .timer-box {
    padding: 18px 28px;
    border-radius: 12px;
  }

  .timer-text {
    font-size: 42px;
    letter-spacing: 4px;
  }

  .timer-display {
    margin-bottom: 14px;
  }

  .progress-bar {
    max-width: 260px;
    margin-top: 12px;
  }
}
</style>