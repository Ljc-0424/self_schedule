import { ref, computed } from 'vue'
import type { UserVO } from '../../types'
import { userApi, avatarApi } from '../../api'

const user = ref<UserVO | null>(null)
const isEditing = ref(false)
const isSaving = ref(false)
const isUploadingAvatar = ref(false)
const error = ref('')
const success = ref('')
const isLoading = ref(true)

const CACHE_KEY = 'user_info_cache'
const CACHE_EXPIRE = 5 * 60 * 1000

const userForm = ref({
  nickname: '',
  email: '',
  avatarUrl: '',
  settings: '',
  occupation: '',
  birthday: '',
  phone: '',
  gender: '',
  city: '',
  hobbies: '',
  bio: '',
  weChatWebhookUrl: '',
  aiApiKey: '',
  dailyFocusGoal: 120,
  minEffectiveDuration: 30
})

const getCachedUserInfo = (): UserVO | null => {
  try {
    const cached = localStorage.getItem(CACHE_KEY)
    if (cached) {
      const parsed = JSON.parse(cached)
      const now = Date.now()
      if (parsed.expireTime && parsed.expireTime > now) {
        return parsed.data
      }
    }
  } catch (e) {
    console.warn('Failed to read user info cache:', e)
  }
  return null
}

const cacheUserInfo = (userInfo: UserVO) => {
  try {
    localStorage.setItem(CACHE_KEY, JSON.stringify({
      data: userInfo,
      expireTime: Date.now() + CACHE_EXPIRE
    }))
  } catch (e) {
    console.warn('Failed to cache user info:', e)
  }
}

const clearUserCache = () => {
  localStorage.removeItem(CACHE_KEY)
}

const fetchUserInfo = async () => {
  isLoading.value = true
  try {
    error.value = ''
    const cachedUser = getCachedUserInfo()
    if (cachedUser) {
      user.value = cachedUser
      populateUserForm(cachedUser)
      isLoading.value = false
      refreshUserInfo()
      return
    }
    const response = await userApi.getInfo()
    if (response.data.code === 200) {
      user.value = response.data.data
      cacheUserInfo(response.data.data)
      populateUserForm(response.data.data)
    } else {
      error.value = response.data.message
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '获取用户信息失败'
  } finally {
    isLoading.value = false
  }
}

const refreshUserInfo = async () => {
  try {
    const response = await userApi.getInfo()
    if (response.data.code === 200) {
      const newData = response.data.data
      if (JSON.stringify(user.value) !== JSON.stringify(newData)) {
        user.value = newData
        cacheUserInfo(newData)
        populateUserForm(newData)
      }
    }
  } catch (e) {
    console.warn('Background refresh failed:', e)
  }
}

const populateUserForm = (data: UserVO) => {
  userForm.value = {
    nickname: data.nickname || '',
    email: data.email || '',
    avatarUrl: data.avatarUrl || '',
    settings: data.settings || '',
    occupation: data.occupation || '',
    birthday: data.birthday || '',
    phone: data.phone || '',
    gender: data.gender || '',
    city: data.city || '',
    hobbies: data.hobbies || '',
    bio: data.bio || '',
    weChatWebhookUrl: data.weChatWebhookUrl || '',
    aiApiKey: (data as any).aiApiKey || '',
    dailyFocusGoal: data.dailyFocusGoal ? Math.round(data.dailyFocusGoal / 60) : 120,
    minEffectiveDuration: data.minEffectiveDuration ? Math.round(data.minEffectiveDuration / 60) : 30
  }
}

const startEdit = () => {
  isEditing.value = true
}

const handleAvatarUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/bmp', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    error.value = '不支持的文件格式，仅支持jpg、jpeg、png、gif、bmp、webp'
    return
  }
  
  if (file.size > 10 * 1024 * 1024) {
    error.value = '文件大小不能超过10MB'
    return
  }
  
  isUploadingAvatar.value = true
  try {
    error.value = ''
    const response = await avatarApi.upload(file)
    if (response.data.code === 200) {
      const avatarUrl = response.data.data.avatarUrl
      user.value = user.value || {} as UserVO
      user.value.avatarUrl = avatarUrl
      userForm.value.avatarUrl = avatarUrl
      success.value = '头像上传成功'
      setTimeout(() => success.value = '', 3000)
    } else {
      error.value = response.data.message
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '上传失败'
  } finally {
    isUploadingAvatar.value = false
  }
  
  target.value = ''
}

const handleAvatarDelete = async () => {
  if (!confirm('确定删除头像？')) return
  
  try {
    error.value = ''
    const response = await avatarApi.delete()
    if (response.data.code === 200) {
      if (user.value) {
        user.value.avatarUrl = ''
      }
      userForm.value.avatarUrl = ''
      success.value = '头像删除成功'
      setTimeout(() => success.value = '', 3000)
    } else {
      error.value = response.data.message
    }
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '删除失败'
  }
}

const saveUser = async () => {
  try {
    error.value = ''
    isSaving.value = true

    const prevUser = user.value ? { ...user.value } : null
    const requestData = {
      nickname: userForm.value.nickname,
      email: userForm.value.email,
      avatarUrl: userForm.value.avatarUrl,
      settings: userForm.value.settings,
      occupation: userForm.value.occupation,
      birthday: userForm.value.birthday,
      phone: userForm.value.phone,
      gender: userForm.value.gender,
      city: userForm.value.city,
      hobbies: userForm.value.hobbies,
      bio: userForm.value.bio,
      weChatWebhookUrl: userForm.value.weChatWebhookUrl,
      aiApiKey: userForm.value.aiApiKey,
      dailyFocusGoal: userForm.value.dailyFocusGoal ? userForm.value.dailyFocusGoal * 60 : undefined,
      minEffectiveDuration: userForm.value.minEffectiveDuration ? userForm.value.minEffectiveDuration * 60 : undefined
    }

    if (user.value) {
      user.value = { ...user.value, ...requestData } as UserVO
    }

    const response = await userApi.updateInfo(requestData)

    if (response.data.code === 200) {
      user.value = response.data.data
      clearUserCache()
      cacheUserInfo(response.data.data)
      isEditing.value = false
      success.value = '更新成功'
      setTimeout(() => success.value = '', 3000)
    } else {
      user.value = prevUser
      error.value = response.data.message
    }
  } catch (e: any) {
    if (user.value && userForm.value) {
      user.value = { ...user.value, nickname: userForm.value.nickname, email: userForm.value.email } as UserVO
    }
    error.value = e.response?.data?.message || e.message || '更新失败'
  } finally {
    isSaving.value = false
  }
}

const cancelEdit = () => {
  isEditing.value = false
  if (user.value) {
    populateUserForm(user.value)
  }
}

const formatDateTime = (dateTime: string | undefined) => {
  if (!dateTime) return '未设置'
  try {
    const date = new Date(dateTime)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    })
  } catch {
    return dateTime
  }
}

const formatDate = (dateStr: string | undefined) => {
  if (!dateStr) return '未设置'
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  } catch {
    return dateStr
  }
}

const getUserTags = (settings: string | undefined): string[] => {
  if (!settings || settings.trim() === '') return []
  try {
    const parsed = JSON.parse(settings)
    if (Array.isArray(parsed)) {
      return parsed.map(t => String(t).trim()).filter(t => t)
    }
  } catch {
    // 如果不是 JSON 格式，尝试按分号分隔
  }
  return settings.split(/[;；]/).map(t => t.trim()).filter(t => t)
}

const statusText = computed(() => {
  return user.value?.isActive === 1 ? '正常' : '禁用'
})

const statusColor = computed(() => {
  return user.value?.isActive === 1 ? '#4CAF50' : '#f44336'
})

export {
  user,
  isEditing,
  isSaving,
  isUploadingAvatar,
  error,
  success,
  isLoading,
  userForm,
  statusText,
  statusColor,
  fetchUserInfo,
  startEdit,
  handleAvatarUpload,
  handleAvatarDelete,
  saveUser,
  cancelEdit,
  formatDateTime,
  formatDate,
  getUserTags
}