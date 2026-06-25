<template>
  <div class="section-card">
    <div class="section-header">
      <span class="section-icon"></span>
      <h2>智谱API Key配置</h2>
    </div>
    <div class="section-content">
      <div class="guide-step">
        <div class="step-number">1</div>
        <div class="step-content">
          <h4>注册智谱开放平台账号</h4>
          <p>访问 <a href="https://platform.zhipuai.cn/" target="_blank" class="link">智谱开放平台</a>，注册并登录您的账号。</p>
        </div>
      </div>
      <div class="guide-step">
        <div class="step-number">2</div>
        <div class="step-content">
          <h4>创建API Key</h4>
          <p>登录后进入控制台，在"API Key管理"页面创建新的API Key。</p>
        </div>
      </div>
      <div class="guide-step">
        <div class="step-number">3</div>
        <div class="step-content">
          <h4>复制API Key</h4>
          <p>复制生成的API Key（格式类似：sk-xxxxxxxxxxxxxxxxxxxxxxxx）。</p>
        </div>
      </div>
      <div class="guide-step">
        <div class="step-number">4</div>
        <div class="step-content">
          <h4>配置到 SelfSchedule</h4>
          <p>进入【用户信息】页面，在"AI API Key"部分粘贴您的API Key并保存。</p>
        </div>
      </div>
      <div class="tip-box">
        <span class="tip-icon"></span>
        <p>API Key会被加密存储在服务器端，请妥善保管您的密钥，不要泄露给他人。</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.section-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 22px;
  background: #E5E7EB;
  border-bottom: 1px solid #D1D5DB;
}

.section-icon {
  font-size: 22px;
}

.section-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.section-content {
  padding: 20px 22px;
}

.guide-step {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px dashed #D1D5DB;
}

.guide-step:last-of-type {
  border-bottom: none;
}

.step-number {
  width: 32px;
  height: 32px;
  background: var(--primary);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-content h4 {
  margin: 0 0 6px 0;
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.step-content p {
  margin: 0;
  font-size: 13px;
  color: #6B7280;
}

.link {
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}

.tip-box {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #fefce8;
  border-left: 4px solid #eab308;
  padding: 12px 16px;
  border-radius: 0 8px 8px 0;
  margin-top: 16px;
}

.tip-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.tip-box p {
  margin: 0;
  font-size: 13px;
  color: #854d0e;
}

@media (max-width: 768px) {
  .guide-step {
    flex-direction: column;
    gap: 10px;
  }
}
</style>