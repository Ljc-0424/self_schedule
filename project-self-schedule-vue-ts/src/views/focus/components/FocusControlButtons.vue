<template>
  <div class="focus-actions">
    <template v-if="!isFocusing">
      <button class="btn-primary" @click="$emit('start-focus')">
        开始专注
      </button>
    </template>
    <template v-else>
      <template v-if="!isPaused">
        <button class="btn-pause" @click="$emit('pause-focus')">
          暂停专注
        </button>
      </template>
      <template v-else>
        <button class="btn-resume" @click="$emit('resume-focus')">
          继续专注
        </button>
      </template>
      <button class="btn-primary" @click="$emit('stop-focus', true)">
        完成专注
      </button>
      <button class="btn-give-up" @click="$emit('give-up-focus')">
        放弃专注
      </button>
    </template>
  </div>
</template>

<script setup lang="ts">
interface Props {
  isFocusing: boolean
  isPaused: boolean
}

defineProps<Props>()
defineEmits<{
  'start-focus': []
  'pause-focus': []
  'resume-focus': []
  'stop-focus': [success: boolean]
  'give-up-focus': []
}>()
</script>

<style scoped>
.focus-actions {
  display: flex;
  justify-content: center;
  gap: 18px;
}

.btn-primary {
  padding: 15px 45px;
  background: #3F6212;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 5px 20px rgba(16, 185, 129, 0.4);
}

.btn-primary:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.5);
}

.btn-pause {
  padding: 15px 35px;
  background: #F59E0B;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 5px 20px rgba(245, 158, 11, 0.4);
}

.btn-pause:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(245, 158, 11, 0.5);
}

.btn-resume {
  padding: 15px 35px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 5px 20px rgba(var(--primary-rgb), 0.4);
}

.btn-resume:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(var(--primary-rgb), 0.5);
}

.btn-give-up {
  padding: 15px 35px;
  background: #B91C1C;
  color: white;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 5px 20px rgba(239, 68, 68, 0.4);
}

.btn-give-up:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(239, 68, 68, 0.5);
}

@media (max-width: 575px) {
  .focus-actions {
    gap: 10px;
    flex-wrap: wrap;
  }

  .btn-primary,
  .btn-pause,
  .btn-resume,
  .btn-give-up {
    padding: 12px 24px;
    font-size: 14px;
    border-radius: 10px;
  }
}
</style>