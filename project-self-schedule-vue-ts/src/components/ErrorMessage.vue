<script setup lang="ts">
defineProps<{
  message: string
  type?: 'error' | 'warning' | 'info'
  showIcon?: boolean
  closable?: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()
</script>

<template>
  <el-alert
    :title="message"
    :type="type || 'error'"
    :closable="closable !== false"
    :show-icon="showIcon !== false"
    @close="emit('close')"
    style="margin-bottom: 12px;"
  />
</template>
