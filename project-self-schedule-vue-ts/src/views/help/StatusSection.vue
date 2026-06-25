<template>
  <div class="section-card">
    <div class="section-header">
      <span class="section-icon"></span>
      <h2>状态说明</h2>
    </div>
    <div class="section-content">
      <div class="status-section">
        <h3>任务状态</h3>
        <p>任务状态反映了任务的当前进度和处理情况，各状态之间的关系如下：</p>

        <div class="status-flow">
          <div class="flow-item">
            <div class="status-badge pending">○ 待办</div>
            <p>任务创建后默认状态，等待开始处理</p>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-item">
            <div class="status-badge in-progress">▶ 进行中</div>
            <p>任务正在处理中，尚未完成</p>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-item">
            <div class="status-badge completed">✓ 完成</div>
            <p>任务已成功完成</p>
          </div>
        </div>

        <div class="status-alternative">
          <p><strong>其他状态：</strong></p>
          <div class="status-grid">
            <div class="status-item">
              <div class="status-badge cancelled">✕ 已取消</div>
              <p>删除待办/进行中任务后的状态，可恢复为待办</p>
            </div>
            <div class="status-item">
              <div class="status-badge archived">已存档</div>
              <p>删除已完成任务后的状态，可恢复为完成</p>
            </div>
          </div>
        </div>

        <div class="status-note">
          <span class="tip-icon"></span>
          <p><strong>状态流转规则：</strong><br>
          待办 → 进行中 → 完成（正常流程）<br>
          待办/进行中 → 已取消（删除未完成任务）<br>
          完成 → 已存档（删除已完成任务）<br>
          已取消 → 待办（恢复任务）<br>
          已存档 → 完成（恢复任务）</p>
        </div>
      </div>

      <div class="status-section">
        <h3>提醒记录状态</h3>
        <p>提醒记录状态表示提醒消息的发送情况：</p>

        <div class="status-grid">
          <div class="status-item">
            <div class="status-badge pending">待发送</div>
            <p>提醒已创建，等待到达触发时间</p>
          </div>
          <div class="status-item">
            <div class="status-badge sent">已发送</div>
            <p>提醒已成功发送到企业微信或系统通知</p>
          </div>
          <div class="status-item">
            <div class="status-badge failed">发送失败</div>
            <p>提醒发送失败，系统会自动重试</p>
          </div>
          <div class="status-item">
            <div class="status-badge expired">已过期</div>
            <p>提醒时间已过，但因系统问题未能发送</p>
          </div>
        </div>

        <div class="status-note">
          <span class="tip-icon"></span>
          <p><strong>重试机制：</strong>发送失败的提醒会每5分钟自动重试一次，最多重试3次。</p>
        </div>
      </div>

      <div class="status-section">
        <h3>消息状态</h3>
        <p>消息中心中的消息包含以下类型和状态：</p>

        <div class="status-grid">
          <div class="status-item">
            <div class="status-badge system">系统通知</div>
            <p>管理员群发的公告类消息，所有用户都会收到</p>
          </div>
          <div class="status-item">
            <div class="status-badge private">管理员私信</div>
            <p>管理员单独发送给您的消息，仅您可见</p>
          </div>
          <div class="status-item">
            <div class="status-badge unread">未读</div>
            <p>尚未查看的消息，导航栏消息中心会显示红点提示</p>
          </div>
          <div class="status-item">
            <div class="status-badge read">已读</div>
            <p>已查看过的消息，点击消息即可标记为已读</p>
          </div>
        </div>

        <div class="status-note">
          <span class="tip-icon"></span>
          <p><strong>消息提示：</strong>导航栏消息中心旁的红点数字表示未读消息数量，点击消息可自动标记为已读，也可使用"全部已读"按钮一键清除所有未读状态。</p>
        </div>
      </div>

      <div class="status-section">
        <h3>反馈状态</h3>
        <p>您提交的意见反馈会经历以下状态：</p>

        <div class="status-flow">
          <div class="flow-item">
            <div class="status-badge feedback-pending">待处理</div>
            <p>反馈已提交，等待管理员查看</p>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-item">
            <div class="status-badge feedback-processed">已回复</div>
            <p>管理员已查看并回复您的反馈</p>
          </div>
          <div class="flow-arrow">→</div>
          <div class="flow-item">
            <div class="status-badge feedback-closed">已关闭</div>
            <p>反馈问题已解决或关闭</p>
          </div>
        </div>

        <div class="status-note">
          <span class="tip-icon"></span>
          <p><strong>回复通知：</strong>管理员回复您的反馈后，反馈页面导航栏会显示红点提示，方便您及时查看回复内容。</p>
        </div>
      </div>

      <div class="status-section">
        <h3>账号状态</h3>
        <p>您的账号可能处于以下状态：</p>

        <div class="status-grid">
          <div class="status-item">
            <div class="status-badge active">正常</div>
            <p>账号正常使用，可以登录和操作所有功能</p>
          </div>
          <div class="status-item">
            <div class="status-badge banned-perm">永久禁用</div>
            <p>账号被管理员永久封禁，无法登录。可通过申诉申请解封，申诉驳回后无法再次申诉</p>
          </div>
          <div class="status-item">
            <div class="status-badge banned-temp">临时禁用</div>
            <p>账号被管理员临时封禁，到期后自动解封。期间可提交一次申诉</p>
          </div>
        </div>

        <div class="status-note">
          <span class="tip-icon"></span>
          <p><strong>账号安全：</strong>请遵守平台使用规范，避免发布违规内容。如账号被封禁，登录时会显示封禁原因和申诉入口，您可以在未登录状态下提交申诉。</p>
        </div>

        <div class="status-note" style="margin-top: 12px;">
          <span class="tip-icon"></span>
          <p><strong>申诉规则：</strong><br>
          1. 同一封禁记录只能提交1次申诉，请认真填写申诉理由<br>
          2. 申诉提交后，管理员会尽快审核，审核结果在登录页展示<br>
          3. 申诉通过后账号自动解封，驳回后永久禁用无法再申诉<br>
          4. 临时禁用到期后自动解封，无需申诉</p>
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

.status-section {
  margin-bottom: 24px;
}

.status-section:last-child {
  margin-bottom: 0;
}

.status-section h3 {
  margin: 0 0 12px 0;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.status-section p {
  margin: 0 0 16px 0;
  font-size: 13px;
  color: #6B7280;
  line-height: 1.6;
}

.status-flow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.flow-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.flow-arrow {
  font-size: 20px;
  color: #9CA3AF;
  font-weight: bold;
}

.status-badge {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.in-progress {
  background: #dbeafe;
  color: var(--primary-dark);
}

.status-badge.completed {
  background: #dcfce7;
  color: #16a34a;
}

.status-badge.cancelled {
  background: #fee2e2;
  color: #B91C1C;
}

.status-badge.archived {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.sent {
  background: #dcfce7;
  color: #16a34a;
}

.status-badge.failed {
  background: #fee2e2;
  color: #B91C1C;
}

.status-badge.expired {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.system {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.private {
  background: #eef2ff;
  color: var(--primary);
}

.status-badge.unread {
  background: #fee2e2;
  color: #B91C1C;
}

.status-badge.read {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.feedback-pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.feedback-processed {
  background: #dcfce7;
  color: #16a34a;
}

.status-badge.feedback-closed {
  background: #f3f4f6;
  color: #6b7280;
}

.status-badge.active {
  background: #dcfce7;
  color: #16a34a;
}

.status-badge.disabled {
  background: #fee2e2;
  color: #B91C1C;
}

.status-badge.banned-perm {
  background: #fee2e2;
  color: #991b1b;
}

.status-badge.banned-temp {
  background: #fef3c7;
  color: #92400e;
}

.status-alternative {
  margin-bottom: 16px;
}

.status-alternative p {
  margin-bottom: 12px;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.status-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  background: #f8fafc;
  border-radius: 10px;
}

.status-item p {
  margin: 0;
  font-size: 12px;
  color: #6B7280;
  line-height: 1.5;
}

.status-note {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  background: #f0f9ff;
  border-left: 4px solid #0ea5e9;
  padding: 14px;
  border-radius: 0 8px 8px 0;
}

.status-note p {
  margin: 0;
  font-size: 12px;
  color: #0c4a6e;
  line-height: 1.6;
}

.tip-icon {
  font-size: 16px;
}
</style>
