<script setup lang="ts">
import { ref } from 'vue';

interface ReminderStats {
  total: number
  pending: number
  sent: number
  read: number
  dismissed: number
  expired: number
}

defineProps<{
  stats: ReminderStats;
}>();

const emit = defineEmits<{
  (e: 'markAllAsRead'): void;
  (e: 'triggerScan'): void;
  (e: 'cleanupCompleted'): void;
}>();

const showMenu = ref(false);

const toggleMenu = () => {
  showMenu.value = !showMenu.value;
};

const handleCloseMenu = (e: MouseEvent) => {
  const target = e.target as HTMLElement;
  if (!target.closest('.menu-wrapper')) {
    showMenu.value = false;
  }
};

defineExpose({
  closeMenu: () => { showMenu.value = false; }
});

if (typeof window !== 'undefined') {
  document.addEventListener('click', handleCloseMenu);
}
</script>

<template>
  <div class="stats-bar">
    <div class="stat-item">
      <span class="stat-value">{{ stats.total }}</span>
      <span class="stat-label">总提醒</span>
    </div>
    <div class="stat-item pending">
      <span class="stat-value">{{ stats.pending }}</span>
      <span class="stat-label">待处理</span>
    </div>
    <div class="stat-item read">
      <span class="stat-value">{{ stats.read }}</span>
      <span class="stat-label">已读</span>
    </div>
    <div class="stat-item dismissed">
      <span class="stat-value">{{ stats.dismissed }}</span>
      <span class="stat-label">已忽略</span>
    </div>
    <div class="stat-item expired">
      <span class="stat-value">{{ stats.expired }}</span>
      <span class="stat-label">已过期</span>
    </div>
    <button class="action-btn" @click="emit('markAllAsRead')">✓ 全部标为已读</button>
    
    <div class="menu-wrapper">
      <button class="action-btn secondary menu-btn" @click.stop="toggleMenu">
        管理
        <span class="menu-arrow">{{ showMenu ? '▲' : '▼' }}</span>
      </button>
      <div v-if="showMenu" class="dropdown-menu">
        <button class="menu-item" @click="emit('triggerScan'); showMenu = false">触发扫描</button>
        <button class="menu-item danger" @click="emit('cleanupCompleted'); showMenu = false">清理已完成</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stats-bar {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 
    0 6px 24px rgba(0, 0, 0, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  margin-bottom: 20px;
  overflow: visible;
  position: relative;
  z-index: 10;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
}

.stat-item.pending .stat-value {
  color: #B91C1C;
}

.stat-item.read .stat-value {
  color: #3F6212;
}

.stat-item.dismissed .stat-value {
  color: #6b7280;
}

.stat-item.expired .stat-value {
  color: #f59e0b;
}

.stat-label {
  font-size: 12px;
  color: #6B7280;
  margin-top: 2px;
}

.action-btn {
  padding: 8px 16px;
  background: var(--primary);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.action-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(var(--primary-rgb), 0.35);
}

.action-btn.secondary {
  background: #E5E7EB;
  color: #6B7280;
}

.action-btn.secondary:hover {
  background: #D1D5DB;
  box-shadow: none;
}

.action-btn.menu-btn {
  display: flex;
  align-items: center;
  gap: 4px;
}

.menu-arrow {
  font-size: 10px;
  transition: transform 0.2s;
}

.menu-wrapper {
  position: relative;
  z-index: 1000;
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.15);
  padding: 4px;
  min-width: 140px;
  z-index: 1001;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.menu-item {
  width: 100%;
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
}

.menu-item:hover {
  background: #f3f4f6;
}

.menu-item.danger {
  color: #B91C1C;
}

.menu-item.danger:hover {
  background: #fef2f2;
}

@media (max-width: 1199.98px) {
  .stats-bar {
    gap: 16px;
    padding: 14px 16px;
  }
  
  .stat-value {
    font-size: 22px;
  }
  
  .action-btn {
    padding: 7px 14px;
    font-size: 12px;
  }
}

@media (max-width: 991.98px) {
  .stats-bar {
    flex-wrap: wrap;
    gap: 12px;
    padding: 12px 14px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .stat-label {
    font-size: 11px;
  }
  
  .action-btn {
    padding: 6px 12px;
    font-size: 11px;
  }
}

@media (max-width: 767.98px) {
  .stats-bar {
    gap: 10px;
    padding: 10px 12px;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .stat-label {
    font-size: 10px;
  }
  
  .action-btn {
    padding: 5px 10px;
    font-size: 10px;
    border-radius: 8px;
  }
}

@media (max-width: 575.98px) {
  .stats-bar {
    gap: 8px;
    padding: 8px 10px;
    border-radius: 12px;
  }
  
  .stat-value {
    font-size: 16px;
  }
  
  .stat-label {
    font-size: 9px;
  }
  
  .action-btn {
    padding: 4px 8px;
    font-size: 9px;
    border-radius: 6px;
  }
  
  .dropdown-menu {
    min-width: 120px;
    padding: 3px;
  }
  
  .menu-item {
    padding: 6px 10px;
    font-size: 12px;
  }
}
</style>