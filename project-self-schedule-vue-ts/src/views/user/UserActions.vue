<script setup lang="ts">
import { isEditing, startEdit, saveUser, cancelEdit } from './useUserInfo'
</script>

<template>
  <div style="display: flex; gap: 12px; padding-top: 20px; border-top: 1px solid #D1D5DB;">
    <button v-if="!isEditing" @click="startEdit" style="flex: 1; padding: 12px; background-color: var(--primary); color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 500; transition: background-color 0.2s;"
            @mouseenter="($event.currentTarget as HTMLElement).style.backgroundColor = '#4338CA'"
            @mouseleave="($event.currentTarget as HTMLElement).style.backgroundColor = '#3B82F6'">编辑资料</button>

    <div v-else style="flex: 1; display: flex; gap: 12px;">
      <button @click="saveUser" style="flex: 1; padding: 12px; background-color: var(--primary); color: white; border: none; border-radius: 8px; cursor: pointer; font-size: 14px; font-weight: 500; transition: background-color 0.2s;"
              @mouseenter="($event.currentTarget as HTMLElement).style.backgroundColor = '#4338CA'"
              @mouseleave="($event.currentTarget as HTMLElement).style.backgroundColor = '#3B82F6'">保存修改</button>
      <button @click="cancelEdit" style="padding: 12px 24px; background-color: #F9FAFB; color: #6B7280; border: 1px solid #D1D5DB; border-radius: 8px; cursor: pointer; font-size: 14px; transition: background-color 0.2s;"
              @mouseenter="($event.currentTarget as HTMLElement).style.backgroundColor = '#E5E7EB'"
              @mouseleave="($event.currentTarget as HTMLElement).style.backgroundColor = '#F9FAFB'">取消</button>
    </div>
  </div>
</template>
