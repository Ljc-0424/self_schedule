<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

const store = useStore()
const router = useRouter()

const account = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)
const isVisible = ref(false)

onMounted(() => {
  setTimeout(() => {
    isVisible.value = true
  }, 100)
})

const handleLogin = async () => {
  error.value = ''
  if (!account.value || !password.value) {
    error.value = '请输入账号和密码'
    return
  }

  loading.value = true
  try {
    await store.dispatch('auth/login', {
      account: account.value,
      password: password.value
    })
    router.push('/tasks')
  } catch (e: any) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <!-- 登录卡片 -->
    <div :class="['login-card', { 'visible': isVisible }]">
      <!-- Logo区域 -->
      <div class="card-header">
        <img src="/SelfSchedule.svg" alt="Logo" class="logo" />
        <h1 class="app-title">SelfSchedule</h1>
      </div>

      <!-- 表单 -->
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-header">
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-desc">请登录您的账号</p>
        </div>

        <!-- 账号 -->
        <div class="input-group">
          <label class="input-label">账号</label>
          <input
            v-model="account"
            type="text"
            class="input-field"
            placeholder="用户名或邮箱"
          />
        </div>

        <!-- 密码 -->
        <div class="input-group">
          <div class="label-row">
            <label class="input-label">密码</label>
            <router-link to="/forgot-password" class="forgot-link">
              忘记密码？
            </router-link>
          </div>
          <div class="password-input">
            <input
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              class="input-field"
              placeholder="请输入密码"
            />
            <button type="button" class="eye-btn" @click="showPassword = !showPassword">
              <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
              <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- 错误提示 -->
        <div v-if="error" class="error-msg">
          <span>{{ error }}</span>
        </div>

        <!-- 登录按钮 -->
        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="loading" class="loading-text">
            <span class="spinner"></span>
            登录中...
          </span>
          <span v-else>登录</span>
        </button>

        <!-- 注册 -->
        <div class="register-section">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </form>

      <!-- 底部 -->
      <div class="card-footer">
        <router-link to="/help">使用帮助</router-link>
        <span class="dot">·</span>
        <span>服务条款</span>
        <span class="dot">·</span>
        <span>隐私政策</span>
      </div>
    </div>

    <!-- 底部文字 -->
    <div class="page-footer">
      <p>© 2024 SelfSchedule</p>
    </div>
  </div>
</template>

<style scoped>
.login-page {
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
  overflow: hidden;
  background: #FFFFFF;
}

/* 登录卡片 */
.login-card {
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

.login-card.visible {
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
.login-form {
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

.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.input-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.label-row .input-label {
  margin-bottom: 0;
}

.forgot-link {
  font-size: 13px;
  color: var(--primary);
  text-decoration: none;
  font-weight: 500;
}

.forgot-link:hover {
  color: #4338CA;
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

.password-input {
  position: relative;
}

.password-input .input-field {
  padding-right: 40px;
}

.eye-btn {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: #9CA3AF;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;
}

.eye-btn:hover {
  color: #6B7280;
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

/* 注册区域 */
.register-section {
  text-align: center;
  font-size: 14px;
  color: #6B7280;
}

.register-link {
  color: var(--primary);
  font-weight: 500;
  text-decoration: none;
  margin-left: 4px;
}

.register-link:hover {
  color: #4338CA;
}

/* 底部 */
.card-footer {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 16px 28px;
  border-top: 1px solid #E5E7EB;
  font-size: 12px;
  color: #9CA3AF;
}

.card-footer a {
  color: #9CA3AF;
  text-decoration: none;
}

.card-footer a:hover {
  color: #6B7280;
}

.dot {
  color: #D1D5DB;
}

/* 页面底部 */
.page-footer {
  position: fixed;
  bottom: 20px;
  left: 0;
  right: 0;
  text-align: center;
  z-index: 10;
}

.page-footer p {
  font-size: 12px;
  color: #9CA3AF;
  margin: 0;
}

/* 响应式 */
@media (max-width: 440px) {
  .login-page {
    padding: 16px;
    align-items: flex-start;
    padding-top: 10vh;
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

  .login-form {
    padding: 20px;
  }

  .form-title {
    font-size: 16px;
  }

  .page-footer {
    display: none;
  }
}

@media (max-width: 360px) {
  .login-page {
    padding-top: 5vh;
  }

  .login-form {
    padding: 16px;
  }

  .card-footer {
    padding: 14px 16px;
    gap: 8px;
  }

  .dot {
    display: none;
  }
}
</style>
