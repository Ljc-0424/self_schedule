<template>
  <div class="section-card">
    <div class="section-header">
      <span class="section-icon"></span>
      <h2>功能介绍</h2>
    </div>
    <div class="section-content">
      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>智能任务助手</h3>
          <p>使用自然语言描述您的任务，AI会自动解析并创建任务，支持一次输入同时创建多个任务。例如："明天上午9点开会"、"下周五下午3点提交报告"</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>任务清单</h3>
          <p>管理您的所有任务，支持优先级设置、状态追踪、标签分类、子任务拆分等功能，让您的任务一目了然。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>提醒中心</h3>
          <p>查看即将到来的任务提醒，支持多种提醒方式，确保您不会错过重要事项。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>专注记录</h3>
          <p>记录您的专注时长，支持倒计时和自定义时长模式，追踪工作效率，帮助您更好地管理时间和提升生产力。专注记录会自动关联任务分类。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>个人中心</h3>
          <p>管理您的个人信息，包括头像上传、联系方式、API配置等。数据统计页面展示专注数据、任务完成情况、打卡记录和勋章成就。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>专注时间分析</h3>
          <p>在个人中心的数据统计中，通过圆环图直观查看各任务分类的专注时间占比，中心显示总时长，支持按日、周、月或自定义时间段筛选，帮助您了解时间分配。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>消息中心</h3>
          <p>接收管理员发送的系统通知和私信，包括平台公告、功能更新、账号相关等重要消息。有新消息时导航栏会显示红点提示。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>意见反馈</h3>
          <p>遇到问题或有改进建议？通过意见反馈功能直接向管理员提交反馈，管理员回复后您会收到通知，确保您的声音被听到。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>账号申诉</h3>
          <p>如果账号被封禁，登录时会展示封禁原因和申诉入口。您可以在未登录状态下提交申诉，管理员审核后结果会在登录页展示。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>企业微信推送</h3>
          <p>配置企业微信机器人后，任务提醒会自动推送到您的企业微信，随时随地接收通知。</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon"></div>
          <h3>勋章成就</h3>
          <p>通过坚持专注和完成任务解锁各类勋章，包括基础坚持、任务专精和效率成就三大类，激励您持续进步。</p>
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

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.feature-card {
  background: #FFFFFF;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  border: 1px solid #E5E7EB;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.feature-card h3 {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.feature-card p {
  margin: 0;
  font-size: 13px;
  color: #6B7280;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .feature-grid {
    grid-template-columns: 1fr;
  }
}
</style>
