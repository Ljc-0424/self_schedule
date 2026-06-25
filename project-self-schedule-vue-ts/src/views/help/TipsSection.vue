<template>
  <div class="section-card">
    <div class="section-header">
      <span class="section-icon"></span>
      <h2>使用技巧</h2>
    </div>
    <div class="section-content">
      <ul class="tips-list">
        <li><strong>自然语言输入：</strong>支持多种时间表达方式，如"明天下午"、"下周一"、"3天后"等。</li>
        <li><strong>任务优先级：</strong>高优先级任务会在列表顶部显示，便于您优先处理重要事项。</li>
        <li><strong>专注模式：</strong>使用专注记录功能时，建议关闭其他干扰，保持专注状态。专注记录会自动关联您选择的任务分类，方便后续统计分析。</li>
        <li><strong>提醒设置：</strong>合理设置提醒时间，避免错过重要任务的截止日期。</li>
        <li><strong>专注时间分析：</strong>在个人中心的数据统计页面，切换日/周/月/自定义时间段查看饼图，了解各分类的专注时间占比，优化时间分配。</li>
        <li><strong>消息中心：</strong>关注导航栏的红点提示，及时查看管理员发送的通知和私信，避免遗漏重要信息。</li>
        <li><strong>意见反馈：</strong>遇到问题或有建议时，尽量详细描述问题现象和复现步骤，附带联系方式，方便管理员快速定位和回复。</li>
        <li><strong>勋章收集：</strong>坚持每日专注和完成任务可以解锁更多勋章，查看勋章墙了解解锁条件，给自己设定小目标。</li>
        <li><strong>移动端适配：</strong>本应用已全面适配移动端，在手机浏览器中使用也能获得良好体验，弹窗和表单均支持响应式布局。</li>
      </ul>
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

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  color: #6B7280;
  line-height: 1.8;
  margin-bottom: 10px;
}

.tips-list li:last-child {
  margin-bottom: 0;
}
</style>
