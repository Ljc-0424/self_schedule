<template>
  <div class="section-card">
    <div class="section-header">
      <span class="section-icon"></span>
      <h2>常见问题</h2>
    </div>
    <div class="section-content">
      <div class="faq-item">
        <h4 class="faq-question">Q: AI任务助手无法使用怎么办？</h4>
        <p class="faq-answer">A: 请检查您是否已配置智谱API Key。进入【用户信息】页面查看API Key状态。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: AI可以同时创建多个任务吗？</h4>
        <p class="faq-answer">A: 是的，您可以在一句话中描述多个任务，AI会自动识别并分别创建，例如："明天上午9点开会，后天下午3点提交报告，周末去超市购物"。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: 企业微信收不到消息怎么办？</h4>
        <p class="faq-answer">A: 请检查Webhook地址是否正确，确保机器人仍在群聊中，没有被移除。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: 头像上传失败怎么办？</h4>
        <p class="faq-answer">A: 请确保图片格式正确（支持jpg、png、gif等），文件大小不超过10MB。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: 消息中心的红点提示怎么消除？</h4>
        <p class="faq-answer">A: 点击进入消息中心，查看未读消息即可自动标记为已读。也可以点击"全部已读"按钮一键清除所有未读状态。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: 专注时间饼图显示没有数据怎么办？</h4>
        <p class="faq-answer">A: 饼图数据来源于您的专注记录。请确保您已经使用专注功能记录过专注时长，并且选择了包含专注记录的时间范围。如果选择的时间段内没有专注记录，饼图将显示为空。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: 提交反馈后多久能收到回复？</h4>
        <p class="faq-answer">A: 管理员会尽快处理您的反馈。回复后反馈页面导航栏会出现红点提示，您也可以在反馈列表中查看回复内容。建议在反馈中留下联系方式，方便管理员与您沟通。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: 账号被封禁了怎么办？</h4>
        <p class="faq-answer">A: 如果您的账号被封禁，在登录时会自动显示封禁原因和封禁类型。您可以在登录页点击"提交申诉"按钮填写申诉理由。同一封禁记录只能提交1次申诉，请认真填写。管理员审核后，结果会在您下次登录时展示。临时禁用到期后会自动解封，无需申诉。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: 勋章怎么解锁？</h4>
        <p class="faq-answer">A: 勋章分为基础坚持、任务专精和效率成就三大类。通过坚持每日专注、完成不同数量的任务、保持连续打卡等方式可以逐步解锁。在个人中心的勋章墙页面可以查看所有勋章的解锁条件。</p>
      </div>
      <div class="faq-item">
        <h4 class="faq-question">Q: 手机上使用体验不好怎么办？</h4>
        <p class="faq-answer">A: 本应用已全面适配移动端，包括弹窗、表单、图表等均支持响应式布局。如果仍有显示问题，请通过意见反馈功能告知我们您的手机型号和具体问题，我们会尽快修复。</p>
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

.faq-item {
  padding: 12px 0;
  border-bottom: 1px dashed #D1D5DB;
}

.faq-item:last-child {
  border-bottom: none;
}

.faq-question {
  margin: 0 0 6px 0;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.faq-answer {
  margin: 0;
  font-size: 13px;
  color: #6B7280;
  padding-left: 16px;
}
</style>
