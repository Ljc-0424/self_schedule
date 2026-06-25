<script setup lang="ts">
const previewSrcList = Array.from({ length: 9 }, (_, i) => `/images/wechat-guide/${i + 1}.png`)
</script>

<template>
  <div class="section-card">
    <div class="section-header">
      <span class="section-icon"></span>
      <h2>企业微信机器人配置指南</h2>
    </div>
    <div class="section-content">
      <div class="hint-card hint-card-warning">
        <span class="hint-card-icon"></span>
        <div class="hint-card-content">
          <p><strong>权限限制提醒</strong></p>
          <p>如果您使用的是学校或公司的企业微信，可能会受到群聊创建、机器人添加等权限限制。</p>
          <p>如果遇到限制，您可以在企业微信中注册一个新的企业（属于您自己的企业），这样就不会受到权限限制了。</p>
        </div>
      </div>

      <h3 style="margin-top: 24px; margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #111827; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px;">
        操作步骤
      </h3>

      <div class="guide-step">
        <div class="step-number">1</div>
        <div class="step-content">
          <h4>进入企业微信群聊</h4>
          <p>打开企业微信，进入您想要接收通知的群聊。</p>
          <p style="margin-top: 8px; font-size: 12px; color: #9CA3AF;">群聊可以只有您一个人，创建一个只有自己的群聊即可。</p>
        </div>
      </div>

      <div class="guide-step">
        <div class="step-number">2</div>
        <div class="step-content">
          <h4>添加群机器人</h4>
          <p>点击群聊右上角「...」→「添加群机器人」→「新创建一个机器人」。</p>
        </div>
      </div>

      <div class="guide-step">
        <div class="step-number">3</div>
        <div class="step-content">
          <h4>获取 Webhook 地址</h4>
          <p>为机器人命名后，复制生成的 Webhook 地址：</p>
          <p style="margin-top: 8px;"><code class="code-inline">https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxxxxxx</code></p>
        </div>
      </div>

      <div class="guide-step">
        <div class="step-number">4</div>
        <div class="step-content">
          <h4>配置到 SelfSchedule</h4>
          <p>进入【用户信息】页面，在「企业微信机器人」模块粘贴 Webhook 地址并保存。</p>
        </div>
      </div>

      <div class="step-complete">
        <p>配置完成后，您的任务提醒将会自动发送到该群聊中。</p>
      </div>

      <h3 style="margin-top: 24px; margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #111827; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px;">
        操作流程图示
      </h3>

      <p class="gallery-hint">点击图片可查看大图</p>

      <div class="image-gallery">
        <div
          v-for="i in 9"
          :key="i"
          class="gallery-item"
        >
          <el-image
            :src="`/images/wechat-guide/${i}.png`"
            :preview-src-list="previewSrcList"
            :initial-index="i - 1"
            fit="contain"
            preview-teleported
          >
            <template #error>
              <div class="image-placeholder">
                <span>{{ i }}.png</span>
                <span class="placeholder-hint">请放入图片</span>
              </div>
            </template>
          </el-image>
          <span class="gallery-step-label">第{{ i }}步</span>
        </div>
      </div>

      <h3 style="margin-top: 24px; margin-bottom: 16px; font-size: 16px; font-weight: 600; color: #111827; border-bottom: 2px solid #e2e8f0; padding-bottom: 8px;">
        补充提示
      </h3>

      <div class="hint-card hint-card-success">
        <span class="hint-card-icon"></span>
        <div class="hint-card-content">
          <p><strong>隐私安全</strong></p>
          <p>Webhook 地址会被加密存储在服务器端，保护您的隐私安全。</p>
        </div>
      </div>

      <div class="hint-card hint-card-info">
        <span class="hint-card-icon"></span>
        <div class="hint-card-content">
          <p><strong>后台权限设置</strong></p>
          <p>为确保全天候接收任务提醒，请开启企业微信后台权限：</p>
          <ul>
            <li>iOS：设置 → 企业微信 → 开启「后台应用刷新」和「通知」权限</li>
            <li>Android：在应用设置中开启「后台运行」权限</li>
          </ul>
        </div>
      </div>

      <div class="hint-card hint-card-tip">
        <span class="hint-card-icon"></span>
        <div class="hint-card-content">
          <p><strong>通知优化</strong></p>
          <p>建议将群聊名称设置为简短名称，并将机器人名称设置为「待办」，这样在手机通知栏显示时会更加清晰直观。</p>
        </div>
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

.hint-card {
  padding: 16px;
  border-radius: 8px;
  margin-top: 12px;
  display: flex;
  gap: 12px;
}

.hint-card-warning {
  background: #fff7ed;
  border-left: 4px solid #f97316;
}

.hint-card-warning p {
  color: #c2410c;
}

.hint-card-success {
  background: #f0fdf4;
  border-left: 4px solid #3F6212;
}

.hint-card-success p {
  color: #166534;
}

.hint-card-info {
  background: #eff6ff;
  border-left: 4px solid var(--primary);
}

.hint-card-info p {
  color: #1e40af;
}

.hint-card-tip {
  background: #fefce8;
  border-left: 4px solid #eab308;
}

.hint-card-tip p {
  color: #854d0e;
}

.hint-card-icon {
  font-size: 20px;
  flex-shrink: 0;
}

.hint-card-content p {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
}

.hint-card-content ul {
  margin: 8px 0 0 0;
  padding-left: 20px;
}

.hint-card-content li {
  font-size: 13px;
  line-height: 1.6;
  color: inherit;
}

.code-inline {
  font-family: 'Consolas', 'Monaco', monospace;
  background: #111827;
  color: #f8fafc;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.step-complete {
  margin-top: 16px;
  padding: 12px 16px;
  background: #f0fdf4;
  border-radius: 8px;
  text-align: center;
}

.step-complete p {
  margin: 0;
  font-size: 14px;
  color: #166534;
  font-weight: 500;
}

.gallery-hint {
  font-size: 12px;
  color: #9CA3AF;
  margin-bottom: 16px;
}

.image-gallery {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
  margin-bottom: 8px;
}

.gallery-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.gallery-item :deep(.el-image) {
  width: 100%;
  aspect-ratio: 9 / 16;
  border-radius: 10px;
  border: 1px solid #D1D5DB;
  background: #f8fafc;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
  overflow: hidden;
}

.gallery-item :deep(.el-image:hover) {
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.15);
  transform: translateY(-2px);
}

.gallery-item :deep(.el-image img) {
  border-radius: 10px;
}

.gallery-step-label {
  font-size: 12px;
  color: var(--primary);
  font-weight: 600;
  background: #eef2ff;
  padding: 2px 10px;
  border-radius: 10px;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9CA3AF;
  font-size: 14px;
}

.placeholder-hint {
  font-size: 11px;
  color: #D1D5DB;
  margin-top: 4px;
}

@media (max-width: 768px) {
  .guide-step {
    flex-direction: column;
    gap: 10px;
  }

  .image-gallery {
    grid-template-columns: repeat(3, 1fr);
    gap: 10px;
  }
}

@media (max-width: 480px) {
  .image-gallery {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>