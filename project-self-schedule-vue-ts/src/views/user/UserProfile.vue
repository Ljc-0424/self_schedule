<script setup lang="ts">
import { user, statusText, statusColor, handleAvatarUpload, handleAvatarDelete } from './useUserInfo'
</script>

<template>
  <div style="display: flex; align-items: center; padding: 20px; background: var(--primary);">
    <div style="position: relative; width: 100px; height: 100px; margin-right: 20px;">
      <div style="width: 100%; height: 100%; border-radius: 50%; background-color: white; display: flex; align-items: center; justify-content: center; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.15);">
        <img v-if="user?.avatarUrl" :src="user.avatarUrl" alt="头像" style="width: 100%; height: 100%; object-fit: cover;"></img>
        <span v-else style="font-size: 40px; color: var(--primary);">{{ user?.nickname?.charAt(0) || user?.username?.charAt(0) || '?' }}</span>
      </div>
      <div style="position: absolute; bottom: 0; left: 0; right: 0; background: linear-gradient(transparent, rgba(0,0,0,0.5)); padding: 15px 5px 5px; border-radius: 0 0 50% 50%; opacity: 0; transition: opacity 0.2s; cursor: pointer;"
           @mouseenter="($event.currentTarget as HTMLElement).style.opacity = '1'"
           @mouseleave="($event.currentTarget as HTMLElement).style.opacity = '0'">
        <input type="file" accept="image/*" @change="handleAvatarUpload" style="display: none;" id="avatar-upload"></input>
        <label for="avatar-upload" style="cursor: pointer; color: white; font-size: 10px; display: block;">更换头像</label>
      </div>
    </div>
    <div style="flex: 1;">
      <h2 style="color: white; margin: 0 0 4px; font-size: 20px; font-weight: 600;">{{ user?.nickname || user?.username }}</h2>
      <p style="color: rgba(255,255,255,0.85); margin: 0 0 8px; font-size: 13px;">@{{ user?.username }}</p>
      <span :style="{ display: 'inline-block', padding: '3px 10px', backgroundColor: 'rgba(255,255,255,0.2)', borderRadius: '6px', color: 'white', fontSize: '11px' }">{{ statusText }}</span>
    </div>
    <div v-if="user?.avatarUrl">
      <button @click="handleAvatarDelete" style="padding: 6px 14px; background-color: rgba(255,255,255,0.2); color: white; border: none; border-radius: 6px; font-size: 12px; cursor: pointer; transition: background-color 0.2s;"
              @mouseenter="($event.currentTarget as HTMLElement).style.backgroundColor = 'rgba(255,255,255,0.3)'"
              @mouseleave="($event.currentTarget as HTMLElement).style.backgroundColor = 'rgba(255,255,255,0.2)'">删除头像</button>
    </div>
  </div>
</template>
