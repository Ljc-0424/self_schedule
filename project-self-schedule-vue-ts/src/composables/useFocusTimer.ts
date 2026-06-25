import { computed } from 'vue'
import type { Store } from 'vuex'
import { focusRecordApi, taskApi } from '../api'
import { focusError, fetchFocusRecords } from './useFocusRecord'

export function useFocusTimer(store: Store<any>) {
  const isFocusing = computed(() => store.getters['focus/isFocusing'])
  const isPaused = computed(() => store.getters['focus/isPaused'])
  const remainingSeconds = computed(() => store.getters['focus/focusRemainingSeconds'])
  const startTotalSeconds = computed(() => store.getters['focus/focusStartTotalSeconds'])
  const selectedTask = computed(() => store.getters['focus/focusSelectedTask'])
  const focusNotes = computed(() => store.getters['focus/focusNotes'])
  const startTime = computed(() => store.getters['focus/focusStartTime'])
  const interruptions = computed(() => store.getters['focus/focusInterruptions'])

  const formattedTime = computed(() => {
    const hours = Math.floor(remainingSeconds.value / 3600)
    const minutes = Math.floor((remainingSeconds.value % 3600) / 60)
    const seconds = remainingSeconds.value % 60
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
  })

  const startFocus = async () => {
    if (selectedTask.value) {
      try {
        await focusRecordApi.startFocus(selectedTask.value.id)
      } catch (e) {
        console.error('Failed to start focus:', e)
      }
    }

    let currentRemainingSeconds = remainingSeconds.value
    let currentStartTotalSeconds = startTotalSeconds.value

    if (!selectedTask.value && remainingSeconds.value === 0) {
      currentRemainingSeconds = 25 * 60
      currentStartTotalSeconds = 25 * 60
    } else if (currentStartTotalSeconds === 0) {
      currentStartTotalSeconds = currentRemainingSeconds
    }

    const currentStartTime = new Date().toISOString()

    store.commit('focus/START_FOCUS', {
      remainingSeconds: currentRemainingSeconds,
      startTotalSeconds: currentStartTotalSeconds,
      selectedTask: selectedTask.value,
      startTime: currentStartTime
    })
  }

  const stopFocus = (completed: boolean = false) => {
    if (completed) {
      saveFocusRecord(1)
    }
    store.commit('focus/STOP_FOCUS')
  }

  const giveUpFocus = async () => {
    if (selectedTask.value) {
      try {
        await taskApi.updateTask(selectedTask.value.id, { status: 0 })
      } catch (e) {
        console.error('Failed to reset task status:', e)
      }
    }
    saveFocusRecord(2)
    store.commit('focus/STOP_FOCUS')
  }

  const pauseFocus = () => {
    store.dispatch('focus/pauseFocus')
    store.dispatch('focus/addInterruption')
  }

  const resumeFocus = () => {
    store.dispatch('focus/resumeFocus')
  }

  const saveFocusRecord = async (status: number) => {
    try {
      focusError.value = ''
      const duration = Math.floor(startTotalSeconds.value - remainingSeconds.value)

      const response = await focusRecordApi.createRecord({
        taskId: selectedTask.value?.id || null,
        startTime: startTime.value || new Date().toISOString(),
        endTime: new Date().toISOString(),
        duration: duration > 0 ? duration : 0,
        interruptions: interruptions.value,
        notes: focusNotes.value,
        status: status
      })

      if (response.data.code === 200) {
        await fetchFocusRecords()
        store.commit('focus/CLEAR_FOCUS_NOTES')
      } else {
        focusError.value = response.data.message
      }
    } catch (e: any) {
      focusError.value = e.response?.data?.message || e.message || '保存专注记录失败'
    }
  }

  const setCustomTime = (customHours: number, customMinutes: number, customSeconds: number) => {
    const totalSeconds = customHours * 3600 + customMinutes * 60 + customSeconds
    if (totalSeconds >= 1 && totalSeconds <= 172800) {
      store.commit('focus/UPDATE_FOCUS_TIME', totalSeconds)
      focusError.value = ''
    } else {
      focusError.value = '请输入有效的时长（1秒-48小时）'
    }
  }

  return {
    isFocusing,
    isPaused,
    remainingSeconds,
    startTotalSeconds,
    selectedTask,
    focusNotes,
    interruptions,
    formattedTime,
    startFocus,
    stopFocus,
    giveUpFocus,
    pauseFocus,
    resumeFocus,
    saveFocusRecord,
    setCustomTime
  }
}
