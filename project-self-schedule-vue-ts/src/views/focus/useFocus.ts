/**
 * 专注模块统一导出
 * 保持向后兼容，将拆分后的composables统一导出
 */

// 从composables导入所有专注相关功能
export {
  focusRecords,
  showHistory,
  isLoadingHistory,
  showDetail,
  selectedRecord,
  isLoadingDetail,
  focusError as error,
  fetchFocusRecords,
  fetchFocusRecordDetail,
  closeDetail,
  updateFocusRecordNotes,
  deleteFocusRecord,
  batchDeleteFocusRecords,
  isFocusBatchDeleting,
  selectedRecordIds,
  toggleBatchDeleting,
  cancelBatchDelete,
  toggleRecordSelection,
  toggleSelectAllRecords,
  getFocusStatusLabel,
  formatDuration,
  formatDateTime,
  focusCurrentPage,
  focusTotalPages,
  focusTotalItems,
  focusChangePage,
  filterDate,
  setFilterDate,
  clearFilterDate
} from '../../composables/useFocusRecord'

export {
  useFocusTimer
} from '../../composables/useFocusTimer'

export {
  showTaskSearch,
  searchKeyword,
  taskSearchResults as tasks,
  customHours,
  customMinutes,
  customSeconds,
  toggleTaskSearch,
  searchTasks,
  useFocus,
  taskSearchCurrentPage as taskSearchCurrentPage,
  taskSearchTotalPages as taskSearchTotalPages,
  taskSearchTotalItems as taskSearchTotalItems,
  taskSearchChangePage as taskSearchChangePage
} from '../../composables/useFocusTask'
