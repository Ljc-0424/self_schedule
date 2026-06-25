<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { authApi } from '../../api'

const store = useStore()
const router = useRouter()

const username = ref('')
const password = ref('')
const email = ref('')
const error = ref('')
const registerLoading = ref(false)
const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})

// 验证状态
const validation = reactive({
  username: {
    loading: false,
    valid: false,
    message: ''
  },
  email: {
    loading: false,
    valid: false,
    message: ''
  }
})

// 验证用户名
const validateUsername = async () => {
  if (!username.value) {
    validation.username = { loading: false, valid: false, message: '' }
    return
  }

  if (username.value.length < 5) {
    validation.username = { loading: false, valid: false, message: '用户名至少需要5个字符' }
    return
  }

  if (username.value.length > 50) {
    validation.username = { loading: false, valid: false, message: '用户名不能超过50个字符' }
    return
  }

  validation.username.loading = true

  try {
    const response = await authApi.checkUsername(username.value)
    if (response.data.code === 200) {
      const exists = response.data.data.exists
      if (exists) {
        validation.username = { loading: false, valid: false, message: '该用户名已被使用' }
      } else {
        validation.username = { loading: false, valid: true, message: '用户名可用' }
      }
    } else {
      validation.username = { loading: false, valid: false, message: response.data.message || '验证失败' }
    }
  } catch (e: any) {
    console.error('Username check error:', e)
    const errorMsg = e.response?.data?.message || e.message || '验证失败，请重试'
    validation.username = { loading: false, valid: false, message: errorMsg }
  }
}

// 验证邮箱
const validateEmail = async () => {
  if (!email.value) {
    validation.email = { loading: false, valid: false, message: '请输入邮箱' }
    return
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(email.value)) {
    validation.email = { loading: false, valid: false, message: '请输入有效的邮箱地址' }
    return
  }

  validation.email.loading = true

  try {
    const response = await authApi.checkEmail(email.value)
    if (response.data.code === 200) {
      const exists = response.data.data.exists
      if (exists) {
        validation.email = { loading: false, valid: false, message: '该邮箱已被注册' }
      } else {
        validation.email = { loading: false, valid: true, message: '邮箱可用' }
      }
    } else {
      validation.email = { loading: false, valid: false, message: response.data.message || '验证失败' }
    }
  } catch (e: any) {
    console.error('Email check error:', e)
    const errorMsg = e.response?.data?.message || e.message || '验证失败，请重试'
    validation.email = { loading: false, valid: false, message: errorMsg }
  }
}

const handleRegister = async () => {
  error.value = ''

  if (!username.value || !password.value || !email.value) {
    error.value = '请输入用户名、密码和邮箱'
    return
  }

  if (username.value.length < 5 || username.value.length > 50) {
    error.value = '用户名长度必须在5-50字符之间'
    return
  }

  if (!validation.username.valid) {
    error.value = validation.username.message || '用户名无效'
    return
  }

  if (!validation.email.valid) {
    error.value = validation.email.message || '邮箱无效'
    return
  }

  try {
    registerLoading.value = true
    await store.dispatch('auth/register', {
      username: username.value,
      password: password.value,
      email: email.value
    })
    router.push('/tasks')
  } catch (e: any) {
    error.value = e.message || '注册失败'
  } finally {
    registerLoading.value = false
  }
}
</script>

<template>
  <div class="register-page">
    <div :class="['register-card', { 'visible': isVisible }]">
      <!-- Logo区域 -->
      <div class="card-header">
        <img src="/SelfSchedule.svg" alt="Logo" class="logo" />
        <h1 class="app-title">SelfSchedule</h1>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-header">
          <h2 class="form-title">创建账号</h2>
          <p class="form-desc">开始高效管理您的任务</p>
        </div>

        <!-- 用户名 -->
        <div class="input-group">
          <label class="input-label">用户名</label>
          <input
            v-model="username"
            type="text"
            class="input-field"
            placeholder="5-50个字符"
            @blur="validateUsername"
          />
          <div v-if="validation.username.loading" class="field-hint loading">验证中...</div>
          <div v-else-if="validation.username.message" :class="['field-hint', validation.username.valid ? 'success' : 'error']">
            {{ validation.username.message }}
          </div>
        </div>

        <!-- 邮箱 -->
        <div class="input-group">
          <label class="input-label">
            邮箱
            <span class="label-hint">（用于密码找回）</span>
          </label>
          <input
            v-model="email"
            type="email"
            class="input-field"
            placeholder="请输入邮箱"
            @blur="validateEmail"
          />
          <div v-if="validation.email.loading" class="field-hint loading">验证中...</div>
          <div v-else-if="validation.email.message" :class="['field-hint', validation.email.valid ? 'success' : 'error']">
            {{ validation.email.message }}
          </div>
        </div>

        <!-- 密码 -->
        <div class="input-group">
          <label class="input-label">密码</label>
          <input
            v-model="password"
            type="password"
            class="input-field"
            placeholder="请输入密码"
            autocomplete="new-password"
          />
        </div>

        <!-- 错误提示 -->
        <div v-if="error" class="error-msg">
          <span>{{ error }}</span>
        </div>

        <!-- 注册按钮 -->
        <button type="submit" class="submit-btn" :disabled="registerLoading">
          <span v-if="registerLoading" class="loading-text">
            <span class="spinner"></span>
            注册中...
          </span>
          <span v-else>注册</span>
        </button>

        <!-- 返回登录 -->
        <div class="login-section">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">返回登录</router-link>
        </div>
      </form>

      <!-- 底部 -->
      <div class="card-footer">
        <router-link to="/help">使用帮助</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
  overflow: auto;
  background: #FFFFFF;
}

.register-card {
  width: 100%;
  max-width: 400px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  overflow: hidden;
  opacity: 0;
  transform: translateY(8px);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.register-card.visible {
  opacity: 1;
  transform: translateY(0);
}

/* Logo区域 */
.card-header {
  text-align: center;
  padding: 32px 28px 0;
}

.logo {
  width: 48px;
  height: 48px;
}

.app-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 12px 0 0;
  letter-spacing: -0.01em;
}

/* 表单 */
.register-form {
  padding: 28px;
}

.form-header {
  text-align: center;
  margin-bottom: 24px;
}

.form-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px;
  line-height: 1.3;
}

.form-desc {
  font-size: 14px;
  color: #6B7280;
  margin: 0;
}

/* 输入组 */
.input-group {
  margin-bottom: 16px;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.label-hint {
  font-weight: 400;
  color: #9CA3AF;
  font-size: 12px;
}

.input-field {
  width: 100%;
  height: 40px;
  padding: 0 12px;
  font-size: 14px;
  color: #111827;
  background: #E5E7EB;
  border: 1px solid transparent;
  border-radius: 6px;
  outline: none;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.input-field:focus {
  background: #FFFFFF;
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(var(--primary-rgb), 0.08);
}

.input-field::placeholder {
  color: #9CA3AF;
}

.field-hint {
  font-size: 12px;
  margin-top: 4px;
}

.field-hint.loading {
  color: var(--primary);
}

.field-hint.success {
  color: #3F6212;
}

.field-hint.error {
  color: #B91C1C;
}

/* 错误提示 */
.error-msg {
  padding: 10px 14px;
  background: #FEF2F2;
  color: #B91C1C;
  border-radius: 6px;
  font-size: 13px;
  border: 1px solid #FECACA;
  margin-bottom: 16px;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  color: #FFFFFF;
  background: var(--primary);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s ease;
  margin-bottom: 16px;
}

.submit-btn:hover:not(:disabled) {
  background: #4338CA;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 返回登录 */
.login-section {
  text-align: center;
  font-size: 14px;
  color: #6B7280;
}

.login-link {
  color: var(--primary);
  font-weight: 500;
  text-decoration: none;
  margin-left: 4px;
}

.login-link:hover {
  color: #4338CA;
}

/* 底部 */
.card-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 28px;
  border-top: 1px solid #E5E7EB;
  font-size: 12px;
}

.card-footer a {
  color: #9CA3AF;
  text-decoration: none;
}

.card-footer a:hover {
  color: #6B7280;
}

/* 响应式 */
@media (max-width: 440px) {
  .register-page {
    align-items: flex-start;
    padding-top: 8vh;
  }

  .card-header {
    padding: 24px 20px 0;
  }

  .logo {
    width: 40px;
    height: 40px;
  }

  .app-title {
    font-size: 16px;
  }

  .register-form {
    padding: 20px;
  }
}
</style>
