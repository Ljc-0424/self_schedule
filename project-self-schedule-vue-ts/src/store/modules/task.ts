import type { TaskVO } from '../../types'
import { taskApi } from '../../api'
import { ElMessage } from 'element-plus'

export interface TaskState {
  tasks: TaskVO[]
  currentTask: TaskVO | null
  loading: boolean
  error: string | null
  lastFetchTime: number | null
}

const state: TaskState = {
  tasks: [],
  currentTask: null,
  loading: false,
  error: null,
  lastFetchTime: null
}

const mutations = {
  SET_TASKS(state: TaskState, tasks: TaskVO[]) {
    state.tasks = tasks
    state.lastFetchTime = Date.now()
    state.error = null
  },
  ADD_TASK(state: TaskState, task: TaskVO) {
    state.tasks.unshift(task)
  },
  UPDATE_TASK(state: TaskState, task: TaskVO) {
    const index = state.tasks.findIndex(t => t.id === task.id)
    if (index !== -1) {
      state.tasks[index] = { ...state.tasks[index], ...task }
    }
    if (state.currentTask?.id === task.id) {
      state.currentTask = { ...state.currentTask, ...task }
    }
  },
  DELETE_TASK(state: TaskState, taskId: number) {
    state.tasks = state.tasks.filter(t => t.id !== taskId)
    if (state.currentTask?.id === taskId) {
      state.currentTask = null
    }
  },
  SET_CURRENT_TASK(state: TaskState, task: TaskVO | null) {
    state.currentTask = task
  },
  SET_LOADING(state: TaskState, loading: boolean) {
    state.loading = loading
  },
  SET_ERROR(state: TaskState, error: string | null) {
    state.error = error
  }
}

const actions = {
  /**
   * 获取任务列表
   */
  async fetchTasks({ commit, state }: { commit: Function; state: TaskState }, query?: Record<string, any>) {
    // 防止重复请求（5秒内）
    if (state.loading || (state.lastFetchTime && Date.now() - state.lastFetchTime < 5000)) {
      return
    }

    commit('SET_LOADING', true)
    commit('SET_ERROR', null)

    try {
      const response = await taskApi.getTasks(query)
      if (response.data.code === 200) {
        commit('SET_TASKS', response.data.data.records || response.data.data)
      }
    } catch (error: any) {
      const errorMsg = error.message || '获取任务列表失败'
      commit('SET_ERROR', errorMsg)
      console.error('获取任务列表失败:', error)
    } finally {
      commit('SET_LOADING', false)
    }
  },

  /**
   * 创建任务（乐观更新）
   */
  async createTask({ commit }: { commit: Function }, taskData: Record<string, any>) {
    try {
      const response = await taskApi.createTask(taskData as Parameters<typeof taskApi.createTask>[0])
      if (response.data.code === 200) {
        commit('ADD_TASK', response.data.data)
        ElMessage.success('任务创建成功')
        return response.data.data
      }
      throw new Error(response.data.message || '创建任务失败')
    } catch (error: any) {
      ElMessage.error(error.message || '创建任务失败')
      throw error
    }
  },

  /**
   * 更新任务
   */
  async updateTask({ commit }: { commit: Function }, { id, data }: { id: number; data: Record<string, any> }) {
    try {
      const response = await taskApi.updateTask(id, data)
      if (response.data.code === 200) {
        commit('UPDATE_TASK', response.data.data)
        ElMessage.success('任务更新成功')
        return response.data.data
      }
      throw new Error(response.data.message || '更新任务失败')
    } catch (error: any) {
      ElMessage.error(error.message || '更新任务失败')
      throw error
    }
  },

  /**
   * 删除任务
   */
  async deleteTask({ commit }: { commit: Function }, taskId: number) {
    try {
      const response = await taskApi.deleteTask(taskId)
      if (response.data.code === 200) {
        commit('DELETE_TASK', taskId)
        ElMessage.success('任务删除成功')
      } else {
        throw new Error(response.data.message || '删除任务失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '删除任务失败')
      throw error
    }
  },

  /**
   * 获取单个任务详情
   */
  async getTask({ commit }: { commit: Function }, taskId: number) {
    try {
      const response = await taskApi.getTask(taskId)
      if (response.data.code === 200) {
        commit('SET_CURRENT_TASK', response.data.data)
        return response.data.data
      }
      throw new Error(response.data.message || '获取任务失败')
    } catch (error: any) {
      ElMessage.error(error.message || '获取任务失败')
      throw error
    }
  },

  /**
   * 完成任务
   */
  async completeTask({ commit }: { commit: Function }, taskId: number) {
    try {
      const response = await taskApi.completeTask(taskId)
      if (response.data.code === 200) {
        commit('UPDATE_TASK', { id: taskId, status: 2, completedTime: new Date().toISOString() })
        ElMessage.success('任务已完成')
        return response.data.data
      }
      throw new Error(response.data.message || '完成任务失败')
    } catch (error: any) {
      ElMessage.error(error.message || '完成任务失败')
      throw error
    }
  },

  /**
   * 撤销完成任务
   */
  async undoTask({ commit }: { commit: Function }, taskId: number) {
    try {
      const response = await taskApi.undoTask(taskId)
      if (response.data.code === 200) {
        commit('UPDATE_TASK', { id: taskId, status: 0, completedTime: null })
        ElMessage.success('已撤销完成状态')
        return response.data.data
      }
      throw new Error(response.data.message || '撤销失败')
    } catch (error: any) {
      ElMessage.error(error.message || '撤销失败')
      throw error
    }
  },

  setCurrentTask({ commit }: { commit: Function }, task: TaskVO | null) {
    commit('SET_CURRENT_TASK', task)
  },

  /**
   * 清除错误状态
   */
  clearError({ commit }: { commit: Function }) {
    commit('SET_ERROR', null)
  }
}

const getters = {
  tasks: (state: TaskState) => state.tasks,
  currentTask: (state: TaskState) => state.currentTask,
  loading: (state: TaskState) => state.loading,
  error: (state: TaskState) => state.error,
  pendingTasks: (state: TaskState) => state.tasks.filter(t => t.status === 0),
  inProgressTasks: (state: TaskState) => state.tasks.filter(t => t.status === 1),
  completedTasks: (state: TaskState) => state.tasks.filter(t => t.status === 2),
  highPriorityTasks: (state: TaskState) => state.tasks.filter(t => t.priority === 2),
  taskById: (state: TaskState) => (id: number) => state.tasks.find(t => t.id === id)
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
