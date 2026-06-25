<script setup lang="ts">
import { ref, onMounted } from 'vue'

defineProps<{
  title: string
  subtitle?: string
  icon?: string
}>()

const emit = defineEmits<{
  (e: 'back'): void
}>()

const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 50)
})
</script>

<template>
  <div :class="['compact-header', { 'visible': isVisible }]">
    <div class="header-left">
      <div class="header-icon" v-if="icon">
        {{ icon }}
      </div>
      <div class="header-text">
        <h1 class="header-title">{{ title }}</h1>
        <p v-if="subtitle" class="header-subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div class="header-right">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<style scoped>
.compact-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: var(--primary);
  border-radius: 16px;
  color: white;
  margin-bottom: 16px;
  opacity: 0;
  transform: translateY(10px);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.compact-header.visible {
  opacity: 1;
  transform: translateY(0);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 24px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
}

.header-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  line-height: 1.2;
}

.header-subtitle {
  font-size: 13px;
  opacity: 0.9;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式 */
@media (max-width: 768px) {
  .compact-header {
    padding: 14px 18px;
    border-radius: 14px;
  }

  .header-icon {
    width: 36px;
    height: 36px;
    font-size: 20px;
  }

  .header-title {
    font-size: 16px;
  }

  .header-subtitle {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .compact-header {
    padding: 12px 16px;
    border-radius: 12px;
  }

  .header-icon {
    display: none;
  }

  .header-title {
    font-size: 15px;
  }
}
</style>
