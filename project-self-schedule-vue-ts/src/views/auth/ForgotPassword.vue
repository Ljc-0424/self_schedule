<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../../api/authApi'

const router = useRouter()

const step = ref(1)
const mode = ref<'email' | 'username'>('email')
const email = ref('')
const username = ref('')
const code = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const error = ref('')
const success = ref('')
const loading = ref(false)
const countdown = ref(0)

let countdownTimer: ReturnType<typeof setInterval> | null = null

const canResend = computed(() => countdown.value === 0)

const resolvedEmail = computed(() => {
  if (mode.value === 'email') return email.value
  return email.value
})

const maskedEmail = computed(() => {
  const addr = resolvedEmail.value
  if (!addr) return ''
  const [name, domain] = addr.split('@')
  if (!domain) return addr
  if (name.length <= 2) return name[0] + '***@' + domain
  return name[0] + '***' + name[name.length - 1] + '@' + domain
})

const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      countdown.value = 0
      if (countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }
  }, 1000)
}

const handleSendCode = async () => {
  error.value = ''
  success.value = ''

  if (mode.value === 'email') {
    if (!email.value.trim()) {
      error.value = '请输入邮箱地址'
      return
    }
  } else {
    if (!username.value.trim()) {
      error.value = '请输入用户名'
      return
    }
  }

  loading.value = true
  try {
    const params: { email?: string; username?: string } = {}
    if (mode.value === 'email') {
      params.email = email.value.trim()
    } else {
      params.username = username.value.trim()
    }
    const response = await authApi.forgotPassword(params)
    if (response.data.code !== 200) {
      error.value = response.data.message || '发送失败'
      return
    }
    const resolved = response.data.data
    if (resolved) {
      email.value = resolved
    }
    success.value = '验证码已发送到您的邮箱'
    startCountdown()
    step.value = 2
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '发送失败'
  } finally {
    loading.value = false
  }
}

const handleVerifyCode = async () => {
  error.value = ''
  success.value = ''
  if (!code.value.trim()) {
    error.value = '请输入验证码'
    return
  }
  loading.value = true
  try {
    const response = await authApi.verifyCode({ email: email.value.trim(), code: code.value.trim() })
    if (response.data.code !== 200) {
      error.value = response.data.message || '验证失败'
      return
    }
    success.value = '验证成功，请设置新密码'
    step.value = 3
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '验证失败'
  } finally {
    loading.value = false
  }
}

const handleResetPassword = async () => {
  error.value = ''
  success.value = ''
  if (!newPassword.value) {
    error.value = '请输入新密码'
    return
  }
  if (newPassword.value.length < 6 || newPassword.value.length > 50) {
    error.value = '密码长度需要在6-50个字符之间'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    const response = await authApi.resetPassword({
      email: email.value.trim(),
      code: code.value.trim(),
      newPassword: newPassword.value
    })
    if (response.data.code !== 200) {
      error.value = response.data.message || '重置失败'
      return
    }
    success.value = '密码重置成功！'
    setTimeout(() => {
      router.push('/login')
    }, 1500)
  } catch (e: any) {
    error.value = e.response?.data?.message || e.message || '重置失败'
  } finally {
    loading.value = false
  }
}

const handleResendCode = async () => {
  error.value = ''
  try {
    const params: { email?: string; username?: string } = {}
    if (mode.value === 'email') {
      params.email = email.value.trim()
    } else {
      params.username = username.value.trim()
    }
    await authApi.forgotPassword(params)
    success.value = '验证码已重新发送'
    startCountdown()
  } catch (e: any) {
    error.value = e.response?.data?.message || '发送失败'
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="forgot-page">
    <el-card class="forgot-card">
      <div class="forgot-header">
        <div class="logo-icon"></div>
        <h1>找回密码</h1>
        <p v-if="step === 1">通过邮箱或用户名找回密码</p>
        <p v-else-if="step === 2">输入邮箱收到的验证码</p>
        <p v-else>设置您的新密码</p>
      </div>

      <div class="step-indicator">
        <div class="step-dot" :class="{ active: step >= 1, done: step > 1 }">1</div>
        <div class="step-line" :class="{ active: step > 1 }"></div>
        <div class="step-dot" :class="{ active: step >= 2, done: step > 2 }">2</div>
        <div class="step-line" :class="{ active: step > 2 }"></div>
        <div class="step-dot" :class="{ active: step >= 3 }">3</div>
      </div>

      <el-alert
        v-if="error"
        :title="error"
        type="error"
        show-icon
        :closable="false"
        style="margin-bottom: 16px;"
      />

      <el-alert
        v-if="success"
        :title="success"
        type="success"
        show-icon
        :closable="false"
        style="margin-bottom: 16px;"
      />

      <el-form v-if="step === 1" label-position="top" @submit.prevent="handleSendCode">
        <div class="mode-switch">
          <div
            class="mode-tab"
            :class="{ active: mode === 'email' }"
            @click="mode = 'email'"
          >
            邮箱找回
          </div>
          <div
            class="mode-tab"
            :class="{ active: mode === 'username' }"
            @click="mode = 'username'"
          >
            用户名找回
          </div>
        </div>

        <el-form-item v-if="mode === 'email'" label="注册邮箱">
          <el-input
            v-model="email"
            placeholder="请输入注册时填写的邮箱"
            size="large"
          />
        </el-form-item>

        <el-form-item v-else label="用户名">
          <el-input
            v-model="username"
            placeholder="请输入注册时的用户名"
            size="large"
          />
          <div class="mode-hint">
            系统将向该用户绑定的邮箱发送验证码
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            native-type="submit"
            type="primary"
            size="large"
            style="width: 100%;"
            :loading="loading"
          >
            发送验证码
          </el-button>
        </el-form-item>
      </el-form>

      <el-form v-if="step === 2" label-position="top" @submit.prevent="handleVerifyCode">
        <el-form-item label="验证码">
          <div class="code-input-row">
            <el-input
              v-model="code"
              placeholder="请输入6位验证码"
              size="large"
              maxlength="6"
            />
            <el-button
              size="large"
              :disabled="!canResend"
              @click="handleResendCode"
            >
              {{ canResend ? '重新发送' : countdown + '秒' }}
            </el-button>
          </div>
          <div class="email-hint">已发送至 {{ maskedEmail }}</div>
        </el-form-item>
        <el-form-item>
          <el-button
            native-type="submit"
            type="primary"
            size="large"
            style="width: 100%;"
            :loading="loading"
          >
            验证
          </el-button>
        </el-form-item>
      </el-form>

      <el-form v-if="step === 3" label-position="top" @submit.prevent="handleResetPassword">
        <el-form-item label="新密码">
          <el-input
            v-model="newPassword"
            type="password"
            placeholder="请输入新密码（6-50位）"
            size="large"
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            size="large"
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            native-type="submit"
            type="primary"
            size="large"
            style="width: 100%;"
            :loading="loading"
          >
            重置密码
          </el-button>
        </el-form-item>
      </el-form>

      <div class="auth-extra">
        <el-link type="primary" @click="goToLogin">← 返回登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.forgot-page {
  width: 100%;
  min-height: 100vh;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
}

.forgot-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
}

.forgot-header {
  text-align: center;
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.forgot-header h1 {
  color: var(--primary);
  font-size: 28px;
  font-weight: 700;
  margin: 0;
}

.forgot-header p {
  color: #9CA3AF;
  font-size: 13px;
  margin: 6px 0 0;
}

.step-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 20px 0 24px;
  gap: 0;
}

.step-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #D1D5DB;
  color: #9CA3AF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s;
}

.step-dot.active {
  background: var(--primary);
  color: #fff;
}

.step-dot.done {
  background: #3F6212;
  color: #fff;
}

.step-line {
  width: 60px;
  height: 2px;
  background: #D1D5DB;
  transition: all 0.3s;
}

.step-line.active {
  background: #3F6212;
}

.mode-switch {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #D1D5DB;
  margin-bottom: 16px;
}

.mode-tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  cursor: pointer;
  font-size: 14px;
  color: #9CA3AF;
  background: #F9FAFB;
  transition: all 0.25s;
  user-select: none;
}

.mode-tab:first-child {
  border-right: 1px solid #D1D5DB;
}

.mode-tab.active {
  background: #EEF2FF;
  color: var(--primary);
  font-weight: 600;
}

.mode-hint {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 4px;
}

.code-input-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.code-input-row .el-input {
  flex: 1;
}

.email-hint {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 4px;
}

.auth-extra {
  text-align: center;
  margin-top: 8px;
  margin-bottom: 8px;
}
</style>
