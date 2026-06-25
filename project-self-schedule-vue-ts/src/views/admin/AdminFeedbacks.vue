<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageLayout from '../../components/PageLayout.vue'
import Loading from '../../components/Loading.vue'
import Pagination from '../../components/Pagination.vue'
import { adminApi } from '../../api/adminApi'

interface FeedbackVO {
  id: number
  userId: number
  username: string
  nickname: string
  category: string
  title: string
  content: string
  contact: string | null
  status: number
  adminReply: string | null
  createdTime: string
  updatedTime: string
}

const feedbacks = ref<FeedbackVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = 20
const filterStatus = ref<number | null>(null)
const loading = ref(false)
const stats = ref({ total: 0, pending: 0, processed: 0, closed: 0 })

const showReply = ref(false)
const replyForm = ref({ id: 0, status: 1, adminReply: '' })
const replyLoading = ref(false)

const statusMap: Record<number, { label: string; cls: string }> = {
  0: { label: '未处理', cls: 'status-pending' },
  1: { label: '已处理', cls: 'status-done' },
  2: { label: '已关闭', cls: 'status-closed' }
}

const categoryLabelMap: Record<string, string> = {
  bug: 'BUG',
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

const fetchFeedbacks = async (page: number = 1) => {
  loading.value = true
  try {
    const res = await adminApi.getFeedbacks({ pageNum: page, pageSize: pageSize, status: filterStatus.value })
    const pageResult = res.data.data
    feedbacks.value = pageResult.records
    total.value = pageResult.total
    currentPage.value = pageResult.pageNum || page
    totalPages.value = pageResult.pages || 1
  } catch {
    ElMessage.error('获取反馈列表失败')
  } finally {
    loading.value = false
  }
}

const fetchStats = async () => {
  try {
    const res = await adminApi.getFeedbackStats()
    stats.value = res.data.data
  } catch {}
}

const handleFilter = (status: number | null) => {
  filterStatus.value = status
  fetchFeedbacks(1)
}

const handlePageChange = (page: number) => {
  fetchFeedbacks(page)
}

const openReply = (item: FeedbackVO) => {
  replyForm.value = {
    id: item.id,
    status: item.status === 0 ? 1 : item.status,
    adminReply: item.adminReply || ''
  }
  showReply.value = true
}

const handleReply = async () => {
  if (!replyForm.value.adminReply.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  replyLoading.value = true
  try {
    await adminApi.replyFeedback(replyForm.value.id, {
      status: replyForm.value.status,
      adminReply: replyForm.value.adminReply
    })
    ElMessage.success('回复成功')
    showReply.value = false
    fetchFeedbacks(currentPage.value)
    fetchStats()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '回复失败')
  } finally {
    replyLoading.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(() => {
  fetchFeedbacks()
  fetchStats()
})
</script>

<template>
  <PageLayout title="反馈管理" subtitle="查看和处理用户反馈">
    <div class="admin-feedback-page">
      <div class="admin-feedback-header">
        <div class="stats-bar">
          <div class="stat-card" :class="{ active: filterStatus === null }" @click="handleFilter(null)">
            <span class="stat-num">{{ stats.total }}</span>
            <span class="stat-label">全部</span>
          </div>
          <div class="stat-card warn" :class="{ active: filterStatus === 0 }" @click="handleFilter(0)">
            <span class="stat-num">{{ stats.pending }}</span>
            <span class="stat-label">待处理</span>
          </div>
          <div class="stat-card success" :class="{ active: filterStatus === 1 }" @click="handleFilter(1)">
            <span class="stat-num">{{ stats.processed }}</span>
            <span class="stat-label">已处理</span>
          </div>
          <div class="stat-card info" :class="{ active: filterStatus === 2 }" @click="handleFilter(2)">
            <span class="stat-num">{{ stats.closed }}</span>
            <span class="stat-label">已关闭</span>
          </div>
        </div>
      </div>

      <div class="admin-feedback-body">
        <Loading v-if="loading" text="加载中..." />

        <template v-else>
          <div v-if="feedbacks.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
            </div>
            <div class="empty-title">暂无反馈记录</div>
            <div class="empty-desc">用户提交的反馈将在这里显示</div>
          </div>

          <div v-else class="feedback-list">
            <div v-for="item in feedbacks" :key="item.id" class="feedback-card" :class="statusMap[item.status]?.cls">
              <div class="feedback-top">
                <span class="user-badge">{{ item.nickname || item.username }}</span>
                <span class="category-badge">
                  {{ categoryIconMap[item.category] || '' }}
                  {{ categoryLabelMap[item.category] || item.category }}
                </span>
                <span class="status-badge" :class="statusMap[item.status]?.cls">
                  {{ statusMap[item.status]?.label }}
                </span>
                <span class="feedback-time">{{ formatDate(item.createdTime) }}</span>
              </div>
              <h3 class="feedback-title">{{ item.title }}</h3>
              <p class="feedback-content">{{ item.content }}</p>
              <div v-if="item.contact" class="feedback-contact">{{ item.contact }}</div>

              <div v-if="item.adminReply" class="admin-reply">
                <div class="reply-header">管理员回复</div>
                <p>{{ item.adminReply }}</p>
              </div>

              <div class="feedback-actions">
                <el-button size="small" type="primary" plain @click="openReply(item)">
                  {{ item.adminReply ? '修改回复' : '回复' }}
                </el-button>
              </div>
            </div>
          </div>
        </template>
        <div class="admin-feedback-body-spacer"></div>
      </div>

      <div v-if="total > 0" class="admin-feedback-footer">
        <Pagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :total-items="total"
          @page-change="handlePageChange"
        />
      </div>
    </div>

    <el-dialog v-model="showReply" title="回复反馈" width="500px" :append-to-body="true">
      <el-form label-position="top">
        <el-form-item label="处理状态">
          <el-select v-model="replyForm.status" style="width: 100%;">
            <el-option :value="1" label="已处理" />
            <el-option :value="2" label="已关闭" />
          </el-select>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.adminReply"
            type="textarea"
            :rows="4"
            placeholder="输入回复内容..."
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showReply = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="handleReply">确认回复</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<style scoped>
.admin-feedback-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-width: 900px;
  margin: 0 auto;
}

.admin-feedback-header {
  flex-shrink: 0;
  margin-bottom: 16px;
}

.stats-bar {
  display: flex;
  gap: 12px;
}

.stat-card {
  flex: 1;
  background: #FFFFFF;
  border-radius: var(--radius-md);
  padding: 16px;
  text-align: center;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-card.active {
  border-color: transparent;
  background: #EFF6FF;
}

.stat-card.warn .stat-num { color: #E6A23C; }
.stat-card.success .stat-num { color: #3F6212; }
.stat-card.info .stat-num { color: #9CA3AF; }

.stat-num {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: var(--primary);
}

.stat-label {
  font-size: 12px;
  color: #9CA3AF;
}

.admin-feedback-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.admin-feedback-body::-webkit-scrollbar {
  width: 5px;
}

.admin-feedback-body::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.admin-feedback-body::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.admin-feedback-body::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--primary-rgb), 0.45);
}

.admin-feedback-body-spacer {
  height: 8px;
}

.admin-feedback-footer {
  flex-shrink: 0;
  padding: 12px 0 20px;
}

.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feedback-card {
  background: #FFFFFF;
  border-radius: var(--radius-lg);
  padding: 18px;
  border: 1px solid #D1D5DB;
  box-shadow: var(--shadow-sm);
  border-left: 4px solid #e2e8f0;
  transition: all 0.2s;
}

.feedback-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.feedback-card.status-pending {
  border-left-color: #E6A23C;
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

.user-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
  background: rgba(var(--primary-rgb), 0.12);
  color: var(--primary);
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
  color: #6B7280;
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

.feedback-contact {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 8px;
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
  color: #475569;
  line-height: 1.6;
  white-space: pre-wrap;
}

.feedback-actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #FFFFFF;
  border-radius: var(--radius-lg);
  border: 1px solid #D1D5DB;
  box-shadow: var(--shadow-sm);
}

.empty-icon {
  margin-bottom: 12px;
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
  .stats-bar {
    gap: 8px;
  }

  .stat-card {
    padding: 12px 8px;
  }

  .stat-num {
    font-size: 20px;
  }

  .stat-label {
    font-size: 11px;
  }

  .feedback-card {
    padding: 14px;
  }

  .feedback-top {
    flex-wrap: wrap;
    gap: 6px;
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
  .admin-feedback-page {
    max-width: 100%;
  }

  .stats-bar {
    flex-wrap: wrap;
  }

  .stat-card {
    min-width: calc(50% - 4px);
  }
}
</style>