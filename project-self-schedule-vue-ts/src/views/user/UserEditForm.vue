<script setup lang="ts">
import { computed } from 'vue'
import { user, isEditing, userForm, formatDate, getUserTags, startEdit } from './useUserInfo'

const props = defineProps<{
  mode?: 'profile' | 'settings'
}>()

const isProfile = computed(() => !props.mode || props.mode === 'profile')
const isSettings = computed(() => props.mode === 'settings')

const handleFieldClick = () => {
  if (!isEditing.value) {
    startEdit()
  }
}
</script>

<template>
  <div class="edit-form">
    <!-- 基本信息 -->
    <div v-if="isProfile" class="form-section">
      <div class="section-header">
        <span class="section-icon"></span>
        <h4>基本信息</h4>
      </div>
      <div class="form-grid">
        <div class="form-group">
          <label>用户名</label>
          <div class="field-value readonly">{{ user?.username }}</div>
        </div>
        <div class="form-group" @click="handleFieldClick">
          <label>昵称</label>
          <template v-if="!isEditing">
            <div class="field-value editable">{{ user?.nickname || '未设置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input v-else v-model="userForm.nickname" type="text" class="field-input" placeholder="请输入昵称" />
        </div>
        <div class="form-group" @click="handleFieldClick">
          <label>邮箱</label>
          <template v-if="!isEditing">
            <div class="field-value editable">{{ user?.email || '未设置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input v-else v-model="userForm.email" type="email" class="field-input" placeholder="请输入邮箱" />
        </div>
        <div class="form-group" @click="handleFieldClick">
          <label>手机</label>
          <template v-if="!isEditing">
            <div class="field-value editable">{{ user?.phone || '未设置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input v-else v-model="userForm.phone" type="tel" class="field-input" placeholder="请输入手机号" />
        </div>
      </div>
    </div>

    <!-- 个人资料 -->
    <div v-if="isProfile" class="form-section">
      <div class="section-header">
        <span class="section-icon"></span>
        <h4>个人资料</h4>
      </div>
      <div class="form-grid">
        <div class="form-group" @click="handleFieldClick">
          <label>性别</label>
          <template v-if="!isEditing">
            <div class="field-value editable">{{ user?.gender || '未设置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <select v-else v-model="userForm.gender" class="field-select">
            <option value="">请选择</option>
            <option value="男">男</option>
            <option value="女">女</option>
            <option value="保密">保密</option>
          </select>
        </div>
        <div class="form-group" @click="handleFieldClick">
          <label>生日</label>
          <template v-if="!isEditing">
            <div class="field-value editable">{{ formatDate(user?.birthday) }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input v-else v-model="userForm.birthday" type="date" class="field-input" />
        </div>
        <div class="form-group" @click="handleFieldClick">
          <label>职业</label>
          <template v-if="!isEditing">
            <div class="field-value editable">{{ user?.occupation || '未设置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input v-else v-model="userForm.occupation" type="text" class="field-input" placeholder="请输入职业" />
        </div>
        <div class="form-group" @click="handleFieldClick">
          <label>城市</label>
          <template v-if="!isEditing">
            <div class="field-value editable">{{ user?.city || '未设置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input v-else v-model="userForm.city" type="text" class="field-input" placeholder="请输入城市" />
        </div>
      </div>
    </div>

    <!-- 兴趣爱好 -->
    <div v-if="isProfile" class="form-section">
      <div class="section-header">
        <span class="section-icon"></span>
        <h4>兴趣爱好</h4>
      </div>
      <div class="form-row">
        <div class="form-group full-width" @click="handleFieldClick">
          <label>爱好</label>
          <template v-if="!isEditing">
            <div class="field-value editable">{{ user?.hobbies || '未设置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input v-else v-model="userForm.hobbies" type="text" class="field-input" placeholder="多个爱好用逗号分隔" />
        </div>
        <div class="form-group full-width" @click="handleFieldClick">
          <label>简介</label>
          <template v-if="!isEditing">
            <div class="field-value textarea-value editable">{{ user?.bio || '未设置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <textarea v-else v-model="userForm.bio" class="field-textarea" placeholder="介绍一下你自己..."></textarea>
        </div>
      </div>
    </div>

    <!-- 个性标签 -->
    <div v-if="isProfile" class="form-section">
      <div class="section-header">
        <span class="section-icon"></span>
        <h4>个性标签</h4>
      </div>
      <div class="form-row">
        <div class="form-group full-width" @click="handleFieldClick">
          <label>标签</label>
          <template v-if="!isEditing">
            <div v-if="getUserTags(user?.settings).length > 0" class="tags-container">
              <span v-for="(tag, index) in getUserTags(user?.settings)" :key="index" class="tag-item">#{{ tag }}</span>
            </div>
            <div v-else class="field-value editable">未设置
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input v-else v-model="userForm.settings" type="text" class="field-input" placeholder="多个标签用分号分隔" />
        </div>
      </div>
    </div>

    <!-- 企业微信机器人 -->
    <div v-if="isSettings" class="form-section">
      <div class="section-header">
        <span class="section-icon"></span>
        <h4>企业微信机器人</h4>
      </div>
      <div class="form-row">
        <div class="form-group full-width" @click="handleFieldClick">
          <label>Webhook地址</label>
          <template v-if="!isEditing">
            <div class="field-value editable" :class="{ configured: user?.weChatWebhookUrl }">
              {{ user?.weChatWebhookUrl ? '已配置' : '未配置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input 
            v-else 
            v-model="userForm.weChatWebhookUrl" 
            type="url" 
            class="field-input" 
            placeholder="https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=..."
          />
        </div>
      </div>
      <p class="field-hint">设置后，任务提醒将发送到您的企业微信机器人</p>
    </div>

    <!-- AI API Key -->
    <div v-if="isSettings" class="form-section">
      <div class="section-header">
        <span class="section-icon"></span>
        <h4>AI API Key</h4>
      </div>
      <div class="form-row">
        <div class="form-group full-width" @click="handleFieldClick">
          <label>智谱API Key</label>
          <template v-if="!isEditing">
            <div class="field-value editable" :class="{ configured: user?.aiApiKey }">
              {{ (user as any)?.aiApiKey ? '已配置' : '未配置' }}
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input 
            v-else 
            v-model="userForm.aiApiKey" 
            type="text" 
            class="field-input" 
            placeholder="请输入您的智谱API Key"
          />
        </div>
      </div>
      <p class="field-hint">设置您的智谱API Key后才能使用AI任务解析功能</p>
    </div>

    <!-- 专注目标设置 -->
    <div v-if="isSettings" class="form-section">
      <div class="section-header">
        <span class="section-icon"></span>
        <h4>专注目标</h4>
      </div>
      <div class="form-grid">
        <div class="form-group" @click="handleFieldClick">
          <label>每日目标时长（分钟）</label>
          <template v-if="!isEditing">
            <div class="field-value editable">
              {{ userForm.dailyFocusGoal || 120 }}分钟
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input 
            v-else 
            v-model.number="userForm.dailyFocusGoal" 
            type="number" 
            class="field-input" 
            placeholder="120"
            min="10"
            max="1440"
          />
        </div>
        <div class="form-group" @click="handleFieldClick">
          <label>最低有效时长（分钟）</label>
          <template v-if="!isEditing">
            <div class="field-value editable">
              {{ userForm.minEffectiveDuration || 30 }}分钟
              <span class="edit-indicator"></span>
            </div>
          </template>
          <input 
            v-else 
            v-model.number="userForm.minEffectiveDuration" 
            type="number" 
            class="field-input" 
            placeholder="30"
            min="5"
            max="480"
          />
        </div>
      </div>
      <p class="field-hint">每日目标时长用于计算今日目标完成率；最低有效时长为单日打卡所需最少专注时间</p>
    </div>
  </div>
</template>

<style scoped>
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-section {
  background: #F9FAFB;
  border-radius: 16px;
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: #E5E7EB;
}

.section-icon {
  font-size: 18px;
}

.section-header h4 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  padding: 16px 18px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px 18px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  cursor: pointer;
  transition: background 0.2s ease;
  padding: 4px;
  border-radius: 8px;
}

.form-group:hover {
  background: rgba(var(--primary-rgb), 0.05);
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-size: 13px;
  color: #6B7280;
  font-weight: 500;
}

.field-value {
  font-size: 14px;
  color: #374151;
  padding: 10px 14px;
  background: white;
  border-radius: 10px;
  border: 1px solid #D1D5DB;
  min-height: 40px;
  display: flex;
  align-items: center;
  transition: all 0.2s ease;
}

.field-value.readonly {
  cursor: default;
  opacity: 0.7;
}

.field-value.editable {
  position: relative;
  padding-right: 36px;
}

.field-value.editable:hover {
  border-color: var(--primary);
  background: #F9FAFB;
}

.edit-indicator {
  position: absolute;
  right: 10px;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.field-value.editable:hover .edit-indicator {
  opacity: 0.6;
}

.field-value.configured {
  color: #3F6212;
  border-color: #D1FAE5;
  background: #ECFDF5;
}

.textarea-value {
  min-height: 80px;
  flex-wrap: wrap;
  white-space: pre-wrap;
}

.field-input {
  padding: 10px 14px;
  border: 2px solid var(--primary);
  border-radius: 10px;
  font-size: 14px;
  background: white;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.field-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 4px rgba(var(--primary-rgb), 0.15);
}

.field-select {
  padding: 10px 14px;
  border: 2px solid var(--primary);
  border-radius: 10px;
  font-size: 14px;
  background: white;
  transition: border-color 0.2s ease;
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.field-select:focus {
  outline: none;
  border-color: var(--primary);
}

.field-textarea {
  padding: 10px 14px;
  border: 2px solid var(--primary);
  border-radius: 10px;
  font-size: 14px;
  background: white;
  min-height: 100px;
  resize: vertical;
  transition: border-color 0.2s ease;
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.field-textarea:focus {
  outline: none;
  border-color: var(--primary);
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px;
  background: white;
  border-radius: 10px;
  border: 1px solid #D1D5DB;
  min-height: 40px;
  align-items: center;
}

.tag-item {
  display: inline-flex;
  padding: 5px 12px;
  background: #EEF2FF;
  color: #4338CA;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 500;
}

.field-hint {
  font-size: 12px;
  color: #9CA3AF;
  margin: 0 0 16px 18px;
  padding-left: 4px;
}

/* 响应式设计 */
@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
    gap: 14px;
    padding: 14px;
  }

  .section-header {
    padding: 12px 14px;
  }

  .form-row {
    padding: 14px;
  }

  .field-input, .field-select, .field-textarea {
    padding: 8px 12px;
    font-size: 13px;
  }

  .field-value {
    padding: 8px 12px;
    font-size: 13px;
  }

  .field-hint {
    margin-left: 14px;
  }
}
</style>