<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageLayout from '../../components/PageLayout.vue'
import Loading from '../../components/Loading.vue'
import { adminApi } from '../../api/adminApi'

interface OnlineUser {
  id: number
  username: string
  nickname: string
  avatarUrl: string | null
  role: number
  isActive: number
  status: number
  lastLoginTime: string | null
  lastActiveTime: string | null
  createdTime: string
  isOnline: boolean
}

const stats = ref({ onlineCount: 0, todayActiveCount: 0, totalActive: 0, totalUsers: 0, onlineThresholdMinutes: 15 })
const users = ref<OnlineUser[]>([])
const loading = ref(false)
let refreshTimer: ReturnType<typeof setInterval> | null = null

const keyword = ref('')
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

const showMessageDialog = ref(false)
const showBroadcastDialog = ref(false)
const messageForm = ref({ recipientId: 0, recipientName: '', title: '', content: '' })
const broadcastForm = ref({ title: '', content: '' })
const sendingMessage = ref(false)

const showBanDialog = ref(false)
const banForm = ref({ userId: 0, username: '', banType: 1, banReason: '', banDuration: 7, customEndTime: '' })
const banning = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUsers({
      keyword: keyword.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const data = res.data.data
    stats.value = {
      onlineCount: data.onlineCount,
      todayActiveCount: data.todayActiveCount,
      totalActive: data.totalActive,
      totalUsers: data.totalUsers,
      onlineThresholdMinutes: data.onlineThresholdMinutes
    }
    total.value = data.total || 0
    users.value = (data.users || []).sort((a: OnlineUser, b: OnlineUser) => {
      if (a.isOnline !== b.isOnline) return a.isOnline ? -1 : 1
      if (!a.lastLoginTime) return 1
      if (!b.lastLoginTime) return -1
      return new Date(b.lastLoginTime).getTime() - new Date(a.lastLoginTime).getTime()
    })
  } catch {
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageNum.value = 1
  fetchData()
}

const handlePageChange = (page: number) => {
  pageNum.value = page
  fetchData()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  pageNum.value = 1
  fetchData()
}

const formatDate = (dateStr: string | null, fallback = '无记录') => {
  if (!dateStr) return fallback
  try {
    return new Date(dateStr).toLocaleString('zh-CN', {
      year: 'numeric', month: '2-digit', day: '2-digit',
      hour: '2-digit', minute: '2-digit', second: '2-digit'
    })
  } catch {
    return '格式错误'
  }
}

const getInitial = (user: OnlineUser) => {
  return (user.nickname || user.username || '?').charAt(0).toUpperCase()
}

const openSendMessageDialog = (user: OnlineUser) => {
  messageForm.value = {
    recipientId: user.id,
    recipientName: user.nickname || user.username,
    title: '',
    content: ''
  }
  showMessageDialog.value = true
}

const openBroadcastDialog = () => {
  broadcastForm.value = { title: '', content: '' }
  showBroadcastDialog.value = true
}

const sendMessage = async () => {
  if (!messageForm.value.content.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  sendingMessage.value = true
  try {
    await adminApi.sendMessage({
      recipientId: messageForm.value.recipientId,
      title: messageForm.value.title || undefined,
      content: messageForm.value.content
    })
    ElMessage.success('消息发送成功')
    showMessageDialog.value = false
  } catch {
    ElMessage.error('消息发送失败')
  } finally {
    sendingMessage.value = false
  }
}

const sendBroadcast = async () => {
  if (!broadcastForm.value.content.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  sendingMessage.value = true
  try {
    await adminApi.broadcastMessage({
      title: broadcastForm.value.title || undefined,
      content: broadcastForm.value.content
    })
    ElMessage.success('群发消息成功')
    showBroadcastDialog.value = false
  } catch {
    ElMessage.error('群发消息失败')
  } finally {
    sendingMessage.value = false
  }
}

const openBanDialog = (user: OnlineUser) => {
  if (user.status !== null && user.status !== 0) {
    ElMessageBox.confirm(
      `用户 ${user.nickname || user.username} 当前已被封禁，是否要解封？`,
      '解封确认',
      { confirmButtonText: '解封', cancelButtonText: '取消', type: 'warning' }
    ).then(async () => {
      try {
        await adminApi.unbanUser(user.id)
        ElMessage.success('用户已解封')
        fetchData()
      } catch {
        ElMessage.error('解封失败')
      }
    }).catch(() => {})
    return
  }
  banForm.value = { userId: user.id, username: user.nickname || user.username, banType: 1, banReason: '', banDuration: 7, customEndTime: '' }
  showBanDialog.value = true
}

const handleBanUser = async () => {
  if (!banForm.value.banReason.trim()) {
    ElMessage.warning('请填写禁用原因')
    return
  }
  if (banForm.value.banType === 2 && !banForm.value.customEndTime) {
    ElMessage.warning('请选择临时禁用的到期时间')
    return
  }

  banning.value = true
  try {
    const data: any = {
      banType: banForm.value.banType,
      banReason: banForm.value.banReason.trim()
    }
    if (banForm.value.banType === 2) {
      data.banEndTime = banForm.value.customEndTime
    }
    await adminApi.banUser(banForm.value.userId, data)
    ElMessage.success('用户已封禁')
    showBanDialog.value = false
    fetchData()
  } catch (e: any) {
    const msg = e.response?.data?.message || '封禁失败'
    ElMessage.error(msg)
  } finally {
    banning.value = false
  }
}

onMounted(() => {
  fetchData()
  refreshTimer = setInterval(fetchData, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<template>
  <PageLayout title="用户状态" subtitle="查看所有用户的在线情况">
    <div class="online-page">
      <div class="online-header">
        <div class="stats-bar">
          <div class="stat-card online">
            <span class="stat-dot green"></span>
            <span class="stat-num">{{ stats.onlineCount }}</span>
            <span class="stat-label">当前在线</span>
          </div>
          <div class="stat-card">
            <span class="stat-dot blue"></span>
            <span class="stat-num">{{ stats.todayActiveCount }}</span>
            <span class="stat-label">今日活跃</span>
          </div>
          <div class="stat-card">
            <span class="stat-dot"></span>
            <span class="stat-num">{{ stats.totalActive }}</span>
            <span class="stat-label">历史活跃</span>
          </div>
          <div class="stat-card">
            <span class="stat-dot gray"></span>
            <span class="stat-num">{{ stats.totalUsers }}</span>
            <span class="stat-label">总注册</span>
          </div>
        </div>
        <div class="header-actions">
          <el-button type="primary" size="small" @click="openBroadcastDialog">群发消息</el-button>
          <p class="threshold-note">判定标准：{{ stats.onlineThresholdMinutes }} 分钟内有活跃行为视为在线，每30秒自动刷新</p>
        </div>
      </div>

      <div class="online-body">
        <div class="search-bar">
          <el-input
            v-model="keyword"
            placeholder="搜索用户名或昵称..."
            clearable
            style="width: 280px;"
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix></template>
          </el-input>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </div>

        <Loading v-if="loading && users.length === 0" text="加载中..." />

        <template v-else>
          <div v-if="users.length === 0" class="empty-state">
            <div class="empty-icon">
              <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
            </div>
            <div class="empty-title">暂无用户数据</div>
          </div>

          <div v-else class="user-list">
            <div v-for="user in users" :key="user.id" class="user-card" :class="{ 'is-online': user.isOnline, 'is-banned': user.status && user.status !== 0 }">
              <div class="user-row">
                <div class="user-avatar-wrap">
                  <el-avatar v-if="user.avatarUrl" :src="user.avatarUrl" :size="44" />
                  <el-avatar v-else :size="44" class="avatar-placeholder">{{ getInitial(user) }}</el-avatar>
                  <span class="online-dot" :class="{ active: user.isOnline }"></span>
                </div>
                <div class="user-info">
                  <div class="user-name">
                    {{ user.nickname || user.username }}
                    <span v-if="user.role === 1" class="admin-tag">管理员</span>
                  </div>
                  <div class="user-meta">@{{ user.username }} · 注册于 {{ formatDate(user.createdTime) }}</div>
                </div>
                <div class="user-actions-col">
                  <div class="user-status">
                    <span v-if="user.isOnline" class="status-badge status-online">在线</span>
                    <span v-else-if="user.status === 1" class="status-badge status-banned-perm">永久禁用</span>
                    <span v-else-if="user.status === 2" class="status-badge status-banned-temp">临时禁用</span>
                    <span v-else class="status-badge status-offline">离线</span>
                    <div class="last-active">最后活跃：{{ formatDate(user.lastActiveTime, '从未活跃') }}</div>
                    <div class="last-active">最后登录：{{ formatDate(user.lastLoginTime, '从未登录') }}</div>
                  </div>
                  <div class="user-actions" v-if="user.role !== 1">
                    <el-button size="small" :type="user.status && user.status !== 0 ? 'danger' : 'warning'" plain @click="openBanDialog(user)">
                      {{ user.status && user.status !== 0 ? '解封' : '封禁' }}
                    </el-button>
                    <el-button size="small" type="primary" plain @click="openSendMessageDialog(user)">发消息</el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="total > pageSize" class="pagination-wrap">
            <el-pagination
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
            />
          </div>
        </template>
        <div class="online-body-spacer"></div>
      </div>
    </div>

    <el-dialog v-model="showMessageDialog" title="发送消息" width="500px" :append-to-body="true">
      <el-form label-position="top">
        <el-form-item label="接收用户">
          <el-input :model-value="messageForm.recipientName" disabled />
        </el-form-item>
        <el-form-item label="消息标题（选填）">
          <el-input v-model="messageForm.title" placeholder="简要描述消息主题" maxlength="100" />
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input
            v-model="messageForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入消息内容..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showMessageDialog = false">取消</el-button>
        <el-button type="primary" :loading="sendingMessage" @click="sendMessage">发送</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showBroadcastDialog" title="群发消息" width="500px" :append-to-body="true">
      <el-form label-position="top">
        <el-form-item label="消息标题（选填）">
          <el-input v-model="broadcastForm.title" placeholder="简要描述消息主题" maxlength="100" />
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input
            v-model="broadcastForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入群发消息内容，将发送给所有普通用户..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBroadcastDialog = false">取消</el-button>
        <el-button type="primary" :loading="sendingMessage" @click="sendBroadcast">群发</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showBanDialog" title="封禁用户" width="480px" :append-to-body="true" :close-on-click-modal="false">
      <el-form label-position="top">
        <el-form-item label="封禁用户">
          <el-input :model-value="banForm.username" disabled />
        </el-form-item>
        <el-form-item label="封禁类型">
          <el-radio-group v-model="banForm.banType">
            <el-radio :value="1">永久禁用</el-radio>
            <el-radio :value="2">临时禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="banForm.banType === 2" label="到期时间">
          <el-date-picker
            v-model="banForm.customEndTime"
            type="datetime"
            placeholder="选择到期时间"
            :disabled-date="(date: Date) => date.getTime() < Date.now()"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="禁用原因（用户可见，必填）">
          <el-input
            v-model="banForm.banReason"
            type="textarea"
            :rows="3"
            placeholder="请填写禁用原因，此原因将展示给被封禁用户..."
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showBanDialog = false">取消</el-button>
        <el-button type="danger" :loading="banning" @click="handleBanUser">确认封禁</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<style scoped>
.online-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-width: 900px;
  margin: 0 auto;
}

.online-header {
  flex-shrink: 0;
  margin-bottom: 16px;
}

.stats-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.stat-card {
  flex: 1;
  background: #FFFFFF;
  border-radius: var(--radius-md);
  padding: 16px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #D1D5DB;
}

.stat-dot.green { background: #3F6212; animation: pulse 2s infinite; }
.stat-dot.blue { background: var(--primary); }
.stat-dot.gray { background: #9CA3AF; }

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.stat-label {
  font-size: 12px;
  color: #9CA3AF;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.threshold-note {
  font-size: 12px;
  color: #9CA3AF;
  margin: 0;
}

.online-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.online-body::-webkit-scrollbar {
  width: 5px;
}

.online-body::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.online-body::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.online-body::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--primary-rgb), 0.45);
}

.online-body-spacer {
  height: 8px;
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.user-card {
  background: #FFFFFF;
  border-radius: var(--radius-lg);
  padding: 16px 18px;
  border: 1px solid #D1D5DB;
  box-shadow: var(--shadow-sm);
  border-left: 4px solid #D1D5DB;
  transition: all 0.2s;
}

.user-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.user-card.is-online {
  border-left-color: #3F6212;
  background: rgba(240, 253, 244, 0.92);
}

.user-card.is-banned {
  opacity: 0.7;
  border-left-color: #B91C1C;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 14px;
}

.user-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.avatar-placeholder {
  background: var(--primary);
  color: white;
  font-weight: 600;
}

.online-dot {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #D1D5DB;
  border: 2.5px solid #fff;
}

.online-dot.active {
  background: #3F6212;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.admin-tag {
  font-size: 10px;
  color: #d97706;
  background: #fef3c7;
  padding: 2px 6px;
  border-radius: 6px;
  margin-left: 6px;
}

.user-meta {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 3px;
}

.user-actions-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.user-status {
  text-align: right;
}

.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.status-online {
  background: #dcfce7;
  color: #166534;
}

.status-offline {
  background: #f3f4f6;
  color: #6b7280;
}

.status-disabled {
  background: #fee2e2;
  color: #dc2626;
}

.status-banned-perm {
  background: #fee2e2;
  color: #991b1b;
}

.status-banned-temp {
  background: #fef3c7;
  color: #92400e;
}

.last-active {
  font-size: 11px;
  color: #9CA3AF;
  margin-top: 5px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 8px;
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

  .user-card {
    padding: 14px;
  }

  .user-row {
    gap: 10px;
  }

.search-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 20px 0 8px;
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
}

@media (max-width: 575px) {
  .online-page {
    max-width: 100%;
  }

  .stats-bar {
    flex-wrap: wrap;
  }

  .stat-card {
    min-width: calc(50% - 4px);
  }

  .user-row {
    flex-wrap: wrap;
  }

  .user-actions-col {
    width: 100%;
    align-items: flex-start;
    padding-left: 58px;
  }

  .user-status {
    text-align: left;
  }

  .user-actions {
    flex-wrap: wrap;
  }

  .header-actions {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
