<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import PageLayout from '../../components/PageLayout.vue'
import AiChatPanel from './components/AiChatPanel.vue'
import RecentTasksPanel from './components/RecentTasksPanel.vue'
import FeaturesModal from './components/FeaturesModal.vue'

const store = useStore()
const router = useRouter()
const showFeaturesModal = ref(false)
const showTasks = ref(false)
const isVisible = ref(false)

const stats = computed(() => ({
  todayFocus: store.getters['focus/todayFocusMinutes'] || 0,
  completedTasks: store.getters['task/completedTasks']?.length || 0,
  totalTasks: store.getters['task/tasks']?.length || 0,
  focusStreak: store.getters['focus/focusStreak'] || 0
}))

// 今日任务进度环形图数据
const taskProgress = computed(() => {
  const total = stats.value.totalTasks
  const completed = stats.value.completedTasks
  if (total === 0) return { percent: 0, style: {} }
  const percent = Math.round((completed / total) * 100)
  return {
    percent,
    style: {
      background: `conic-gradient(var(--primary) ${percent * 3.6}deg, #D1D5DB ${percent * 3.6}deg)`
    }
  }
})

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<template>
  <PageLayout>
    <div class="home-page">
      <!-- 页面头部 -->
      <div :class="['page-header', { 'visible': isVisible }]">
        <div class="header-left">
          <h1 class="header-title">智能任务助手</h1>
          <p class="header-subtitle">AI帮您管理日常任务</p>
        </div>
        <div class="header-right">
          <button class="header-btn" @click="showFeaturesModal = true">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/>
              <line x1="12" y1="17" x2="12.01" y2="17"/>
            </svg>
            帮助
          </button>
          <button class="header-btn" @click="router.push('/tasks')">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
            管理任务
          </button>
          <button class="header-btn" @click="showTasks = !showTasks">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/>
              <line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/>
            </svg>
            {{ showTasks ? '收起' : '快览' }}
          </button>
        </div>
      </div>

      <!-- 智能助手 - 主体 -->
      <div :class="['main-content', { 'visible': isVisible }]">
        <div class="ai-wrapper">
          <AiChatPanel
            :show-tasks="showTasks"
            @show-help="showFeaturesModal = true"
            @toggle-tasks="showTasks = !showTasks"
          />

          <Transition name="slide">
            <div v-if="showTasks" class="tasks-panel">
              <RecentTasksPanel ref="tasksPanelRef" @close="showTasks = false" />
            </div>
          </Transition>
        </div>
      </div>

      <!-- AI聊天区域与统计区域分隔线 -->
      <div :class="['section-divider', { 'visible': isVisible }]"></div>

      <!-- 底部统计 -->
      <div :class="['bottom-stats', { 'visible': isVisible }]">
        <!-- 今日任务进度环形图 -->
        <div class="stat-item ring-chart-item" @click="router.push('/tasks')">
          <div class="ring-chart">
            <div class="ring-bg" :style="taskProgress.style">
              <div class="ring-center">
                <span class="ring-value">{{ taskProgress.percent }}</span>
                <span class="ring-unit">%</span>
              </div>
            </div>
          </div>
          <div class="stat-info">
            <span class="stat-label">任务进度</span>
          </div>
        </div>
        <div class="stat-item" @click="router.push('/focus')">
          <div class="stat-info">
            <span class="stat-value">{{ stats.todayFocus }}<span class="stat-unit">分钟</span></span>
            <span class="stat-label">今日专注</span>
          </div>
        </div>
        <div class="stat-item" @click="router.push('/tasks')">
          <div class="stat-info">
            <span class="stat-value">{{ stats.completedTasks }}<span class="stat-unit">个</span></span>
            <span class="stat-label">已完成</span>
          </div>
        </div>
        <div class="stat-item" @click="router.push('/tasks')">
          <div class="stat-info">
            <span class="stat-value">{{ stats.totalTasks }}<span class="stat-unit">个</span></span>
            <span class="stat-label">总任务</span>
          </div>
        </div>
        <div class="stat-item" @click="router.push('/focus')">
          <div class="stat-info">
            <span class="stat-value">{{ stats.focusStreak }}<span class="stat-unit">天</span></span>
            <span class="stat-label">连续专注</span>
          </div>
        </div>
      </div>
    </div>

    <FeaturesModal :show="showFeaturesModal" @close="showFeaturesModal = false" />
  </PageLayout>
</template>

<style scoped>
.home-page {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 入场动画 */
.page-header,
.main-content,
.bottom-stats,
.section-divider {
  opacity: 0;
  transform: translateY(8px);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.page-header.visible,
.main-content.visible,
.bottom-stats.visible,
.section-divider.visible {
  opacity: 1;
  transform: translateY(0);
}

.main-content { transition-delay: 0.05s; }
.section-divider { transition-delay: 0.08s; }
.bottom-stats { transition-delay: 0.1s; }

/* 页面头部 — 白色卡片 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
}

.header-left {
  display: flex;
  flex-direction: column;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  line-height: 1.3;
}

.header-subtitle {
  font-size: 13px;
  color: #6B7280;
  margin: 2px 0 0;
}

.header-right {
  display: flex;
  gap: 8px;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  color: #374151;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.header-btn:hover {
  background: #F9FAFB;
  border-color: #D1D5DB;
}

/* 主内容区域 */
.main-content {
  flex: 1;
  min-height: 0;
}

.ai-wrapper {
  height: 100%;
  position: relative;
}

.tasks-panel {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 320px;
  background: #FFFFFF;
  border-radius: 8px;
  border: 1px solid #D1D5DB;
  box-shadow: var(--shadow-md);
  z-index: 10;
  overflow: hidden;
}

/* 区域分隔线 */
.section-divider {
  width: 60%;
  max-width: 400px;
  height: 1px;
  background: rgba(0, 0, 0, 0.1);
  margin: 0 auto;
}

/* 底部统计 — 简洁白卡片 */
.bottom-stats {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  background: #FFFFFF;
  border-radius: 8px;
  border: 1px solid #D1D5DB;
  box-shadow: var(--shadow-sm);
}

.stat-item {
  flex: 1;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.15s ease;
}

.stat-item:not(:last-child) {
  border-right: 1px solid rgba(0, 0, 0, 0.12);
}

.stat-item:hover {
  background: #F9FAFB;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  line-height: 1.2;
}

.stat-unit {
  font-size: 12px;
  font-weight: 400;
  color: #9CA3AF;
  margin-left: 2px;
}

.stat-label {
  font-size: 12px;
  color: #6B7280;
  margin-top: 2px;
}

/* 任务进度环形图 */
.ring-chart-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ring-chart {
  flex-shrink: 0;
}

.ring-bg {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #D1D5DB;
}

.ring-center {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-value {
  font-size: 13px;
  font-weight: 600;
  color: var(--primary);
  line-height: 1;
}

.ring-unit {
  font-size: 9px;
  font-weight: 500;
  color: var(--primary);
  margin-left: 1px;
}

.ring-chart-item .stat-info {
  display: flex;
  flex-direction: column;
}

/* 动画 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* 响应式 */
@media (max-width: 768px) {
  .home-page {
    gap: 12px;
  }

  .page-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .header-right {
    justify-content: flex-end;
  }

  .header-btn {
    padding: 5px 10px;
    font-size: 12px;
  }

  .tasks-panel {
    width: 100%;
  }

  .bottom-stats {
    flex-wrap: wrap;
  }

  .stat-item {
    flex: 0 0 calc(50% - 6px);
  }
}
</style>
