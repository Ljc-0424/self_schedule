import type { TaskVO } from '../../types'

export interface FocusState {
  isFocusing: boolean
  isPaused: boolean
  remainingSeconds: number
  startTotalSeconds: number
  selectedTask: TaskVO | null
  focusNotes: string
  startTime: string | null
  interruptions: number
  focusCompleted: boolean
  completedDuration: number
  lastTickTime: number | null  // 新增：上次计时时间戳
  version: number              // 新增：状态版本号
}

const STORAGE_KEY = 'focusState'
const STATE_VERSION = 2  // 状态版本，用于数据迁移

const state: FocusState = {
  isFocusing: false,
  isPaused: false,
  remainingSeconds: 0,
  startTotalSeconds: 0,
  selectedTask: null,
  focusNotes: '',
  startTime: null,
  interruptions: 0,
  focusCompleted: false,
  completedDuration: 0,
  lastTickTime: null,
  version: STATE_VERSION
}

/**
 * 保存状态到localStorage
 */
function saveToStorage(state: FocusState) {
  try {
    const stateToSave = {
      ...state,
      lastTickTime: Date.now()
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(stateToSave))
  } catch (e) {
    console.error('保存专注状态失败:', e)
  }
}

/**
 * 从localStorage恢复状态
 */
function loadFromStorage(): FocusState | null {
  try {
    const savedState = localStorage.getItem(STORAGE_KEY)
    if (!savedState) return null

    const parsed = JSON.parse(savedState) as FocusState

    // 版本检查
    if (!parsed.version || parsed.version < STATE_VERSION) {
      localStorage.removeItem(STORAGE_KEY)
      return null
    }

    // 检查是否过期（超过24小时）
    if (parsed.lastTickTime && Date.now() - parsed.lastTickTime > 24 * 60 * 60 * 1000) {
      localStorage.removeItem(STORAGE_KEY)
      return null
    }

    return parsed
  } catch (e) {
    console.error('读取专注状态失败:', e)
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

/**
 * 计算实际剩余时间
 */
function calculateActualRemainingSeconds(savedState: FocusState): number {
  if (!savedState.isFocusing || !savedState.lastTickTime) {
    return savedState.remainingSeconds
  }

  const now = Date.now()
  const elapsed = Math.floor((now - savedState.lastTickTime) / 1000)

  // 如果是暂停状态，不扣除时间
  if (savedState.isPaused) {
    return savedState.remainingSeconds
  }

  // 计算实际剩余时间
  const actualRemaining = savedState.remainingSeconds - elapsed
  return Math.max(0, actualRemaining)
}

const mutations = {
  START_FOCUS(state: FocusState, payload: { remainingSeconds: number; startTotalSeconds: number; selectedTask: TaskVO | null; startTime?: string }) {
    state.isFocusing = true
    state.isPaused = false
    state.remainingSeconds = payload.remainingSeconds
    state.startTotalSeconds = payload.startTotalSeconds
    state.selectedTask = payload.selectedTask
    state.startTime = payload.startTime || new Date().toISOString()
    state.interruptions = 0
    state.lastTickTime = Date.now()
    state.version = STATE_VERSION
    saveToStorage(state)
  },

  UPDATE_FOCUS_TIME(state: FocusState, remainingSeconds: number) {
    state.remainingSeconds = remainingSeconds
    state.lastTickTime = Date.now()
    saveToStorage(state)
  },

  TICK_FOCUS(state: FocusState) {
    if (state.isFocusing && !state.isPaused) {
      state.remainingSeconds = Math.max(0, state.remainingSeconds - 1)
      state.lastTickTime = Date.now()

      // 每10秒保存一次，减少IO
      if (state.remainingSeconds % 10 === 0) {
        saveToStorage(state)
      }
    }
  },

  STOP_FOCUS(state: FocusState) {
    state.isFocusing = false
    state.isPaused = false
    state.remainingSeconds = 0
    state.startTotalSeconds = 0
    state.selectedTask = null
    state.focusNotes = ''
    state.startTime = null
    state.interruptions = 0
    state.lastTickTime = null
    localStorage.removeItem(STORAGE_KEY)
  },

  FOCUS_COMPLETED(state: FocusState, duration: number) {
    state.isFocusing = false
    state.isPaused = false
    state.remainingSeconds = 0
    state.focusCompleted = true
    state.completedDuration = duration
    state.selectedTask = null
    state.focusNotes = ''
    state.startTime = null
    state.interruptions = 0
    state.lastTickTime = null
    localStorage.removeItem(STORAGE_KEY)
  },

  DISMISS_FOCUS_COMPLETED(state: FocusState) {
    state.focusCompleted = false
    state.completedDuration = 0
  },

  PAUSE_FOCUS(state: FocusState) {
    state.isPaused = true
    state.lastTickTime = Date.now()
    saveToStorage(state)
  },

  RESUME_FOCUS(state: FocusState) {
    state.isPaused = false
    state.lastTickTime = Date.now()
    saveToStorage(state)
  },

  ADD_INTERRUPTION(state: FocusState) {
    state.interruptions++
    saveToStorage(state)
  },

  SET_FOCUS_NOTES(state: FocusState, notes: string) {
    state.focusNotes = notes
  },

  SET_SELECTED_TASK(state: FocusState, task: TaskVO | null) {
    state.selectedTask = task
  },

  SET_START_TOTAL_SECONDS(state: FocusState, seconds: number) {
    state.startTotalSeconds = seconds
  },

  RESTORE_FOCUS_STATE(state: FocusState, focusState: FocusState) {
    state.isFocusing = focusState.isFocusing
    state.isPaused = focusState.isPaused
    state.remainingSeconds = focusState.remainingSeconds
    state.startTotalSeconds = focusState.startTotalSeconds
    state.selectedTask = focusState.selectedTask
    state.focusNotes = focusState.focusNotes
    state.startTime = focusState.startTime
    state.interruptions = focusState.interruptions || 0
    state.lastTickTime = focusState.lastTickTime
    state.version = focusState.version || STATE_VERSION
  },

  CLEAR_FOCUS_NOTES(state: FocusState) {
    state.focusNotes = ''
  }
}

const actions = {
  startFocus({ commit }: { commit: Function }, payload: { remainingSeconds: number; startTotalSeconds: number; selectedTask: TaskVO | null; startTime?: string }) {
    commit('START_FOCUS', payload)
  },

  updateFocusTime({ commit }: { commit: Function }, remainingSeconds: number) {
    commit('UPDATE_FOCUS_TIME', remainingSeconds)
  },

  tickFocus({ commit }: { commit: Function }) {
    commit('TICK_FOCUS')
  },

  stopFocus({ commit }: { commit: Function }) {
    commit('STOP_FOCUS')
  },

  pauseFocus({ commit }: { commit: Function }) {
    commit('PAUSE_FOCUS')
  },

  resumeFocus({ commit }: { commit: Function }) {
    commit('RESUME_FOCUS')
  },

  addInterruption({ commit }: { commit: Function }) {
    commit('ADD_INTERRUPTION')
  },

  setFocusNotes({ commit }: { commit: Function }, notes: string) {
    commit('SET_FOCUS_NOTES', notes)
  },

  setSelectedTask({ commit }: { commit: Function }, task: TaskVO | null) {
    commit('SET_SELECTED_TASK', task)
  },

  /**
   * 尝试恢复专注状态
   */
  tryRestoreFocusState({ commit }: { commit: Function }): boolean {
    const savedState = loadFromStorage()

    if (savedState && savedState.isFocusing && savedState.remainingSeconds > 0) {
      // 计算实际剩余时间
      const actualRemaining = calculateActualRemainingSeconds(savedState)

      if (actualRemaining > 0) {
        // 恢复状态并修正剩余时间
        const restoredState = {
          ...savedState,
          remainingSeconds: actualRemaining,
          lastTickTime: Date.now()
        }

        commit('RESTORE_FOCUS_STATE', restoredState)
        saveToStorage(restoredState)
        return true
      } else {
        // 专注时间已过，清除状态
        localStorage.removeItem(STORAGE_KEY)
        return false
      }
    }

    return false
  }
}

const getters = {
  isFocusing: (state: FocusState) => state.isFocusing,
  isPaused: (state: FocusState) => state.isPaused,
  focusRemainingSeconds: (state: FocusState) => state.remainingSeconds,
  focusStartTotalSeconds: (state: FocusState) => state.startTotalSeconds,
  focusSelectedTask: (state: FocusState) => state.selectedTask,
  focusNotes: (state: FocusState) => state.focusNotes,
  focusStartTime: (state: FocusState) => state.startTime,
  focusInterruptions: (state: FocusState) => state.interruptions,
  focusCompleted: (state: FocusState) => state.focusCompleted,
  completedDuration: (state: FocusState) => state.completedDuration,

  /**
   * 计算专注进度百分比
   */
  focusProgress: (state: FocusState) => {
    if (state.startTotalSeconds === 0) return 0
    return Math.round(((state.startTotalSeconds - state.remainingSeconds) / state.startTotalSeconds) * 100)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
