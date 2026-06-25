<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageLayout from '../../components/PageLayout.vue'
import Loading from '../../components/Loading.vue'
import { feedbackApi } from '../../api/feedbackApi'

interface FeedbackVO {
  id: number
  userId: number
  category: string
  title: string
  content: string
  contact: string
  status: number
  adminReply: string | null
  createdTime: string
  updatedTime: string
}

const showCreate = ref(false)
const feedbacks = ref<FeedbackVO[]>([])
const loading = ref(false)
const submitting = ref(false)

const form = ref({
  category: 'other',
  title: '',
  content: '',
  contact: ''
})

const categoryOptions = [
  { value: 'bug', label: 'BUG反馈' },
  { value: 'feature', label: '功能建议' },
  { value: 'experience', label: '体验问题' },
  { value: 'other', label: '其他' }
]

const statusMap: Record<number, { label: string; cls: string }> = {
  0: { label: '未处理', cls: 'status-pending' },
  1: { label: '已处理', cls: 'status-done' },
  2: { label: '已关闭', cls: 'status-closed' }
}

const categoryLabelMap: Record<string, string> = {
  bug: 'BUG反馈',
  feature: '功能建议',
  experience: '体验问题',
  other: '其他'
}

const categoryIconMap: Record<string, string> = {
  bug: '🐛',
  feature: '💡',
  experience: '🎨',
  other: '💬'
}

const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getMyFeedbacks()
    feedbacks.value = res.data.data
  } catch {
    ElMessage.error('获取反馈列表失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入反馈标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入反馈内容')
    return
  }
  submitting.value = true
  try {
    await feedbackApi.create(form.value)
    ElMessage.success('反馈提交成功')
    showCreate.value = false
    form.value = { category: 'other', title: '', content: '', contact: '' }
    fetchFeedbacks()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchFeedbacks()
  feedbackApi.markAllAsRead().catch(() => {})
})
</script>

<template>
  <PageLayout title="用户反馈" subtitle="提交问题或建议，帮助我们改进产品">
    <div class="feedback-page">
      <div class="feedback-header">
        <el-button type="primary" class="add-btn" @click="showCreate = true">提交反馈</el-button>
      </div>

      <div class="feedback-body">
        <Loading v-if="loading" text="加载中..." />

        <template v-else>
          <div v-if="feedbacks.length === 0" class="empty-state">
            <div class="empty-icon"></div>
            <div class="empty-title">暂无反馈记录</div>
            <div class="empty-desc">点击上方按钮提交你的第一条反馈</div>
          </div>

          <div v-else class="feedback-list">
            <div v-for="item in feedbacks" :key="item.id" class="feedback-card" :class="statusMap[item.status]?.cls">
              <div class="feedback-top">
                <span class="category-badge">
                  {{ categoryIconMap[item.category] || '' }}
                  {{ categoryLabelMap[item.category] || item.category }}
                </span>
                <span class="status-badge" :class="statusMap[item.status]?.cls">
                  {{ statusMap[item.status]?.label || '未知' }}
                </span>
                <span class="feedback-time">{{ formatDate(item.createdTime) }}</span>
              </div>
              <h3 class="feedback-title">{{ item.title }}</h3>
              <p class="feedback-content">{{ item.content }}</p>

              <div v-if="item.adminReply" class="admin-reply">
                <div class="reply-header">管理员回复</div>
                <p>{{ item.adminReply }}</p>
              </div>
            </div>
          </div>
        </template>
        <div class="feedback-body-spacer"></div>
      </div>
    </div>

    <el-dialog v-model="showCreate" title="提交反馈" width="500px" :append-to-body="true">
      <el-form label-position="top">
        <el-form-item label="反馈类型">
          <el-select v-model="form.category" style="width: 100%;">
            <el-option
              v-for="opt in categoryOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="简要描述你的问题或建议" maxlength="100" />
        </el-form-item>
        <el-form-item label="详细内容">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            placeholder="请详细描述你遇到的问题或建议..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="联系方式（选填）">
          <el-input v-model="form.contact" placeholder="邮箱或其他联系方式，方便我们回复你" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<style scoped>
.feedback-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-width: 1200px;
  margin: 0 auto;
}

.feedback-header {
  flex-shrink: 0;
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-end;
}

.add-btn {
  background: var(--primary);
  border: none;
  border-radius: 8px;
  padding: 10px 24px;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: background 0.2s ease;
}

.add-btn:hover {
  background: #4338CA;
}

.feedback-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.feedback-body::-webkit-scrollbar {
  width: 5px;
}

.feedback-body::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.feedback-body::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.feedback-body::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--primary-rgb), 0.45);
}

.feedback-body-spacer {
  height: 8px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feedback-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  padding: 18px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 6px 24px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-left: 4px solid #e2e8f0;
  transition: all 0.2s;
}

.feedback-card:hover {
  transform: translateY(-2px);
  box-shadow:
    0 8px 30px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.feedback-card.status-pending {
  border-left-color: #e6a23c;
}

.feedback-card.status-done {
  border-left-color: #3F6212;
  opacity: 0.9;
}

.feedback-card.status-closed {
  border-left-color: #9CA3AF;
  opacity: 0.8;
}

.feedback-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.category-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
  background: #eef2ff;
  color: var(--primary);
}

.status-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.status-badge.status-pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.status-done {
  background: #dcfce7;
  color: #166534;
}

.status-badge.status-closed {
  background: #f3f4f6;
  color: #6b7280;
}

.feedback-time {
  margin-left: auto;
  font-size: 12px;
  color: #9CA3AF;
}

.feedback-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px 0;
}

.feedback-content {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
}

.admin-reply {
  margin-top: 14px;
  padding: 12px 16px;
  background: rgba(219, 234, 254, 0.5);
  border-radius: 10px;
  border-left: 3px solid var(--primary);
}

.reply-header {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 4px;
}

.admin-reply p {
  margin: 0;
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
  white-space: pre-wrap;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 6px 24px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  color: #6B7280;
  margin-bottom: 8px;
}

.empty-desc {
  font-size: 14px;
  color: #9CA3AF;
}

@media (max-width: 767px) {
  .feedback-card {
    padding: 14px;
  }

  .empty-state {
    padding: 40px 16px;
  }

  .empty-icon {
    font-size: 40px;
    margin-bottom: 12px;
  }

  .empty-title {
    font-size: 14px;
  }

  .empty-desc {
    font-size: 12px;
  }
}

@media (max-width: 575px) {
  .feedback-page {
    max-width: 100%;
  }
}
</style>