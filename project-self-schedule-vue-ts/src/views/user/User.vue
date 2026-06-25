<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { Chart, ArcElement, Tooltip, Legend, DoughnutController, PieController } from 'chart.js'
import PageLayout from '../../components/PageLayout.vue'
import UserEditForm from './UserEditForm.vue'
import AchievementWall from './AchievementWall.vue'
import { authApi } from '../../api/authApi'
import { userApi } from '../../api/userApi'
import { user, isEditing, isSaving, isUploadingAvatar, startEdit, cancelEdit, saveUser, formatDateTime, fetchUserInfo, handleAvatarUpload, handleAvatarDelete } from './useUserInfo'
import type { UserStatsVO } from '../../types'

Chart.register(ArcElement, Tooltip, Legend, DoughnutController)

const store = useStore()
const showSuccess = ref(false)
const isAdmin = computed(() => store.getters['auth/isAdmin'])
const avatarInputRef = ref<HTMLInputElement | null>(null)

const showChangePwd = ref(false)
const changePwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const changePwdLoading = ref(false)

const stats = ref<UserStatsVO | null>(null)
const statsLoading = ref(false)

const showBadgeHelp = ref(false)
const activeTab = ref<'stats' | 'profile' | 'settings' | 'achievements'>('stats')

// ========== 主题切换 ==========
const themeColors = [
  { name: '默认蓝', color: '#3B82F6', theme: '' },
  { name: '深灰蓝', color: '#475569', theme: 'slate' },
  { name: '墨绿', color: '#059669', theme: 'emerald' },
  { name: '暖橙', color: '#D97706', theme: 'amber' },
  { name: '淡紫', color: '#7C3AED', theme: 'violet' },
]

const currentTheme = ref(localStorage.getItem('app_theme') || '')

const switchTheme = (theme: string) => {
  currentTheme.value = theme
  if (theme) {
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('app_theme', theme)
  } else {
    document.documentElement.removeAttribute('data-theme')
    localStorage.removeItem('app_theme')
  }
}

const handleAvatarClick = () => {
  avatarInputRef.value?.click()
}

fetchUserInfo()
fetchStats()

const handleUpdate = async () => {
  await saveUser()
  showSuccess.value = true
  setTimeout(() => {
    showSuccess.value = false
  }, 3000)
}

const openChangePwd = () => {
  changePwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  showChangePwd.value = true
}

const handleChangePwd = async () => {
  const { oldPassword, newPassword, confirmPassword } = changePwdForm.value
  if (!oldPassword || !newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (newPassword.length < 6 || newPassword.length > 50) {
    ElMessage.warning('新密码长度需要在6-50个字符之间')
    return
  }
  if (newPassword !== confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  if (oldPassword === newPassword) {
    ElMessage.warning('新密码不能与旧密码相同')
    return
  }
  changePwdLoading.value = true
  try {
    await authApi.changePassword({ oldPassword, newPassword })
    ElMessage.success('密码修改成功')
    showChangePwd.value = false
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '修改失败')
  } finally {
    changePwdLoading.value = false
  }
}

async function fetchStats() {
  statsLoading.value = true
  try {
    const res = await userApi.getStats()
    if (res.data.code === 200) {
      stats.value = res.data.data
    }
  } catch (e) {
    console.error('获取统计数据失败', e)
  } finally {
    statsLoading.value = false
  }
}

function formatSeconds(seconds: number): string {
  if (!seconds || seconds <= 0) return '0分钟'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return minutes > 0 ? `${hours}小时${minutes}分钟` : `${hours}小时`
  }
  return `${minutes}分钟`
}

function formatRate(rate: number): string {
  return (rate * 100).toFixed(1) + '%'
}

// ========== 徽章定义与计算 ==========

interface Badge {
  id: string
  name: string
  icon: string
  description: string
  category: 'focus' | 'task' | 'streak' | 'special'
  condition: string
  progressValue: number
  maxProgress: number
  unlocked: boolean
  progressPercent: number
  progressText: string
}

// 计算专注时长徽章（基于累计分钟数）
function computeFocusBadges(totalSeconds: number): Badge[] {
  const totalMinutes = totalSeconds / 60
  const tiers = [
    { id: 'focus_beginner', name: '初学者', icon: '◯', condition: '累计专注 60 分钟', target: 60 },
    { id: 'focus_advanced', name: '进阶者', icon: '◉', condition: '累计专注 600 分钟', target: 600 },
    { id: 'focus_expert', name: '专家', icon: '★', condition: '累计专注 3000 分钟', target: 3000 },
    { id: 'focus_master', name: '大师', icon: '♛', condition: '累计专注 12000 分钟', target: 12000 },
    { id: 'focus_legend', name: '传奇', icon: '✦', condition: '累计专注 60000 分钟', target: 60000 },
  ]
  let lastUnlocked = true
  return tiers.map(t => {
    const unlocked = lastUnlocked && totalMinutes >= t.target
    if (!unlocked) lastUnlocked = false
    const pv = Math.min(totalMinutes, t.target)
    const pct = Math.min(Math.round((pv / t.target) * 100), 100)
    return {
      id: t.id, name: t.name, icon: t.icon,
      description: `累计专注 ${t.target} 分钟`,
      category: 'focus' as const, condition: t.condition,
      progressValue: pv, maxProgress: t.target, unlocked, progressPercent: pct,
      progressText: unlocked ? '已解锁' : `${formatMinutes(totalMinutes)} / ${t.target}分钟`,
    }
  })
}

function formatMinutes(m: number): string {
  if (m >= 1000) return Math.round(m) + '分钟'
  if (m >= 100) return m.toFixed(0) + '分钟'
  return m.toFixed(1) + '分钟'
}

// 计算任务完成徽章
function computeTaskBadges(completed: number): Badge[] {
  const tiers = [
    { id: 'task_start', name: '起步', icon: '◯', condition: '累计完成 10 个任务', target: 10 },
    { id: 'task_efficient', name: '高效', icon: '◉', condition: '累计完成 50 个任务', target: 50 },
    { id: 'task_pro', name: '达人', icon: '★', condition: '累计完成 200 个任务', target: 200 },
    { id: 'task_excellent', name: '卓越', icon: '♛', condition: '累计完成 500 个任务', target: 500 },
    { id: 'task_peak', name: '巅峰', icon: '✦', condition: '累计完成 1000 个任务', target: 1000 },
  ]
  let lastUnlocked = true
  return tiers.map(t => {
    const unlocked = lastUnlocked && completed >= t.target
    if (!unlocked) lastUnlocked = false
    const pv = Math.min(completed, t.target)
    const pct = Math.min(Math.round((pv / t.target) * 100), 100)
    return {
      id: t.id, name: t.name, icon: t.icon,
      description: `累计完成 ${t.target} 个任务`,
      category: 'task' as const, condition: t.condition,
      progressValue: pv, maxProgress: t.target, unlocked, progressPercent: pct,
      progressText: unlocked ? '已解锁' : `${completed} / ${t.target}`,
    }
  })
}

// 连续专注天数（mock数据，后续接入后端）
const mockMaxStreak = ref(12)

// 计算连续打卡徽章
function computeStreakBadges(maxStreak: number): Badge[] {
  const tiers = [
    { id: 'streak_start', name: '启程', condition: '连续专注 3 天', target: 3 },
    { id: 'streak_persist', name: '坚持', condition: '连续专注 7 天', target: 7 },
    { id: 'streak_habit', name: '习惯', condition: '连续专注 21 天', target: 21 },
    { id: 'streak_discipline', name: '自律', condition: '连续专注 60 天', target: 60 },
    { id: 'streak_ultimate', name: '极致', condition: '连续专注 365 天', target: 365 },
  ]
  let lastUnlocked = true
  return tiers.map(t => {
    const unlocked = lastUnlocked && maxStreak >= t.target
    if (!unlocked) lastUnlocked = false
    const pv = Math.min(maxStreak, t.target)
    const pct = Math.min(Math.round((pv / t.target) * 100), 100)
    return {
      id: t.id, name: t.name, icon: '●',
      description: `连续专注 ${t.target} 天`,
      category: 'streak' as const, condition: t.condition,
      progressValue: pv, maxProgress: t.target, unlocked, progressPercent: pct,
      progressText: unlocked ? '已解锁' : `${maxStreak} / ${t.target}天`,
    }
  })
}

// 特殊成就（mock数据，后续接入后端）
const mockSpecialFlags = ref({
  nightOwl: false,    // 夜猫子：凌晨0-5点完成过任务
  earlyBird: true,    // 早起鸟：早上6点前开始过专注
})

// 计算特殊成就徽章
function computeSpecialBadges(stats: UserStatsVO): Badge[] {
  const maxDailyMinutes = stats.maxDailyFocusSeconds / 60
  const allDayAchieved = stats.maxStreak >= 30 || stats.totalCheckInDays >= 30
  const flags = mockSpecialFlags.value

  const specialDefs = [
    {
      id: 'special_nightowl', name: '夜猫子', icon: '●',
      description: '凌晨 0:00 - 5:59 完成过任务',
      condition: '凌晨完成任务', unlocked: flags.nightOwl,
      progressText: flags.nightOwl ? '已解锁' : '未达成',
    },
    {
      id: 'special_earlybird', name: '早起鸟', icon: '●',
      description: '早上 6:00 前开始过专注',
      condition: '早上6点前开始专注', unlocked: flags.earlyBird,
      progressText: flags.earlyBird ? '已解锁' : '未达成',
    },
    {
      id: 'special_marathon', name: '马拉松', icon: '●',
      description: '单次专注超过 120 分钟',
      condition: '单次专注超过120分钟',
      unlocked: maxDailyMinutes >= 120,
      progressText: maxDailyMinutes >= 120 ? '已解锁' : `最高 ${maxDailyMinutes.toFixed(0)}分钟 / 120分钟`,
    },
    {
      id: 'special_allday', name: '全勤王', icon: '●',
      description: '30天内每天至少完成 1 个任务',
      condition: '30天内每天至少1个任务',
      unlocked: allDayAchieved,
      progressText: allDayAchieved ? '已解锁' : `${Math.max(stats.maxStreak, stats.totalCheckInDays)} / 30天`,
    },
  ]

  return specialDefs.map(d => ({
    ...d,
    category: 'special' as const,
    progressValue: d.unlocked ? 1 : 0,
    maxProgress: 1,
    progressPercent: d.unlocked ? 100 : 0,
  }))
}

// 前端计算全部徽章
const computedBadges = computed<Badge[]>(() => {
  if (!stats.value) return []
  const s = stats.value
  return [
    ...computeFocusBadges(s.totalFocusSeconds),
    ...computeTaskBadges(s.totalCompletedTasks),
    ...computeStreakBadges(mockMaxStreak.value),
    ...computeSpecialBadges(s),
  ]
})

const focusBadges = computed(() => computedBadges.value.filter(b => b.category === 'focus'))
const taskBadges = computed(() => computedBadges.value.filter(b => b.category === 'task'))
const streakBadges = computed(() => computedBadges.value.filter(b => b.category === 'streak'))
const specialBadges = computed(() => computedBadges.value.filter(b => b.category === 'special'))

const unlockedCount = computed(() => computedBadges.value.filter(b => b.unlocked).length)
const totalBadgesCount = computed(() => computedBadges.value.length)

// ========== 图表数据 ==========
const weekDays = ['一', '二', '三', '四', '五', '六', '日']

// 本周专注数据（基于统计数据生成，后端可替换为真实数据）
const weekFocusData = computed(() => {
  if (!stats.value) return [0, 0, 0, 0, 0, 0, 0]
  const total = stats.value.totalFocusSeconds || 0
  // 基于总时长按比例分配7天，加随机波动
  const base = total / 7 / 60 || 0
  const seeds = [1.2, 0.8, 1.0, 1.3, 0.7, 0.5, 0.9]
  return seeds.map(s => Math.round(base * s))
})

const linePoints = computed(() => {
  const data = weekFocusData.value
  const max = Math.max(...data, 1)
  const w = 280, h = 80, padY = 10
  return data.map((v, i) => {
    const x = (i / (data.length - 1)) * w
    const y = h - padY - ((v / max) * (h - padY * 2))
    return `${x},${y}`
  }).join(' ')
})

const lineAreaPath = computed(() => {
  const data = weekFocusData.value
  const max = Math.max(...data, 1)
  const w = 280, h = 80, padY = 10
  const pts = data.map((v, i) => {
    const x = (i / (data.length - 1)) * w
    const y = h - padY - ((v / max) * (h - padY * 2))
    return `${x},${y}`
  })
  if (pts.length === 0) return ''
  return `M0,${h} L${pts.join(' L')} L${w},${h} Z`
})

const lineDots = computed(() => {
  const data = weekFocusData.value
  const max = Math.max(...data, 1)
  const w = 280, h = 80, padY = 10
  return data.map((v, i) => ({
    x: (i / (data.length - 1)) * w,
    y: h - padY - ((v / max) * (h - padY * 2)),
  }))
})

const taskCompletionPct = computed(() => {
  if (!stats.value) return 0
  const rate = stats.value.weekTaskRate ?? 0
  return Math.round(rate * 100)
})

const donutGradient = computed(() => {
  const pct = taskCompletionPct.value
  return `conic-gradient(#3B82F6 0% ${pct}%, #D1D5DB ${pct}% 100%)`
})

const focusRange = ref('week')
const focusCustomStart = ref('')
const focusCustomEnd = ref('')
const focusCategoryData = ref<{ category: string; duration: number }[]>([])
const focusCategoryLoading = ref(false)
const pieChartRef = ref<HTMLCanvasElement | null>(null)
let pieChartInstance: Chart | null = null

const PIE_COLORS = [
  '#3B82F6', '#8b5cf6', '#ec4899', '#f43f5e', '#f97316',
  '#eab308', '#22c55e', '#14b8a6', '#06b6d4', '#3b82f6',
  '#a855f7', '#d946ef', '#64748b', '#78716c', '#0ea5e9'
]

const PIE_HOVER_COLORS = [
  '#818cf8', '#a78bfa', '#f472b6', '#fb7185', '#fb923c',
  '#facc15', '#4ade80', '#2dd4bf', '#22d3ee', '#60a5fa',
  '#c084fc', '#e879f9', '#9CA3AF', '#a8a29e', '#38bdf8'
]

const fetchFocusCategory = async () => {
  focusCategoryLoading.value = true
  try {
    const params: { range: string; startDate?: string; endDate?: string } = { range: focusRange.value }
    if (focusRange.value === 'custom' && focusCustomStart.value && focusCustomEnd.value) {
      params.startDate = focusCustomStart.value
      params.endDate = focusCustomEnd.value
    }
    const res = await userApi.getFocusTimeByCategory(params)
    focusCategoryData.value = (res.data?.data || []).filter((d: any) => d.duration > 0)
  } catch {
    focusCategoryData.value = []
  } finally {
    focusCategoryLoading.value = false
  }
}

const renderPieChart = () => {
  if (pieChartInstance) {
    pieChartInstance.destroy()
    pieChartInstance = null
  }
  if (!pieChartRef.value || focusCategoryData.value.length === 0) return

  const labels = focusCategoryData.value.map(d => d.category || '未分类')
  const data = focusCategoryData.value.map(d => d.duration || 0)
  const totalSeconds = data.reduce((a, b) => a + b, 0)
  if (totalSeconds === 0) return
  const colors = focusCategoryData.value.map((_, i) => PIE_COLORS[i % PIE_COLORS.length])
  const hoverColors = focusCategoryData.value.map((_, i) => PIE_HOVER_COLORS[i % PIE_HOVER_COLORS.length])

  const shadowPlugin = {
    id: 'pieShadow',
    beforeDraw: (chart: any) => {
      const { ctx, chartArea } = chart
      if (!chartArea) return
      ctx.save()
      ctx.shadowColor = 'rgba(0, 0, 0, 0.15)'
      ctx.shadowBlur = 16
      ctx.shadowOffsetX = 4
      ctx.shadowOffsetY = 4
    },
    afterDraw: (chart: any) => {
      chart.ctx.restore()
    }
  }

  const centerTextPlugin = {
    id: 'centerText',
    afterDraw: (chart: any) => {
      const { ctx, chartArea } = chart
      if (!chartArea) return
      const centerX = (chartArea.left + chartArea.right) / 2
      const centerY = (chartArea.top + chartArea.bottom) / 2
      const h = Math.floor(totalSeconds / 3600)
      const m = Math.floor((totalSeconds % 3600) / 60)
      const s = totalSeconds % 60
      let timeStr = ''
      if (h > 0) timeStr = `${h}h${m > 0 ? m + 'm' : ''}`
      else if (m > 0) timeStr = `${m}m${s > 0 ? s + 's' : ''}`
      else timeStr = `${s}s`

      ctx.save()
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      ctx.font = 'bold 18px sans-serif'
      ctx.fillStyle = '#111827'
      ctx.fillText(timeStr, centerX, centerY - 8)
      ctx.font = '12px sans-serif'
      ctx.fillStyle = '#9CA3AF'
      ctx.fillText('总时长', centerX, centerY + 12)
      ctx.restore()
    }
  }

  pieChartInstance = new Chart(pieChartRef.value, {
    type: 'doughnut',
    data: {
      labels,
      datasets: [{
        data,
        backgroundColor: colors,
        hoverBackgroundColor: hoverColors,
        borderColor: '#fff',
        borderWidth: 3,
        hoverBorderColor: '#fff',
        hoverOffset: 12,
        borderRadius: 4,
        spacing: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '42%',
      layout: {
        padding: 10
      },
      animation: {
        animateRotate: true,
        animateScale: true,
        duration: 800,
        easing: 'easeOutQuart'
      },
      plugins: {
        legend: {
          position: 'right',
          labels: {
            padding: 16,
            usePointStyle: true,
            pointStyle: 'rectRounded',
            pointStyleWidth: 14,
            font: { size: 12, family: 'sans-serif' },
            color: '#475569',
            generateLabels: (chart: any) => {
              const dataset = chart.data.datasets[0]
              return chart.data.labels.map((label: string, i: number) => {
                const value = dataset.data[i]
                const pct = totalSeconds > 0 ? ((value / totalSeconds) * 100).toFixed(1) : '0'
                return {
                  text: `${label}  ${pct}%`,
                  fillStyle: dataset.backgroundColor[i],
                  strokeStyle: '#fff',
                  lineWidth: 0,
                  pointStyle: 'rectRounded',
                  index: i
                }
              })
            }
          }
        },
        tooltip: {
          backgroundColor: 'rgba(15, 23, 42, 0.9)',
          titleFont: { size: 13, weight: 'bold' as const },
          bodyFont: { size: 12 },
          padding: 12,
          cornerRadius: 8,
          displayColors: true,
          boxPadding: 4,
          callbacks: {
            label: (ctx: any) => {
              const secs = ctx.raw as number
              const h = Math.floor(secs / 3600)
              const m = Math.floor((secs % 3600) / 60)
              const s = secs % 60
              let timeStr = ''
              if (h > 0) timeStr = `${h}小时${m > 0 ? m + '分钟' : ''}`
              else if (m > 0) timeStr = `${m}分钟${s > 0 ? s + '秒' : ''}`
              else timeStr = `${s}秒`
              const pct = totalSeconds > 0 ? ((secs / totalSeconds) * 100).toFixed(1) : '0'
              return ` ${timeStr}（${pct}%）`
            }
          }
        }
      }
    },
    plugins: [shadowPlugin, centerTextPlugin]
  })
}

watch(focusRange, () => {
  if (focusRange.value !== 'custom') {
    fetchFocusCategory()
  }
})

let focusCategoryFetched = false

const initFocusCategory = async () => {
  if (focusCategoryFetched) return
  focusCategoryFetched = true
  await nextTick()
  fetchFocusCategory()
}

watch(activeTab, (tab) => {
  if (tab === 'stats') {
    initFocusCategory()
  }
})

if (activeTab.value === 'stats') {
  initFocusCategory()
}

watch(
  [() => focusCategoryData.value, pieChartRef],
  () => {
    if (focusCategoryData.value.length > 0 && pieChartRef.value) {
      nextTick(() => renderPieChart())
    }
  },
  { deep: true }
)
</script>

<template>
  <PageLayout title="个人中心" subtitle="管理你的账户与个人信息">
    <Transition name="fade">
      <div v-if="showSuccess" class="success-toast">
        <span class="toast-icon">✓</span>
        <span>保存成功</span>
      </div>
    </Transition>

    <div class="user-page">
      <div class="profile-header-bar">
        <div class="avatar-wrapper" @click="handleAvatarClick">
          <el-avatar v-if="user?.avatarUrl" :src="user?.avatarUrl" :size="48" />
          <el-avatar v-else :size="48">{{ user?.nickname?.charAt(0) || user?.username?.charAt(0) || 'U' }}</el-avatar>
          <div v-if="isUploadingAvatar" class="avatar-loading-overlay">
            <el-icon class="is-loading"><Loading /></el-icon>
          </div>
          <div v-else class="avatar-upload-icon"></div>
        </div>
        <input ref="avatarInputRef" type="file" accept="image/*" style="display: none;" @change="handleAvatarUpload" />
        <div class="profile-info">
          <h2 class="profile-name">
            {{ user?.nickname || user?.username }}
            <span v-if="isAdmin" class="admin-badge">管理员</span>
          </h2>
          <p class="profile-role">@{{ user?.username }}</p>
        </div>
        <div v-if="isEditing" class="profile-actions">
          <el-button @click="cancelEdit">取消</el-button>
          <el-button type="primary" :loading="isSaving" @click="handleUpdate">保存</el-button>
        </div>
        <el-button v-if="isEditing && user?.avatarUrl" type="danger" size="small" @click.stop="handleAvatarDelete">删除头像</el-button>
      </div>

      <el-tabs v-model="activeTab" class="user-tabs">
        <el-tab-pane label="数据统计" name="stats">
          <div class="tab-scroll">
            <div v-if="!statsLoading && stats" class="section-block">
              <div class="section-title-row">
                <span class="section-text">专注统计</span>
              </div>
              <div class="stats-grid">
                <div class="stat-item">
                  <div class="stat-value">{{ formatSeconds(stats.totalFocusSeconds) }}</div>
                  <div class="stat-label">总专注时长</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ formatSeconds(stats.monthlyFocusSeconds) }}</div>
                  <div class="stat-label">本月专注时长</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ formatSeconds(stats.todayFocusSeconds) }}</div>
                  <div class="stat-label">今日专注时长</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value highlight">{{ formatRate(stats.todayGoalRate) }}</div>
                  <div class="stat-label">今日目标完成率</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ formatSeconds(stats.maxDailyFocusSeconds) }}</div>
                  <div class="stat-label">历史单日最高</div>
                </div>
              </div>
            </div>

            <div v-if="!statsLoading && stats" class="section-block">
              <div class="section-title-row">
                <span class="section-text">专注时间分类分布</span>
              </div>
              <div class="pie-range-bar">
                <el-radio-group v-model="focusRange" size="small">
                  <el-radio-button value="day">日</el-radio-button>
                  <el-radio-button value="week">周</el-radio-button>
                  <el-radio-button value="month">月</el-radio-button>
                  <el-radio-button value="custom">自定义</el-radio-button>
                </el-radio-group>
                <div v-if="focusRange === 'custom'" class="custom-range-row">
                  <el-date-picker
                    v-model="focusCustomStart"
                    type="date"
                    placeholder="开始日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    size="small"
                    style="width: 140px;"
                  />
                  <span class="range-sep">~</span>
                  <el-date-picker
                    v-model="focusCustomEnd"
                    type="date"
                    placeholder="结束日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    size="small"
                    style="width: 140px;"
                  />
                  <el-button size="small" type="primary" @click="fetchFocusCategory" :loading="focusCategoryLoading">查询</el-button>
                </div>
              </div>
              <div class="pie-chart-area">
                <div v-if="focusCategoryLoading" class="pie-loading">
                  <el-icon class="is-loading" :size="24"><svg viewBox="0 0 1024 1024" width="1em" height="1em"><path d="M512 64a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V96a32 32 0 0 1 32-32z" fill="currentColor"/><path d="M512 736a32 32 0 0 1 32 32v192a32 32 0 0 1-64 0V768a32 32 0 0 1 32-32z" fill="currentColor" opacity=".5"/><path d="M96 512a32 32 0 0 1 32-32h192a32 32 0 0 1 0 64H128a32 32 0 0 1-32-32z" fill="currentColor" opacity=".7"/><path d="M736 512a32 32 0 0 1 32-32h192a32 32 0 0 1 0 64H768a32 32 0 0 1-32-32z" fill="currentColor" opacity=".3"/></svg></el-icon>
                </div>
                <div v-else-if="focusCategoryData.length === 0" class="pie-empty">
                  <span></span>
                  <p>该时间段内暂无专注记录</p>
                </div>
                <canvas v-else ref="pieChartRef" class="pie-canvas"></canvas>
              </div>
            </div>

            <div v-if="!statsLoading && stats" class="section-block">
              <div class="section-title-row">
                <span class="section-icon"></span>
                <span class="section-text">任务 & 打卡</span>
              </div>
              <div class="stats-grid stats-grid-4">
                <div class="stat-item">
                  <div class="stat-value">{{ stats.totalCompletedTasks }}</div>
                  <div class="stat-label">累计完成任务</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ stats.todayCompletedTasks }}</div>
                  <div class="stat-label">今日完成 ({{ formatRate(stats.todayTaskRate) }})</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ stats.weekCompletedTasks }}</div>
                  <div class="stat-label">本周完成 ({{ formatRate(stats.weekTaskRate) }})</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">{{ stats.monthCompletedTasks }}</div>
                  <div class="stat-label">本月完成 ({{ formatRate(stats.monthTaskRate) }})</div>
                </div>
              </div>
              <div class="checkin-row">
                <div class="checkin-item">
                  <span class="checkin-icon"></span>
                  <span class="checkin-num">{{ stats.currentStreak }}</span>
                  <span class="checkin-label">天连续打卡</span>
                </div>
                <div class="checkin-item">
                  <span class="checkin-icon"></span>
                  <span class="checkin-num">{{ stats.maxStreak }}</span>
                  <span class="checkin-label">天历史最长</span>
                </div>
                <div class="checkin-item">
                  <span class="checkin-icon"></span>
                  <span class="checkin-num">{{ stats.totalCheckInDays }}</span>
                  <span class="checkin-label">天累计打卡</span>
                </div>
              </div>
            </div>

            <!-- 本周专注趋势 & 任务完成率 -->
            <div v-if="!statsLoading && stats" class="section-block">
              <div class="charts-row">
                <!-- 本周专注折线图 -->
                <div class="chart-card">
                  <h4 class="chart-title">本周专注趋势</h4>
                  <div class="line-chart">
                    <div class="line-chart-area">
                      <svg class="line-svg" viewBox="0 0 280 100" preserveAspectRatio="none">
                        <defs>
                          <linearGradient id="lineFill" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stop-color="var(--primary)" stop-opacity="0.15"/>
                            <stop offset="100%" stop-color="var(--primary)" stop-opacity="0.02"/>
                          </linearGradient>
                        </defs>
                        <path :d="lineAreaPath" fill="url(#lineFill)"/>
                        <polyline :points="linePoints" fill="none" stroke="var(--primary)" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"/>
                        <circle
                          v-for="(pt, i) in lineDots"
                          :key="i"
                          :cx="pt.x"
                          :cy="pt.y"
                          r="3"
                          fill="#fff"
                          stroke="var(--primary)"
                          stroke-width="2"
                        />
                      </svg>
                    </div>
                    <div class="line-labels">
                      <span v-for="d in weekDays" :key="d">{{ d }}</span>
                    </div>
                  </div>
                </div>

                <!-- 任务完成率圆环图 -->
                <div class="chart-card">
                  <h4 class="chart-title">任务完成率</h4>
                  <div class="donut-chart-wrap">
                    <div class="donut-chart" :style="{ background: donutGradient }">
                      <div class="donut-center">
                        <span class="donut-pct">{{ taskCompletionPct }}%</span>
                        <span class="donut-label">完成率</span>
                      </div>
                    </div>
                    <div class="donut-legend">
                      <span class="legend-item"><span class="legend-dot" style="background:var(--primary)"></span>已完成</span>
                      <span class="legend-item"><span class="legend-dot" style="background:#D1D5DB"></span>未完成</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="!statsLoading && stats" class="section-block badge-wall-section">
              <div class="section-title-row">
                <div class="section-title-left">
                  <span class="section-text">成就系统</span>
                </div>
                <div class="badge-wall-header-right">
                  <span class="badge-count-label">{{ unlockedCount }}/{{ totalBadgesCount }}</span>
                  <el-button class="badge-help-btn" size="small" text @click="showBadgeHelp = true">获取条件</el-button>
                </div>
              </div>

              <div class="badge-group">
                <h4 class="badge-group-title">专注大师</h4>
                <div class="badge-row">
                  <div
                    v-for="badge in focusBadges"
                    :key="badge.id"
                    class="badge-card"
                    :class="{ 'is-unlocked': badge.unlocked }"
                  >
                    <div class="badge-card-icon">
                      <span class="badge-symbol">{{ badge.icon }}</span>
                    </div>
                    <div class="badge-card-info">
                      <div class="badge-card-name">{{ badge.name }}</div>
                      <div class="badge-card-desc">{{ badge.condition }}</div>
                      <div class="badge-progress-bar">
                        <div class="badge-progress-track">
                          <div class="badge-progress-fill" :style="{ width: badge.progressPercent + '%' }"></div>
                        </div>
                        <span class="badge-progress-text">{{ badge.progressText }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="badge-group">
                <h4 class="badge-group-title">任务达人</h4>
                <div class="badge-row">
                  <div
                    v-for="badge in taskBadges"
                    :key="badge.id"
                    class="badge-card"
                    :class="{ 'is-unlocked': badge.unlocked }"
                  >
                    <div class="badge-card-icon">
                      <span class="badge-symbol">{{ badge.icon }}</span>
                    </div>
                    <div class="badge-card-info">
                      <div class="badge-card-name">{{ badge.name }}</div>
                      <div class="badge-card-desc">{{ badge.condition }}</div>
                      <div class="badge-progress-bar">
                        <div class="badge-progress-track">
                          <div class="badge-progress-fill" :style="{ width: badge.progressPercent + '%' }"></div>
                        </div>
                        <span class="badge-progress-text">{{ badge.progressText }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="badge-group">
                <h4 class="badge-group-title">坚持之路</h4>
                <div class="badge-row">
                  <div
                    v-for="badge in streakBadges"
                    :key="badge.id"
                    class="badge-card"
                    :class="{ 'is-unlocked': badge.unlocked }"
                  >
                    <div class="badge-card-icon">
                      <span class="badge-symbol">{{ badge.icon }}</span>
                    </div>
                    <div class="badge-card-info">
                      <div class="badge-card-name">{{ badge.name }}</div>
                      <div class="badge-card-desc">{{ badge.condition }}</div>
                      <div class="badge-progress-bar">
                        <div class="badge-progress-track">
                          <div class="badge-progress-fill" :style="{ width: badge.progressPercent + '%' }"></div>
                        </div>
                        <span class="badge-progress-text">{{ badge.progressText }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="badge-group">
                <h4 class="badge-group-title">特殊成就</h4>
                <div class="badge-row">
                  <div
                    v-for="badge in specialBadges"
                    :key="badge.id"
                    class="badge-card"
                    :class="{ 'is-unlocked': badge.unlocked }"
                  >
                    <div class="badge-card-icon">
                      <span class="badge-symbol">{{ badge.icon }}</span>
                    </div>
                    <div class="badge-card-info">
                      <div class="badge-card-name">{{ badge.name }}</div>
                      <div class="badge-card-desc">{{ badge.condition }}</div>
                      <div class="badge-progress-bar">
                        <div class="badge-progress-track">
                          <div class="badge-progress-fill" :style="{ width: badge.progressPercent + '%' }"></div>
                        </div>
                        <span class="badge-progress-text">{{ badge.progressText }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="个人资料" name="profile">
          <div class="tab-scroll">
            <div class="section-block">
              <UserEditForm mode="profile" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="设置" name="settings">
          <div class="tab-scroll">
            <div class="section-block">
              <UserEditForm mode="settings" />
            </div>

            <div class="section-block">
              <div class="section-title-row">
                <span class="section-text">账户信息</span>
              </div>
              <el-descriptions :column="1" border size="small">
                <el-descriptions-item label="注册时间">
                  {{ formatDateTime(user?.createdTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="最后更新">
                  {{ formatDateTime(user?.updatedTime) }}
                </el-descriptions-item>
                <el-descriptions-item label="账户状态">
                  <el-tag :type="user?.isActive === 1 ? 'success' : 'danger'">
                    {{ user?.isActive === 1 ? '正常' : '禁用' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="section-block">
              <div class="section-title-row">
                <span class="section-icon"></span>
                <span class="section-text">安全设置</span>
              </div>
              <el-button type="primary" plain @click="openChangePwd">修改密码</el-button>
            </div>

            <div class="section-block">
              <div class="section-title-row">
                <span class="section-text">主题颜色</span>
              </div>
              <div class="theme-switcher">
                <button
                  v-for="tc in themeColors"
                  :key="tc.theme"
                  :class="['theme-dot', { active: currentTheme === tc.theme }]"
                  :style="{ background: tc.color }"
                  :title="tc.name"
                  @click="switchTheme(tc.theme)"
                />
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="成就" name="achievements">
          <div class="tab-scroll">
            <AchievementWall
              v-if="stats"
              :total-focus-seconds="stats.totalFocusSeconds"
              :total-completed-tasks="stats.totalCompletedTasks"
              :current-streak="stats.currentStreak"
              :max-streak="stats.maxStreak"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog v-model="showBadgeHelp" title="成就获取条件" width="480px" align-center class="badge-help-dialog" append-to-body>
      <div class="badge-help-content">
        <div class="help-category">
          <h4>专注大师</h4>
          <div class="help-item"><strong>初学者</strong>：累计专注 ≥ 60 分钟</div>
          <div class="help-item"><strong>进阶者</strong>：累计专注 ≥ 600 分钟</div>
          <div class="help-item"><strong>专家</strong>：累计专注 ≥ 3000 分钟</div>
          <div class="help-item"><strong>大师</strong>：累计专注 ≥ 12000 分钟</div>
          <div class="help-item"><strong>传奇</strong>：累计专注 ≥ 60000 分钟</div>
        </div>
        <div class="help-category">
          <h4>任务达人</h4>
          <div class="help-item"><strong>起步</strong>：累计完成 ≥ 10 个任务</div>
          <div class="help-item"><strong>高效</strong>：累计完成 ≥ 50 个任务</div>
          <div class="help-item"><strong>达人</strong>：累计完成 ≥ 200 个任务</div>
          <div class="help-item"><strong>卓越</strong>：累计完成 ≥ 500 个任务</div>
          <div class="help-item"><strong>巅峰</strong>：累计完成 ≥ 1000 个任务</div>
        </div>
        <div class="help-category">
          <h4>坚持之路</h4>
          <div class="help-item"><strong>启程</strong>：连续专注 ≥ 3 天</div>
          <div class="help-item"><strong>坚持</strong>：连续专注 ≥ 7 天</div>
          <div class="help-item"><strong>习惯</strong>：连续专注 ≥ 21 天</div>
          <div class="help-item"><strong>自律</strong>：连续专注 ≥ 60 天</div>
          <div class="help-item"><strong>极致</strong>：连续专注 ≥ 365 天</div>
        </div>
        <div class="help-category">
          <h4>特殊成就</h4>
          <div class="help-item"><strong>夜猫子</strong>：在凌晨 0:00 - 5:59 完成任务</div>
          <div class="help-item"><strong>早起鸟</strong>：在早上 6:00 前开始专注</div>
          <div class="help-item"><strong>马拉松</strong>：单次专注超过 120 分钟</div>
          <div class="help-item"><strong>全勤王</strong>：30 天内每天至少完成 1 个任务</div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" size="small" @click="showBadgeHelp = false">知道了</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showChangePwd" title="修改密码" width="400px" append-to-body>
      <el-form label-position="top">
        <el-form-item label="旧密码">
          <el-input v-model="changePwdForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="changePwdForm.newPassword" type="password" placeholder="6-50个字符" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="changePwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showChangePwd = false">取消</el-button>
        <el-button type="primary" :loading="changePwdLoading" @click="handleChangePwd">确认修改</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<style scoped>
.success-toast {
  position: fixed;
  top: 24px;
  right: 24px;
  background: #3F6212;
  color: white;
  padding: 12px 20px;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(16, 185, 129, 0.3);
  font-weight: 500;
  z-index: 2000;
  display: flex;
  align-items: center;
  gap: 8px;
}

.toast-icon {
  width: 20px;
  height: 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
}

.user-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header-bar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
  padding: 12px 18px;
  background: #FFFFFF;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  margin-bottom: 12px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  border-radius: 50%;
  transition: box-shadow 0.2s;
  flex-shrink: 0;
}

.avatar-wrapper:hover {
  box-shadow: 0 0 0 4px #EEF2FF;
}

.avatar-upload-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  background: rgba(var(--primary-rgb), 0.85);
  color: #fff;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  border: 2px solid #fff;
}

.avatar-loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary);
  font-size: 18px;
}

.profile-info {
  flex: 1;
  min-width: 80px;
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 2px 0;
}

.profile-role {
  font-size: 12px;
  color: #9CA3AF;
  margin: 0;
}

.profile-actions {
  display: flex;
  gap: 8px;
}

.admin-badge {
  display: inline-block;
  font-size: 11px;
  font-weight: 500;
  color: #f59e0b;
  background: #fef3c7;
  padding: 2px 8px;
  border-radius: 6px;
  margin-left: 8px;
  vertical-align: middle;
}

.user-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: var(--radius-lg);
  overflow: hidden;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  box-shadow: var(--shadow-sm);
}

.user-tabs :deep(.el-tabs__header) {
  flex-shrink: 0;
  margin-bottom: 0;
  padding: 0 16px;
  background: #FFFFFF;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
  border-bottom: 1px solid #D1D5DB;
}

.user-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.user-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  padding: 0 20px;
  height: 46px;
  line-height: 46px;
  color: #6B7280;
}

.user-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary);
  font-weight: 600;
}

.user-tabs :deep(.el-tabs__active-bar) {
  background: var(--primary);
  height: 2px;
  border-radius: 1px;
}

.user-tabs :deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.user-tabs :deep(.el-tab-pane) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tab-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.tab-scroll::-webkit-scrollbar {
  width: 5px;
}

.tab-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.tab-scroll::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.section-block {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
}

.section-block:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-title-left {
  display: flex;
  align-items: center;
  gap: 6px;
}

.section-icon {
  font-size: 18px;
}

.section-text {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.badge-help-btn {
  font-size: 13px;
  color: var(--primary);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.stats-grid-4 {
  grid-template-columns: repeat(4, 1fr);
}

.stat-item {
  text-align: center;
  padding: 12px 6px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s;
}

.stat-item:hover {
  transform: translateY(-2px);
}

.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 4px;
}

.stat-value.highlight {
  color: var(--primary);
}

.stat-label {
  font-size: 11px;
  color: #9CA3AF;
  line-height: 1.4;
}

.checkin-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
  padding: 12px 0 4px;
}

.checkin-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.checkin-icon {
  font-size: 22px;
}

.checkin-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
}

.checkin-label {
  font-size: 12px;
  color: #9CA3AF;
}

/* 成就系统样式 */
.badge-wall-header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.badge-count-label {
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
  padding: 3px 10px;
  background: #f3f4f6;
  border-radius: 10px;
}

.badge-wall-section {
  border-bottom: none !important;
  padding-bottom: 0 !important;
}

.badge-group {
  margin-bottom: 20px;
}

.badge-group:last-child {
  margin-bottom: 0;
}

.badge-group-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 10px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge-row {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 8px;
}

.badge-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: var(--radius-md);
  background: #fff;
  border: 1px solid #D1D5DB;
  box-shadow: var(--shadow-sm);
  transition: border-color 0.2s, box-shadow 0.2s;
}

.badge-card.is-unlocked {
  border-color: #C7D2FE;
}

.badge-card:hover {
  box-shadow: var(--shadow-md);
}

.badge-card-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: #D1D5DB;
  transition: background-color 0.2s;
}

.badge-card.is-unlocked .badge-card-icon {
  background: var(--primary);
}

.badge-symbol {
  font-size: 16px;
  color: #fff;
  line-height: 1;
  user-select: none;
}

.badge-card-info {
  flex: 1;
  min-width: 0;
}

.badge-card-name {
  font-size: 13px;
  font-weight: 600;
  color: #9CA3AF;
  margin-bottom: 2px;
  line-height: 1.3;
}

.badge-card.is-unlocked .badge-card-name {
  color: #111827;
}

.badge-card-desc {
  font-size: 11px;
  color: #9ca3af;
  margin-bottom: 6px;
  line-height: 1.3;
}

.badge-progress-bar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge-progress-track {
  flex: 1;
  height: 4px;
  background: #e5e7eb;
  border-radius: 2px;
  overflow: hidden;
}

.badge-progress-fill {
  height: 100%;
  background: #D1D5DB;
  border-radius: 2px;
  transition: width 0.4s ease;
}

.badge-card.is-unlocked .badge-progress-fill {
  background: var(--primary);
}

.badge-progress-text {
  font-size: 10px;
  color: #9ca3af;
  white-space: nowrap;
  flex-shrink: 0;
}

.badge-card.is-unlocked .badge-progress-text {
  color: var(--primary);
  font-weight: 500;
}

.badge-help-content {
  line-height: 1.8;
}

.help-category {
  margin-bottom: 16px;
}

.help-category:last-child {
  margin-bottom: 0;
}

.help-category h4 {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 6px 0;
  padding-bottom: 4px;
  border-bottom: 1px solid #D1D5DB;
}

.help-item {
  font-size: 12px;
  color: #6B7280;
  padding: 2px 0;
  padding-left: 8px;
}

.pie-range-bar {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.custom-range-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.range-sep {
  color: #9CA3AF;
  font-size: 14px;
}

.pie-chart-area {
  position: relative;
  width: 100%;
  max-width: 520px;
  height: 300px;
  margin: 0 auto;
}

.pie-canvas {
  width: 100% !important;
  height: 100% !important;
}

.pie-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--primary);
  font-size: 24px;
}

.pie-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #9CA3AF;
}

.pie-empty span {
  font-size: 36px;
  margin-bottom: 8px;
}

.pie-empty p {
  font-size: 14px;
  margin: 0;
}

@media (max-width: 600px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stats-grid-4 {
    grid-template-columns: repeat(2, 1fr);
  }
  .badge-row {
    grid-template-columns: 1fr;
  }
  .checkin-row {
    flex-direction: column;
    gap: 10px;
  }
  .profile-header-bar {
    padding: 10px 12px;
    gap: 8px;
  }
  .tab-scroll {
    padding: 14px;
  }
  .custom-range-row {
    flex-direction: column;
    align-items: stretch !important;
  }
  .custom-range-row .el-date-picker {
    width: 100% !important;
  }
}

/* ========== 图表区域 ========== */
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.chart-card {
  background: #fff;
  border: 1px solid #D1D5DB;
  border-radius: 10px;
  padding: 20px;
  box-shadow: var(--shadow-sm);
}

.chart-title {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

/* 折线图 */
.line-chart {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.line-chart-area {
  width: 100%;
  height: 100px;
}

.line-svg {
  width: 100%;
  height: 100%;
}

.line-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #9CA3AF;
}

/* 圆环图 */
.donut-chart-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.donut-chart {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  position: relative;
}

.donut-center {
  position: absolute;
  inset: 20px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.donut-pct {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  line-height: 1;
}

.donut-label {
  font-size: 10px;
  color: #9CA3AF;
  margin-top: 2px;
}

.donut-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #6B7280;
}

.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

@media (max-width: 767.98px) {
  .charts-row {
    grid-template-columns: 1fr;
  }
}

/* ========== 主题切换器 ========== */
.theme-switcher {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.theme-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: 3px solid transparent;
  cursor: pointer;
  transition: border-color 0.2s, transform 0.15s;
  padding: 0;
  outline: none;
}

.theme-dot:hover {
  transform: scale(1.1);
}

.theme-dot.active {
  border-color: var(--primary);
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px var(--primary);
}
</style>

<style>
.badge-help-dialog.el-dialog {
  border-radius: 16px;
  overflow: hidden;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.badge-help-dialog .el-dialog__header {
  padding: 14px 18px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  flex-shrink: 0;
}

.badge-help-dialog .el-dialog__title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.badge-help-dialog .el-dialog__body {
  padding: 12px 18px;
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.badge-help-dialog .el-dialog__body::-webkit-scrollbar {
  width: 4px;
}

.badge-help-dialog .el-dialog__body::-webkit-scrollbar-track {
  background: transparent;
}

.badge-help-dialog .el-dialog__body::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.2);
  border-radius: 2px;
}

.badge-help-dialog .el-dialog__footer {
  padding: 10px 18px 14px;
  border-top: 1px solid rgba(226, 232, 240, 0.6);
  flex-shrink: 0;
}

@media (max-width: 480px) {
  .badge-help-dialog.el-dialog {
    width: 92% !important;
    max-height: 70vh;
  }
  .badge-help-dialog .el-dialog__body {
    padding: 10px 14px;
  }
}
</style>
