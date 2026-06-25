<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { authApi, feedbackApi, adminApi, messageApi } from '../api'

const store = useStore()
const router = useRouter()

const currentPath = computed(() => router.currentRoute.value.path)
const isCollapsed = computed(() => store.getters['sidebar/isCollapsed'])
const mobileOpen = ref(false)
const isMobile = ref(false)

const checkMobile = () => {
  isMobile.value = window.innerWidth < 576
  if (isMobile.value) {
    store.dispatch('sidebar/setCollapsed', true)
  }
}

const unreadFeedbackCount = ref(0)
const unreadMessageCount = ref(0)
const pendingAdminCount = ref(0)
const pendingAppealCount = ref(0)
let pollTimer: ReturnType<typeof setInterval> | null = null

const fetchBadgeCounts = async () => {
  try {
    if (store.getters['auth/isLoggedIn']) {
      const feedbackRes = await feedbackApi.getUnreadCount()
      unreadFeedbackCount.value = feedbackRes.data?.data?.count || 0

      const messageRes = await messageApi.getUnreadCount()
      unreadMessageCount.value = messageRes.data?.data?.count || 0
    }
    if (store.getters['auth/isAdmin']) {
      const res = await adminApi.getPendingFeedbackCount()
      pendingAdminCount.value = res.data?.data?.count || 0

      const appealRes = await adminApi.getPendingAppealCount()
      pendingAppealCount.value = appealRes.data?.data?.count || 0
    }
  } catch {}
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  if (store.getters['auth/isLoggedIn']) {
    fetchBadgeCounts()
    pollTimer = setInterval(fetchBadgeCounts, 60000)
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
  if (pollTimer) clearInterval(pollTimer)
})

const toggleSidebar = () => {
  if (isMobile.value) {
    mobileOpen.value = !mobileOpen.value
  } else {
    store.dispatch('sidebar/toggleCollapsed')
  }
}

const handleNavClick = (path: string) => {
  router.push(path)
  if (isMobile.value) {
    mobileOpen.value = false
  }
}

const showDonate = ref(false)
const donateQrUrl = '/images/wechat-pay.png'

const handleLogout = async () => {
  store.commit('auth/CLEAR_AUTH')
  try { await authApi.logout() } catch {}
  router.push('/login')
}

const isActive = (path: string) => currentPath.value === path

const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])
const isAdmin = computed(() => store.getters['auth/isAdmin'])

// SVG 线性图标（16x16 viewBox，currentColor，stroke 风格）
const iconMap: Record<string, string> = {
  home: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12L12 3l9 9"/><path d="M9 21V9h6v12"/></svg>',
  clipboard: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="8" y="2" width="8" height="4" rx="1"/><path d="M16 4h2a2 2 0 012 2v14a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h2"/></svg>',
  bell: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></svg>',
  clock: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>',
  user: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>',
  inbox: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 16 12 14 15 10 15 8 12 2 12"/><path d="M5.45 5.11L2 12v6a2 2 0 002 2h16a2 2 0 002-2v-6l-3.45-6.89A2 2 0 0016.76 4H7.24a2 2 0 00-1.79 1.11z"/></svg>',
  book: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 014 4v14a3 3 0 00-3-3H2z"/><path d="M22 3h-6a4 4 0 00-4 4v14a3 3 0 013-3h7z"/></svg>',
  message: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z"/></svg>',
  list: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>',
  users: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></svg>',
  scale: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="3" x2="12" y2="21"/><polyline points="4 8 12 3 20 8"/><line x1="4" y1="8" x2="4" y2="13"/><line x1="20" y1="8" x2="20" y2="13"/><circle cx="4" cy="15" r="2"/><circle cx="20" cy="15" r="2"/></svg>',
  logo: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="4" y="2" width="16" height="4" rx="2"/><path d="M16 4v16a2 2 0 01-2 2H6a2 2 0 01-2-2V4"/><line x1="8" y1="10" x2="16" y2="10"/><line x1="8" y1="14" x2="12" y2="14"/></svg>'
}

const getIconHtml = (key: string) => iconMap[key] || ''

const navItems = [
  { path: '/', iconKey: 'home', label: '智能助手' },
  { path: '/tasks', iconKey: 'list', label: '任务清单' },
  { path: '/reminders', iconKey: 'bell', label: '提醒中心' },
  { path: '/focus', iconKey: 'clock', label: '专注记录' },
  { path: '/user', iconKey: 'user', label: '用户信息' },
  { path: '/messages', iconKey: 'inbox', label: '消息中心', badge: () => unreadMessageCount.value },
  { path: '/help', iconKey: 'book', label: '使用帮助' },
  { path: '/feedback', iconKey: 'message', label: '意见反馈' }
]

const adminItems = [
  { path: '/admin/feedbacks', iconKey: 'clipboard', label: '反馈管理' },
  { path: '/admin/online-users', iconKey: 'users', label: '用户状态' },
  { path: '/admin/appeals', iconKey: 'scale', label: '申诉管理' }
]

const publicNavItems = [
  { path: '/help', iconKey: 'book', label: '使用帮助' }
]

</script>

<template>
  <div
    v-if="isMobile && !mobileOpen"
    class="mobile-hamburger"
    @click="toggleSidebar"
  >
    <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
      <path d="M3 6H21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M3 12H21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
      <path d="M3 18H21" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
    </svg>
  </div>

  <div
    v-if="isMobile && mobileOpen"
    class="mobile-overlay"
    @click="mobileOpen = false"
  />

  <div
    class="sidebar-wrapper"
    :class="{
      collapsed: isCollapsed && !isMobile,
      'mobile-open': isMobile && mobileOpen,
      'mobile-hidden': isMobile && !mobileOpen
    }"
  >
    <div class="sidebar-header">
      <span class="logo-icon" v-html="getIconHtml('logo')"></span>
      <span class="logo-text">SelfSchedule</span>
      <span
        class="collapse-btn"
        @click="toggleSidebar"
        :title="isCollapsed && !isMobile ? '展开菜单' : '收起菜单'"
      >
        <template v-if="!isMobile">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path d="M5.5 3L10.5 8L5.5 13" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
          </svg>
        </template>
        <template v-else>
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
            <path d="M15 5L5 15M5 5L15 15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
        </template>
      </span>
    </div>

    <nav class="sidebar-nav">
      <template v-if="isLoggedIn">
        <div
          v-for="item in navItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="handleNavClick(item.path)"
          :title="isCollapsed && !isMobile ? item.label : ''"
        >
          <span class="nav-icon" v-html="getIconHtml(item.iconKey)"></span>
          <span class="nav-label">{{ item.label }}</span>
          <span
            v-if="item.path === '/feedback' && unreadFeedbackCount > 0"
            class="nav-badge"
          >{{ unreadFeedbackCount > 99 ? '99+' : unreadFeedbackCount }}</span>
          <span
            v-else-if="item.path === '/messages' && unreadMessageCount > 0"
            class="nav-badge"
          >{{ unreadMessageCount > 99 ? '99+' : unreadMessageCount }}</span>
        </div>
      </template>

      <template v-if="isAdmin && isLoggedIn">
        <div class="nav-divider" v-if="!isCollapsed || isMobile">
          <span class="divider-text">管理后台</span>
        </div>
        <div
          v-for="item in adminItems"
          :key="item.path"
          class="nav-item admin-item"
          :class="{ active: isActive(item.path) }"
          @click="handleNavClick(item.path)"
          :title="isCollapsed && !isMobile ? item.label : ''"
        >
          <span class="nav-icon" v-html="getIconHtml(item.iconKey)"></span>
          <span class="nav-label">{{ item.label }}</span>
          <span
            v-if="item.path === '/admin/feedbacks' && pendingAdminCount > 0"
            class="nav-badge"
          >{{ pendingAdminCount > 99 ? '99+' : pendingAdminCount }}</span>
          <span
            v-if="item.path === '/admin/appeals' && pendingAppealCount > 0"
            class="nav-badge appeal-badge"
          >{{ pendingAppealCount > 99 ? '99+' : pendingAppealCount }}</span>
        </div>
      </template>

      <template v-if="!isLoggedIn">
        <div
          v-for="item in publicNavItems"
          :key="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="handleNavClick(item.path)"
        >
          <span class="nav-icon" v-html="getIconHtml(item.iconKey)"></span>
          <span class="nav-label">{{ item.label }}</span>
        </div>
      </template>
    </nav>

    <div class="sidebar-footer">
      <template v-if="isLoggedIn">
        <div class="footer-top">
          <div class="footer-avatar">
            <el-avatar
              v-if="store.getters['auth/avatarUrl']"
              :src="store.getters['auth/avatarUrl']"
              class="avatar-img"
            />
            <el-avatar v-else class="avatar-img">
              {{ store.getters['auth/username']?.charAt(0)?.toUpperCase() }}
            </el-avatar>
          </div>
          <div class="footer-info">
            <div class="user-name">
              {{ store.getters['auth/username'] }}
              <span v-if="store.getters['auth/isAdmin']" class="admin-badge">管理员</span>
            </div>
            <div class="user-motto">让每一天更高效</div>
          </div>
        </div>
        <div class="footer-actions">
          <div class="donate-btn" @click="showDonate = true" title="支持开发者">
            <span class="donate-icon"></span>
            <span class="donate-text">支持开发者</span>
          </div>
          <div class="logout-btn" @click="handleLogout" title="退出登录">
            <span class="logout-icon"></span>
            <span class="logout-text">退出登录</span>
          </div>
        </div>
      </template>
      <template v-else>
        <div class="logout-btn" @click="router.push('/login')" title="去登录">
          <span class="logout-icon"></span>
          <span class="logout-text">去登录</span>
        </div>
      </template>
    </div>

    <Teleport to="body">
      <div v-if="showDonate" class="donate-overlay" @click.self="showDonate = false">
        <div class="donate-dialog">
          <div class="donate-header">
            <h3>支持开发者</h3>
            <span class="donate-close" @click="showDonate = false">✕</span>
          </div>
          <div class="donate-body">
            <p>如果 SelfSchedule 对你有帮助，欢迎请作者喝杯咖啡~</p>
            <div class="donate-qr">
              <img :src="donateQrUrl" alt="微信收款码" />
            </div>
            <p class="donate-thanks">感谢你的支持！祝你高效每一天</p>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.sidebar-wrapper {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 220px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  border-right: 1px solid #D1D5DB;
  transition: transform 0.25s ease, width 0.25s ease;
}

.sidebar-wrapper.collapsed {
  width: 64px;
}

.sidebar-wrapper.collapsed .logo-text,
.sidebar-wrapper.collapsed .nav-label,
.sidebar-wrapper.collapsed .footer-info,
.sidebar-wrapper.collapsed .logout-text {
  display: none;
}

.sidebar-wrapper.collapsed .sidebar-header {
  justify-content: center;
  padding: 0;
}

.sidebar-wrapper.collapsed .collapse-btn svg {
  transform: rotate(180deg);
}

.sidebar-wrapper.collapsed .nav-item {
  justify-content: center;
  padding: 0;
  gap: 0;
}

.sidebar-wrapper.collapsed .sidebar-footer {
  padding: 14px 0;
  gap: 10px;
}

.sidebar-wrapper.collapsed .footer-top {
  justify-content: center;
}

.sidebar-wrapper.collapsed .logout-btn {
  justify-content: center;
  padding: 4px;
}

/* ========== 移动端 ========== */

.mobile-hamburger {
  position: fixed;
  top: 12px;
  left: 12px;
  z-index: 1100;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
  border-radius: 6px;
  border: 1px solid #D1D5DB;
  cursor: pointer;
  color: var(--primary);
  transition: all 0.2s;
}

.mobile-hamburger:hover {
  background: #F9FAFB;
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  z-index: 999;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.sidebar-wrapper.mobile-hidden {
  transform: translateX(-100%);
}

.sidebar-wrapper.mobile-open {
  transform: translateX(0);
  width: 240px;
}

/* ========== 头部 ========== */

.sidebar-header {
  display: flex;
  align-items: center;
  height: 56px;
  padding: 0 16px;
  gap: 8px;
  transition: all 0.25s ease;
  flex-shrink: 0;
}

.logo-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  line-height: 1;
  color: var(--primary);
}

.logo-icon :deep(svg) {
  width: 20px;
  height: 20px;
}

.logo-text {
  font-size: 15px;
  font-weight: 600;
  color: var(--primary);
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.collapse-btn {
  cursor: pointer;
  color: #9CA3AF;
  padding: 4px;
  border-radius: 4px;
  flex-shrink: 0;
  line-height: 1;
  user-select: none;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s, background 0.2s;
}

.collapse-btn svg {
  transition: transform 0.25s ease;
}

.collapse-btn:hover {
  color: var(--primary);
  background: #E5E7EB;
}

/* ========== 导航菜单 ========== */

.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 10px;
}

.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #D1D5DB;
  border-radius: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  position: relative;
  height: 40px;
  padding: 0 12px;
  margin-bottom: 2px;
  border-radius: 6px;
  cursor: pointer;
  color: #6B7280;
  font-size: 14px;
  gap: 10px;
  transition: all 0.2s ease;
  user-select: none;
}

.nav-item:hover {
  background-color: #F9FAFB;
  color: #111827;
}

.nav-item.active {
  background: #EFF6FF;
  color: var(--primary);
  font-weight: 500;
  border-left: 3px solid var(--primary);
  padding-left: 9px;
}

.nav-divider {
  padding: 16px 16px 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-divider::before,
.nav-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #D1D5DB;
}

.divider-text {
  font-size: 10px;
  color: #9CA3AF;
  text-transform: uppercase;
  letter-spacing: 1px;
  white-space: nowrap;
}

.nav-icon {
  width: 18px;
  height: 18px;
  min-width: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  color: inherit;
}

.nav-icon :deep(svg) {
  width: 18px;
  height: 18px;
}

.nav-label {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #B91C1C;
  color: #fff;
  font-size: 11px;
  font-weight: 500;
  line-height: 1;
  flex-shrink: 0;
  margin-left: auto;
}

.sidebar-wrapper.collapsed .nav-badge {
  position: absolute;
  top: 6px;
  right: 6px;
  margin-left: 0;
  min-width: 14px;
  height: 14px;
  padding: 0;
  font-size: 0;
}

.sidebar-wrapper.collapsed .nav-badge::after {
  content: '';
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #B91C1C;
}

/* ========== 底部用户区 ========== */

.sidebar-footer {
  border-top: 1px solid #E5E7EB;
  flex-shrink: 0;
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  min-height: 56px;
  transition: all 0.25s ease;
}

.footer-top {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.footer-avatar {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.avatar-img {
  border: 1px solid #D1D5DB;
}

.footer-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.admin-badge {
  display: inline-block;
  font-size: 10px;
  font-weight: 500;
  color: #D97706;
  background: #FEF3C7;
  padding: 1px 5px;
  border-radius: 4px;
  margin-left: 4px;
  vertical-align: middle;
}

.user-motto {
  font-size: 11px;
  color: #9CA3AF;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 5px 0;
  width: 100%;
  border-radius: 6px;
  cursor: pointer;
  color: #9CA3AF;
  font-size: 12px;
  transition: all 0.2s ease;
  user-select: none;
}

.logout-icon {
  font-size: 14px;
  line-height: 1;
}

.logout-text {
  white-space: nowrap;
}

.logout-btn:hover {
  color: #B91C1C;
  background: #FEF2F2;
}

.footer-actions {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.donate-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 5px 0;
  width: 100%;
  border-radius: 6px;
  cursor: pointer;
  color: #9CA3AF;
  font-size: 12px;
  transition: all 0.2s ease;
  user-select: none;
}

.donate-btn:hover {
  color: #D97706;
  background: #FFFBEB;
}

.donate-icon {
  font-size: 14px;
  line-height: 1;
}

.donate-text {
  white-space: nowrap;
}

.sidebar-wrapper.collapsed .donate-text {
  display: none;
}

.sidebar-wrapper.collapsed .donate-btn {
  padding: 4px;
}

/* ========== 打赏弹窗 ========== */

.donate-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.2s ease;
}

.donate-dialog {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #D1D5DB;
  width: 360px;
  max-width: 90vw;
  overflow: hidden;
  animation: scaleIn 0.2s ease;
}

@keyframes scaleIn {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.donate-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px 16px;
}

.donate-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.donate-close {
  cursor: pointer;
  font-size: 16px;
  color: #9CA3AF;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.15s;
  line-height: 1;
}

.donate-close:hover {
  background: #E5E7EB;
  color: #374151;
}

.donate-body {
  padding: 0 24px 24px;
  text-align: center;
}

.donate-body p {
  margin: 0 0 16px;
  font-size: 14px;
  color: #6B7280;
  line-height: 1.6;
}

.donate-qr {
  width: 200px;
  height: 200px;
  margin: 0 auto 16px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #D1D5DB;
  background: #F9FAFB;
  display: flex;
  align-items: center;
  justify-content: center;
}

.donate-qr img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.donate-thanks {
  font-size: 13px !important;
  color: #6B7280 !important;
}

/* ========== 平板小屏：自动按折叠态显示 ========== */

@media (max-width: 767px) {
  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) {
    width: 64px;
  }

  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .logo-text,
  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .nav-label,
  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .footer-info,
  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .logout-text {
    display: none;
  }

  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .sidebar-header {
    justify-content: center;
    padding: 0;
  }

  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .collapse-btn svg {
    transform: rotate(180deg);
  }

  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .nav-item {
    justify-content: center;
    padding: 0;
    gap: 0;
  }

  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .sidebar-footer {
    padding: 14px 0;
    gap: 10px;
  }

  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .footer-top {
    justify-content: center;
  }

  .sidebar-wrapper:not(.mobile-hidden):not(.mobile-open) .logout-btn {
    justify-content: center;
    padding: 4px;
  }
}
</style>
