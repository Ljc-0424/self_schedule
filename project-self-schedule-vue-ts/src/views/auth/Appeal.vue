<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { appealApi } from '../../api/appealApi'
import type { BanInfoVO } from '../../types'

const route = useRoute()
const router = useRouter()

const username = ref('')
const content = ref('')
const loading = ref(false)
const banInfo = ref<BanInfoVO | null>(null)
const checkingBan = ref(true)

const formatDateTime = (dateStr: string | null | undefined) => {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN')
}

onMounted(async () => {
  username.value = (route.query.username as string) || ''
  if (username.value) {
    try {
      const res = await appealApi.getBanInfo(username.value)
      if (res.data.code === 200) {
        banInfo.value = res.data.data
      }
    } catch {
      banInfo.value = null
    }
  }
  checkingBan.value = false
})

const handleSubmit = async () => {
  if (!username.value.trim()) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!content.value.trim()) {
    ElMessage.warning('请输入申诉理由')
    return
  }
  if (content.value.trim().length > 2000) {
    ElMessage.warning('申诉理由不能超过2000字')
    return
  }

  loading.value = true
  try {
    const res = await appealApi.submitAppeal({
      username: username.value.trim(),
      content: content.value.trim()
    })
    if (res.data.code === 200) {
      ElMessage.success('申诉提交成功，请耐心等待审核')
      router.push('/login')
    } else {
      ElMessage.error(res.data.message || '提交失败')
    }
  } catch (e: any) {
    const msg = e.response?.data?.message || e.message || '提交失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<template>
  <div class="appeal-page">
    <el-card class="appeal-card">
      <div class="appeal-header">
        <div class="logo-icon"></div>
        <h1>提交申诉</h1>
        <p>如果您认为账号被误封，可以在此提交申诉</p>
      </div>

      <div v-if="checkingBan" class="loading-state">
        <el-icon class="is-loading" :size="24"><Loading /></el-icon>
        <span>正在验证账号状态...</span>
      </div>

      <template v-else>
        <div v-if="banInfo" class="ban-summary">
          <el-alert type="error" :closable="false" show-icon>
            <template #title>
              <span>您的账号已被封禁</span>
            </template>
            <div class="ban-detail-inline">
              <p><strong>封禁原因：</strong>{{ banInfo.banReason }}</p>
              <p><strong>封禁时间：</strong>{{ formatDateTime(banInfo.banTime) }}</p>
              <p v-if="banInfo.status === 2 && banInfo.banEndTime">
                <strong>到期时间：</strong>{{ formatDateTime(banInfo.banEndTime) }}
              </p>
            </div>
          </el-alert>
        </div>

        <div v-if="banInfo && banInfo.appealStatus === 0" class="appeal-status-notice">
          <el-alert title="您已提交申诉，正在审核中，请耐心等待" type="warning" :closable="false" show-icon />
        </div>

        <div v-else-if="banInfo && banInfo.appealStatus === 2" class="appeal-status-notice">
          <el-alert title="您的申诉已被驳回，无法再次申诉" type="error" :closable="false" show-icon>
            <template v-if="banInfo.auditNote" #default>
              <p>驳回理由：{{ banInfo.auditNote }}</p>
            </template>
          </el-alert>
        </div>

        <el-form
          v-if="!banInfo || banInfo.appealStatus === -1"
          label-position="top"
          @submit.prevent="handleSubmit"
        >
          <el-form-item label="用户名">
            <el-input
              v-model="username"
              placeholder="请输入被封禁的用户名"
              size="large"
              :disabled="!!route.query.username"
            />
          </el-form-item>

          <el-form-item label="申诉理由">
            <el-input
              v-model="content"
              type="textarea"
              :rows="6"
              placeholder="请详细说明您认为账号被误封的原因，我们会尽快审核..."
              maxlength="2000"
              show-word-limit
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
              提交申诉
            </el-button>
          </el-form-item>
        </el-form>
      </template>

      <div class="appeal-footer">
        <el-link type="primary" @click="goToLogin">← 返回登录</el-link>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts">
import { Loading } from '@element-plus/icons-vue'
export default {
  components: { Loading }
}
</script>

<style scoped>
.appeal-page {
  min-height: 100vh;
  background-image: url('/images/login-bg.png');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.appeal-card {
  width: 100%;
  max-width: 500px;
  border-radius: 12px;
}

.appeal-header {
  text-align: center;
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.appeal-header h1 {
  color: #111827;
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.appeal-header p {
  color: #9CA3AF;
  font-size: 13px;
  margin: 8px 0 0;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: #9CA3AF;
}

.ban-summary {
  margin-bottom: 20px;
}

.ban-detail-inline {
  margin-top: 8px;
}

.ban-detail-inline p {
  margin: 4px 0;
  font-size: 13px;
  color: #6B7280;
}

.appeal-status-notice {
  margin-bottom: 20px;
}

.appeal-footer {
  text-align: center;
  margin-top: 16px;
}
</style>
