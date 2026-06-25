<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted, computed } from 'vue'
import {
  categories,
  selectedCategory,
  selectedStatuses,
  selectedPriorities,
  deadlineBefore,
  keyword,
  statusOptions,
  priorityOptions,
  handleCategoryChange,
  handleDeadlineChange,
  handleKeywordSearch,
  toggleStatus,
  togglePriority,
  openAddModal,
  tasks,
  isTaskBatchDeleting,
  selectedTaskIds,
  toggleTaskBatchDeleting,
  cancelTaskBatchDelete,
  toggleSelectAll,
  batchDeleteTasks
} from './useTasks'

const emit = defineEmits<{ (e: 'export'): void }>()

const isAllSelected = computed(() => {
  return tasks.value.length > 0 && selectedTaskIds.value.size === tasks.value.length
})

const showFilters = ref(window.innerWidth > 575)

const handleResize = () => { if (window.innerWidth > 575) showFilters.value = true }
onMounted(() => window.addEventListener('resize', handleResize))
let debounceTimer: ReturnType<typeof setTimeout> | null = null

const debouncedSearch = (searchKeyword: string) => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  debounceTimer = setTimeout(() => {
    handleKeywordSearch(searchKeyword)
  }, 500)
}

// 监听keyword变化，使用防抖
watch(keyword, (newKeyword) => {
  debouncedSearch(newKeyword)
})

const handleSearchKeyup = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    handleKeywordSearch(keyword.value)
  }
}

// 日期时间分开选择
const deadlineDate = ref('')
const deadlineTime = ref('23:59')
const showTimePicker = ref(false)  // 控制时间选择器的显示/隐藏

// 从deadlineBefore解析日期和时间
if (deadlineBefore.value) {
  const match = deadlineBefore.value.match(/^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2}):\d{2}$/)
  if (match) {
    deadlineDate.value = match[1]
    deadlineTime.value = match[2]
    // 如果有具体时间，显示时间选择器
    if (match[2] !== '23:59') {
      showTimePicker.value = true
    }
  }
}

// 拼接日期时间并提交
const updateDeadline = () => {
  if (deadlineDate.value) {
    // 默认使用23:59以包含当天所有任务
    const time = deadlineTime.value || '23:59'
    deadlineBefore.value = `${deadlineDate.value}T${time}:00`
    handleDeadlineChange(deadlineBefore.value)
  } else {
    deadlineBefore.value = ''
    handleDeadlineChange('')
    showTimePicker.value = false
  }
}


// 切换时间选择器显示
const toggleTimePicker = () => {
  if (deadlineDate.value) {
    const wasOpened = showTimePicker.value
    showTimePicker.value = !wasOpened

    // 收起时恢复默认23:59
    if (!showTimePicker.value && wasOpened) {
      deadlineTime.value = '23:59'
      updateDeadline()
    }
    // 展开时也默认显示23:59
    if (showTimePicker.value && !wasOpened) {
      deadlineTime.value = '23:59'
      updateDeadline()
    }
  }
}

// 日期快捷选择
const showDateShortcuts = ref(false)

const dateShortcuts = [
  { label: '今天', value: 'today' },
  { label: '明天', value: 'tomorrow' },
  { label: '本周', value: 'week' },
  { label: '下周', value: 'nextWeek' },
  { label: '本月', value: 'month' }
]

const formatDate = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const selectDateShortcut = (shortcut: string) => {
  const today = new Date()
  let targetDate = new Date()

  switch (shortcut) {
    case 'today':
      targetDate = today
      break
    case 'tomorrow':
      targetDate.setDate(today.getDate() + 1)
      break
    case 'week':
      targetDate.setDate(today.getDate() + (5 - today.getDay() + 7) % 7)
      break
    case 'nextWeek':
      targetDate.setDate(today.getDate() + 7 + (5 - today.getDay() + 7) % 7)
      break
    case 'month':
      targetDate.setMonth(today.getMonth() + 1)
      targetDate.setDate(0)
      break
  }

  deadlineDate.value = formatDate(targetDate)
  updateDeadline()
  showDateShortcuts.value = false
}

// 日期快捷菜单位置
const shortcutMenuStyle = ref<{ left: string; top: string }>({ left: '0px', top: '0px' })

const toggleDateShortcuts = (event: MouseEvent) => {
  showDateShortcuts.value = !showDateShortcuts.value
  
  if (showDateShortcuts.value) {
    // 获取按钮位置
    const button = event.currentTarget as HTMLElement
    const rect = button.getBoundingClientRect()
    shortcutMenuStyle.value = {
      left: `${rect.left}px`,
      top: `${rect.bottom + 8}px`
    }
  }
}

const closeDateShortcuts = (e: MouseEvent) => {
  const target = e.target as HTMLElement
  if (!target.closest('.deadline-input-wrapper') && !target.closest('.date-shortcuts')) {
    showDateShortcuts.value = false
  }
}

onUnmounted(() => {
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('click', closeDateShortcuts)
})

// 监听点击外部关闭快捷菜单
setTimeout(() => {
  document.addEventListener('click', closeDateShortcuts)
}, 0)
</script>

<template>
  <div class="filter-bar">
    <!-- 第一行：搜索 + 操作按钮 -->
    <div class="filter-row-top">
      <div class="filter-search">
        <el-input
          v-model="keyword"
          placeholder="输入关键词自动搜索..."
          clearable
          size="small"
          @keyup.enter="handleKeywordSearch(keyword)"
          @clear="keyword = ''"
        >
          <template #prefix>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#9CA3AF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
          </template>
        </el-input>
      </div>

      <div class="filter-actions">
        <template v-if="!isTaskBatchDeleting">
          <el-button size="small" @click="toggleTaskBatchDeleting">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
            批量删除
          </el-button>
        </template>
        <template v-else>
          <label class="select-all-label">
            <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" />
            全选 ({{ selectedTaskIds.size }})
          </label>
          <el-button size="small" type="danger" @click="batchDeleteTasks" :disabled="selectedTaskIds.size === 0">
            删除 ({{ selectedTaskIds.size }})
          </el-button>
          <el-button size="small" @click="cancelTaskBatchDelete">
            取消
          </el-button>
        </template>
        <el-button size="small" type="primary" @click="openAddModal">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          添加任务
        </el-button>
        <el-button size="small" @click="emit('export')">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="7 10 12 15 17 10"/><line x1="12" y1="15" x2="12" y2="3"/></svg>
          导出
        </el-button>
        <el-button size="small" class="filter-toggle-btn" @click="showFilters = !showFilters">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
          筛选
        </el-button>
      </div>
    </div>

    <!-- 第二行：筛选条件 -->
    <div v-show="showFilters" class="filter-row-bottom">
      <div class="filter-group">
        <label class="filter-label">分类</label>
        <el-select
          v-model="selectedCategory"
          @change="handleCategoryChange(selectedCategory)"
          placeholder="全部"
          size="small"
          clearable
          style="width: 130px;"
        >
          <el-option label="全部" value="" />
          <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
        </el-select>
      </div>

      <div class="filter-divider"></div>

      <div class="filter-group">
        <label class="filter-label">状态</label>
        <div class="filter-options">
          <label v-for="status in statusOptions" :key="status.value" class="option-item">
            <input
              type="checkbox"
              :checked="selectedStatuses.includes(status.value)"
              @click="toggleStatus(status.value)"
              class="option-checkbox"
            />
            <span class="option-label" :style="{ color: status.color }">{{ status.label }}</span>
          </label>
        </div>
      </div>

      <div class="filter-divider"></div>

      <div class="filter-group">
        <label class="filter-label">优先级</label>
        <div class="filter-options">
          <label v-for="priority in priorityOptions" :key="priority.value" class="option-item">
            <input
              type="checkbox"
              :checked="selectedPriorities.includes(priority.value)"
              @click="togglePriority(priority.value)"
              class="option-checkbox"
            />
            <span class="option-label" :style="{ color: priority.color }">{{ priority.label }}</span>
          </label>
        </div>
      </div>

      <div class="filter-divider"></div>

      <div class="filter-group deadline-group">
        <label class="filter-label">时间</label>
        <div class="deadline-input-wrapper">
          <input
            type="date"
            v-model="deadlineDate"
            @change="updateDeadline"
            class="deadline-date-input"
          />
          <Transition name="fade">
            <input
              v-if="showTimePicker"
              type="time"
              v-model="deadlineTime"
              @change="updateDeadline"
              class="deadline-time-input"
            />
          </Transition>
          <!-- 时间展开按钮 -->
          <button
            v-if="deadlineDate"
            class="deadline-time-toggle-btn"
            @click.stop="toggleTimePicker"
            :class="{ active: showTimePicker }"
            title="选择具体时间"
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
          </button>
          <!-- 清除日期按钮 -->
          <button
            v-if="deadlineDate"
            class="deadline-clear-btn"
            @click.stop="deadlineDate = ''; deadlineTime = '23:59'; showTimePicker = false; updateDeadline()"
            title="清除日期"
          >
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
          <!-- 日期快捷选择按钮 -->
          <button
            class="deadline-dropdown-btn"
            @click.stop="toggleDateShortcuts($event)"
            :class="{ active: showDateShortcuts }"
            title="快捷日期选择"
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
          </button>

          <Teleport to="body">
            <div
              v-if="showDateShortcuts"
              class="date-shortcuts"
              :style="shortcutMenuStyle"
            >
              <button
                v-for="shortcut in dateShortcuts"
                :key="shortcut.value"
                class="shortcut-item"
                @click="selectDateShortcut(shortcut.value)"
              >
                {{ shortcut.label }}
              </button>
            </div>
          </Teleport>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.filter-bar {
  background: #FFFFFF;
  border-radius: 10px;
  border: 1px solid #D1D5DB;
  padding: 12px 16px;
  box-shadow: var(--shadow-sm);
}

/* 第一行：搜索 + 操作按钮 */
.filter-row-top {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-search {
  min-width: 200px;
  flex: 1;
  max-width: 280px;
}

.filter-search :deep(.el-input) {
  width: 100%;
}

.filter-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.filter-toggle-btn {
  display: none;
}

/* 操作按钮统一样式 */
.filter-actions :deep(.el-button) {
  height: 32px;
  padding: 0 12px;
  font-size: 13px;
  border-radius: 6px;
}

/* 第二行：筛选条件 */
.filter-row-bottom {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding-top: 10px;
  margin-top: 10px;
  border-top: 1px solid #E5E7EB;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 6px;
}

.filter-divider {
  width: 1px;
  height: 20px;
  background: #D1D5DB;
  margin: 0 4px;
  flex-shrink: 0;
}

.filter-label {
  font-weight: 500;
  color: #9CA3AF;
  font-size: 12px;
  white-space: nowrap;
  flex-shrink: 0;
}

.filter-options {
  display: flex;
  gap: 4px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 3px 8px;
  border-radius: 6px;
  transition: background-color 0.15s;
}

.option-item:hover {
  background-color: #E5E7EB;
}

.option-checkbox {
  width: 13px;
  height: 13px;
  accent-color: var(--primary);
  cursor: pointer;
}

.option-label {
  font-size: 13px;
  font-weight: 500;
  color: #4B5563;
  white-space: nowrap;
  cursor: pointer;
}

/* 日期筛选组 */
.deadline-group {
  flex-direction: row;
  align-items: center;
  gap: 6px;
}

.deadline-input-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.deadline-date-input {
  padding: 5px 10px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-size: 12px;
  color: #334155;
  background: #f8fafc;
  min-width: 120px;
  transition: all 0.2s ease;
}

.deadline-date-input::-webkit-calendar-picker-indicator,
:deep(.deadline-date-input::-webkit-calendar-picker-indicator) {
  opacity: 0 !important;
  width: 20px;
  cursor: pointer;
}

.deadline-date-input:hover {
  border-color: #D1D5DB;
}

.deadline-date-input:focus {
  outline: none;
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.1);
}

.deadline-time-input {
  padding: 5px 8px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-size: 12px;
  color: #334155;
  background: #f8fafc;
  min-width: 70px;
  transition: all 0.2s ease;
}

.deadline-time-input:hover {
  border-color: #D1D5DB;
}

.deadline-time-input:focus {
  outline: none;
  border-color: var(--primary);
  background: white;
  box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.1);
}

/* 圆形按钮通用样式 */
.deadline-clear-btn,
.deadline-time-toggle-btn,
.deadline-dropdown-btn {
  width: 24px;
  height: 24px;
  border: 1px solid #D1D5DB;
  background-color: #FFFFFF;
  border-radius: 50%;
  cursor: pointer;
  color: #6B7280;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s;
  flex-shrink: 0;
  padding: 0;
}

.deadline-clear-btn:hover {
  background-color: #FEE2E2;
  border-color: #FECACA;
  color: #DC2626;
}

.deadline-time-toggle-btn:hover,
.deadline-time-toggle-btn.active {
  background-color: #FEF3C7;
  border-color: #FCD34D;
  color: #D97706;
}

.deadline-dropdown-btn:hover,
.deadline-dropdown-btn.active {
  background-color: #F5F3FF;
  border-color: #C4B5FD;
  color: var(--primary);
}

/* 时间选择器过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

.date-shortcuts {
  position: fixed;
  background: white;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  border: 1px solid #D1D5DB;
  padding: 6px;
  z-index: 9999;
  min-width: 120px;
}

.shortcut-item {
  display: block;
  width: 100%;
  padding: 8px 12px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  color: #475569;
  text-align: left;
  border-radius: 6px;
  transition: all 0.15s;
}

.shortcut-item:hover {
  background-color: #E5E7EB;
  color: var(--primary);
}

.select-all-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #334155;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
}

.select-all-label input[type="checkbox"] {
  width: 15px;
  height: 15px;
  cursor: pointer;
  accent-color: var(--primary);
}

/* 响应式 */
@media (max-width: 767px) {
  .filter-bar {
    padding: 10px 14px;
  }

  .filter-row-top {
    gap: 8px;
  }

  .filter-row-bottom {
    gap: 10px;
  }

  .filter-group {
    flex-wrap: wrap;
  }

  .filter-options {
    flex-wrap: wrap;
  }

  .filter-search {
    min-width: 140px;
  }

  .filter-actions {
    flex-wrap: wrap;
  }

  .filter-divider {
    display: none;
  }
}

@media (max-width: 575px) {
  .filter-bar {
    padding: 8px 10px;
    border-radius: 8px;
  }

  .filter-row-top {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }

  .filter-toggle-btn {
    display: inline-flex;
  }

  .filter-row-bottom {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .filter-group {
    width: 100%;
  }

  .filter-label {
    min-width: 36px;
    font-size: 11px;
  }

  .filter-options {
    flex-wrap: wrap;
    gap: 4px;
  }

  .option-item {
    padding: 2px 6px;
  }

  .option-label {
    font-size: 11px;
  }

  .deadline-input-wrapper {
    flex-wrap: wrap;
  }

  .filter-actions {
    gap: 6px;
    justify-content: flex-start;
  }

  .filter-actions :deep(.el-button) {
    padding: 5px 10px;
    font-size: 12px;
  }

  .filter-search {
    flex: 1;
    min-width: 0;
  }

  .filter-search :deep(.el-input) {
    width: 100%;
  }
}
</style>

<style>
/* 非scoped：隐藏原生日历图标 */
.deadline-date-input::-webkit-calendar-picker-indicator {
  opacity: 0 !important;
  width: 20px;
  cursor: pointer;
}
</style>