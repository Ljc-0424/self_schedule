<script setup lang="ts">
import { ref } from 'vue'
import PageLayout from '../../components/PageLayout.vue'
import FocusTimer from './FocusTimer.vue'
import FocusHistory from './FocusHistory.vue'
import TaskSearchModal from './TaskSearchModal.vue'
import FocusRecordDetail from './FocusRecordDetail.vue'

const activeTab = ref<'timer' | 'history'>('timer')
</script>

<template>
  <PageLayout>
    <div class="focus-page">
      <el-tabs v-model="activeTab" class="focus-tabs">
        <el-tab-pane label="专注计时" name="timer">
          <div class="tab-content">
            <FocusTimer />
          </div>
        </el-tab-pane>
        <el-tab-pane label="专注记录" name="history">
          <div class="tab-content">
            <FocusHistory />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <TaskSearchModal />
    <FocusRecordDetail />
  </PageLayout>
</template>

<style scoped>
.focus-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.focus-tabs {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.focus-tabs :deep(.el-tabs__header) {
  flex-shrink: 0;
  margin-bottom: 0;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-bottom: none;
  border-radius: 8px 8px 0 0;
  padding: 0 16px;
}

.focus-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.focus-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  font-weight: 500;
  padding: 0 24px;
  height: 44px;
  line-height: 44px;
  color: #6B7280;
  transition: color 0.2s ease;
}

.focus-tabs :deep(.el-tabs__item:hover) {
  color: #111827;
}

.focus-tabs :deep(.el-tabs__item.is-active) {
  color: var(--primary);
  font-weight: 600;
}

.focus-tabs :deep(.el-tabs__active-bar) {
  background: var(--primary);
  height: 2px;
  border-radius: 1px;
}

.focus-tabs :deep(.el-tabs__content) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-top: none;
  border-radius: 0 0 8px 8px;
}

.focus-tabs :deep(.el-tab-pane) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.tab-content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
}
</style>
