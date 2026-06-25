<script setup lang="ts">
import { ref } from 'vue'
import { useStore } from 'vuex'

const store = useStore()
const testResult = ref('')
const isLoading = ref(false)

const testAI = async () => {
  isLoading.value = true
  testResult.value = '🔍 开始诊断...\n\n'

  try {
    // 1. 检查用户登录状态
    testResult.value += '1. 检查登录状态...\n'
    const token = localStorage.getItem('token')
    if (!token) {
      testResult.value += '❌ 未登录\n\n'
      return
    }
    testResult.value += '✅ 已登录\n\n'

    // 2. 检查用户信息
    testResult.value += '2. 检查用户信息...\n'
    const userResponse = await fetch('/api/user/info', {
      headers: { 'Authorization': `Bearer ${token}` }
    })
    const userData = await userResponse.json()
    if (userData.code === 200 && userData.data) {
      const hasApiKey = !!userData.data.aiApiKey
      testResult.value += hasApiKey ?
        '✅ 已配置API Key\n' :
        '❌ 未配置API Key\n'
      testResult.value += `   用户ID: ${userData.data.id}\n`
      testResult.value += `   用户名: ${userData.data.username}\n\n`
    } else {
      testResult.value += '❌ 获取用户信息失败\n\n'
    }

    // 3. 测试同步AI接口
    testResult.value += '3. 测试同步AI接口...\n'
    try {
      const syncResponse = await fetch('/api/ai/parse-task', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ prompt: '测试' })
      })
      const syncResult = await syncResponse.json()
      testResult.value += `   响应码: ${syncResult.code}\n`
      testResult.value += `   消息: ${syncResult.message || '无'}\n`
      if (syncResult.data) {
        testResult.value += `   数据: ${JSON.stringify(syncResult.data, null, 2)}\n`
      }
      testResult.value += '\n'
    } catch (e: any) {
      testResult.value += `   ❌ 请求失败: ${e.message}\n\n`
    }

    // 4. 测试异步AI接口
    testResult.value += '4. 测试异步AI接口...\n'
    try {
      const asyncResponse = await fetch('/api/ai/parse-task/async', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ prompt: '测试' })
      })
      const asyncResult = await asyncResponse.json()
      testResult.value += `   响应码: ${asyncResult.code}\n`
      testResult.value += `   消息: ${asyncResult.message || '无'}\n`
      if (asyncResult.data) {
        testResult.value += `   数据: ${JSON.stringify(asyncResult.data, null, 2)}\n`
      }
      testResult.value += '\n'
    } catch (e: any) {
      testResult.value += `   ❌ 请求失败: ${e.message}\n\n`
    }

    // 5. 检查AI日志
    testResult.value += '5. 检查AI日志目录...\n'
    testResult.value += '   请手动检查 D:/1.5hour_per_day/self_schedule/ai_logs/ 目录\n\n'

    testResult.value += '📋 诊断完成\n'

  } catch (error: any) {
    testResult.value += `❌ 诊断过程出错: ${error.message}\n`
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="diagnostic-panel">
    <h3>AI功能诊断</h3>
    <p class="hint">检查AI功能是否正常工作</p>
    <button @click="testAI" :disabled="isLoading" class="test-btn">
      {{ isLoading ? '诊断中...' : '开始诊断' }}
    </button>
    <pre class="test-output">{{ testResult }}</pre>
  </div>
</template>

<style scoped>
.diagnostic-panel {
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  margin: 20px;
}

.hint {
  color: #666;
  font-size: 13px;
  margin-bottom: 16px;
}

.test-btn {
  padding: 10px 20px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 16px;
  transition: background 0.2s ease;
}

.test-btn:hover {
  background: #4338CA;
}

.test-btn:disabled {
  background: #ccc;
}

.test-output {
  background: white;
  padding: 16px;
  border-radius: 8px;
  font-size: 13px;
  white-space: pre-wrap;
  overflow-x: auto;
  border: 1px solid #e5e7eb;
  max-height: 400px;
  overflow-y: auto;
}
</style>
