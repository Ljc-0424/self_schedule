<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import Pagination from '../../components/Pagination.vue'
import Loading from '../../components/Loading.vue'
import { focusRecordApi } from '../../api'
import {
  isLoadingHistory,
  focusRecords,
  fetchFocusRecords,
  fetchFocusRecordDetail,
  deleteFocusRecord,
  batchDeleteFocusRecords,
  isFocusBatchDeleting as isBatchDeleting,
  selectedRecordIds,
  toggleBatchDeleting,
  cancelBatchDelete,
  toggleRecordSelection,
  toggleSelectAllRecords,
  getFocusStatusLabel,
  formatDuration,
  formatDateTime,
  focusCurrentPage as currentPage,
  focusTotalPages as totalPages,
  focusTotalItems as totalItems,
  focusChangePage as changePage,
  filterDate,
  setFilterDate,
  clearFilterDate
} from './useFocus'

const handlePageChange = (page: number) => {
  changePage(page)
}

const isAllSelected = computed(() => {
  return focusRecords.value.length > 0 && selectedRecordIds.value.size === focusRecords.value.length
})

// 本周专注数据
const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const weeklyMinutes = ref<number[]>([0, 0, 0, 0, 0, 0, 0])
const isLoadingWeekly = ref(false)
const maxWeeklyMinutes = computed(() => Math.max(...weeklyMinutes.value, 1))

// 获取本周的起止时间
const getWeekRange = () => {
  const now = new Date()
  const dayOfWeek = now.getDay() || 7 // 周日为7
  const monday = new Date(now)
  monday.setDate(now.getDate() - dayOfWeek + 1)
  monday.setHours(0, 0, 0, 0)
  const sunday = new Date(monday)
  sunday.setDate(monday.getDate() + 6)
  sunday.setHours(23, 59, 59, 999)
  return { monday, sunday }
}

// 获取本周专注数据
const fetchWeeklyFocus = async () => {
  isLoadingWeekly.value = true
  try {
    const { monday, sunday } = getWeekRange()
    const startTimeAfter = monday.toISOString().slice(0, 19)
    const endTimeBefore = sunday.toISOString().slice(0, 19)

    // 分页获取本周所有记录
    let allRecords: any[] = []
    let pageNum = 1
    let hasMore = true

    while (hasMore) {
      const res = await focusRecordApi.getRecords(pageNum, {
        startTimeAfter,
        endTimeBefore
      })
      if (res.data.code === 200) {
        const pageData = res.data.data
        allRecords = allRecords.concat(pageData.records || [])
        if (pageNum >= pageData.pages) {
          hasMore = false
        } else {
          pageNum++
        }
      } else {
        hasMore = false
      }
    }

    // 按天分组统计时长（duration 单位为秒，转换为分钟）
    const dailyTotals = [0, 0, 0, 0, 0, 0, 0]
    allRecords.forEach((record: any) => {
      const date = new Date(record.startTime)
      const jsDay = date.getDay() // 0=周日
      const weekIndex = jsDay === 0 ? 6 : jsDay - 1 // 转换为周一=0
      dailyTotals[weekIndex] += (record.duration || 0) / 60
    })
    weeklyMinutes.value = dailyTotals.map(m => Math.round(m))
  } catch (e) {
    console.error('获取本周专注数据失败', e)
  } finally {
    isLoadingWeekly.value = false
  }
}

onMounted(() => {
  fetchFocusRecords(1)
  fetchWeeklyFocus()
})
</script>

<template>
  <div class="history-page">
    <div class="history-header">
      <div class="date-filter-bar">
        <div class="date-filter">
          <input
            type="date"
            v-model="filterDate"
            @change="setFilterDate(filterDate)"
            class="date-input"
            placeholder="选择日期"
          />
          <button
            v-if="filterDate"
            class="clear-btn"
            @click="clearFilterDate"
            title="清除筛选"
          >
          </button>
        </div>
        <button v-if="!isBatchDeleting && focusRecords.length > 0" class="batch-btn batch-delete-btn" @click="toggleBatchDeleting">
          批量删除
        </button>
      </div>

        <div v-if="isBatchDeleting" class="batch-actions">
          <label class="select-all-label">
            <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAllRecords" />
            全选 ({{ selectedRecordIds.size }})
          </label>
          <button class="batch-btn batch-dl" @click="batchDeleteFocusRecords" :disabled="selectedRecordIds.size === 0">
            删除选中 ({{ selectedRecordIds.size }})
          </button>
          <button class="batch-btn batch-cancel" @click="cancelBatchDelete">
            取消
          </button>
        </div>
      </div>

      <!-- 本周专注柱状图 -->
      <div class="weekly-chart">
        <div class="weekly-chart-title">本周专注时长</div>
        <div v-if="isLoadingWeekly" class="weekly-chart-loading">
          <Loading text="" />
        </div>
        <div v-else class="weekly-bars">
          <div v-for="(minutes, index) in weeklyMinutes" :key="index" class="bar-column">
            <div class="bar-value">{{ minutes > 0 ? minutes + 'm' : '' }}</div>
            <div class="bar-track">
              <div
                class="bar-fill"
                :style="{ height: maxWeeklyMinutes > 0 ? (minutes / maxWeeklyMinutes * 100) + '%' : '0%' }"
              ></div>
            </div>
            <div class="bar-label">{{ weekDays[index] }}</div>
          </div>
        </div>
      </div>

      <div class="history-list-wrapper">
        <Loading v-if="isLoadingHistory" text="加载中..." />
        <div v-else-if="focusRecords.length === 0" class="empty">
          暂无专注记录
        </div>
        <div v-else class="records-list">
          <div v-if="!isBatchDeleting" class="double-click-hint">
            单击记录可查看详情
          </div>

          <div
            v-for="(record, index) in focusRecords"
            :key="record.id"
            class="record-item"
            :class="{ selected: selectedRecordIds.has(record.id) }"
            @click="isBatchDeleting ? toggleRecordSelection(record.id) : fetchFocusRecordDetail(record.id)"
          >
            <div class="record-number">{{ index + 1 }}</div>

            <div v-if="isBatchDeleting" class="checkbox-wrapper">
              <input
                type="checkbox"
                :checked="selectedRecordIds.has(record.id)"
                @change.stop="toggleRecordSelection(record.id)"
                class="record-checkbox"
              />
            </div>

            <div class="record-info">
              <div class="record-title">{{ record.taskTitle || '无关联任务' }}</div>
              <div class="record-tags">
                <span class="tag tag-duration">
                  {{ formatDuration(record.duration) }}
                </span>
                <span :class="['tag', `tag-status-${record.status}`]">
                  {{ record.status === 1 ? '✓ 已完成' : record.status === 2 ? '✗ 中途放弃' : '已取消' }}
                </span>
                <span class="tag tag-date">
                  {{ formatDateTime(record.startTime) }}
                </span>
              </div>
              <div v-if="record.notes" class="record-notes">
                {{ record.notes }}
              </div>
            </div>

            <button
              v-if="!isBatchDeleting"
              @click.stop="deleteFocusRecord(record.id)"
              class="item-delete-btn"
            >
              删除
            </button>
          </div>
        </div>
        <div class="history-list-spacer"></div>
      </div>

      <div class="history-footer">
        <Pagination
          :current-page="currentPage"
          :total-pages="totalPages"
          :total-items="totalItems"
          @page-change="handlePageChange"
        />
      </div>
  </div>
</template>

<style scoped>
.history-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.history-header {
  flex-shrink: 0;
}

.date-filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  margin-bottom: 8px;
  background-color: rgba(248, 250, 252, 0.85);
  border-radius: 10px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.date-filter {
  display: flex;
  align-items: center;
  gap: 4px;
}

.date-input {
  padding: 6px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 13px;
  background-color: white;
  cursor: pointer;
}

.date-input:focus {
  outline: none;
  border-color: var(--primary);
  box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.1);
}

.clear-btn {
  padding: 4px 6px;
  border: none;
  background-color: rgba(148, 163, 184, 0.2);
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  color: #6B7280;
}

.clear-btn:hover {
  background-color: rgba(148, 163, 184, 0.3);
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  margin-bottom: 8px;
  background-color: rgba(248, 250, 252, 0.85);
  border-radius: 10px;
  border: 1px solid rgba(226, 232, 240, 0.6);
}

.select-all-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #334155;
  font-weight: 500;
  cursor: pointer;
}

.select-all-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.batch-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.batch-delete-btn {
  background-color: rgba(220, 38, 38, 0.15);
  color: #B91C1C;
}

.batch-delete-btn:hover {
  background-color: rgba(220, 38, 38, 0.25);
}

.batch-dl {
  background-color: rgba(220, 38, 38, 0.15);
  color: #B91C1C;
}

.batch-dl:hover:not(:disabled) {
  background-color: rgba(220, 38, 38, 0.25);
}

.batch-dl:disabled {
  background-color: #f1f5f9;
  color: #9CA3AF;
  cursor: not-allowed;
}

.batch-cancel {
  background-color: rgba(100, 116, 139, 0.15);
  color: #6B7280;
}

.batch-cancel:hover {
  background-color: rgba(100, 116, 139, 0.25);
}

.history-list-wrapper {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding-right: 4px;
}

.history-list-wrapper::-webkit-scrollbar {
  width: 5px;
}

.history-list-wrapper::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 3px;
}

.history-list-wrapper::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.25);
  border-radius: 3px;
}

.history-list-spacer {
  height: 8px;
}

.history-footer {
  flex-shrink: 0;
  padding: 10px 0 14px;
}

.empty {
  padding: 20px;
  text-align: center;
  color: #6B7280;
}

.double-click-hint {
  text-align: center;
  font-size: 12px;
  color: #9CA3AF;
  margin-bottom: 10px;
  padding: 6px;
  background-color: rgba(251, 191, 36, 0.08);
  border-radius: 6px;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.record-item {
  padding: 12px 16px;
  background-color: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
  gap: 10px;
}

.record-item:hover {
  background-color: #f8fafc;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.record-item.selected {
  background-color: rgba(var(--primary-rgb), 0.08);
  border: 1px solid rgba(var(--primary-rgb), 0.3);
}

.record-number {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--primary);
  color: white;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.record-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
  accent-color: var(--primary);
}

.record-info {
  flex: 1;
  min-width: 0;
}

.record-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 6px;
}

.record-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.tag {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
  font-weight: 500;
}

.tag-duration {
  background: #e0e7ff;
  color: #4338ca;
}

.tag-status-1 {
  background: #dcfce7;
  color: #166534;
}

.tag-status-2 {
  background: #fee2e2;
  color: #991b1b;
}

.tag-status-0 {
  background: #f3f4f6;
  color: #4b5563;
}

.tag-date {
  background: #f1f5f9;
  color: #6B7280;
}

.record-notes {
  margin-top: 6px;
  font-size: 12px;
  color: #6B7280;
}

.item-delete-btn {
  padding: 4px 10px;
  border: none;
  border-radius: 6px;
  background-color: rgba(239, 68, 68, 0.1);
  color: #B91C1C;
  font-size: 12px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}

.item-delete-btn:hover {
  background-color: rgba(239, 68, 68, 0.2);
}

/* 本周专注柱状图 */
.weekly-chart {
  flex-shrink: 0;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 8px;
}

.weekly-chart-title {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.weekly-chart-loading {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.weekly-bars {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  height: 90px;
  padding: 0 4px;
}

.bar-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  gap: 4px;
}

.bar-value {
  font-size: 10px;
  color: #6B7280;
  font-weight: 500;
  min-height: 14px;
  line-height: 14px;
}

.bar-track {
  flex: 1;
  width: 100%;
  max-width: 28px;
  background: #D1D5DB;
  border-radius: 4px;
  display: flex;
  align-items: flex-end;
  overflow: hidden;
}

.bar-fill {
  width: 100%;
  background: var(--primary);
  border-radius: 4px;
  min-height: 2px;
  transition: height 0.3s ease;
}

.bar-label {
  font-size: 11px;
  color: #6B7280;
  flex-shrink: 0;
}
</style>
