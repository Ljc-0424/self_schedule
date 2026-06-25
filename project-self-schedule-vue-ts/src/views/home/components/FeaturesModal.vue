<script setup lang="ts">
defineProps<{
  show: boolean;
}>();

const emit = defineEmits<{
  (e: 'close'): void;
}>();
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="modal-overlay" @click.self="emit('close')">
      <div class="features-modal">
        <div class="modal-header">
          <h3>AI助手功能说明</h3>
          <button class="close-btn" @click="emit('close')">✕</button>
        </div>
        <div class="modal-content">
          <div class="features-grid">
            <div class="feature-item">
              <div class="feature-icon"></div>
              <div class="feature-content">
                <h4>智能任务创建</h4>
                <p>支持自然语言创建任务，自动识别截止时间、提醒时间</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon"></div>
              <div class="feature-content">
                <h4>智能时间计算</h4>
                <p>根据任务类型自动预估时长，智能设置提醒时间</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon"></div>
              <div class="feature-content">
                <h4>子任务拆解</h4>
                <p>自动识别多步骤任务，智能拆分为子任务</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon"></div>
              <div class="feature-content">
                <h4>重复任务设置</h4>
                <p>支持每天/每周/每月/每年重复规则</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon"></div>
              <div class="feature-content">
                <h4>智能分类标签</h4>
                <p>自动匹配分类、优先级、生成精准标签</p>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon"></div>
              <div class="feature-content">
                <h4>个性化适配</h4>
                <p>结合您的职业、作息习惯提供个性化服务</p>
              </div>
            </div>
          </div>
          <div class="usage-tips">
            <h4>使用提示</h4>
            <ul>
              <li>时间表达：明天早上9点、后天下午3点、本周三前</li>
              <li>任务描述：数学报告明天截止、周末去超市购物</li>
              <li>重复任务：每天早上7点起床、每周一三五跑步</li>
              <li>多任务：明天开会、写报告、准备资料（用逗号分隔）</li>
            </ul>
          </div>
          <div class="model-info">
            <div class="model-badge"></div>
            <div class="model-content">
              <h4>关于AI模型</h4>
              <p>本助手使用 <strong>智谱AI GLM-4.7-flash</strong> 模型，这是一个长期免费且性能优秀的模型，无需担心会消耗您其他模型的额度。响应速度快，理解能力强，是智能任务管理的理想选择。</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.features-modal {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  max-width: 800px;
  width: 90%;
  max-height: 85vh;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { 
    opacity: 0;
    transform: translateY(20px);
  }
  to { 
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: var(--primary);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 8px;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.modal-content {
  padding: 24px;
  max-height: calc(85vh - 80px);
  overflow-y: auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: rgba(248, 250, 252, 0.8);
  border-radius: 16px;
  text-align: center;
  transition: all 0.2s;
}

.feature-item:hover {
  background: rgba(var(--primary-rgb), 0.08);
  transform: translateY(-2px);
}

.feature-icon {
  font-size: 28px;
  margin-bottom: 10px;
}

.feature-content h4 {
  margin: 0 0 6px 0;
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

.feature-content p {
  margin: 0;
  font-size: 12px;
  color: #6B7280;
  line-height: 1.4;
}

.usage-tips {
  padding: 16px 24px 20px;
  background: rgba(var(--primary-rgb), 0.04);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.usage-tips h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.usage-tips ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.usage-tips li {
  font-size: 12px;
  color: #6B7280;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.model-info {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 16px 24px;
  background: rgba(16, 185, 129, 0.06);
  border-top: 1px solid rgba(0, 0, 0, 0.1);
  margin-top: 16px;
  border-radius: 16px;
}

.model-badge {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #3F6212;
  border-radius: 12px;
  font-size: 20px;
  flex-shrink: 0;
}

.model-content h4 {
  margin: 0 0 6px 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.model-content p {
  margin: 0;
  font-size: 13px;
  color: #6B7280;
  line-height: 1.5;
}

.model-content strong {
  color: #16a34a;
  font-weight: 600;
}

@media (max-width: 991.98px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .usage-tips ul {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 767.98px) {
  .features-modal {
    border-radius: 16px;
    width: 95%;
  }
  
  .modal-header {
    padding: 16px 20px;
  }
  
  .modal-header h3 {
    font-size: 16px;
  }
  
  .modal-content {
    padding: 16px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    margin-bottom: 16px;
  }
  
  .feature-item {
    padding: 16px;
    flex-direction: row;
    text-align: left;
    gap: 12px;
  }
  
  .feature-icon {
    font-size: 24px;
    margin-bottom: 0;
    flex-shrink: 0;
  }
  
  .feature-content h4 {
    font-size: 14px;
    margin-bottom: 4px;
  }
  
  .feature-content p {
    font-size: 12px;
  }
  
  .usage-tips {
    padding: 14px;
  }
  
  .usage-tips h4 {
    font-size: 13px;
    margin-bottom: 10px;
  }
  
  .usage-tips ul {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .usage-tips li {
    font-size: 11px;
    padding: 8px;
  }
  
  .model-info {
    padding: 14px;
    margin-top: 12px;
  }
  
  .model-badge {
    width: 36px;
    height: 36px;
    font-size: 18px;
  }
  
  .model-content h4 {
    font-size: 13px;
  }
  
  .model-content p {
    font-size: 12px;
  }
}

@media (max-width: 575.98px) {
  .features-modal {
    border-radius: 12px;
  }
  
  .modal-header {
    padding: 12px 16px;
  }
  
  .modal-header h3 {
    font-size: 15px;
  }
  
  .close-btn {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
  
  .modal-content {
    padding: 12px;
  }
  
  .feature-item {
    padding: 14px;
    gap: 10px;
  }
  
  .feature-icon {
    font-size: 20px;
  }
  
  .feature-content h4 {
    font-size: 13px;
  }
  
  .feature-content p {
    font-size: 11px;
  }
  
  .usage-tips {
    padding: 12px;
  }
  
  .usage-tips h4 {
    font-size: 12px;
  }
  
  .usage-tips li {
    font-size: 10px;
    padding: 6px 8px;
  }
  
  .model-info {
    padding: 12px;
    gap: 10px;
  }
  
  .model-badge {
    width: 32px;
    height: 32px;
    font-size: 16px;
  }
  
  .model-content h4 {
    font-size: 12px;
  }
  
  .model-content p {
    font-size: 11px;
  }
}
</style>