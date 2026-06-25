<script setup lang="ts">
import { computed } from 'vue'
import { useStore } from 'vuex'
import Navbar from './Navbar.vue'

defineProps<{
  title?: string
  subtitle?: string
}>()

const store = useStore()

const sidebarCollapsed = computed(() => store.getters['sidebar/isCollapsed'])
</script>

<template>
  <el-container class="page-layout">
    <Navbar />
    
    <el-main class="page-content" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <div class="content-wrapper">
        <header v-if="title || subtitle" class="page-header">
          <div class="header-content">
            <h1 class="page-title">{{ title }}</h1>
            <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
          </div>
        </header>
        
        <div class="page-body">
          <slot />
        </div>
      </div>
    </el-main>
  </el-container>
</template>

<style scoped>
.page-layout {
  height: 100vh;
  overflow: hidden;
}

.page-content {
  padding: 30px;
  padding-left: 250px;
  height: 100%;
  box-sizing: border-box;
  transition: padding-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  justify-content: center;
  overflow: hidden;
}

.page-content.sidebar-collapsed {
  padding-left: 104px;
}

.content-wrapper {
  width: 100%;
  max-width: 1200px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
  flex: 1;
}

.page-body {
  width: 100%;
  flex: 1;
  min-height: 0;
  overflow: hidden;
  padding-bottom: 24px;
}

.page-header {
  flex-shrink: 0;
  margin-bottom: 24px;
}

.header-content {
  width: 100%;
  background: #FFFFFF;
  padding: 16px 28px;
  border-radius: 8px;
  border: 1px solid #D1D5DB;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.08);
}

.page-title {
  font-size: 26px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  word-break: break-word;
}

.page-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin: 8px 0 0 0;
}

@media (max-width: 991px) {
  .page-content {
    padding: 24px;
    padding-left: 250px;
  }

  .page-content.sidebar-collapsed {
    padding-left: 104px;
  }

  .page-title {
    font-size: 22px;
  }
}

@media (max-width: 767px) {
  .page-content {
    padding: 16px;
    padding-left: 104px;
  }

  .page-content.sidebar-collapsed {
    padding-left: 104px;
  }

  .content-wrapper {
    max-width: 100%;
  }

  .page-header {
    margin-bottom: 16px;
  }

  .header-content {
    padding: 12px 20px;
    border-radius: 10px;
    width: 100%;
  }

  .page-subtitle {
    font-size: 13px;
  }
}

@media (max-width: 575px) {
  .page-content,
  .page-content.sidebar-collapsed {
    padding: 6px;
    padding-left: 6px;
    padding-top: 54px;
  }

  .content-wrapper {
    max-width: 100%;
  }

  .page-header {
    display: none;
  }
}
</style>
