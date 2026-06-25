<template>
  <button 
    @click="$emit('toggle-history')" 
    class="history-toggle"
  >
    <span>专注历史记录</span>
    <span class="toggle-icon" :class="{ rotated: showHistory }">▼</span>
  </button>
</template>

<script setup lang="ts">
interface Props {
  showHistory: boolean
}

defineProps<Props>()
defineEmits<{
  'toggle-history': []
}>()
</script>

<style scoped>
.history-toggle {
  width: 100%;
  padding: 14px 16px;
  background-color: #f8fafc;
  border: none;
  text-align: left;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: #334155;
  display: flex;
  justify-content: space-between;
  align-items: center;
  transition: background-color 0.2s;
}

.history-toggle:hover {
  background-color: #f1f5f9;
}

.toggle-icon {
  transition: transform 0.2s;
}

.toggle-icon.rotated {
  transform: rotate(180deg);
}
</style>