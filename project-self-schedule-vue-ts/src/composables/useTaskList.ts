import { ref, computed } from 'vue'
import type { TaskVO } from '../types'
import { taskApi } from '../api'

/**
 * 任务列表管理composable
 * 提供任务列表获取、过滤、格式化等功能
 */

// 状态定义
export const tasks = ref<TaskVO[]>([])
export const categories = ref<string[]>([])
export const selectedCategory = ref<string>('')
export const selectedStatuses = ref<number[]>([])
export const selectedPriorities = ref<number[]>([])
export const deadlineBefore = ref<string>('')
export const keyword = ref('')
export const error = ref('')
export const expandedTasks = ref<Set<number>>(new Set())
export const isLoading = ref(false)

// 分页状态
export const currentPage = ref(1)
export const totalPages = ref(1)
export const totalItems = ref(0)

// 状态选项
export const statusOptions = [
  { value: 0, label: '待办', color: '#909399' },
  { value: 1, label: '进行中', color: '#409EFF' },
  { value: 2, label: '已完成', color: '#67C23A' },
  { value: 3, label: '已取消', color: '#F56C6C' },
  { value: 4, label: '已存档', color: '#B37FEB' }
]

export const priorityOptions = [
  { value: 0, label: '低', color: '#22c55e', bgColor: '#dcfce7', borderColor: '#bbf7d0', shadow: '0 2px 8px rgba(34, 197, 94, 0.15)' },
  { value: 1, label: '中', color: '#f59e0b', bgColor: '#fef9c3', borderColor: '#fde047', shadow: '0 2px 8px rgba(245, 158, 11, 0.2)' },
  { value: 2, label: '高', color: '#ef4444', bgColor: '#fee2e2', borderColor: '#fca5a5', shadow: '0 2px 8px rgba(239, 68, 68, 0.25)' }
]

/**
 * 获取任务列表
 */
export const fetchTasks = async () => {
  try {
    isLoading.value = true
    error.value = ''
    const params: Record<string, any> = {}
    
    if (selectedCategory.value) {
      params.category = selectedCategory.value
    }
    if (selectedStatuses.value.length > 0) {
      params.statusList = selectedStatuses.value
    }
    if (selectedPriorities.value.length > 0) {
      params.priorityList = selectedPriorities.value
    }
    if (deadlineBefore.value) {
      params.deadlineBefore = deadlineBefore.value
    }
    if (keyword.value) {
      params.keyword = keyword.value
    }
    
    // 添加分页参数
    params.pageNum = currentPage.value
    params.pageSize = 10
    
    const response = await taskApi.getTasks(params)
    
    if (response.data.code === 200) {
      tasks.value = response.data.data.data
      totalPages.value = response.data.data.pages
      totalItems.value = response.data.data.total
      currentPage.value = response.data.data.pageNum
    } else {
      error.value = response.data.message
    }
  } catch (e: any) {
    console.error('Fetch tasks error:', e)
    error.value = e.response?.data?.message || e.message || '获取任务列表失败'
  } finally {
    isLoading.value = false
  }
}

/**
 * 切换分页
 * @param page 目标页码
 */
export const changePage = (page: number) => {
  currentPage.value = page
  fetchTasks()
}

/**
 * 重置分页到第一页
 */
export const resetPage = () => {
  currentPage.value = 1
}

/**
 * 获取分类列表
 */
export const fetchCategories = async () => {
  try {
    error.value = ''
    const response = await taskApi.getCategories()
    if (response.data.code === 200) {
      categories.value = response.data.data
    } else {
      error.value = response.data.message
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '获取分类列表失败'
  }
}

/**
 * 切换任务展开状态
 * @param taskId 任务ID
 */
export const toggleExpand = (taskId: number) => {
  if (expandedTasks.value.has(taskId)) {
    expandedTasks.value.delete(taskId)
  } else {
    expandedTasks.value.add(taskId)
  }
}

/**
 * 切换状态选择
 * @param status 状态值
 */
export const toggleStatus = (status: number) => {
  const index = selectedStatuses.value.indexOf(status)
  if (index === -1) {
    selectedStatuses.value.push(status)
  } else {
    selectedStatuses.value.splice(index, 1)
  }
  resetPage()
  fetchTasks()
}

/**
 * 切换优先级选择
 * @param priority 优先级值
 */
export const togglePriority = (priority: number) => {
  const index = selectedPriorities.value.indexOf(priority)
  if (index === -1) {
    selectedPriorities.value.push(priority)
  } else {
    selectedPriorities.value.splice(index, 1)
  }
  resetPage()
  fetchTasks()
}

/**
 * 处理分类变更
 * @param categoryId 分类ID
 */
export const handleCategoryChange = (categoryId: string | number) => {
  selectedCategory.value = String(categoryId)
  fetchTasks()
}

/**
 * 处理截止日期变更
 * @param date 日期字符串
 */
export const handleDeadlineChange = (date: string) => {
  // 转换日期格式：将 "YYYY-MM-DD HH:mm" 转换为 "YYYY-MM-DDTHH:mm:ss"
  if (date && !date.includes('T')) {
    const parts = date.split(' ')
    if (parts.length === 2) {
      deadlineBefore.value = `${parts[0]}T${parts[1]}:00`
    } else {
      deadlineBefore.value = `${date}T23:59:00`
    }
  } else {
    deadlineBefore.value = date
  }
  fetchTasks()
}

/**
 * 处理关键词搜索
 * @param searchKeyword 搜索关键词
 */
export const handleKeywordSearch = (searchKeyword: string) => {
  keyword.value = searchKeyword
  fetchTasks()
}

/**
 * 判断任务是否只有提醒时间（无截止时间）
 * @param task 任务对象
 * @returns 是否为仅提醒任务
 */
export const isReminderOnly = (task: TaskVO): boolean => {
  return !task.deadline && !!task.remindTime
}

/**
 * 格式化预估时间（秒）为友好格式
 * @param seconds 秒数
 * @returns 格式化后的时间文本
 */
export const formatEstimatedTime = (seconds: number): string => {
  if (!seconds || seconds <= 0) return ''
  
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  
  const parts: string[] = []
  if (days > 0) parts.push(`${days}天`)
  if (hours > 0) parts.push(`${hours}小时`)
  if (minutes > 0) parts.push(`${minutes}分钟`)
  if (secs > 0) parts.push(`${secs}秒`)
  
  return parts.join(' ')
}

/**
 * 格式化截止日期
 * @param deadline 截止日期字符串
 * @returns 格式化后的日期文本
 */
export const formatDeadline = (deadline: string | undefined): string => {
  if (!deadline) return ''
  try {
    const date = new Date(deadline)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch {
    return deadline
  }
}

/**
 * 格式化提醒时间
 * @param remindTime 提醒时间字符串
 * @returns 格式化后的时间文本
 */
export const formatRemindTime = (remindTime: string | undefined): string => {
  if (!remindTime) return ''
  try {
    const date = new Date(remindTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      timeZone: 'Asia/Shanghai'
    })
  } catch {
    return remindTime
  }
}

/**
 * 格式化时间（仅显示时分）
 * @param datetime 日期时间字符串
 * @returns 格式化后的时间文本（如：09:00）
 */
export const formatTimeOnly = (datetime: string | undefined): string => {
  if (!datetime) return ''
  try {
    const date = new Date(datetime)
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      timeZone: 'Asia/Shanghai'
    })
  } catch {
    return datetime
  }
}

/**
 * 获取优先级信息
 * @param priority 优先级值
 * @returns 优先级配置对象
 */
export const getPriorityInfo = (priority: number) => {
  return priorityOptions.find(p => p.value === priority) || priorityOptions[0]
}

/**
 * 获取状态标签
 * @param status 状态值
 * @returns 状态标签文本
 */
export const getStatusLabel = (status: number) => {
  return statusOptions.find(s => s.value === status)?.label || '未知'
}

/**
 * 获取优先级标签
 * @param priority 优先级值
 * @returns 优先级标签文本
 */
export const getPriorityLabel = (priority: number) => {
  return priorityOptions.find(p => p.value === priority)?.label || '未知'
}

/**
 * 获取任务标签列表
 * @param task 任务对象
 * @returns 标签字符串数组
 */
export const getTaskTags = (task: TaskVO): string[] => {
  if (!task.tags) return []
  try {
    const parsed = typeof task.tags === 'string' ? JSON.parse(task.tags) : task.tags
    if (!Array.isArray(parsed)) return []
    return parsed.filter((t: any) => t != null && String(t).trim() !== '')
  } catch {
    return []
  }
}