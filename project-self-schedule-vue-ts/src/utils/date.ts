export const dateUtils = {
  formatDateTime(date: Date | string): string {
    const d = typeof date === 'string' ? new Date(date) : date
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hours}:${minutes}`
  },

  formatDate(date: Date | string): string {
    const d = typeof date === 'string' ? new Date(date) : date
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  },

  formatTime(date: Date | string): string {
    const d = typeof date === 'string' ? new Date(date) : date
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    return `${hours}:${minutes}`
  },

  formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    
    if (hours > 0) {
      return `${hours}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`
    }
    return `${minutes}:${String(secs).padStart(2, '0')}`
  },

  formatDurationWords(seconds: number): string {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    
    if (hours > 0) {
      return `${hours}小时${minutes}分钟`
    }
    return `${minutes}分钟`
  },

  toISOString(date: Date): string {
    return date.toISOString().replace('T', ' ').substring(0, 19)
  },

  parseISOString(dateStr: string): Date {
    return new Date(dateStr.replace(' ', 'T'))
  },

  isToday(date: Date | string): boolean {
    const d = typeof date === 'string' ? new Date(date) : date
    const today = new Date()
    return this.formatDate(d) === this.formatDate(today)
  },

  isYesterday(date: Date | string): boolean {
    const d = typeof date === 'string' ? new Date(date) : date
    const yesterday = new Date()
    yesterday.setDate(yesterday.getDate() - 1)
    return this.formatDate(d) === this.formatDate(yesterday)
  },

  getDaysAgo(days: number): Date {
    const date = new Date()
    date.setDate(date.getDate() - days)
    return date
  },

  getWeekStart(): Date {
    const date = new Date()
    const day = date.getDay()
    const diff = date.getDate() - day + (day === 0 ? -6 : 1)
    return new Date(date.setDate(diff))
  },

  getMonthStart(): Date {
    return new Date(new Date().getFullYear(), new Date().getMonth(), 1)
  }
}
