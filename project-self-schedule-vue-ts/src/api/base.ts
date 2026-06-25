import axios, { InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

/**
 * 基础API模块 - 优化版
 * 封装axios实例和拦截器配置
 */

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
  timeout: 60000,  // 60秒，AI处理需要较长时间
  paramsSerializer: {
    indexes: null
  }
})

// 请求取消控制器映射
const pendingRequests = new Map<string, AbortController>()

// 生成请求key
const getRequestKey = (config: InternalAxiosRequestConfig): string => {
  const { method, url, params, data } = config
  return [method, url, JSON.stringify(params), JSON.stringify(data)].join('&')
}

/**
 * 请求拦截器
 */
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  // 添加token
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }

  // 取消重复请求
  const requestKey = getRequestKey(config)
  if (pendingRequests.has(requestKey)) {
    const controller = pendingRequests.get(requestKey)
    controller?.abort()
  }

  const controller = new AbortController()
  config.signal = controller.signal
  pendingRequests.set(requestKey, controller)

  return config
})

/**
 * 响应拦截器 - 优化版
 */
api.interceptors.response.use(
  (response: AxiosResponse) => {
    // 移除已完成的请求
    const requestKey = getRequestKey(response.config as InternalAxiosRequestConfig)
    pendingRequests.delete(requestKey)

    // 统一处理业务错误
    const { data } = response
    if (data.code && data.code !== 200) {
      const errorMsg = data.message || '请求失败'

      // 特定错误码处理
      switch (data.code) {
        case 401:
          handleUnauthorized()
          break
        case 403:
          ElMessage.error('没有权限执行此操作')
          break
        case 404:
          ElMessage.warning('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误，请稍后重试')
          break
        default:
          ElMessage.error(errorMsg)
      }

      return Promise.reject(new Error(errorMsg))
    }

    return response
  },
  (error: AxiosError) => {
    // 移除已完成的请求
    if (error.config) {
      const requestKey = getRequestKey(error.config as InternalAxiosRequestConfig)
      pendingRequests.delete(requestKey)
    }

    // 网络错误或超时
    if (!error.response) {
      if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请检查网络后重试')
      } else if (error.message === 'canceled') {
        // 请求被取消，不提示
        return Promise.reject(error)
      } else {
        ElMessage.error('网络连接失败，请检查网络设置')
      }
      return Promise.reject(error)
    }

    // HTTP错误处理
    const { status } = error.response
    switch (status) {
      case 401:
        handleUnauthorized()
        break
      case 403:
        ElMessage.error('没有权限执行此操作')
        break
      case 404:
        ElMessage.error('请求的接口不存在')
        break
      case 408:
        ElMessage.error('请求超时，请稍后重试')
        break
      case 429:
        ElMessage.warning('请求过于频繁，请稍后再试')
        break
      case 500:
        ElMessage.error('服务器内部错误，请稍后重试')
        break
      case 502:
        ElMessage.error('网关错误，请稍后重试')
        break
      case 503:
        ElMessage.error('服务暂不可用，请稍后重试')
        break
      default:
        ElMessage.error(`请求失败 (${status})`)
    }

    return Promise.reject(error)
  }
)

/**
 * 处理未授权（401）情况
 */
let isShowingAuthDialog = false
function handleUnauthorized() {
  if (isShowingAuthDialog) return
  isShowingAuthDialog = true

  ElMessageBox.confirm(
    '登录状态已过期，请重新登录',
    '提示',
    {
      confirmButtonText: '重新登录',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    window.location.href = '/login'
  }).catch(() => {
    // 用户取消
  }).finally(() => {
    isShowingAuthDialog = false
  })
}

/**
 * 清除所有待处理的请求
 */
export function clearPendingRequests() {
  pendingRequests.forEach((controller) => {
    controller.abort()
  })
  pendingRequests.clear()
}

/**
 * 带重试的请求方法
 */
export async function requestWithRetry<T>(
  requestFn: () => Promise<T>,
  maxRetries: number = 2,
  retryDelay: number = 1000
): Promise<T> {
  let lastError: Error | null = null

  for (let i = 0; i <= maxRetries; i++) {
    try {
      return await requestFn()
    } catch (error: any) {
      lastError = error

      // 不重试的情况
      if (error.response?.status === 401 || error.response?.status === 403) {
        throw error
      }

      // 最后一次重试失败
      if (i === maxRetries) {
        throw error
      }

      // 等待后重试
      await new Promise(resolve => setTimeout(resolve, retryDelay * (i + 1)))
    }
  }

  throw lastError
}

export default api
