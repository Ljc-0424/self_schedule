export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  avatarUrl: string
  settings: string
  isActive: number
  status: number
  role: number
  lastLoginTime: string
  createdTime: string
  updatedTime: string
  occupation: string
  birthday: string
  phone: string
  gender: string
  city: string
  hobbies: string
  bio: string
  weChatWebhookUrl: string
  aiApiKey: string
  dailyFocusGoal: number
  minEffectiveDuration: number
}

export interface LoginVO {
  token: string
  user: UserVO
}

export interface TaskVO {
  id: number
  userId: number
  title: string
  description: string
  priority: number
  status: number
  deadline: string           // 截止时间（有DDL的任务）
  remindTime: string         // 提醒时间（纯日程提醒）
  estimatedSeconds: number   // 预估时间（秒）
  actualSeconds: number      // 实际用时（秒）
  category: string
  tags: string[]
  reminderConfig: string
  isAiGenerated: boolean
  recurrenceRule: string     // 重复规则（如：DAILY, WEEKLY:1,3,5, MONTHLY:1）
  recurrenceEndDate: string  // 重复结束日期
  createdTime: string
  updatedTime: string
  completedTime: string | null
}

export interface SubtaskVO {
  id: number
  taskId: number
  title: string
  status: number
  sortOrder: number
  createdTime: string
  completedTime: string | null
}

export interface FocusRecordVO {
  id: number
  userId: number
  taskId: number | null
  taskTitle: string
  startTime: string
  endTime: string | null
  duration: number // 单位：秒
  interruptions: number
  focusScore: number | null
  notes: string
  status: number
  createdTime: string
  task?: TaskVO // 关联任务的完整信息
}

export interface ReminderRecordVO {
  id: number
  taskId: number
  taskTitle: string
  triggerTime: string
  taskTime?: string
  reminderType: string
  status: string
  statusLabel?: string
  message: string
  createdAt?: string
}

export interface BadgeVO {
  id: string
  name: string
  icon: string
  description: string
  category: 'PERSISTENCE' | 'SPECIALTY' | 'EFFICIENCY' | 'FOCUS' | 'TASK' | 'STREAK' | 'SPECIAL'
  unlocked: boolean
  progress: string
  /** 前端计算用：当前进度数值 */
  progressValue?: number
  /** 前端计算用：目标进度数值 */
  maxProgress?: number
  /** 前端计算用：解锁条件说明 */
  condition?: string
}

export interface UserStatsVO {
  totalFocusSeconds: number
  monthlyFocusSeconds: number
  todayFocusSeconds: number
  todayGoalRate: number
  maxDailyFocusSeconds: number
  dailyFocusGoalSeconds: number
  totalCompletedTasks: number
  todayCompletedTasks: number
  weekCompletedTasks: number
  monthCompletedTasks: number
  todayTaskRate: number
  weekTaskRate: number
  monthTaskRate: number
  currentStreak: number
  maxStreak: number
  totalCheckInDays: number
  minEffectiveDurationSeconds: number
  badges: BadgeVO[]
}

export interface BanInfoVO {
  status: number
  banReason: string
  banTime: string
  banEndTime: string | null
  appealStatus: number
  auditNote: string | null
  username: string
}

export interface AppealVO {
  id: number
  userId: number
  username: string
  nickname: string
  content: string
  status: number
  auditAdminId: number | null
  auditAdminName: string | null
  auditTime: string | null
  auditNote: string | null
  createdTime: string
  banReason: string | null
  userStatus: number | null
  banEndTime: string | null
  banTime: string | null
}
