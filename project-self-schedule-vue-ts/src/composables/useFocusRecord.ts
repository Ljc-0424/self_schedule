import { ref } from 'vue'
import type { FocusRecordVO, TaskVO } from '../types'
import { focusRecordApi } from '../api'

/**
 * 专注记录管理composable
 * 提供专注记录的获取、删除等功能
 */

// 状态定义（全局共享）
export const focusRecords = ref<FocusRecordVO[]>([])
export const focusError = ref('')

// 历史记录相关
export const showHistory = ref(false)
export const isLoadingHistory = ref(false)

// 分页状态
export const focusCurrentPage = ref(1)
export const focusTotalPages = ref(1)
export const focusTotalItems = ref(0)

// 详情弹窗状态
export const showDetail = ref(false)
export const selectedRecord = ref<FocusRecordVO | null>(null)
export const isLoadingDetail = ref(false)

// 批量删除状态
export const isFocusBatchDeleting = ref(false)
export const selectedRecordIds = ref<Set<number>>(new Set())

// 日期筛选状态
export const filterDate = ref('')

/**
 * 切换批量删除模式
 */
export const toggleBatchDeleting = () => {
  isFocusBatchDeleting.value = !isFocusBatchDeleting.value
  if (!isFocusBatchDeleting.value) {
    selectedRecordIds.value.clear()
  }
}

/**
 * 取消批量删除模式
 */
export const cancelBatchDelete = () => {
  isFocusBatchDeleting.value = false
  selectedRecordIds.value.clear()
}

/**
 * 切换记录勾选状态
 */
export const toggleRecordSelection = (recordId: number) => {
  if (selectedRecordIds.value.has(recordId)) {
    selectedRecordIds.value.delete(recordId)
  } else {
    selectedRecordIds.value.add(recordId)
  }
}

/**
 * 全选/取消全选
 */
export const toggleSelectAllRecords = () => {
  if (selectedRecordIds.value.size === focusRecords.value.length) {
    selectedRecordIds.value.clear()
  } else {
    focusRecords.value.forEach(record => selectedRecordIds.value.add(record.id))
  }
}

/**
 * 获取专注记录列表（分页）
 */
export const fetchFocusRecords = async (pageNum: number = 1) => {
  isLoadingHistory.value = true
  try {
    focusError.value = ''

    const params: Record<string, any> = {}

    // 如果设置了筛选日期，添加日期范围筛选
    if (filterDate.value) {
      params.startTimeAfter = `${filterDate.value}T00:00:00`
      params.endTimeBefore = `${filterDate.value}T23:59:59`
    }

    const response = await focusRecordApi.getRecords(pageNum, params)
    if (response.data.code === 200) {
      focusRecords.value = response.data.data.data
      focusTotalPages.value = response.data.data.pages
      focusTotalItems.value = response.data.data.total
      focusCurrentPage.value = response.data.data.pageNum
    } else {
      focusError.value = response.data.message
    }
  } catch (e: any) {
    focusError.value = e.response?.data?.message || e.message || '获取专注记录失败'
  } finally {
    isLoadingHistory.value = false
  }
}

/**
 * 设置筛选日期
 * @param date 日期字符串（格式：YYYY-MM-DD）
 */
export const setFilterDate = (date: string) => {
  filterDate.value = date
  focusCurrentPage.value = 1
  fetchFocusRecords(1)
}

/**
 * 清除日期筛选
 */
export const clearFilterDate = () => {
  filterDate.value = ''
  focusCurrentPage.value = 1
  fetchFocusRecords(1)
}

/**
 * 切换分页
 * @param page 目标页码
 */
export const focusChangePage = (page: number) => {
  focusCurrentPage.value = page
  fetchFocusRecords(page)
}

/**
 * 获取专注记录详情
 * @param recordId 记录ID
 */
export const fetchFocusRecordDetail = async (recordId: number) => {
  isLoadingDetail.value = true
  try {
    focusError.value = ''
    const response = await focusRecordApi.getRecordDetail(recordId)
    if (response.data.code === 200) {
      selectedRecord.value = response.data.data
      showDetail.value = true
    } else {
      focusError.value = response.data.message
    }
  } catch (e: any) {
    focusError.value = e.response?.data?.message || e.message || '获取专注记录详情失败'
  } finally {
    isLoadingDetail.value = false
  }
}

/**
 * 关闭详情弹窗
 */
export const closeDetail = () => {
  showDetail.value = false
  selectedRecord.value = null
}

/**
 * 更新专注记录笔记
 * @param recordId 记录ID
 * @param notes 笔记内容
 */
export const updateFocusRecordNotes = async (recordId: number, notes: string) => {
  try {
    focusError.value = ''
    const response = await focusRecordApi.updateNotes(recordId, notes)
    if (response.data.code === 200) {
      // 更新成功后刷新详情
      if (selectedRecord.value && selectedRecord.value.id === recordId) {
        selectedRecord.value.notes = notes
      }
    } else {
      focusError.value = response.data.message
    }
  } catch (e: any) {
    focusError.value = e.response?.data?.message || e.message || '更新失败'
  }
}

/**
 * 删除专注记录
 * @param recordId 记录ID
 */
export const deleteFocusRecord = async (recordId: number) => {
  if (!confirm('确定删除此记录？')) return

  try {
    focusError.value = ''
    const response = await focusRecordApi.deleteRecord(recordId)
    if (response.data.code === 200) {
      await fetchFocusRecords()
    } else {
      focusError.value = response.data.message
    }
  } catch (e: any) {
    focusError.value = e.response?.data?.message || e.message || '删除失败'
  }
}

/**
 * 批量删除专注记录
 */
export const batchDeleteFocusRecords = async () => {
  if (selectedRecordIds.value.size === 0) {
    focusError.value = '请先选择要删除的记录'
    return
  }

  const selectedIds = Array.from(selectedRecordIds.value)

  if (!confirm(`确定要删除选中的 ${selectedIds.length} 条专注记录吗？此操作不可撤销！`)) return

  try {
    focusError.value = ''

    for (const recordId of selectedIds) {
      await focusRecordApi.deleteRecord(recordId)
    }

    cancelBatchDelete()
    await fetchFocusRecords()
  } catch (e: any) {
    focusError.value = e.response?.data?.message || e.message || '批量删除失败'
  }
}

/**
 * 获取专注记录状态标签
 * @param status 状态值
 * @returns 状态标签文本
 */
export const getFocusStatusLabel = (status: number) => {
  switch (status) {
    case 0: return '已取消'
    case 1: return '已完成'
    case 2: return '中途放弃'
    default: return '未知'
  }
}

/**
 * 格式化时长（秒）为友好的文本格式
 * @param seconds 秒数
 * @returns 格式化后的时长文本
 */
export const formatDuration = (seconds: number): string => {
  if (seconds < 60) {
    return `${seconds}秒`
  } else if (seconds < 3600) {
    const minutes = Math.floor(seconds / 60)
    const remainingSecs = seconds % 60
    if (remainingSecs === 0) {
      return `${minutes}分钟`
    }
    return `${minutes}分${remainingSecs}秒`
  } else {
    const hours = Math.floor(seconds / 3600)
    const remainingSecs = seconds % 3600
    const minutes = Math.floor(remainingSecs / 60)
    const secs = remainingSecs % 60
    if (minutes === 0 && secs === 0) {
      return `${hours}小时`
    } else if (secs === 0) {
      return `${hours}小时${minutes}分钟`
    }
    return `${hours}小时${minutes}分${secs}秒`
  }
}

/**
 * 格式化日期时间字符串
 * 使用上海时区显示
 * @param dateTimeStr 日期时间字符串
 * @returns 格式化后的日期时间文本
 */
export const formatDateTime = (dateTimeStr: string | undefined): string => {
  if (!dateTimeStr) return '未知'
  try {
    const date = new Date(dateTimeStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      timeZone: 'Asia/Shanghai'
    })
  } catch {
    return dateTimeStr
  }
}
