<script setup lang="ts">
import { ref, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import PageLayout from '../../components/PageLayout.vue'
import FeatureSection from './FeatureSection.vue'
import ApiConfigSection from './ApiConfigSection.vue'
import WeChatConfigSection from './WeChatConfigSection.vue'
import StatusSection from './StatusSection.vue'
import TipsSection from './TipsSection.vue'
import FAQSection from './FAQSection.vue'

const store = useStore()
const router = useRouter()
const isLoggedIn = computed(() => store.getters['auth/isLoggedIn'])
const activeTab = ref('intro')
</script>

<template>
  <PageLayout title="使用帮助" subtitle="了解如何使用这个项目的所有功能">
    <div class="help-page">
      <div v-if="!isLoggedIn" class="guest-banner">
        <span>您还未登录，以下为项目功能介绍，欢迎体验！</span>
        <el-button type="primary" size="small" @click="router.push('/login')">去登录</el-button>
        <el-button size="small" @click="router.push('/register')">去注册</el-button>
      </div>

      <el-tabs v-model="activeTab" class="help-tabs">
        <el-tab-pane label="项目介绍" name="intro">
          <div class="tab-scroll">
            <div class="section-card">
              <div class="section-header">
                <span class="section-icon"></span>
                <h2>项目简介</h2>
              </div>
              <div class="section-content">
                <p>SelfSchedule 是一款智能任务管理助手，帮助您高效管理日常任务和日程安排。通过集成AI技术，您可以自然语言输入任务，系统会自动解析并创建任务提醒。系统默认使用 <strong>智谱AI GLM-4.7-flash</strong> 模型，这是一个长期免费的模型，无需担心会消耗您其他模型的额度。</p>
              </div>
            </div>
            <FeatureSection />
          </div>
        </el-tab-pane>

        <el-tab-pane label="配置指南" name="config">
          <div class="tab-scroll">
            <ApiConfigSection />
            <WeChatConfigSection />
          </div>
        </el-tab-pane>

        <el-tab-pane label="使用说明" name="usage">
          <div class="tab-scroll">
            <StatusSection />
            <TipsSection />
          </div>
        </el-tab-pane>

        <el-tab-pane label="常见问题" name="faq">
          <div class="tab-scroll">
            <FAQSection />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </PageLayout>
</template>

<style scoped>
.help-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.guest-banner {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 14px 20px;
  margin-bottom: 12px;
  background: #EEF2FF;
  border: 1px solid #C7D2FE;
  border-radius: 8px;
  color: var(--primary);
  font-size: 14px;
  flex-wrap: wrap;
}

.help-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  overflow: hidden;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.help-tabs :deep(.el-tabs__header) {
  flex-shrink: 0;
  margin-bottom: 0;
  padding: 0 12px;
  background: #FFFFFF;
  border-radius: 8px 8px 0 0;
  border-bottom: 1px solid #E5E7EB;
}

.help-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.help-tabs :deep(.el-tabs__item) {
  font-size: 13px;
  font-weight: 500;
  padding: 0 14px;
  height: 46px;
  line-height: 46px;
  color: #6B7280;
}

.help-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary);
  font-weight: 600;
}

.help-tabs :deep(.el-tabs__active-bar) {
  background: var(--primary);
  height: 2px;
  border-radius: 1px;
}

.help-tabs :deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.help-tabs :deep(.el-tab-pane) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tab-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.tab-scroll::-webkit-scrollbar {
  width: 5px;
}

.tab-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.tab-scroll::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.section-card {
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid #D1D5DB;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  flex-shrink: 0;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 22px;
  background: #E5E7EB;
  border-bottom: 1px solid #D1D5DB;
}

.section-icon {
  font-size: 22px;
}

.section-header h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.section-content {
  padding: 20px 22px;
}

.section-content p {
  color: #6B7280;
  line-height: 1.7;
  margin: 0 0 12px 0;
}

.section-content p:last-child {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .help-tabs :deep(.el-tabs__item) {
    font-size: 12px;
    padding: 0 10px;
  }

  .section-header {
    padding: 14px 16px;
  }

  .section-content {
    padding: 16px;
  }
}
</style>
