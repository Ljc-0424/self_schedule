<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageLayout from '../../components/PageLayout.vue'
import Loading from '../../components/Loading.vue'
import { adminApi } from '../../api/adminApi'
import type { AppealVO } from '../../types'

const appeals = ref<AppealVO[]>([])
const loading = ref(false)
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const filterStatus = ref<number | string>('all')

const showAuditDialog = ref(false)
const auditForm = ref({ id: 0, status: 1, auditNote: '' })
const auditing = ref(false)

const fetchAppeals = async () => {
  loading.value = true
  try {
    const res = await adminApi.getAppeals({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      status: filterStatus.value === 'all' ? null : (filterStatus.value as number)
    })
    if (res.data.code === 200) {
      const data = res.data.data
      appeals.value = data.records
      total.value = data.total
    }
  } catch {
    ElMessage.error('获取申诉列表失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  pageNum.value = page
  fetchAppeals()
}

const handleFilterChange = () => {
  pageNum.value = 1
  fetchAppeals()
}

const formatDate = (dateStr: string | null | undefined) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const getStatusTag = (status: number) => {
  switch (status) {
    case 0: return { type: 'warning' as const, text: '待审核' }
    case 1: return { type: 'success' as const, text: '已通过' }
    case 2: return { type: 'danger' as const, text: '已驳回' }
    default: return { type: 'info' as const, text: '未知' }
  }
}

const getBanTypeText = (status: number | null) => {
  if (status === 1) return '永久禁用'
  if (status === 2) return '临时禁用'
  return '-'
}

const openAuditDialog = (appeal: AppealVO, status: number) => {
  auditForm.value = { id: appeal.id, status, auditNote: '' }
  showAuditDialog.value = true
}

const handleAudit = async () => {
  if (auditForm.value.status === 2 && !auditForm.value.auditNote.trim()) {
    ElMessage.warning('驳回申诉必须填写驳回理由')
    return
  }
  auditing.value = true
  try {
    await adminApi.auditAppeal(auditForm.value.id, {
      status: auditForm.value.status,
      auditNote: auditForm.value.auditNote.trim() || undefined
    })
    ElMessage.success(auditForm.value.status === 1 ? '申诉已通过，账号已解封' : '申诉已驳回')
    showAuditDialog.value = false
    fetchAppeals()
  } catch (e: any) {
    const msg = e.response?.data?.message || '审核失败'
    ElMessage.error(msg)
  } finally {
    auditing.value = false
  }
}

onMounted(() => {
  fetchAppeals()
})
</script>

<template>
  <PageLayout title="申诉管理" subtitle="审核用户提交的封禁申诉">
    <div class="appeal-page">
      <div class="appeal-header">
        <div class="filter-bar">
          <el-radio-group v-model="filterStatus" @change="handleFilterChange">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button :value="0">待审核</el-radio-button>
            <el-radio-button :value="1">已通过</el-radio-button>
            <el-radio-button :value="2">已驳回</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="appeal-body">
        <Loading v-if="loading && appeals.length === 0" text="加载中..." />

        <template v-else>
          <div v-if="appeals.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10 9 9 9 8 9"/>
              </svg>
            </div>
            <div class="empty-title">暂无申诉记录</div>
          </div>

          <div v-else class="appeal-list">
            <div v-for="appeal in appeals" :key="appeal.id" class="appeal-card" :class="{ 'is-pending': appeal.status === 0 }">
              <div class="appeal-card-header">
                <div class="user-info">
                  <span class="user-name">{{ appeal.nickname || appeal.username }}</span>
                  <span class="user-username">@{{ appeal.username }}</span>
                </div>
                <el-tag :type="getStatusTag(appeal.status).type" size="small">
                  {{ getStatusTag(appeal.status).text }}
                </el-tag>
              </div>

              <div class="appeal-card-body">
                <div class="info-grid">
                  <div class="info-item">
                    <span class="info-label">封禁类型</span>
                    <span class="info-value">
                      <el-tag :type="appeal.userStatus === 1 ? 'danger' : 'warning'" size="small">
                        {{ getBanTypeText(appeal.userStatus) }}
                      </el-tag>
                    </span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">封禁原因</span>
                    <span class="info-value">{{ appeal.banReason || '-' }}</span>
                  </div>
                  <div class="info-item">
                    <span class="info-label">封禁时间</span>
                    <span class="info-value">{{ formatDate(appeal.banTime) }}</span>
                  </div>
                  <div v-if="appeal.userStatus === 2 && appeal.banEndTime" class="info-item">
                    <span class="info-label">到期时间</span>
                    <span class="info-value">{{ formatDate(appeal.banEndTime) }}</span>
                  </div>
                </div>

                <el-divider style="margin: 12px 0;" />

                <div class="appeal-content">
                  <span class="info-label">申诉理由</span>
                  <p class="content-text">{{ appeal.content }}</p>
                  <span class="info-label" style="margin-top: 8px;">提交时间</span>
                  <p class="content-text">{{ formatDate(appeal.createdTime) }}</p>
                </div>

                <template v-if="appeal.status !== 0">
                  <el-divider style="margin: 12px 0;" />
                  <div class="audit-result">
                    <div class="info-item">
                      <span class="info-label">审核人</span>
                      <span class="info-value">{{ appeal.auditAdminName || '-' }}</span>
                    </div>
                    <div class="info-item">
                      <span class="info-label">审核时间</span>
                      <span class="info-value">{{ formatDate(appeal.auditTime) }}</span>
                    </div>
                    <div v-if="appeal.auditNote" class="info-item">
                      <span class="info-label">审核备注</span>
                      <span class="info-value">{{ appeal.auditNote }}</span>
                    </div>
                  </div>
                </template>
              </div>

              <div v-if="appeal.status === 0" class="appeal-card-footer">
                <el-button type="success" @click="openAuditDialog(appeal, 1)">通过（解封）</el-button>
                <el-button type="danger" @click="openAuditDialog(appeal, 2)">驳回</el-button>
              </div>
            </div>
          </div>

          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="pageNum"
              :page-size="pageSize"
              :total="total"
              layout="prev, pager, next"
              @current-change="handlePageChange"
            />
          </div>
        </template>
      </div>
    </div>

    <el-dialog v-model="showAuditDialog" :title="auditForm.status === 1 ? '通过申诉' : '驳回申诉'" width="460px" :append-to-body="true" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item v-if="auditForm.status === 1" label="审核备注（选填）">
          <el-input
            v-model="auditForm.auditNote"
            type="textarea"
            :rows="3"
            placeholder="如：核实后确认可解封"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        <el-form-item v-if="auditForm.status === 2" label="驳回理由（必填，用户可见）">
          <el-input
            v-model="auditForm.auditNote"
            type="textarea"
            :rows="3"
            placeholder="请填写驳回理由，此理由将展示给用户..."
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAuditDialog = false">取消</el-button>
        <el-button :type="auditForm.status === 1 ? 'success' : 'danger'" :loading="auditing" @click="handleAudit">
          {{ auditForm.status === 1 ? '确认通过' : '确认驳回' }}
        </el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<style scoped>
.appeal-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-width: 900px;
  margin: 0 auto;
}

.appeal-header {
  flex-shrink: 0;
  margin-bottom: 16px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.appeal-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.appeal-body::-webkit-scrollbar {
  width: 5px;
}

.appeal-body::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.appeal-body::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.appeal-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.appeal-card {
  background: #FFFFFF;
  border-radius: var(--radius-lg);
  padding: 18px;
  border: 1px solid #D1D5DB;
  box-shadow: var(--shadow-sm);
  border-left: 4px solid #D1D5DB;
  transition: all 0.2s;
}

.appeal-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.appeal-card.is-pending {
  border-left-color: #f59e0b;
  background: rgba(255, 251, 235, 0.92);
}

.appeal-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.user-username {
  font-size: 12px;
  color: #9CA3AF;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 12px;
  color: #9CA3AF;
}

.info-value {
  font-size: 14px;
  color: #111827;
  word-break: break-all;
}

.appeal-content {
  display: flex;
  flex-direction: column;
}

.content-text {
  margin: 0;
  font-size: 14px;
  color: #334155;
  line-height: 1.6;
  white-space: pre-wrap;
}

.audit-result {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px 16px;
}

.appeal-card-footer {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid #E5E7EB;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
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
}

@media (max-width: 767px) {
  .info-grid {
    grid-template-columns: 1fr;
  }

  .audit-result {
    grid-template-columns: 1fr;
  }

  .appeal-card {
    padding: 14px;
  }

  .appeal-card-footer {
    flex-direction: column;
  }

  .appeal-card-footer .el-button {
    width: 100%;
  }
}
</style>
