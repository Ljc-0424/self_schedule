<script setup lang="ts">
/**
 * 成就墙组件
 * 根据用户统计数据展示成就解锁情况
 */
import { computed } from 'vue';

interface Props {
  totalFocusSeconds?: number;
  totalCompletedTasks?: number;
  currentStreak?: number;
  maxStreak?: number;
}

const props = withDefaults(defineProps<Props>(), {
  totalFocusSeconds: 0,
  totalCompletedTasks: 0,
  currentStreak: 0,
  maxStreak: 0,
});

interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  category: string;
  target: number;
  current: number;
  unlocked: boolean;
  unlockedAt?: string;
}

// 根据统计数据计算成就列表
const achievements = computed<Achievement[]>(() => {
  const focusMinutes = Math.floor(props.totalFocusSeconds / 60);

  const defs: Array<{ id: string; name: string; desc: string; icon: string; cat: string; target: number; current: number; unlockedAt?: string }> = [
    { id: 'focus-1', name: '初学者', desc: '累计专注60分钟', icon: 'focus', cat: '专注时长', target: 60, current: focusMinutes },
    { id: 'focus-2', name: '进阶者', desc: '累计专注600分钟', icon: 'focus', cat: '专注时长', target: 600, current: focusMinutes },
    { id: 'focus-3', name: '大师', desc: '累计专注3600分钟', icon: 'focus', cat: '专注时长', target: 3600, current: focusMinutes },
    { id: 'task-1', name: '起步', desc: '完成10个任务', icon: 'task', cat: '任务完成', target: 10, current: props.totalCompletedTasks },
    { id: 'task-2', name: '高效', desc: '完成50个任务', icon: 'task', cat: '任务完成', target: 50, current: props.totalCompletedTasks },
    { id: 'task-3', name: '卓越', desc: '完成200个任务', icon: 'task', cat: '任务完成', target: 200, current: props.totalCompletedTasks },
    { id: 'streak-1', name: '启程', desc: '连续专注3天', icon: 'streak', cat: '连续天数', target: 3, current: props.maxStreak },
    { id: 'streak-2', name: '坚持', desc: '连续专注7天', icon: 'streak', cat: '连续天数', target: 7, current: props.maxStreak },
    { id: 'streak-3', name: '习惯', desc: '连续专注21天', icon: 'streak', cat: '连续天数', target: 21, current: props.maxStreak },
    { id: 'streak-4', name: '自律', desc: '连续专注60天', icon: 'streak', cat: '连续天数', target: 60, current: props.maxStreak },
  ];

  return defs.map(d => {
    const unlocked = d.current >= d.target;
    return {
      id: d.id,
      name: d.name,
      description: d.desc,
      icon: d.icon,
      category: d.cat,
      target: d.target,
      current: Math.min(d.current, d.target),
      unlocked,
      unlockedAt: unlocked ? '已达成' : undefined,
    };
  });
});

// 按类别分组
const categories = computed(() => {
  const map = new Map<string, Achievement[]>();
  for (const a of achievements.value) {
    if (!map.has(a.category)) map.set(a.category, []);
    map.get(a.category)!.push(a);
  }
  return Array.from(map.entries());
});

// 已解锁数量
const unlockedCount = computed(() => achievements.value.filter(a => a.unlocked).length);

const formatProgress = (current: number, target: number) => {
  if (target >= 1000) return `${(current / 1000).toFixed(1)}k / ${(target / 1000).toFixed(0)}k`;
  return `${current} / ${target}`;
};
</script>

<template>
  <div class="achievement-wall">
    <div class="achievement-header">
      <div class="achievement-title-row">
        <h3>成就墙</h3>
        <span class="achievement-count">{{ unlockedCount }} / {{ achievements.length }}</span>
      </div>
      <p class="achievement-desc">通过坚持专注和完成任务来解锁成就</p>
    </div>

    <div v-for="[cat, items] in categories" :key="cat" class="achievement-category">
      <h4 class="category-title">{{ cat }}</h4>
      <div class="achievement-list">
        <div
          v-for="item in items"
          :key="item.id"
          :class="['achievement-item', { unlocked: item.unlocked }]"
        >
          <div class="item-left">
            <div class="item-icon">
              <!-- 专注图标 -->
              <svg v-if="item.icon === 'focus'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"/>
                <circle cx="12" cy="12" r="6"/>
                <circle cx="12" cy="12" r="2"/>
              </svg>
              <!-- 任务图标 -->
              <svg v-else-if="item.icon === 'task'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 11l3 3L22 4"/>
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
              </svg>
              <!-- 连续图标 -->
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
              </svg>
            </div>
            <div class="item-info">
              <span class="item-name">{{ item.name }}</span>
              <span v-if="item.unlocked" class="item-date">{{ item.unlockedAt }}</span>
              <span v-else class="item-desc">{{ item.description }}</span>
            </div>
          </div>
          <div class="item-right">
            <div v-if="!item.unlocked" class="progress-bar">
              <div
                class="progress-fill"
                :style="{ width: Math.min((item.current / item.target) * 100, 100) + '%' }"
              />
            </div>
            <span class="progress-text">{{ formatProgress(item.current, item.target) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.achievement-wall {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
  border: 1px solid #D1D5DB;
  padding: 24px;
}

.achievement-header {
  margin-bottom: 24px;
}

.achievement-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.achievement-title-row h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
}

.achievement-count {
  font-size: 13px;
  color: #6B7280;
  background: #E5E7EB;
  padding: 2px 10px;
  border-radius: 10px;
}

.achievement-desc {
  margin: 6px 0 0 0;
  font-size: 13px;
  color: #9CA3AF;
}

.achievement-category {
  margin-bottom: 20px;
}

.achievement-category:last-child {
  margin-bottom: 0;
}

.category-title {
  margin: 0 0 10px 0;
  font-size: 13px;
  font-weight: 500;
  color: #6B7280;
  padding-bottom: 6px;
  border-bottom: 1px solid #E5E7EB;
}

.achievement-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.achievement-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  transition: background 0.15s;
}

.achievement-item:not(.unlocked) {
  background: #FAFAFA;
}

.achievement-item.unlocked {
  background: #F0F7FF;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.item-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.unlocked .item-icon {
  background: #EFF6FF;
  color: var(--primary);
}

.item-icon {
  background: #E5E7EB;
  color: #9CA3AF;
}

.item-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
}

.unlocked .item-name {
  color: #111827;
}

.item-name {
  color: #6B7280;
}

.item-date {
  font-size: 11px;
  color: var(--primary);
}

.item-desc {
  font-size: 11px;
  color: #9CA3AF;
}

.item-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.progress-bar {
  width: 80px;
  height: 6px;
  background: #D1D5DB;
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: var(--primary);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 11px;
  color: #9CA3AF;
  white-space: nowrap;
  min-width: 60px;
  text-align: right;
}

@media (max-width: 575.98px) {
  .achievement-wall {
    padding: 16px;
  }

  .achievement-item {
    padding: 8px 10px;
  }

  .progress-bar {
    width: 60px;
  }
}
</style>
