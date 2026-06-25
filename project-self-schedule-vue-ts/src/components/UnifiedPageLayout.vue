<script setup lang="ts">
import { ref, onMounted } from 'vue'

const props = defineProps<{
  title?: string
  subtitle?: string
  badge?: string
  badgeIcon?: string
  showHeader?: boolean
}>()

const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<template>
  <div class="unified-page">
    <!-- 页面头部 -->
    <div
      v-if="showHeader !== false"
      :class="['unified-page-header', { 'visible': isVisible }]"
    >
      <div class="header-content">
        <div v-if="badge" class="header-badge">
          <span class="badge-icon">{{ badgeIcon }}</span>
          <span class="badge-text">{{ badge }}</span>
        </div>
        <h1 v-if="title" class="page-title">{{ title }}</h1>
        <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
      </div>
      <div class="header-actions">
        <slot name="header-actions"></slot>
      </div>
    </div>

    <!-- 页面内容 -->
    <div :class="['unified-page-content', { 'visible': isVisible }]">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
.unified-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  min-height: calc(100vh - 100px);
}

/* 入场动画 */
.unified-page-header,
.unified-page-content {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.unified-page-header.visible,
.unified-page-content.visible {
  opacity: 1;
  transform: translateY(0);
}

.unified-page-content {
  transition-delay: 0.1s;
}

/* 页面头部 */
.unified-page-header {
  width: 100%;
  background: var(--primary);
  border-radius: 20px;
  padding: 28px;
  color: white;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unified-page-header::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 250px;
  height: 250px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.header-content {
  position: relative;
  z-index: 1;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  margin-bottom: 12px;
}

.badge-icon {
  font-size: 16px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px;
}

.page-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.header-actions {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 页面内容 */
.unified-page-content {
  width: 100%;
}

/* 响应式 */
@media (max-width: 768px) {
  .unified-page {
    padding: 16px;
  }

  .unified-page-header {
    flex-direction: column;
    gap: 20px;
    padding: 24px;
  }

  .page-title {
    font-size: 24px;
  }

  .header-actions {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .unified-page {
    padding: 12px;
  }

  .unified-page-header {
    padding: 20px;
  }

  .page-title {
    font-size: 22px;
  }

  .header-badge {
    font-size: 12px;
    padding: 5px 12px;
  }
}
</style>
