import { tasks, categories, selectedCategory, selectedStatuses, selectedPriorities, deadlineBefore, keyword, expandedTasks, currentPage, totalPages, totalItems } from './useTaskList'
import { focusRecords, showHistory, selectedRecord, showDetail, isFocusBatchDeleting } from './useFocusRecord'
import { showTaskSearch, searchKeyword, customHours, customMinutes, customSeconds } from './useFocusTask'
import { isTaskBatchDeleting } from './useTaskActions'

/**
 * 全局状态清理函数
 * 在用户退出登录时调用，清除所有全局状态
 */
export const clearAllState = () => {
  // 清空任务列表状态
  tasks.value = []
  categories.value = []
  selectedCategory.value = ''
  selectedStatuses.value = []
  selectedPriorities.value = []
  deadlineBefore.value = ''
  keyword.value = ''
  expandedTasks.value = new Set()
  currentPage.value = 1
  totalPages.value = 1
  totalItems.value = 0

  // 清空专注记录状态
  focusRecords.value = []
  showHistory.value = false
  selectedRecord.value = null
  showDetail.value = false
  isFocusBatchDeleting.value = false

  // 清空任务批量删除状态
  isTaskBatchDeleting.value = false

  // 清空专注任务搜索状态
  showTaskSearch.value = false
  searchKeyword.value = ''
  customHours.value = 0
  customMinutes.value = 25
  customSeconds.value = 0

  // 清空 localStorage
  localStorage.clear()
}
