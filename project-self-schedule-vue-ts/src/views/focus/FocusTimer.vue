<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { useFocusTimer, useFocus } from './useFocus'
import { toggleTaskSearch, showTaskSearch } from '../../composables/useFocusTask'

const store = useStore()
const router = useRouter()

const {
  isFocusing,
  isPaused,
  formattedTime,
  selectedTask,
  focusNotes,
  interruptions,
  startFocus,
  stopFocus,
  giveUpFocus,
  pauseFocus,
  resumeFocus,
  setCustomTime
} = useFocusTimer(store)

const {
  clearSelectedTask
} = useFocus(store)

const focusCompleted = computed(() => store.getters['focus/focusCompleted'])
const completedDuration = computed(() => store.getters['focus/completedDuration'])
const focusProgress = computed(() => store.getters['focus/focusProgress'])

const showCompleteDialog = ref(false)
const currentEncourage = ref('')
const pomodoroMode = ref(true)
const pomodoroCount = ref(0)
const customMinutes = ref(25)
const showCustomTime = ref(false)
const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})

const encourageWords = [
  '太棒了！你完成了一次专注！🎉',
  '坚持就是胜利，你做到了！💪',
  '专注的你最闪耀！✨',
  '每一次专注都是对自己的投资！📈',
  '了不起的专注力，继续保持！🔥'
]

watch(focusCompleted, (val) => {
  if (val) {
    currentEncourage.value = encourageWords[Math.floor(Math.random() * encourageWords.length)]
    showCompleteDialog.value = true
    if (pomodoroMode.value) pomodoroCount.value++
  }
})

const dismissComplete = () => {
  showCompleteDialog.value = false
  store.commit('focus/DISMISS_FOCUS_COMPLETED')
}
</script>

<template>
  <div class="focus-timer">
    <!-- 主容器 -->
    <div class="timer-container">
      <!-- 紧凑的头部 -->
      <div :class="['page-header', { 'visible': isVisible }]">
        <div class="header-left">
          <span class="header-icon"></span>
          <h1 class="header-title">专注模式</h1>
        </div>
        <button class="back-btn" @click="router.push('/')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
          返回
        </button>
      </div>

      <!-- 内容区域 -->
      <div :class="['content-area', { 'visible': isVisible }]">
        <!-- 左侧：计时器 -->
        <div class="timer-card">
          <!-- 模式切换 -->
          <div class="mode-switch">
            <button :class="['mode-btn', { active: pomodoroMode }]" @click="pomodoroMode = true">
              番茄钟
            </button>
            <button :class="['mode-btn', { active: !pomodoroMode }]" @click="pomodoroMode = false">
              自由计时
            </button>
          </div>

          <!-- 进度环 - 小尺寸 -->
          <div class="timer-display">
            <div class="progress-ring">
              <svg viewBox="0 0 160 160">
                <circle cx="80" cy="80" r="70" fill="none" stroke="#e5e7eb" stroke-width="8"/>
                <circle
                  cx="80" cy="80" r="70"
                  fill="none"
                  stroke="url(#timerGradient)"
                  stroke-width="8"
                  stroke-linecap="round"
                  :stroke-dasharray="439.8"
                  :stroke-dashoffset="439.8 * (1 - focusProgress / 100)"
                  transform="rotate(-90 80 80)"
                />
                <defs>
                  <linearGradient id="timerGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                    <stop offset="0%" stop-color="var(--primary)"/>
                    <stop offset="100%" stop-color="#6366F1"/>
                  </linearGradient>
                </defs>
              </svg>
              <div class="time-center">
                <span class="time-value">{{ formattedTime }}</span>
                <span class="time-status">{{ isFocusing ? (isPaused ? '已暂停' : '专注中') : '准备开始' }}</span>
              </div>
            </div>

            <!-- 番茄计数 -->
            <div v-if="pomodoroMode" class="pomodoro-dots">
              <span v-for="i in 4" :key="i" :class="['dot', { filled: i <= pomodoroCount }]"></span>
            </div>
          </div>

          <!-- 控制按钮 -->
          <div class="controls">
            <template v-if="!isFocusing">
              <button class="control-btn primary large" @click="startFocus">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                  <polygon points="5 3 19 12 5 21 5 3"/>
                </svg>
                开始专注
              </button>
              <button class="control-btn outline" @click="showCustomTime = !showCustomTime">
                自定义时长
              </button>
            </template>
            <template v-else>
              <button class="control-btn warning" @click="isPaused ? resumeFocus() : pauseFocus()">
                {{ isPaused ? '继续' : '暂停' }}
              </button>
              <button class="control-btn success" @click="() => stopFocus()">
                完成
              </button>
              <button class="control-btn danger" @click="() => giveUpFocus()">
                放弃
              </button>
            </template>
          </div>

          <!-- 自定义时间 -->
          <div v-if="showCustomTime && !isFocusing" class="custom-time">
            <label class="custom-label">专注时长（分钟）</label>
            <div class="custom-input-group">
              <input v-model.number="customMinutes" type="number" min="1" max="120" class="custom-input"/>
              <button class="apply-btn" @click="setCustomTime(0, customMinutes, 0); showCustomTime = false">
                应用
              </button>
            </div>
          </div>
        </div>

        <!-- 右侧：信息 -->
        <div class="info-panel">
          <!-- 当前任务 -->
          <div class="info-card" v-if="selectedTask">
            <h3 class="info-title">当前任务</h3>
            <p class="task-name">{{ selectedTask.title }}</p>
            <button class="link-btn" @click="clearSelectedTask">切换任务</button>
          </div>
          <div class="info-card" v-else>
            <h3 class="info-title">关联任务</h3>
            <p class="task-hint">选择一个任务开始专注</p>
            <button class="select-task-btn" @click="toggleTaskSearch">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="11" cy="11" r="8"/>
                <line x1="21" y1="21" x2="16.65" y2="16.65"/>
              </svg>
              选择任务
            </button>
          </div>

          <!-- 统计 -->
          <div class="info-card">
            <h3 class="info-title">今日统计</h3>
            <div class="stat-row">
              <span class="stat-label">完成番茄</span>
              <span class="stat-num">{{ pomodoroCount }}个</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">中断次数</span>
              <span class="stat-num">{{ interruptions }}次</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">完成进度</span>
              <span class="stat-num">{{ focusProgress }}%</span>
            </div>
          </div>

          <!-- 笔记 -->
          <div class="info-card" v-if="isFocusing">
            <h3 class="info-title">专注笔记</h3>
            <textarea
              v-model="focusNotes"
              class="notes-textarea"
              placeholder="记录你的专注内容..."
            ></textarea>
          </div>

          <!-- 快捷操作 -->
          <div class="info-card">
            <h3 class="info-title">快捷操作</h3>
            <div class="quick-actions">
              <button class="quick-btn" @click="router.push('/')">返回首页</button>
              <button class="quick-btn" @click="router.push('/tasks')">查看任务</button>
              <button class="quick-btn" @click="router.push('/focus-history')">专注历史</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 完成弹窗 -->
    <Transition name="modal">
      <div v-if="showCompleteDialog" class="modal-overlay" @click.self="dismissComplete">
        <div class="modal-content">
          <div class="modal-icon"></div>
          <h2 class="modal-title">专注完成！</h2>
          <p class="modal-msg">{{ currentEncourage }}</p>
          <div class="modal-stats">
            <div class="modal-stat">
              <span class="modal-stat-value">{{ completedDuration }}秒</span>
              <span class="modal-stat-label">专注时长</span>
            </div>
            <div class="modal-stat">
              <span class="modal-stat-value">{{ interruptions }}次</span>
              <span class="modal-stat-label">中断次数</span>
            </div>
          </div>
          <button class="modal-btn" @click="dismissComplete">太棒了！</button>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
.focus-timer {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
}

/* 主容器 */
.timer-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  gap: 16px;
  overflow-y: auto;
  background: #FFFFFF;
}

/* 头部 — 白色卡片 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 18px;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  color: #6B7280;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s ease;
}

.back-btn:hover {
  background: #F9FAFB;
  color: #111827;
}

/* 内容区域 */
.content-area {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

/* 左侧计时器 — 白色卡片 */
.timer-card {
  flex: 0 0 400px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 模式切换 */
.mode-switch {
  display: flex;
  background: #E5E7EB;
  border-radius: 6px;
  padding: 3px;
}

.mode-btn {
  flex: 1;
  padding: 8px 14px;
  background: none;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  color: #6B7280;
  cursor: pointer;
  transition: all 0.15s ease;
}

.mode-btn.active {
  background: #FFFFFF;
  color: var(--primary);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

/* 进度环 */
.timer-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.progress-ring {
  position: relative;
  width: 180px;
  height: 180px;
}

.progress-ring svg {
  width: 100%;
  height: 100%;
  transform: scaleX(-1);
}

.progress-ring circle {
  transition: stroke-dashoffset 1s linear;
}

.time-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.time-value {
  display: block;
  font-size: 36px;
  font-weight: 600;
  color: #111827;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  letter-spacing: 2px;
  line-height: 1;
  margin-bottom: 4px;
}

.time-status {
  font-size: 13px;
  color: #9CA3AF;
}

.pomodoro-dots {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #D1D5DB;
  opacity: 0.5;
  transition: all 0.3s ease;
}

.dot.filled {
  background: var(--primary);
  opacity: 1;
}

/* 控制按钮 — 纯色 */
.controls {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
}

.control-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 18px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s ease;
}

.control-btn.large {
  flex: 1;
  padding: 12px 20px;
  font-size: 14px;
}

.control-btn.primary {
  background: var(--primary);
  color: #FFFFFF;
}

.control-btn.primary:hover {
  background: #4338CA;
}

.control-btn.warning {
  background: #F59E0B;
  color: #FFFFFF;
}

.control-btn.warning:hover {
  background: #D97706;
}

.control-btn.success {
  background: #3F6212;
  color: #FFFFFF;
}

.control-btn.success:hover {
  background: #059669;
}

.control-btn.danger {
  background: #B91C1C;
  color: #FFFFFF;
}

.control-btn.danger:hover {
  background: #B91C1C;
}

.control-btn.outline {
  background: #E5E7EB;
  color: var(--primary);
  border: 1px solid #D1D5DB;
}

.control-btn.outline:hover {
  background: #EEF2FF;
}

/* 自定义时间 */
.custom-time {
  padding: 14px;
  background: #F9FAFB;
  border-radius: 6px;
  border: 1px solid #D1D5DB;
}

.custom-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #6B7280;
  margin-bottom: 8px;
}

.custom-input-group {
  display: flex;
  gap: 8px;
}

.custom-input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  text-align: center;
  outline: none;
}

.custom-input:focus {
  border-color: var(--primary);
}

.apply-btn {
  padding: 8px 16px;
  background: var(--primary);
  border: none;
  border-radius: 6px;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.apply-btn:hover {
  background: #4338CA;
}

/* 右侧信息面板 */
.info-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

.info-card {
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  padding: 14px;
}

.info-title {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 10px;
}

.task-name {
  font-size: 14px;
  color: var(--primary);
  font-weight: 500;
  margin: 0 0 8px;
}

.link-btn {
  background: none;
  border: none;
  color: var(--primary);
  font-size: 13px;
  cursor: pointer;
  padding: 0;
}

.link-btn:hover {
  color: #4338CA;
}

.task-hint {
  font-size: 13px;
  color: #9CA3AF;
  margin: 0 0 10px;
}

.select-task-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 12px;
  background: var(--primary);
  border: none;
  border-radius: 6px;
  color: #FFFFFF;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s ease;
}

.select-task-btn:hover {
  background: #4338CA;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px solid #E5E7EB;
}

.stat-row:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 13px;
  color: #6B7280;
}

.stat-num {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
}

.notes-textarea {
  width: 100%;
  height: 80px;
  padding: 10px;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-size: 14px;
  resize: none;
  outline: none;
  background: #F9FAFB;
}

.notes-textarea:focus {
  border-color: var(--primary);
  background: #FFFFFF;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.quick-btn {
  padding: 8px 12px;
  background: #F9FAFB;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s ease;
}

.quick-btn:hover {
  background: #E5E7EB;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  padding: 32px;
  max-width: 360px;
  text-align: center;
}

.modal-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 8px;
}

.modal-msg {
  font-size: 14px;
  color: #6B7280;
  margin: 0 0 20px;
}

.modal-stats {
  display: flex;
  gap: 24px;
  justify-content: center;
  margin-bottom: 20px;
  padding: 14px;
  background: #F9FAFB;
  border-radius: 6px;
}

.modal-stat {
  text-align: center;
}

.modal-stat-value {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 2px;
}

.modal-stat-label {
  font-size: 12px;
  color: #6B7280;
}

.modal-btn {
  width: 100%;
  padding: 10px;
  background: var(--primary);
  border: none;
  border-radius: 6px;
  color: #FFFFFF;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.modal-btn:hover {
  background: #4338CA;
}

/* 动画 */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.2s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 900px) {
  .content-area {
    flex-direction: column;
  }

  .timer-card {
    flex: none;
  }
}

@media (max-width: 768px) {
  .timer-container {
    padding: 12px;
    gap: 12px;
  }

  .timer-card {
    padding: 20px;
  }

  .progress-ring {
    width: 160px;
    height: 160px;
  }

  .time-value {
    font-size: 30px;
  }

  .control-btn {
    padding: 8px 14px;
    font-size: 13px;
  }
}

@media (max-width: 480px) {
  .controls {
    flex-direction: column;
  }

  .control-btn {
    width: 100%;
  }

  .info-card {
    padding: 12px;
  }
}
</style>
