<script setup lang="ts">
import { user, isEditing, userForm, formatDate, getUserTags } from './useUserInfo'
</script>

<template>
  <div>
    <div style="margin-bottom: 20px;">
      <h3 style="margin: 0 0 12px; color: #333; border-bottom: 2px solid var(--primary); padding-bottom: 6px; font-size: 15px;">基本信息</h3>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px; margin-bottom: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">用户名</label>
        <span style="color: #333; font-size: 14px;">{{ user?.username }}</span>
      </div>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px; margin-bottom: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">昵称</label>
        <template v-if="!isEditing">
          <span style="color: #333; font-size: 14px;">{{ user?.nickname || '未设置' }}</span>
        </template>
        <input v-else v-model="userForm.nickname" type="text" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;"></input>
      </div>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px; margin-bottom: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">邮箱</label>
        <template v-if="!isEditing">
          <span style="color: #333; font-size: 14px;">{{ user?.email || '未设置' }}</span>
        </template>
        <input v-else v-model="userForm.email" type="email" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;"></input>
      </div>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">手机号</label>
        <template v-if="!isEditing">
          <span style="color: #333; font-size: 14px;">{{ user?.phone || '未设置' }}</span>
        </template>
        <input v-else v-model="userForm.phone" type="tel" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;"></input>
      </div>
    </div>
    
    <div style="margin-bottom: 20px;">
      <h3 style="margin: 0 0 12px; color: #333; border-bottom: 2px solid var(--primary); padding-bottom: 6px; font-size: 15px;">个人资料</h3>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px; margin-bottom: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">性别</label>
        <template v-if="!isEditing">
          <span style="color: #333; font-size: 14px;">{{ user?.gender || '未设置' }}</span>
        </template>
        <select v-else v-model="userForm.gender" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;">
          <option value="">请选择</option>
          <option value="男">男</option>
          <option value="女">女</option>
          <option value="保密">保密</option>
        </select>
      </div>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px; margin-bottom: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">生日</label>
        <template v-if="!isEditing">
          <span style="color: #333; font-size: 14px;">{{ formatDate(user?.birthday) }}</span>
        </template>
        <input v-else v-model="userForm.birthday" type="date" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;"></input>
      </div>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px; margin-bottom: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">职业</label>
        <template v-if="!isEditing">
          <span style="color: #333; font-size: 14px;">{{ user?.occupation || '未设置' }}</span>
        </template>
        <input v-else v-model="userForm.occupation" type="text" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;"></input>
      </div>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">城市</label>
        <template v-if="!isEditing">
          <span style="color: #333; font-size: 14px;">{{ user?.city || '未设置' }}</span>
        </template>
        <input v-else v-model="userForm.city" type="text" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;"></input>
      </div>
    </div>
    
    <div style="margin-bottom: 20px;">
      <h3 style="margin: 0 0 12px; color: #333; border-bottom: 2px solid var(--primary); padding-bottom: 6px; font-size: 15px;">兴趣爱好</h3>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px; margin-bottom: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">爱好</label>
        <template v-if="!isEditing">
          <span style="color: #333; font-size: 14px;">{{ user?.hobbies || '未设置' }}</span>
        </template>
        <input v-else v-model="userForm.hobbies" type="text" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;" placeholder="多个爱好用逗号分隔"></input>
      </div>
      
      <div>
        <label style="display: block; color: #666; margin-bottom: 8px; font-size: 14px;">个人简介</label>
        <template v-if="!isEditing">
          <div style="padding: 10px; background-color: #f8f9fa; border-radius: 5px; word-break: break-all; color: #333; font-size: 14px; line-height: 1.5;">{{ user?.bio || '未设置' }}</div>
        </template>
        <textarea v-else v-model="userForm.bio" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 5px; height: 80px; font-size: 13px; resize: vertical;" placeholder="介绍一下你自己..."></textarea>
      </div>
    </div>
    
    <div style="margin-bottom: 20px;">
      <h3 style="margin: 0 0 12px; color: #333; border-bottom: 2px solid var(--primary); padding-bottom: 6px; font-size: 15px;">个性化设置</h3>
      
      <div style="display: grid; grid-template-columns: 90px 1fr; gap: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">个人标签</label>
        <template v-if="!isEditing">
          <div v-if="getUserTags(user?.settings).length > 0" style="display: flex; gap: 5px; flex-wrap: wrap;">
            <span v-for="(tag, index) in getUserTags(user?.settings)" :key="index" style="background-color: #dbeafe; color: var(--primary-darker); padding: 2px 8px; border-radius: 10px; font-size: 11px;">#{{ tag }}</span>
          </div>
          <span v-else style="color: #333; font-size: 14px;">未设置</span>
        </template>
        <input v-else v-model="userForm.settings" type="text" style="padding: 6px 10px; border: 1px solid #ddd; border-radius: 5px; font-size: 13px;" placeholder="多个标签用分号分隔"></input>
      </div>
    </div>
    
    <div>
      <h3 style="margin: 0 0 12px; color: #333; border-bottom: 2px solid var(--primary); padding-bottom: 6px; font-size: 15px;">账户状态</h3>
      
      <div style="display: grid; grid-template-columns: 100px 1fr; gap: 10px;">
        <label style="color: #666; display: flex; align-items: center; font-size: 14px;">当前状态</label>
        <span :style="{ color: '#4CAF50', fontWeight: '500', fontSize: '14px' }">正常</span>
      </div>
    </div>
  </div>
</template>
