<script setup lang="ts">
import { Loading } from '@element-plus/icons-vue'

defineProps<{
  text?: string
  size?: 'sm' | 'md' | 'lg'
}>()
</script>

<template>
  <div class="loading-container">
    <el-icon class="loading-icon" :size="size === 'sm' ? 24 : size === 'lg' ? 64 : 40">
      <Loading />
    </el-icon>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<style scoped>
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.loading-icon {
  animation: spin 1s linear infinite;
  color: var(--primary);
}

.loading-text {
  color: #9CA3AF;
  margin-top: 12px;
  font-size: 14px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>