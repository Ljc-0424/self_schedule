<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import { userApi, taskApi } from '../../../api';
import type { TaskVO } from '../../../types';

defineProps<{
  showTasks: boolean;
}>();

const STORAGE_KEY = 'ai_chat_messages';

const emit = defineEmits<{
  (e: 'taskCreated'): void;
  (e: 'showHelp'): void;
  (e: 'toggleTasks'): void;
}>();

const inputText = ref('');
const isLoading = ref(false);
const messages = ref<Array<{ type: 'user' | 'ai'; content: string }>>([]);
const hasApiKey = ref(true);
const copySuccess = ref(false);

const pendingTasks = ref<Array<{
  taskId: string;
  prompt: string;
  status: 'PROCESSING' | 'COMPLETED' | 'FAILED';
}>>([]);

const quickSuggestions = [
  '明天上午9点我要去上班',
  '后天下午3点参加会议',
  '周末去超市购物',
  '每天早上7点起床跑步'
];

let pollTimer: number | null = null;

const loadMessages = () => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      messages.value = JSON.parse(stored);
    } else {
      messages.value = [{
        type: 'ai',
        content: '您好！我是您的智能任务助手。请告诉我您的任务安排，比如："明天上午9点我要去上班"，我会帮您创建任务并设置提醒。\n\n⚠️ 温馨提示：AI创建的任务可能存在识别误差，建议创建后检查任务时间和内容是否正确。'
      }];
    }
  } catch {
    messages.value = [{
      type: 'ai',
      content: '您好！我是您的智能任务助手。请告诉我您的任务安排，比如："明天上午9点我要去上班"，我会帮您创建任务并设置提醒。\n\n⚠️ 温馨提示：AI创建的任务可能存在识别误差，建议创建后检查任务时间和内容是否正确。'
    }];
  }
};

const newConversation = () => {
  messages.value = [{
    type: 'ai',
    content: '您好！我是您的智能任务助手。请告诉我您的任务安排，比如："明天上午9点我要去上班"，我会帮您创建任务并设置提醒。\n\n⚠️ 温馨提示：AI创建的任务可能存在识别误差，建议创建后检查任务时间和内容是否正确。'
  }];
  saveMessages();
};

// 清空对话
const clearMessages = () => {
  console.log('清空对话');
  messages.value = [];
  saveMessages();
};

// 复制最近一条AI回复
const copyLastAiMessage = async () => {
  console.log('复制对话');
  const lastAi = [...messages.value].reverse().find(m => m.type === 'ai');
  if (!lastAi) return;
  try {
    await navigator.clipboard.writeText(lastAi.content);
    copySuccess.value = true;
    setTimeout(() => { copySuccess.value = false; }, 1500);
  } catch {
    console.error('复制失败');
  }
};

const checkApiKey = async () => {
  try {
    const response = await userApi.getInfo();
    if (response.data.code === 200 && response.data.data) {
      hasApiKey.value = !!response.data.data.aiApiKey;
      if (!hasApiKey.value && messages.value.length === 0) {
        messages.value.unshift({
          type: 'ai',
          content: '⚠️ 您还未配置智谱API Key，请先在【个人信息】中配置，否则无法使用AI任务助手功能。'
        });
      }
    }
  } catch (error) {
    console.error('检查API Key失败:', error);
  }
};

const saveMessages = () => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(messages.value));
  } catch (error) {
    console.error('保存对话历史失败:', error);
  }
};

const addMessage = (message: { type: 'user' | 'ai'; content: string }) => {
  messages.value.push(message);
  saveMessages();
};

const savePendingTasks = () => {
  try {
    localStorage.setItem('pending_ai_tasks', JSON.stringify(pendingTasks.value));
  } catch (error) {
    console.error('保存待处理任务失败:', error);
  }
};

const addPendingTask = (taskId: string, prompt: string) => {
  pendingTasks.value.push({ taskId, prompt, status: 'PROCESSING' });
  savePendingTasks();
};

const updatePendingTask = (taskId: string, status: 'COMPLETED' | 'FAILED') => {
  const task = pendingTasks.value.find(t => t.taskId === taskId);
  if (task) {
    task.status = status;
    savePendingTasks();
  }
};

const removePendingTask = (taskId: string) => {
  pendingTasks.value = pendingTasks.value.filter(t => t.taskId !== taskId);
  savePendingTasks();
};

const sendMessage = async () => {
  if (!inputText.value.trim() || isLoading.value) return;
  
  if (!hasApiKey.value) {
    addMessage({ type: 'ai', content: '⚠️ 您还未配置智谱API Key，请先在【个人信息】页面配置后再使用AI任务助手功能。' });
    inputText.value = '';
    return;
  }
  
  addMessage({ type: 'user', content: inputText.value.trim() });
  const userInput = inputText.value.trim();
  inputText.value = '';

  const tempTaskId = 'temp_' + Date.now();
  addPendingTask(tempTaskId, userInput);

  try {
    const response = await fetch('/api/ai/parse-task/async', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify({ prompt: userInput })
    });
    const result = await response.json();
    if (result.code === 200 && result.data) {
      removePendingTask(tempTaskId);
      addPendingTask(result.data.taskId, userInput);
    } else {
      removePendingTask(tempTaskId);
      addMessage({ type: 'ai', content: result.message || '抱歉，任务提交失败，请稍后重试。' });
    }
  } catch (error) {
    removePendingTask(tempTaskId);
    addMessage({ type: 'ai', content: '网络错误，请检查网络连接后重试。' });
  }
};

const pollTaskStatus = async () => {
  if (pendingTasks.value.length === 0) return;
  for (const task of pendingTasks.value) {
    if (task.status !== 'PROCESSING') continue;
    try {
      const response = await fetch(`/api/ai/task/status/${task.taskId}`, {
        headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
      });
      const result = await response.json();
      if (result.code === 200 && result.data) {
        if (result.data.status === 'COMPLETED') {
          const resultResponse = await fetch(`/api/ai/task/result/${task.taskId}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
          });
          const resultData = await resultResponse.json();
          if (resultData.code === 200 && resultData.data) {
            addMessage({ type: 'ai', content: resultData.data.message });
            if (resultData.data.taskCreated) emit('taskCreated');
          } else {
            addMessage({ type: 'ai', content: '任务处理完成，但获取结果失败。' });
          }
          updatePendingTask(task.taskId, 'COMPLETED');
          removePendingTask(task.taskId);
        } else if (result.data.status === 'FAILED') {
          const failResponse = await fetch(`/api/ai/task/result/${task.taskId}`, {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
          });
          const failData = await failResponse.json();
          addMessage({
            type: 'ai',
            content: failData.code === 200 && failData.data ? failData.data.message : '任务处理失败，请稍后重试。'
          });
          updatePendingTask(task.taskId, 'FAILED');
          removePendingTask(task.taskId);
        }
      }
    } catch (error) {
      console.error('轮询任务状态失败:', error);
    }
  }
};

const useQuickSuggestion = (suggestion: string) => {
  inputText.value = suggestion;
};

onMounted(() => {
  loadMessages();
  checkApiKey();
  pollTimer = window.setInterval(pollTaskStatus, 2000);
});

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
});
</script>

<template>
  <div class="chat-wrapper">
    <div class="chat-card">
      <div class="chat-header">
        <div class="header-left">
          <span class="avatar">
            <svg width="36" height="36" viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
              <circle cx="18" cy="18" r="18" fill="#E5E7EB"/>
              <rect x="10" y="12" width="16" height="14" rx="4" stroke="#6B7280" stroke-width="1.5" fill="none"/>
              <circle cx="14.5" cy="19" r="1.5" fill="#6B7280"/>
              <circle cx="21.5" cy="19" r="1.5" fill="#6B7280"/>
              <line x1="14" y1="24" x2="22" y2="24" stroke="#6B7280" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="18" y1="8" x2="18" y2="12" stroke="#6B7280" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="18" cy="7" r="1.5" fill="#6B7280"/>
              <line x1="7" y1="18" x2="10" y2="18" stroke="#6B7280" stroke-width="1.5" stroke-linecap="round"/>
              <line x1="26" y1="18" x2="29" y2="18" stroke="#6B7280" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </span>
          <div class="header-info">
            <h3>智能任务助手</h3>
            <p class="subtitle">让AI帮您管理日常任务</p>
          </div>
        </div>
        <div class="header-actions">
          <button class="tasks-tgl-btn" @click="$emit('toggleTasks')" :title="showTasks ? '收起任务' : '最近任务'">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>
          </button>
          <button class="help-btn" @click="$emit('showHelp')" title="功能说明">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
          </button>
          <div class="action-btn-wrap">
            <button class="action-btn" @click="clearMessages" title="清空对话">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
              </svg>
            </button>
            <span class="tooltip">清空对话</span>
          </div>
          <div class="action-btn-wrap">
            <button class="action-btn" @click="copyLastAiMessage" title="复制对话">
              <svg v-if="!copySuccess" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#6B7280" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <rect x="9" y="9" width="13" height="13" rx="2" ry="2"/>
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </button>
            <span class="tooltip">{{ copySuccess ? '已复制' : '复制对话' }}</span>
          </div>
          <div class="action-btn-wrap">
            <button class="new-chat-btn" @click="newConversation" title="新建对话">
              <span>+</span>
            </button>
            <span class="tooltip">新建对话</span>
          </div>
        </div>
      </div>
      
      <div class="ai-feature-tip">
        <span class="tip-text">当前智能AI助手支持自然语言同时创建多个任务</span>
      </div>
      
      <div class="chat-messages">
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          :class="['message-item', `message-${message.type}`]"
        >
          <div class="message-avatar">
            <template v-if="message.type === 'ai'">
              <svg width="24" height="24" viewBox="0 0 36 36" fill="none" xmlns="http://www.w3.org/2000/svg">
                <circle cx="18" cy="18" r="18" fill="#E5E7EB"/>
                <rect x="10" y="12" width="16" height="14" rx="4" stroke="#6B7280" stroke-width="1.5" fill="none"/>
                <circle cx="14.5" cy="19" r="1.5" fill="#6B7280"/>
                <circle cx="21.5" cy="19" r="1.5" fill="#6B7280"/>
                <line x1="14" y1="24" x2="22" y2="24" stroke="#6B7280" stroke-width="1.5" stroke-linecap="round"/>
                <line x1="18" y1="8" x2="18" y2="12" stroke="#6B7280" stroke-width="1.5" stroke-linecap="round"/>
                <circle cx="18" cy="7" r="1.5" fill="#6B7280"/>
              </svg>
            </template>
          </div>
          <div class="message-content">
            <p>{{ message.content }}</p>
          </div>
        </div>
        
        <div v-if="pendingTasks.length > 0" class="loading-indicator">
          <span class="loading-dots">
            <span></span><span></span><span></span>
          </span>
          <span class="loading-text">AI正在思考中...</span>
        </div>
      </div>
      
      <div class="quick-suggestions">
        <span class="suggestions-label">试试这些：</span>
        <button 
          v-for="suggestion in quickSuggestions" 
          :key="suggestion"
          class="suggestion-btn"
          @click="useQuickSuggestion(suggestion)"
        >
          {{ suggestion }}
        </button>
      </div>
      
      <div class="input-area">
        <input 
          v-model="inputText"
          type="text" 
          class="chat-input"
          placeholder="输入您的任务安排，例如：明天上午9点我要去上班"
          @keyup.enter="sendMessage"
        />
        <button class="send-btn" @click="sendMessage" :disabled="!inputText.trim()">
          <span>发送</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-wrapper {
  height: 100%;
  display: flex;
}

.chat-card {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.6);
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.chat-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #FFFFFF;
  border-bottom: 1px solid #D1D5DB;
}

.header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.action-btn-wrap {
  position: relative;
}

.tooltip {
  display: none;
  position: absolute;
  bottom: calc(100% + 6px);
  left: 50%;
  transform: translateX(-50%);
  background: #374151;
  color: #fff;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;
  pointer-events: none;
  z-index: 10;
}

.tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border: 4px solid transparent;
  border-top-color: #374151;
}

.action-btn-wrap:hover .tooltip {
  display: block;
}

.action-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #E5E7EB;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #D1D5DB;
  transform: scale(1.05);
}

.tasks-tgl-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #E5E7EB;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.tasks-tgl-btn:hover {
  background: #D1D5DB;
  transform: scale(1.05);
}

.tasks-tgl-btn span {
  font-size: 18px;
}

.help-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #E5E7EB;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.help-btn:hover {
  background: #D1D5DB;
  transform: scale(1.05);
}

.help-btn span {
  font-size: 18px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar svg {
  display: block;
}

.header-info h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.header-info .subtitle {
  margin: 2px 0 0 0;
  font-size: 12px;
  color: #6B7280;
}

.new-chat-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #E5E7EB;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.new-chat-btn:hover {
  background: #D1D5DB;
  transform: scale(1.05);
}

.new-chat-btn span {
  font-size: 24px;
  color: #374151;
  font-weight: 300;
  line-height: 1;
}

.ai-feature-tip {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #F9FAFB;
  border-left: 2px solid var(--primary);
  border-bottom: 1px solid #D1D5DB;
}

.tip-icon {
  font-size: 16px;
  flex-shrink: 0;
}

.tip-text {
  font-size: 12px;
  color: #374151;
  font-weight: 500;
}

.chat-messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px;
  background: rgba(248, 250, 252, 0.5);
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message-user {
  flex-direction: row-reverse;
}

.message-user .message-content {
  background: var(--primary);
  color: white;
  border-radius: 18px 18px 4px 18px;
}

.message-ai .message-content {
  background: #fafafa;
  color: #111827;
  border-radius: 18px 18px 18px 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.message-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-avatar svg {
  display: block;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  line-height: 1.6;
}

.message-content p {
  margin: 0;
  font-size: 14px;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: #fafafa;
  border-radius: 16px;
  width: fit-content;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
}

.loading-dots {
  display: flex;
  gap: 4px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: var(--primary);
  border-radius: 50%;
  animation: loading 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) { animation-delay: 0s; }
.loading-dots span:nth-child(2) { animation-delay: 0.2s; }
.loading-dots span:nth-child(3) { animation-delay: 0.4s; }

@keyframes loading {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.loading-text {
  font-size: 13px;
  color: #6B7280;
}

.quick-suggestions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 20px;
  background: rgba(248, 250, 252, 0.8);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  overflow-x: auto;
  white-space: nowrap;
}

.suggestions-label {
  font-size: 12px;
  color: #9CA3AF;
  flex-shrink: 0;
}

.suggestion-btn {
  padding: 6px 12px;
  background: white;
  border: 1px solid rgba(var(--primary-rgb), 0.2);
  border-radius: 20px;
  font-size: 12px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}

.suggestion-btn:hover {
  background: rgba(var(--primary-rgb), 0.06);
  border-color: var(--primary);
  color: var(--primary);
}

.input-area {
  flex-shrink: 0;
  display: flex;
  gap: 12px;
  padding: 12px 20px;
  background: white;
  border-top: 1px solid rgba(0, 0, 0, 0.12);
}

.chat-input {
  flex: 1;
  padding: 12px 16px;
  background: #f8fafc;
  border: 1px solid rgba(0, 0, 0, 0.15);
  border-radius: 14px;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
}

.chat-input:focus {
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.1);
}

.send-btn {
  padding: 12px 24px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 14px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(var(--primary-rgb), 0.3);
  background: #4338CA;
}

.send-btn:disabled {
  background: #D1D5DB;
  cursor: not-allowed;
}

@media (max-width: 1199.98px) {
  .chat-messages {
    flex: 1;
    min-height: 0;
    padding: 20px;
  }
}

@media (max-width: 991.98px) {
  .chat-messages {
    flex: 1;
    min-height: 0;
  }
  
  .message-content {
    max-width: 75%;
  }
  
  .chat-header {
    padding: 16px 20px;
  }
}

@media (max-width: 767.98px) {
  .chat-card {
    border-radius: 16px;
  }
  
  .chat-messages {
    flex: 1;
    min-height: 0;
    padding: 16px;
  }
  
  .chat-header {
    padding: 14px 16px;
  }
  
  .avatar {
    font-size: 28px;
  }
  
  .header-info h3 {
    font-size: 15px;
  }
  
  .header-info .subtitle {
    font-size: 11px;
  }
  
  .message-item {
    gap: 10px;
    margin-bottom: 12px;
  }
  
  .message-avatar {
    font-size: 22px;
    width: 30px;
    height: 30px;
  }
  
  .message-content {
    padding: 10px 14px;
    max-width: 80%;
  }
  
  .message-content p {
    font-size: 13px;
  }
  
  .quick-suggestions {
    padding: 12px 16px;
  }
  
  .suggestion-btn {
    padding: 5px 10px;
    font-size: 11px;
  }
  
  .input-area {
    padding: 12px 16px;
    gap: 10px;
  }
  
  .chat-input {
    padding: 10px 14px;
    font-size: 13px;
  }
  
  .send-btn {
    padding: 10px 20px;
    font-size: 13px;
  }
}

@media (max-width: 575.98px) {
  .chat-card {
    border-radius: 12px;
  }
  
  .chat-messages {
    flex: 1;
    min-height: 0;
    padding: 12px;
  }
  
  .chat-header {
    padding: 12px 14px;
  }
  
  .avatar {
    font-size: 24px;
  }
  
  .header-left {
    gap: 10px;
  }
  
  .header-info h3 {
    font-size: 14px;
  }
  
  .header-info .subtitle {
    font-size: 10px;
  }
  
  .message-item {
    gap: 8px;
    margin-bottom: 10px;
  }
  
  .message-avatar {
    font-size: 18px;
    width: 26px;
    height: 26px;
  }
  
  .message-content {
    padding: 8px 12px;
    max-width: 85%;
  }
  
  .message-content p {
    font-size: 12px;
  }
  
  .quick-suggestions {
    padding: 10px 12px;
  }
  
  .suggestions-label {
    font-size: 11px;
    margin-right: 8px;
  }
  
  .suggestion-btn {
    padding: 4px 8px;
    font-size: 10px;
    margin-right: 6px;
  }
  
  .input-area {
    padding: 10px 12px;
    gap: 8px;
  }
  
  .chat-input {
    padding: 8px 12px;
    font-size: 12px;
  }
  
  .send-btn {
    padding: 8px 16px;
    font-size: 12px;
  }
  
  .loading-indicator {
    padding: 10px 14px;
  }
  
  .loading-text {
    font-size: 12px;
  }
  
  .loading-dots span {
    width: 6px;
    height: 6px;
  }
}

@media (max-height: 600px) {
  .chat-messages {
    flex: 1;
    min-height: 0;
  }
}

@media (max-height: 500px) {
  .chat-messages {
    flex: 1;
    min-height: 0;
  }
  
  .quick-suggestions {
    display: none;
  }
}
</style>