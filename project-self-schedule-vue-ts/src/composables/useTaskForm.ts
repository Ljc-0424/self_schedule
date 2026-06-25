import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { TaskVO } from '../types'
import { taskApi } from '../api'
import { tasks, error, fetchTasks, fetchCategories, expandedTasks } from './useTaskList'

/**
 * 任务表单管理composable
 * 提供任务创建、编辑、表单状态管理等功能
 */

// 弹窗状态
export const showAddModal = ref(false)
export const showEditModal = ref(false)
export const showChildModal = ref(false)
export const isSaving = ref(false)

// 预估时间（拆分输入）- 支持天、时、分、秒输入
export const estimatedDays = ref(0)
export const estimatedHours = ref(0)
export const estimatedMinutes = ref(0)
export const estimatedSeconds = ref(0)

// 任务表单数据
export const taskForm = ref({
  title: '',
  description: '',
  priority: 1,
  deadline: '',           // 截止时间（有DDL才填）
  remindTime: '',         // 提醒时间（纯日程提醒）
  estimatedSeconds: 0,    // 预估时间（秒）
  category: '',
  tags: '',
  reminderConfig: '',
  recurrenceRule: '',      // 重复规则
  recurrenceEndDate: ''    // 重复结束日期
})

// 提醒配置表单
export const reminderForm = ref({
  enable: false,
  advanceMinutes: [] as number[]
})

// 自定义提前提醒时间输入（分钟）
export const customAdvanceMinutes = ref<number | null>(null)

// 提醒配置错误信息
export const reminderError = ref('')

// 编辑中的任务
export const editingTask = ref<TaskVO | null>(null)

/** 提醒提前时间选项（分钟） */
export const advanceMinuteOptions = [
  { value: 5, label: '5分钟' },
  { value: 15, label: '15分钟' },
  { value: 30, label: '30分钟' },
  { value: 60, label: '1小时' },
  { value: 120, label: '2小时' },
  { value: 360, label: '6小时' },
  { value: 1440, label: '1天' },
  { value: 2880, label: '2天' },
  { value: 10080, label: '7天' }
]

/** 重复选项 */
export const repeatOptions = [
  { value: 'none', label: '不重复' },
  { value: 'DAILY:1', label: '每天' },
  { value: 'DAILY:2', label: '每2天' },
  { value: 'DAILY:7', label: '每7天' },
  { value: 'WEEKLY:1', label: '每周' },
  { value: 'MONTHLY:1', label: '每月' },
  { value: 'YEARLY:1', label: '每年' }
]

/** 星期选项 */
export const weekDayOptions = [
  { value: '1', label: '周一' },
  { value: '2', label: '周二' },
  { value: '3', label: '周三' },
  { value: '4', label: '周四' },
  { value: '5', label: '周五' },
  { value: '6', label: '周六' },
  { value: '7', label: '周日' }
]

/** 重复规则表单数据 */
export const recurrenceForm = ref({
  selectedDays: [] as string[],
  selectedDate: ''
})

/**
 * 计算总预估秒数
 * 将天、时、分、秒转换为总秒数
 * @returns 总秒数
 */
export const calculateTotalEstimatedSeconds = (): number => {
  const daysInSeconds = estimatedDays.value * 24 * 60 * 60
  const hoursInSeconds = estimatedHours.value * 60 * 60
  const minutesInSeconds = estimatedMinutes.value * 60
  return daysInSeconds + hoursInSeconds + minutesInSeconds + estimatedSeconds.value
}

/**
 * 将总秒数拆分为天、时、分、秒
 * @param totalSeconds 总秒数
 */
export const splitEstimatedSeconds = (totalSeconds: number) => {
  estimatedDays.value = Math.floor(totalSeconds / (24 * 60 * 60))
  const remainingAfterDays = totalSeconds % (24 * 60 * 60)
  estimatedHours.value = Math.floor(remainingAfterDays / (60 * 60))
  const remainingAfterHours = remainingAfterDays % (60 * 60)
  estimatedMinutes.value = Math.floor(remainingAfterHours / 60)
  estimatedSeconds.value = remainingAfterHours % 60
}

/**
 * 切换提醒提前时间选项
 * @param minute 分钟数
 */
export const toggleAdvanceMinute = (minute: number) => {
  reminderError.value = ''
  
  const index = reminderForm.value.advanceMinutes.indexOf(minute)
  if (index > -1) {
    reminderForm.value.advanceMinutes.splice(index, 1)
  } else {
    // 验证时间合理性
    const validation = validateAdvanceTime(minute)
    if (!validation.valid) {
      reminderError.value = validation.message
      return
    }
    reminderForm.value.advanceMinutes.push(minute)
  }
}

/**
 * 验证提前提醒时间是否合理
 * @param advanceMinutes 提前提醒时间（分钟）
 * @returns 是否合理，以及错误信息
 */
export const validateAdvanceTime = (advanceMinutes: number): { valid: boolean; message: string } => {
  // 获取提醒时间
  const remindTimeStr = taskForm.value.remindTime
  const deadlineStr = taskForm.value.deadline
  
  // 如果没有设置提醒时间和截止时间，无法验证
  if (!remindTimeStr && !deadlineStr) {
    return { valid: true, message: '' }
  }
  
  // 使用提醒时间或截止时间
  const targetTimeStr = remindTimeStr || deadlineStr
  const targetTime = new Date(targetTimeStr)
  const now = new Date()
  
  // 计算从现在到目标时间的分钟数
  const diffMinutes = Math.floor((targetTime.getTime() - now.getTime()) / (1000 * 60))
  
  // 验证提前时间是否合理
  if (advanceMinutes >= diffMinutes) {
    return { 
      valid: false, 
      message: `设置的提前时间（${advanceMinutes}分钟）不能大于等于到提醒时间的间隔（${diffMinutes}分钟）` 
    }
  }
  
  return { valid: true, message: '' }
}

/**
 * 添加自定义提前提醒时间（分钟）
 */
export const addCustomAdvanceMinute = () => {
  reminderError.value = ''
  
  if (customAdvanceMinutes.value && customAdvanceMinutes.value > 0) {
    const minute = customAdvanceMinutes.value
    
    // 验证时间合理性
    const validation = validateAdvanceTime(minute)
    if (!validation.valid) {
      reminderError.value = validation.message
      return
    }
    
    if (!reminderForm.value.advanceMinutes.includes(minute)) {
      reminderForm.value.advanceMinutes.push(minute)
    }
    customAdvanceMinutes.value = null
  }
}

/**
 * 构建提醒配置JSON字符串
 * @returns 提醒配置JSON字符串
 */
export const buildReminderConfig = () => {
  return JSON.stringify({
    enable: reminderForm.value.enable,
    advance_minutes: reminderForm.value.advanceMinutes
  })
}

/**
 * 解析提醒配置JSON字符串
 * @param config 提醒配置JSON字符串
 */
export const parseReminderConfig = (config: string) => {
  if (!config) {
    reminderForm.value = {
      enable: false,
      advanceMinutes: []
    }
    return
  }
  try {
    const obj = JSON.parse(config)
    // 兼容旧格式（advance_hours）和新格式（advance_minutes）
    const advanceMinutes = obj.advance_minutes || (obj.advance_hours ? obj.advance_hours.map((h: number) => h * 60) : [])
    reminderForm.value = {
      enable: obj.enable !== false,
      advanceMinutes: advanceMinutes
    }
  } catch {
    reminderForm.value = {
      enable: false,
      advanceMinutes: []
    }
  }
}

/**
 * 切换星期选择
 * @param day 星期值
 */
export const toggleWeekDay = (day: string) => {
  const index = recurrenceForm.value.selectedDays.indexOf(day)
  if (index > -1) {
    recurrenceForm.value.selectedDays.splice(index, 1)
  } else {
    recurrenceForm.value.selectedDays.push(day)
    recurrenceForm.value.selectedDays.sort()
  }
}

/**
 * 构建重复规则字符串
 * @returns 重复规则字符串，如 "DAILY:1", "WEEKLY:1:1,3,5", "MONTHLY:1:15", "YEARLY:1:6-15"
 */
export const buildRecurrenceRule = (): string => {
  const rule = taskForm.value.recurrenceRule
  if (!rule || rule === 'none') return ''
  
  const parts = rule.split(':')
  if (parts.length < 2) return rule
  
  const type = parts[0]
  const interval = parts[1]
  
  // WEEKLY类型需要添加星期参数
  if (type === 'WEEKLY') {
    if (recurrenceForm.value.selectedDays.length > 0) {
      return `${type}:${interval}:${recurrenceForm.value.selectedDays.join(',')}`
    }
    return rule
  }
  
  // MONTHLY类型需要添加日期参数
  if (type === 'MONTHLY') {
    if (recurrenceForm.value.selectedDate) {
      // 提取日期部分（去掉年份和月份，只保留日期）
      const dateStr = recurrenceForm.value.selectedDate
      const day = dateStr.split('-')[2]
      if (day) {
        return `${type}:${interval}:${day}`
      }
    }
    return rule
  }
  
  // YEARLY类型需要添加日期参数（格式：月-日）
  if (type === 'YEARLY') {
    if (recurrenceForm.value.selectedDate) {
      // 提取月-日部分
      const dateParts = recurrenceForm.value.selectedDate.split('-')
      if (dateParts.length === 3) {
        return `${type}:${interval}:${dateParts[1]}-${dateParts[2]}`
      }
    }
    return rule
  }
  
  return rule
}

/**
 * 解析重复规则字符串
 * @param rule 重复规则字符串
 */
export const parseRecurrenceRule = (rule: string) => {
  recurrenceForm.value = {
    selectedDays: [],
    selectedDate: ''
  }
  
  if (!rule) return
  
  const parts = rule.split(':')
  if (parts.length < 2) return
  
  const type = parts[0]
  const params = parts.length > 2 ? parts[2] : ''
  
  // WEEKLY类型解析星期
  if (type === 'WEEKLY' && params) {
    recurrenceForm.value.selectedDays = params.split(',').sort()
  }
  
  // MONTHLY类型解析日期
  if (type === 'MONTHLY' && params) {
    // 补全年月格式：假设当前年月
    const today = new Date()
    const year = today.getFullYear()
    const month = String(today.getMonth() + 1).padStart(2, '0')
    recurrenceForm.value.selectedDate = `${year}-${month}-${params.padStart(2, '0')}`
  }
  
  // YEARLY类型解析日期（格式：月-日）
  if (type === 'YEARLY' && params) {
    // 补全年份：使用当前年份
    const today = new Date()
    const year = today.getFullYear()
    recurrenceForm.value.selectedDate = `${year}-${params}`
  }
}

/**
 * 打开添加任务弹窗
 */
export const openAddModal = () => {
  editingTask.value = null
  taskForm.value = {
    title: '',
    description: '',
    priority: 1,
    deadline: '',
    remindTime: '',
    estimatedSeconds: 0,
    category: '',
    tags: '',
    reminderConfig: '',
    recurrenceRule: '',
    recurrenceEndDate: ''
  }
  // 重置拆分的时间输入
  estimatedDays.value = 0
  estimatedHours.value = 0
  estimatedMinutes.value = 0
  estimatedSeconds.value = 0
  reminderForm.value = { enable: false, advanceMinutes: [] }
  recurrenceForm.value = { selectedDays: [], selectedDate: '' }
  reminderError.value = ''
  customAdvanceMinutes.value = null
  showAddModal.value = true
}

/**
 * 打开编辑任务弹窗
 * @param task 要编辑的任务
 */
export const openEditModal = (task: TaskVO) => {
  editingTask.value = task
  taskForm.value = {
    title: task.title,
    description: task.description || '',
    priority: task.priority,
    deadline: task.deadline || '',
    remindTime: task.remindTime || '',
    estimatedSeconds: task.estimatedSeconds || 0,
    category: task.category || '',
    tags: task.tags?.join(',') || '',
    reminderConfig: task.reminderConfig || '',
    recurrenceRule: task.recurrenceRule || '',
    recurrenceEndDate: task.recurrenceEndDate || ''
  }
  // 拆分预估时间（秒）
  splitEstimatedSeconds(task.estimatedSeconds || 0)
  parseReminderConfig(task.reminderConfig || '')
  parseRecurrenceRule(task.recurrenceRule || '')
  showEditModal.value = true
}

/**
 * 关闭弹窗
 */
export const closeModal = () => {
  showAddModal.value = false
  showEditModal.value = false
  showChildModal.value = false
  editingTask.value = null
  estimatedDays.value = 0
  estimatedHours.value = 0
  estimatedMinutes.value = 0
  estimatedSeconds.value = 0
  reminderForm.value = { enable: false, advanceMinutes: [] }
  recurrenceForm.value = { selectedDays: [], selectedDate: '' }
  reminderError.value = ''
  customAdvanceMinutes.value = null
}

/**
 * 保存任务
 * @param isEdit 是否为编辑模式
 */
export const saveTask = async (isEdit: boolean) => {
  try {
    error.value = ''
    // 必填项验证
    if (!taskForm.value.title || taskForm.value.title.trim() === '') {
      error.value = '请输入任务标题'
      ElMessage.warning('请输入任务标题')
      isSaving.value = false
      return
    }
    isSaving.value = true
    const tagsArray = taskForm.value.tags 
      ? taskForm.value.tags.split(',').map((t: string) => t.trim()).filter((t: string) => t) 
      : []
    
    const totalEstimatedSeconds = calculateTotalEstimatedSeconds()
    const recurrenceRule = buildRecurrenceRule()
    
    const body = {
      title: taskForm.value.title,
      description: taskForm.value.description || undefined,
      priority: taskForm.value.priority,
      deadline: taskForm.value.deadline || null,
      remindTime: taskForm.value.remindTime || null,
      estimatedSeconds: totalEstimatedSeconds,
      category: taskForm.value.category || '',
      tags: tagsArray.length > 0 ? tagsArray : undefined,
      reminderConfig: buildReminderConfig(),
      recurrenceRule: recurrenceRule || undefined,
      recurrenceEndDate: taskForm.value.recurrenceEndDate || null
    }
    
    let response
    
    if (isEdit && editingTask.value) {
      response = await taskApi.updateTask(editingTask.value.id, body)
      if (response.data.code === 200) {
        localStorage.removeItem('focusState')
        await fetchTasks()
        await fetchCategories()
        closeModal()
      } else {
        error.value = response.data.message
      }
    } else {
      response = await taskApi.createTask(body)
      if (response.data.code === 200) {
        localStorage.removeItem('focusState')
        const newTask = response.data.data
        if (newTask && tasks.value.length > 0) {
          tasks.value.unshift(newTask)
        }
        await fetchTasks()
        await fetchCategories()
        closeModal()
      } else {
        error.value = response.data.message
      }
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '保存失败'
  } finally {
    isSaving.value = false
  }
}