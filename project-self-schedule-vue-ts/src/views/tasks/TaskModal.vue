<script setup lang="ts">
import { computed } from 'vue'
import { 
  showAddModal, 
  showEditModal,
  taskForm, 
  recurrenceForm,
  editingTask,
  categories,
  priorityOptions,
  repeatOptions,
  weekDayOptions,
  estimatedDays,
  estimatedHours,
  estimatedMinutes,
  estimatedSeconds,
  toggleWeekDay,
  saveTask,
  isSaving,
  closeModal
} from './useTasks'
import TaskTimeSettings from './TaskTimeSettings.vue'
import TaskReminderSettings from './TaskReminderSettings.vue'

const showWeekDaySelection = computed(() => {
  return taskForm.value.recurrenceRule.startsWith('WEEKLY')
})

const showDateSelection = computed(() => {
  return taskForm.value.recurrenceRule.startsWith('MONTHLY') || 
         taskForm.value.recurrenceRule.startsWith('YEARLY')
})

const dateInputType = computed(() => {
  if (taskForm.value.recurrenceRule.startsWith('YEARLY')) {
    return 'month'
  }
  return 'date'
})

const getTitle = () => {
  if (showAddModal.value) return '添加任务'
  return '编辑任务'
}

const dialogVisible = computed({
  get: () => showAddModal.value || showEditModal.value,
  set: (val) => {
    if (!val) {
      closeModal()
    }
  }
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    :title="getTitle()"
    width="560px"
    :close-on-click-modal="false"
    class="task-dialog"
    align-center
    @close="dialogVisible = false"
  >
    <div class="dialog-scroll">
      <div class="form-section">
        <div class="section-label">基本信息</div>
        <el-form-item label="标题" required>
          <el-input
            v-model="taskForm.title"
            placeholder="请输入任务标题"
          />
        </el-form-item>

        <el-form-item label="描述">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述（可选）"
          />
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="section-label">优先级与重复</div>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priority" style="width: 100%;">
            <el-option
              v-for="p in priorityOptions"
              :key="p.value"
              :label="p.label"
              :value="p.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="重复规则">
          <el-select v-model="taskForm.recurrenceRule" style="width: 100%;" clearable placeholder="不重复">
            <el-option
              v-for="opt in repeatOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value === 'none' ? '' : opt.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="showWeekDaySelection" label="选择星期">
          <el-checkbox-group v-model="recurrenceForm.selectedDays">
            <el-checkbox
              v-for="day in weekDayOptions"
              :key="day.value"
              :value="day.value"
            >
              {{ day.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item v-if="showDateSelection" :label="taskForm.recurrenceRule.startsWith('MONTHLY') ? '选择日期' : '选择日期（月-日）'">
          <el-input
            v-model="recurrenceForm.selectedDate"
            :type="dateInputType"
            :placeholder="taskForm.recurrenceRule.startsWith('MONTHLY') ? '选择日期' : '选择月日'"
          />
        </el-form-item>

        <el-form-item v-if="taskForm.recurrenceRule && taskForm.recurrenceRule !== 'none'" label="重复结束日期（可选）">
          <el-date-picker
            v-model="taskForm.recurrenceEndDate"
            type="date"
            placeholder="设置重复截止日期"
            style="width: 100%;"
          />
          <div class="hint">设置后，任务将在该日期后停止自动重复</div>
        </el-form-item>
      </div>

      <div class="form-section">
        <div class="section-label">时间设置</div>
        <TaskTimeSettings />
      </div>

      <div class="form-section">
        <div class="section-label">分类与标签</div>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="分类">
              <el-input v-model="taskForm.category" placeholder="输入或选择" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-input v-model="taskForm.tags" placeholder="逗号分隔" />
            </el-form-item>
          </el-col>
        </el-row>

        <div v-if="categories.length > 0" class="category-choices">
          <div class="category-hint">选择已有分类:</div>
          <div class="category-list">
            <el-button
              v-for="cat in categories"
              :key="cat"
              size="small"
              :type="taskForm.category === cat ? 'primary' : 'default'"
              @click="taskForm.category = cat"
            >
              {{ cat }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="form-section">
        <div class="section-label">提醒设置</div>
        <TaskReminderSettings />
      </div>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="isSaving" @click="saveTask(!!editingTask)">✓ 保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.hint {
  font-size: 12px;
  color: #9CA3AF;
  margin-top: 4px;
}

.dialog-scroll {
  padding-right: 4px;
}

.form-section {
  margin-bottom: 4px;
}

.section-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary);
  padding: 8px 12px;
  background: rgba(var(--primary-rgb), 0.05);
  border-radius: 8px;
  margin-bottom: 8px;
}

.category-choices {
  margin-bottom: 8px;
}

.category-hint {
  font-size: 12px;
  color: #9CA3AF;
  margin-bottom: 6px;
}

.category-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>

<style>
.task-dialog.el-dialog {
  border-radius: 16px;
  overflow: hidden;
}

.task-dialog .el-dialog__header {
  padding: 18px 24px;
  border-bottom: 1px solid rgba(226, 232, 240, 0.6);
  background: rgba(255, 255, 255, 0.98);
}

.task-dialog .el-dialog__title {
  font-size: 17px;
  font-weight: 700;
  color: #111827;
}

.task-dialog .el-dialog__body {
  padding: 16px 24px;
  max-height: calc(100vh - 220px);
  overflow-y: auto;
}

.task-dialog .el-dialog__body::-webkit-scrollbar {
  width: 4px;
}

.task-dialog .el-dialog__body::-webkit-scrollbar-track {
  background: transparent;
}

.task-dialog .el-dialog__body::-webkit-scrollbar-thumb {
  background: rgba(var(--primary-rgb), 0.2);
  border-radius: 2px;
}

.task-dialog .el-dialog__footer {
  padding: 12px 24px 18px;
  border-top: 1px solid rgba(226, 232, 240, 0.6);
  background: rgba(255, 255, 255, 0.98);
}

.task-dialog .el-form-item {
  margin-bottom: 14px;
}

.task-dialog .el-form-item__label {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
  padding-bottom: 2px;
}

.task-dialog .el-input__wrapper {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #D1D5DB inset;
}

.task-dialog .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #a5b4fc inset;
}

.task-dialog .el-select .el-input__wrapper {
  border-radius: 8px;
}

.task-dialog .el-button--primary {
  background: var(--primary);
  border: none;
  border-radius: 8px;
}
</style>
