/**
 * 认证状态管理模块
 * 
 * 职责：
 * 1. 管理用户登录状态（token、用户信息）
 * 2. 提供登录、注册、注销等认证操作
 * 3. 处理token持久化（localStorage）
 * 
 * 状态结构：
 * - token: 用户登录令牌
 * - user: 用户信息对象
 * 
 * 使用方式：
 * - dispatch('auth/login', { account, password })
 * - dispatch('auth/logout')
 * - getters['auth/isLoggedIn']
 * - getters['auth/user']
 */

import type { UserVO, LoginVO, BanInfoVO } from '../../types'
import { authApi, userApi } from '../../api'
import { clearAllState } from '../../composables/useClearState'

// 状态接口定义
export interface AuthState {
  token: string        // JWT令牌
  user: UserVO | null  // 用户信息
}

// 初始状态
const state: AuthState = {
  token: '',
  user: null
}

// Mutations - 同步状态更新
const mutations = {
  /** 设置token并持久化到localStorage */
  SET_TOKEN(state: AuthState, token: string) {
    state.token = token
    localStorage.setItem('token', token)
  },

  /** 设置用户信息 */
  SET_USER(state: AuthState, user: UserVO | null) {
    state.user = user
  },

  /** 清除认证状态（登出） */
  CLEAR_AUTH(state: AuthState) {
    state.token = ''
    state.user = null
    localStorage.removeItem('token')
  }
}

// Actions - 异步操作
const actions = {
  /** 
   * 获取当前用户信息
   * @throws {Error} 获取失败时抛出错误
   */
  async fetchUserInfo({ commit }: { commit: Function }) {
    try {
      const response = await userApi.getInfo()
      if (response.data.code === 200) {
        commit('SET_USER', response.data.data)
      } else {
        throw new Error(response.data.message)
      }
    } catch (error: any) {
      commit('CLEAR_AUTH')
      throw error
    }
  },

  /** 
   * 用户登录
   * @param account - 账号（用户名或邮箱）
   * @param password - 密码
   * @throws {string} 登录失败时抛出错误消息
   */
  async login({ commit }: { commit: Function }, { account, password }: { account: string; password: string }) {
    try {
      const response = await authApi.login({ account, password })
      if (response.data.code === 200) {
        const loginVO: LoginVO = response.data.data
        commit('SET_TOKEN', loginVO.token)
        commit('SET_USER', loginVO.user)
      } else if (response.data.code === 403 && response.data.data) {
        const banInfo: BanInfoVO = response.data.data
        const banError = new Error('账号已被封禁') as any
        banError.banInfo = banInfo
        throw banError
      } else {
        throw new Error(response.data.message || '登录失败')
      }
    } catch (error: any) {
      if (error?.banInfo) {
        throw error
      }
      if (error.response?.data?.code === 403 && error.response?.data?.data) {
        const banInfo: BanInfoVO = error.response.data.data
        const banError = new Error('账号已被封禁') as any
        banError.banInfo = banInfo
        throw banError
      }
      const message = error.response?.data?.message || error.message || '登录失败'
      throw message
    }
  },

  /** 
   * 用户注册
   * @param username - 用户名
   * @param password - 密码
   * @param email - 邮箱
   * @throws {string} 注册失败时抛出错误消息
   */
  async register({ commit }: { commit: Function }, { username, password, email }: { username: string; password: string; email: string }) {
    try {
      const response = await authApi.register({ username, password, email })
      if (response.data.code === 200) {
        const loginVO: LoginVO = response.data.data
        commit('SET_TOKEN', loginVO.token)
        commit('SET_USER', loginVO.user)
      } else {
        throw new Error(response.data.message || '注册失败')
      }
    } catch (error: any) {
      throw error.response?.data?.message || error.message || '注册失败'
    }
  },

  /** 用户注销 */
  async logout({ commit }: { commit: Function }) {
    try {
      await authApi.logout()
      commit('CLEAR_AUTH')
    } catch (error) {
      // 即使API调用失败，也清除本地状态
      commit('CLEAR_AUTH')
    } finally {
      // 清除所有composables中的全局状态
      clearAllState()
    }
  },

  /** 
   * 初始化认证状态
   * 从localStorage恢复token（页面刷新后）
   */
  initAuth({ commit }: { commit: Function }) {
    const token = localStorage.getItem('token')
    if (token) {
      commit('SET_TOKEN', token)
    }
  }
}

// Getters - 派生状态
const getters = {
  /** 是否已登录 */
  isLoggedIn: (state: AuthState) => !!state.token,

  /** 用户名 */
  username: (state: AuthState) => state.user?.username || '',

  /** 头像URL */
  avatarUrl: (state: AuthState) => state.user?.avatarUrl || '',

  /** 用户信息对象 */
  user: (state: AuthState) => state.user,

  /** 是否管理员 */
  isAdmin: (state: AuthState) => state.user?.role === 1
}

export default {
  namespaced: true,  // 启用命名空间
  state,
  mutations,
  actions,
  getters
}
