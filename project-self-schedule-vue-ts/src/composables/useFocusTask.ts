import { ref } from 'vue'
import type { Store } from 'vuex'
import type { TaskVO } from '../types'
import { focusRecordApi } from '../api'

export const showTaskSearch = ref(false)
export const searchKeyword = ref('')
export const taskSearchResults = ref<TaskVO[]>([])
export const taskSearchCurrentPage = ref(1)
export const taskSearchTotalPages = ref(1)
export const taskSearchTotalItems = ref(0)
export const taskSearchError = ref('')
export const customHours = ref(0)
export const customMinutes = ref(0)
export const customSeconds = ref(0)

export const toggleTaskSearch = async () => {
  showTaskSearch.value = !showTaskSearch.value
  if (showTaskSearch.value) {
    searchKeyword.value = ''
    taskSearchCurrentPage.value = 1
    await searchTasks()
  }
}

export const searchTasks = async (pageNum: number = 1) => {
  try {
    taskSearchError.value = ''
    const response = await focusRecordApi.searchTasks(searchKeyword.value, pageNum)
    if (response.data.code === 200) {
      taskSearchResults.value = response.data.data.data
      taskSearchTotalPages.value = response.data.data.pages
      taskSearchTotalItems.value = response.data.data.total
      taskSearchCurrentPage.value = response.data.data.pageNum
    } else {
      taskSearchError.value = response.data.message
    }
  } catch (e: any) {
    taskSearchError.value = e.response?.data?.message || e.message || '搜索任务失败'
  }
}

export const taskSearchChangePage = (page: number) => {
  taskSearchCurrentPage.value = page
  searchTasks(page)
}

export function useFocus(store: Store<any>) {
  const selectedTask = () => store.getters['focus/focusSelectedTask']

  const selectTask = (task: TaskVO) => {
    const totalSeconds = task.estimatedSeconds || 25 * 60
    store.commit('focus/SET_SELECTED_TASK', task)
    store.commit('focus/UPDATE_FOCUS_TIME', totalSeconds)
    store.commit('focus/SET_START_TOTAL_SECONDS', totalSeconds)
    toggleTaskSearch()
  }

  const clearSelectedTask = () => {
    store.commit('focus/SET_SELECTED_TASK', null)
    store.commit('focus/UPDATE_FOCUS_TIME', 25 * 60)
    store.commit('focus/SET_START_TOTAL_SECONDS', 25 * 60)
  }

  return {
    selectedTask,
    selectTask,
    clearSelectedTask
  }
}
