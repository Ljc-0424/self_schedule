<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import ReminderNotification from './components/ReminderNotification.vue'
import { focusRecordApi } from './api'
import { fetchFocusRecords } from './composables/useFocusRecord'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

const store = useStore()
const router = useRouter()

let timer: number | null = null

const saveFocusRecordOnComplete = async () => {
  try {
    const duration = store.getters['focus/focusStartTotalSeconds'] - store.getters['focus/focusRemainingSeconds']
    const response = await focusRecordApi.createRecord({
      taskId: store.getters['focus/focusSelectedTask']?.id || null,
      startTime: store.getters['focus/focusStartTime'] || new Date().toISOString(),
      endTime: new Date().toISOString(),
      duration: duration > 0 ? duration : 0,
      interruptions: store.getters['focus/focusInterruptions'],
      notes: store.getters['focus/focusNotes'],
      status: 1 // 完成状态
    })

    if (response.data.code === 200) {
      await fetchFocusRecords()
      store.commit('focus/CLEAR_FOCUS_NOTES')
    }
  } catch (e) {
    console.error('Failed to save focus record on complete:', e)
  }
}

const startGlobalTimer = () => {
  if (timer) clearInterval(timer)
  timer = window.setInterval(() => {
    if (store.getters['focus/isFocusing'] && !store.getters['focus/isPaused']) {
      const current = store.getters['focus/focusRemainingSeconds']
      if (current > 0) {
        store.commit('focus/UPDATE_FOCUS_TIME', current - 1)
      } else {
        const duration = store.getters['focus/focusStartTotalSeconds']
        saveFocusRecordOnComplete()
        store.commit('focus/FOCUS_COMPLETED', duration)
        if (timer) {
          clearInterval(timer)
          timer = null
        }
      }
    }
  }, 1000)
}

onMounted(() => {
  // 初始化主题（从 localStorage 恢复）
  const savedTheme = localStorage.getItem('app_theme')
  if (savedTheme) {
    document.documentElement.setAttribute('data-theme', savedTheme)
  }

  store.dispatch('auth/initAuth')
  
  const token = localStorage.getItem('token')
  if (token) {
    store.dispatch('auth/fetchUserInfo').catch(() => {
      localStorage.removeItem('token')
      router.push('/login')
    })
  }
  
  store.dispatch('focus/tryRestoreFocusState').then((wasRestored: boolean) => {
    if (wasRestored) {
      startGlobalTimer()
    }
  })
  
  store.subscribe((mutation, state) => {
    if (mutation.type === 'focus/START_FOCUS') {
      startGlobalTimer()
    } else if (mutation.type === 'focus/STOP_FOCUS') {
      if (timer) {
        clearInterval(timer)
        timer = null
      }
    }
  })
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<template>
  <el-config-provider :locale="zhCn">
    <div class="app-container">
      <ReminderNotification />
      <router-view v-slot="{ Component }">
        <keep-alive :include="['Home']">
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
  </el-config-provider>
</template>

<style>
html, body, #app {
  height: 100% !important;
  overflow: hidden !important;
  margin: 0 !important;
  padding: 0 !important;
}
</style>
