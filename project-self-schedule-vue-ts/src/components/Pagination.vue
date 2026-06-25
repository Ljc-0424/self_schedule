<script setup lang="ts">
defineProps<{
  currentPage: number
  totalPages: number
  totalItems: number
}>()

const emit = defineEmits<{
  (e: 'pageChange', page: number): void
}>()

const handleCurrentChange = (page: number) => {
  emit('pageChange', page)
}
</script>

<template>
  <div class="pagination-wrapper">
    <el-pagination
      :current-page="currentPage"
      :page-size="10"
      :total="totalItems"
      :page-count="totalPages"
      layout="prev, pager, next, total"
      background
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px;
}
</style>
