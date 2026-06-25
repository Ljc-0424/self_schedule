<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageLayout from '../../components/PageLayout.vue'
import Loading from '../../components/Loading.vue'
import ErrorMessage from '../../components/ErrorMessage.vue'
import FilterBar from './FilterBar.vue'
import TaskCard from './TaskCard.vue'
import TaskModal from './TaskModal.vue'
import Pagination from '../../components/Pagination.vue'
import {
  tasks, error, isLoading, fetchTasks, fetchCategories,
  openAddModal, currentPage, totalPages, totalItems, changePage,
  statusOptions, priorityOptions
} from './useTasks'

const viewMode = ref<'list' | 'grid'>('list')
const isVisible = ref(false)

// CSV 导出功能
const exportCSV = () => {
  const headers = ['任务标题', '状态', '优先级', '截止日期', '创建时间']
  const statusMap: Record<number, string> = Object.fromEntries(
    statusOptions.map(o => [o.value, o.label])
  )
  const priorityMap: Record<number, string> = Object.fromEntries(
    priorityOptions.map(o => [o.value, o.label])
  )

  const rows = tasks.value.map(t => [
    `"${(t.title || '').replace(/"/g, '""')}"`,
    statusMap[t.status] || t.status,
    priorityMap[t.priority] || t.priority,
    t.deadline || '',
    t.createdTime || ''
  ])

  const csvContent = '﻿' + [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `任务导出_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

// 快捷任务模板
const taskTemplates = [
  { title: '每日站会', description: '团队每日同步进度与阻碍', priority: 1, estimatedSeconds: 900 },
  { title: '代码审查', description: '审查团队成员提交的代码变更', priority: 1, estimatedSeconds: 1800 },
  { title: '阅读学习', description: '技术文章或书籍阅读', priority: 0, estimatedSeconds: 3600 }
]

const quickCreateTask = async (template: typeof taskTemplates[number]) => {
  try {
    const { taskApi } = await import('../../api')
    const response = await taskApi.createTask({
      title: template.title,
      description: template.description,
      priority: template.priority,
      estimatedSeconds: template.estimatedSeconds
    })
    if (response.data.code === 200) {
      await fetchTasks()
      await fetchCategories()
    }
  } catch (e: any) {
    console.error('创建任务失败', e)
  }
}

onMounted(() => {
  fetchTasks()
  fetchCategories()
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})
</script>

<template>
  <PageLayout title="" subtitle="">
    <div class="tasks-page">
      <!-- 页面头部 -->
      <div :class="['page-header', { 'visible': isVisible }]">
        <div class="header-content">
          <h1 class="page-title">任务清单</h1>
          <p class="page-subtitle">高效管理您的日常任务</p>
        </div>
        <div class="header-actions">
          <div class="view-toggle">
            <button
              :class="['toggle-btn', { active: viewMode === 'list' }]"
              @click="viewMode = 'list'"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="8" y1="6" x2="21" y2="6"/>
                <line x1="8" y1="12" x2="21" y2="12"/>
                <line x1="8" y1="18" x2="21" y2="18"/>
                <line x1="3" y1="6" x2="3.01" y2="6"/>
                <line x1="3" y1="12" x2="3.01" y2="12"/>
                <line x1="3" y1="18" x2="3.01" y2="18"/>
              </svg>
            </button>
            <button
              :class="['toggle-btn', { active: viewMode === 'grid' }]"
              @click="viewMode = 'grid'"
            >
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="7" height="7"/>
                <rect x="14" y="3" width="7" height="7"/>
                <rect x="14" y="14" width="7" height="7"/>
                <rect x="3" y="14" width="7" height="7"/>
              </svg>
            </button>
          </div>
          <button class="add-btn" @click="openAddModal">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            添加任务
          </button>
        </div>
      </div>

      <!-- 筛选栏 -->
      <div :class="['filter-section', { 'visible': isVisible }]">
        <FilterBar @export="exportCSV" />
      </div>

      <!-- 筛选栏与统计栏分隔线 -->
      <div :class="['section-divider', { 'visible': isVisible }]"></div>

      <!-- 统计栏 -->
      <div :class="['stats-bar', { 'visible': isVisible }]">
        <div class="stat-item">
          <span class="stat-number">{{ tasks.length }}</span>
          <span class="stat-text">总任务</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-number">
            {{ tasks.filter(t => t.status === 2).length }}
          </span>
          <span class="stat-text">已完成</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-number">
            {{ tasks.filter(t => t.status === 0).length }}
          </span>
          <span class="stat-text">进行中</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-number">
            {{ tasks.filter(t => t.status === 3).length }}
          </span>
          <span class="stat-text">已过期</span>
        </div>
      </div>

      <!-- 任务列表 -->
      <div :class="['tasks-content', { 'visible': isVisible }]">
        <ErrorMessage v-if="error" :message="error" class="mb-4" />

        <Loading v-if="isLoading" />

        <template v-else>
          <div :class="['task-list', viewMode]">
            <TaskCard
              v-for="(task, index) in tasks"
              :key="task.id"
              :task="task"
              :index="index"
              :view-mode="viewMode"
            />

            <!-- 空状态 -->
            <div v-if="tasks.length === 0" class="empty-state">
              <div class="empty-icon">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#D1D5DB" stroke-width="1.5">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                  <polyline points="10 9 9 9 8 9"/>
                </svg>
              </div>
              <h3 class="empty-title">暂无任务</h3>
              <p class="empty-desc">点击上方"添加任务"按钮创建您的第一个任务</p>
              <button class="add-btn" @click="openAddModal">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                创建第一个任务
              </button>
              <!-- 快捷任务模板 -->
              <div class="quick-templates">
                <span class="templates-label">常用模板</span>
                <div class="templates-list">
                  <button
                    v-for="tpl in taskTemplates"
                    :key="tpl.title"
                    class="template-btn"
                    @click="quickCreateTask(tpl)"
                  >
                    {{ tpl.title }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 分页 -->
      <div v-if="totalPages > 1" class="pagination-section">
        <Pagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :total-items="totalItems"
          @page-change="changePage"
        />
      </div>

      <!-- 浮动按钮 -->
      <button class="fab" @click="openAddModal">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
      </button>
    </div>

    <TaskModal />
  </PageLayout>
</template>

<style scoped>
.tasks-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 入场动画 */
.page-header,
.filter-section,
.section-divider,
.stats-bar,
.tasks-content {
  opacity: 1;
  transform: translateY(0);
}

.page-header.visible,
.filter-section.visible,
.section-divider.visible,
.stats-bar.visible,
.tasks-content.visible {
  opacity: 1;
  transform: translateY(0);
}

.filter-section { transition-delay: 0.05s; }
.section-divider { transition-delay: 0.08s; }
.stats-bar { transition-delay: 0.1s; }
.tasks-content { transition-delay: 0.15s; }

/* 页面头部 — 白色卡片 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 20px 24px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 2px;
  line-height: 1.3;
}

.page-subtitle {
  font-size: 13px;
  color: #6B7280;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-toggle {
  display: flex;
  background: #E5E7EB;
  border-radius: 6px;
  padding: 2px;
}

.toggle-btn {
  padding: 6px 10px;
  background: none;
  border: none;
  color: #9CA3AF;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toggle-btn.active {
  background: #FFFFFF;
  color: var(--primary);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.toggle-btn:hover:not(.active) {
  color: #6B7280;
}

.add-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: var(--primary);
  border: none;
  border-radius: 6px;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s ease;
}

.add-btn:hover {
  background: #4338CA;
}

/* 筛选栏 */
.filter-section {
  margin-bottom: 16px;
  flex-shrink: 0;
}

/* 区域分隔线 */
.section-divider {
  width: 60%;
  max-width: 400px;
  height: 1px;
  background: rgba(0, 0, 0, 0.1);
  margin: 0 auto 16px;
  flex-shrink: 0;
}

/* 统计栏 — 简洁白卡片 */
.stats-bar {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 32px;
  padding: 16px 20px;
  background: #FFFFFF;
  border-radius: 8px;
  border: 1px solid #D1D5DB;
  margin-bottom: 16px;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  line-height: 1;
}

.stat-text {
  font-size: 12px;
  color: #6B7280;
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: #D1D5DB;
}

/* 任务列表 */
.tasks-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-list > * {
  flex-shrink: 0;
}

.task-list.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  margin-bottom: 16px;
}

.empty-title {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 6px;
}

.empty-desc {
  font-size: 14px;
  color: #6B7280;
  margin: 0 0 20px;
}

/* 快捷任务模板 */
.quick-templates {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
}

.templates-label {
  font-size: 12px;
  color: #9CA3AF;
}

.templates-list {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.template-btn {
  padding: 6px 14px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  color: #374151;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.template-btn:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: #F5F3FF;
}

/* 分页 */
.pagination-section {
  display: flex;
  justify-content: center;
  padding: 20px 0;
  flex-shrink: 0;
}

/* 浮动按钮 — 纯色 */
.fab {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 48px;
  height: 48px;
  background: var(--primary);
  border: none;
  border-radius: 12px;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease;
  z-index: 100;
}

.fab:hover {
  background: #4338CA;
}

/* 响应式 */
@media (max-width: 768px) {
  .tasks-page {
    padding: 16px;
  }

  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }

  .page-title {
    font-size: 18px;
  }

  .stats-bar {
    gap: 16px;
    padding: 14px 16px;
  }

  .stat-number {
    font-size: 18px;
  }

  .task-list.grid {
    grid-template-columns: 1fr;
  }

  .fab {
    bottom: 20px;
    right: 20px;
    width: 44px;
    height: 44px;
  }

}

@media (max-width: 480px) {
  .header-actions {
    flex-direction: column;
    width: 100%;
  }

  .add-btn {
    width: 100%;
    justify-content: center;
  }

  .view-toggle {
    align-self: flex-start;
  }

  .stats-bar {
    gap: 12px;
    padding: 12px;
  }

  .stat-divider {
    display: none;
  }
}
</style>
