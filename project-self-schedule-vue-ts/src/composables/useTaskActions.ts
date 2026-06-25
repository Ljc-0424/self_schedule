import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { taskApi } from '../api'
import { error, fetchTasks, tasks, selectedStatuses } from './useTaskList'

/**
 * 批量删除状态管理
 */
export const isTaskBatchDeleting = ref(false)
export const selectedTaskIds = ref<Set<number>>(new Set())

/**
 * 任务操作composable
 * 提供任务完成、删除、级联操作等功能
 */

/**
 * 切换批量删除模式
 */
export const toggleTaskBatchDeleting = () => {
  isTaskBatchDeleting.value = !isTaskBatchDeleting.value
  if (!isTaskBatchDeleting.value) {
    selectedTaskIds.value.clear()
  }
}

/**
 * 取消批量删除模式
 */
export const cancelTaskBatchDelete = () => {
  isTaskBatchDeleting.value = false
  selectedTaskIds.value.clear()
}

/**
 * 切换任务勾选状态
 */
export const toggleTaskSelection = (taskId: number) => {
  if (selectedTaskIds.value.has(taskId)) {
    selectedTaskIds.value.delete(taskId)
  } else {
    selectedTaskIds.value.add(taskId)
  }
}

/**
 * 全选/取消全选
 */
export const toggleSelectAll = () => {
  if (selectedTaskIds.value.size === tasks.value.length) {
    selectedTaskIds.value.clear()
  } else {
    tasks.value.forEach(task => selectedTaskIds.value.add(task.id))
  }
}

/**
 * 完成任务
 * @param taskId 任务ID
 */
export const completeTask = async (taskId: number) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (!task) return

  const prevStatus = task.status
  task.status = 2

  try {
    error.value = ''
    const response = await taskApi.completeTask(taskId)
    if (response.data.code !== 200) {
      task.status = prevStatus
      error.value = response.data.message
    } else {
      if (selectedStatuses.value.length > 0 && !selectedStatuses.value.includes(2)) {
        tasks.value = tasks.value.filter(t => t.id !== taskId)
      }
    }
  } catch (e: any) {
    task.status = prevStatus
    error.value = e.response?.data?.message || e.message || '操作失败'
  }
}

/**
 * 撤销任务状态
 * - 已存档(4) → 完成(2)
 * - 已取消(3) → 待办(0)
 * - 完成(2) → 待办(0)
 * @param taskId 任务ID
 */
export const undoTask = async (taskId: number) => {
  const task = tasks.value.find(t => t.id === taskId)
  if (!task) return

  const prevStatus = task.status
  let newStatus = prevStatus
  if (task.status === 4) newStatus = 2
  else if (task.status === 3 || task.status === 2) newStatus = 0
  task.status = newStatus

  try {
    error.value = ''
    const response = await taskApi.undoTask(taskId)
    if (response.data.code !== 200) {
      task.status = prevStatus
      error.value = response.data.message
    } else {
      if (selectedStatuses.value.length > 0 && !selectedStatuses.value.includes(newStatus)) {
        tasks.value = tasks.value.filter(t => t.id !== taskId)
      } else if (selectedStatuses.value.length === 0 && newStatus !== 0 && newStatus !== 1) {
        tasks.value = tasks.value.filter(t => t.id !== taskId)
      }
    }
  } catch (e: any) {
    task.status = prevStatus
    error.value = e.response?.data?.message || e.message || '撤销失败'
  }
}

/**
 * 删除任务（逻辑删除）
 * @param taskId 任务ID
 */
export const deleteTask = async (taskId: number) => {
  try {
    await ElMessageBox.confirm('确定删除此任务？', '删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
  } catch { return }

  const task = tasks.value.find(t => t.id === taskId)
  if (!task) return

  const prevStatus = task.status
  const newStatus: number = task.status === 2 ? 4 : 3
  task.status = newStatus

  try {
    error.value = ''
    const response = await taskApi.deleteTask(taskId)
    if (response.data.code === 200) {
      localStorage.removeItem('focusState')
      if (selectedStatuses.value.length > 0 && !selectedStatuses.value.includes(newStatus)) {
        tasks.value = tasks.value.filter(t => t.id !== taskId)
      } else if (selectedStatuses.value.length === 0 && newStatus !== 0 && newStatus !== 1) {
        tasks.value = tasks.value.filter(t => t.id !== taskId)
      }
    } else {
      task.status = prevStatus
      error.value = response.data.message
    }
  } catch (e: any) {
    task.status = prevStatus
    error.value = e.response?.data?.message || e.message || '删除失败'
  }
}

/**
 * 彻底删除任务（物理删除）
 * @param taskId 任务ID
 */
export const forceDeleteTask = async (taskId: number) => {
  try {
    await ElMessageBox.confirm('确定彻底删除此任务？此操作不可撤销！', '彻底删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'error' })
  } catch { return }

  try {
    error.value = ''
    const response = await taskApi.forceDeleteTask(taskId)
    if (response.data.code === 200) {
      // 清除可能存在的专注状态（防止恢复已删除任务的状态）
      localStorage.removeItem('focusState')
      await fetchTasks()
    } else {
      error.value = response.data.message
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '删除失败'
  }
}

/**
 * 带重试的请求函数
 */
const requestWithRetry = async <T>(fn: () => Promise<T>, maxRetries: number = 2, delay: number = 1000): Promise<T> => {
  let lastError: any
  for (let i = 0; i <= maxRetries; i++) {
    try {
      return await fn()
    } catch (e: any) {
      lastError = e
      // 网络错误或超时才重试
      if (e.code === 'ERR_NETWORK' || e.code === 'ECONNREFUSED' || e.message?.includes('timeout')) {
        if (i < maxRetries) {
          await new Promise(resolve => setTimeout(resolve, delay * Math.pow(2, i)))
          continue
        }
      }
      throw e
    }
  }
  throw lastError
}

/**
 * 并发执行函数（限制并发数）
 */
const executeWithConcurrency = async <T>(
  tasks: (() => Promise<T>)[],
  concurrency: number = 3
): Promise<T[]> => {
  const results: T[] = []
  const executing: Promise<void>[] = []

  for (const task of tasks) {
    const promise = task().then(result => {
      results.push(result)
    })
    executing.push(promise)

    if (executing.length >= concurrency) {
      await Promise.race(executing)
    }
  }

  await Promise.all(executing)
  return results
}

/**
 * 批量删除任务
 * 删除逻辑：
 * - 待办任务 (status 0): 改为已取消 (status 3)
 * - 已完成任务 (status 2): 改为已存档 (status 4) - 逻辑删除
 * - 已存档/已取消任务 (status 3/4): 物理删除
 */
export const batchDeleteTasks = async () => {
  if (selectedTaskIds.value.size === 0) {
    error.value = '请先选择要删除的任务'
    return
  }

  const selectedIds = Array.from(selectedTaskIds.value)

  // 获取选中任务的状态
  const tasksToDelete = tasks.value.filter(task => selectedIds.includes(task.id))

  const pendingCount = tasksToDelete.filter(t => t.status === 0).length
  const completedCount = tasksToDelete.filter(t => t.status === 2).length
  const archivedCanceledCount = tasksToDelete.filter(t => t.status === 3 || t.status === 4).length

  let confirmMessage = `确定要删除选中的 ${selectedIds.length} 个任务吗？<br/>`
  if (pendingCount > 0) {
    confirmMessage += `· ${pendingCount} 个待办任务将被标记为已取消<br/>`
  }
  if (completedCount > 0) {
    confirmMessage += `· ${completedCount} 个已完成任务将被存档<br/>`
  }
  if (archivedCanceledCount > 0) {
    confirmMessage += `· ${archivedCanceledCount} 个任务将被彻底删除（不可恢复）<br/>`
  }

  try {
    await ElMessageBox.confirm(confirmMessage, '批量删除确认', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning', dangerouslyUseHTMLString: true })
  } catch { return }

  try {
    error.value = ''

    // 构建删除任务列表（带重试机制）
    const deleteTasks: (() => Promise<void>)[] = []
    for (const taskId of selectedIds) {
      deleteTasks.push(async () => {
        await requestWithRetry(() => taskApi.deleteTask(taskId))
      })
    }

    // 限制并发数为3，避免过多请求导致网络问题
    await executeWithConcurrency(deleteTasks, 3)

    localStorage.removeItem('focusState')
    cancelTaskBatchDelete()
    await fetchTasks()
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '批量删除失败'
  }
}