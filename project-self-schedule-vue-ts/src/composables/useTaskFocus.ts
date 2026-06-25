import { useStore } from 'vuex'
import type { TaskVO } from '../types'
import { focusRecordApi } from '../api'
import { focusError, fetchFocusRecords } from './useFocusRecord'

export function useTaskFocus() {
  const store = useStore()

  const startFocusFromTask = async (task: TaskVO) => {
    try {
      if (task.estimatedSeconds) {
        // 设置任务和时间
        store.commit('focus/SET_SELECTED_TASK', task)
        store.commit('focus/UPDATE_FOCUS_TIME', task.estimatedSeconds)
        store.commit('focus/SET_START_TOTAL_SECONDS', task.estimatedSeconds)
      }

      // 如果有任务ID，先调用开始专注API
      if (task.id) {
        await focusRecordApi.startFocus(task.id)
      }

      const remainingSeconds = task.estimatedSeconds || 25 * 60
      const startTotalSeconds = task.estimatedSeconds || 25 * 60
      const currentStartTime = new Date().toISOString()

      store.commit('focus/START_FOCUS', {
        remainingSeconds,
        startTotalSeconds,
        selectedTask: task,
        startTime: currentStartTime
      })

      return true
    } catch (e) {
      console.error('Failed to start focus from task:', e)
      focusError.value = '启动专注失败'
      return false
    }
  }

  return {
    startFocusFromTask
  }
}
