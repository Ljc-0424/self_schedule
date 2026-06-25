<template>
  <div v-if="isFocusing" class="notes-section">
    <div class="notes-label">专注笔记</div>
    <textarea 
      :value="focusNotes"
      @input="$emit('update-notes', ($event.target as HTMLTextAreaElement).value)"
      class="notes-input"
      placeholder="记录专注期间的想法..."
    ></textarea>
  </div>
</template>

<script setup lang="ts">
interface Props {
  isFocusing: boolean
  focusNotes: string
}

defineProps<Props>()
defineEmits<{
  'update-notes': [notes: string]
}>()
</script>

<style scoped>
.notes-section {
  margin-top: 25px;
}

.notes-label {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
  margin-bottom: 10px;
}

.notes-input {
  width: 100%;
  padding: 14px;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  background: #f8fafc;
  color: #111827;
  font-size: 14px;
  resize: vertical;
  min-height: 100px;
  transition: all 0.3s;
}

.notes-input:focus {
  outline: none;
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.notes-input::placeholder {
  color: #9CA3AF;
}
</style>