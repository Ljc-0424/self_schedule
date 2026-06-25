export interface ErrorInfo {
  message: string
  code?: number
  type?: 'api' | 'validation' | 'network' | 'auth' | 'unknown' | 'warning'
  detail?: any
}

export const errorHandler = {
  parse(error: any): ErrorInfo {
    if (!error) {
      return { message: '未知错误', type: 'unknown' }
    }

    if (typeof error === 'string') {
      return { message: error, type: 'unknown' }
    }

    if (error.response) {
      const { status, data } = error.response
      const message = data?.message || data?.msg || this.getStatusMessage(status)
      
      let type: ErrorInfo['type'] = 'api'
      if (status === 401) type = 'auth'
      else if (status === 400) type = 'validation'

      return {
        message,
        code: status,
        type,
        detail: data
      }
    }

    if (error.request) {
      return {
        message: '网络请求失败，请检查网络连接',
        type: 'network'
      }
    }

    return {
      message: error.message || '未知错误',
      type: 'unknown'
    }
  },

  getStatusMessage(status: number): string {
    const messages: Record<number, string> = {
      400: '请求参数错误',
      401: '登录已过期，请重新登录',
      403: '无权限访问该资源',
      404: '请求的资源不存在',
      405: '请求方法不允许',
      408: '请求超时',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务暂时不可用',
      504: '网关超时'
    }
    return messages[status] || `请求失败 (${status})`
  },

  handle(error: any, options: {
    showToast?: boolean
    redirectOnAuth?: boolean
  } = {}): ErrorInfo {
    const errorInfo = this.parse(error)
    
    if (options.showToast !== false) {
      this.showToast(errorInfo)
    }

    if (errorInfo.type === 'auth' && options.redirectOnAuth !== false) {
      setTimeout(() => {
        window.location.href = '/login'
      }, 1500)
    }

    return errorInfo
  },

  showToast(errorInfo: ErrorInfo): void {
    if (typeof window !== 'undefined' && window.dispatchEvent) {
      const event = new CustomEvent('showToast', {
        detail: {
          message: errorInfo.message,
          type: errorInfo.type === 'warning' ? 'warning' : 'error'
        }
      })
      window.dispatchEvent(event)
    } else {
      console.error(errorInfo.message)
    }
  },

  catch<T = any>(promise: Promise<T>, options?: Parameters<typeof this.handle>[1]): Promise<[ErrorInfo | null, T | null]> {
    return promise
      .then((data: T) => [null, data] as [null, T])
      .catch((error) => {
        const errorInfo = this.handle(error, options)
        return [errorInfo, null] as [ErrorInfo, null]
      })
  }
}
