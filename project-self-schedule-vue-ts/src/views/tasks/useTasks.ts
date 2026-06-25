/**
 * 任务模块统一导出
 * 保持向后兼容，将拆分后的composables统一导出
 */

// 从composables导入所有任务相关功能
export {
  tasks,
  categories,
  selectedCategory,
  selectedStatuses,
  selectedPriorities,
  deadlineBefore,
  keyword,
  error,
  isLoading,
  expandedTasks,
  currentPage,
  totalPages,
  totalItems,
  fetchTasks,
  fetchCategories,
  toggleExpand,
  toggleStatus,
  togglePriority,
  handleCategoryChange,
  handleDeadlineChange,
  handleKeywordSearch,
  changePage,
  resetPage,
  isReminderOnly,
  formatEstimatedTime,
  formatDeadline,
  formatRemindTime,
  formatTimeOnly,
  statusOptions,
  priorityOptions,
  getPriorityInfo,
  getStatusLabel,
  getPriorityLabel,
  getTaskTags
} from '../../composables/useTaskList'

export {
  showAddModal,
  showEditModal,
  estimatedDays,
  estimatedHours,
  estimatedMinutes,
  estimatedSeconds,
  taskForm,
  reminderForm,
  recurrenceForm,
  editingTask,
  advanceMinuteOptions,
  repeatOptions,
  weekDayOptions,
  calculateTotalEstimatedSeconds,
  splitEstimatedSeconds,
  toggleAdvanceMinute,
  addCustomAdvanceMinute,
  customAdvanceMinutes,
  reminderError,
  toggleWeekDay,
  buildReminderConfig,
  buildRecurrenceRule,
  parseReminderConfig,
  parseRecurrenceRule,
  openAddModal,
  openEditModal,
  closeModal,
  saveTask,
  isSaving
} from '../../composables/useTaskForm'

export {
  completeTask,
  undoTask,
  deleteTask,
  forceDeleteTask,
  isTaskBatchDeleting,
  selectedTaskIds,
  toggleTaskBatchDeleting,
  cancelTaskBatchDelete,
  toggleTaskSelection,
  toggleSelectAll,
  batchDeleteTasks
} from '../../composables/useTaskActions'

/**
 * 获取重复规则标签
 * @param recurrenceRule 重复规则字符串
 * @returns 显示用的标签文本
 */
export function getRecurrenceLabel(recurrenceRule: string): string {
  if (!recurrenceRule) return ''
  
  const parts = recurrenceRule.split(':')
  if (parts.length < 2) return recurrenceRule
  
  const type = parts[0].toUpperCase()
  const interval = parseInt(parts[1]) || 1
  const params = parts.length > 2 ? parts[2] : ''
  
  if (type === 'DAILY') {
    return interval === 1 ? '每天' : `每${interval}天`
  }
  
  if (type === 'WEEKLY') {
    if (params) {
      const days = params.split(',').map(d => {
        const dayMap: Record<string, string> = {
          '1': '一',
          '2': '二',
          '3': '三',
          '4': '四',
          '5': '五',
          '6': '六',
          '7': '日'
        }
        return dayMap[d] || d
      })
      return interval === 1 ? `每周${days.join('、')}` : `每${interval}周${days.join('、')}`
    }
    return interval === 1 ? '每周' : `每${interval}周`
  }
  
  if (type === 'MONTHLY') {
    if (params) {
      // 检查是否为 "周-星期" 格式
      if (params.includes('-')) {
        const [week, day] = params.split('-')
        const dayMap: Record<string, string> = {
          '1': '一',
          '2': '二',
          '3': '三',
          '4': '四',
          '5': '五',
          '6': '六',
          '7': '日'
        }
        const weekText = parseInt(week) < 0 ? `倒数第${Math.abs(parseInt(week))}个` : `第${week}个`
        return `${weekText}周${dayMap[day] || day}`
      }
      return interval === 1 ? `每月${params}号` : `每${interval}月${params}号`
    }
    return interval === 1 ? '每月' : `每${interval}月`
  }
  
  if (type === 'YEARLY') {
    if (params) {
      const dateParts = params.split('-')
      if (dateParts.length === 2) {
        return `${dateParts[0]}月${dateParts[1]}日`
      }
      return `每年${params}`
    }
    return interval === 1 ? '每年' : `每${interval}年`
  }
  
  return recurrenceRule
}