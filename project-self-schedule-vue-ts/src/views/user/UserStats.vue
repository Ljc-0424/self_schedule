<script setup lang="ts">
import { user, formatDateTime } from './useUserInfo'
</script>

<template>
  <div style="padding: 15px; background-color: #f8f9fa; border-radius: 8px; margin-bottom: 28px;">
    <div style="display: flex; justify-content: space-between; flex-wrap: wrap; gap: 10px;">
      <div>
        <span style="font-size: 12px; color: #999;">注册时间</span>
        <div style="font-size: 12px; color: #888; margin-top: 2px;">{{ formatDateTime(user?.createdTime) }}</div>
      </div>
      <div>
        <span style="font-size: 12px; color: #999;">最后登录</span>
        <div style="font-size: 12px; color: #888; margin-top: 2px;">{{ user?.lastLoginTime ? formatDateTime(user.lastLoginTime) : '从未登录' }}</div>
      </div>
      <div>
        <span style="font-size: 12px; color: #999;">最后更新</span>
        <div style="font-size: 12px; color: #888; margin-top: 2px;">{{ formatDateTime(user?.updatedTime) }}</div>
      </div>
    </div>
  </div>
</template>
