<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">设计系统展示</h1>
      <p class="page-subtitle">展示项目的设计规范和组件库</p>
    </div>

    <!-- 色彩系统 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">色彩系统</h2>
      </div>
      <div class="grid grid-cols-4">
        <div v-for="color in colors" :key="color.name" class="stat-card">
          <div
            class="color-swatch"
            :style="{ backgroundColor: color.value }"
          ></div>
          <div class="color-info">
            <div class="color-name">{{ color.name }}</div>
            <div class="color-value">{{ color.value }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 字体系统 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">字体系统</h2>
      </div>
      <div class="card">
        <div class="card-body">
          <div class="font-sample text-4xl mb-4">大标题 4XL</div>
          <div class="font-sample text-3xl mb-4">标题 3XL</div>
          <div class="font-sample text-2xl mb-4">副标题 2XL</div>
          <div class="font-sample text-xl mb-4">小标题 XL</div>
          <div class="font-sample text-lg mb-4">正文大 LG</div>
          <div class="font-sample text-base mb-4">正文 Base</div>
          <div class="font-sample text-sm mb-4">小字 SM</div>
          <div class="font-sample text-xs">最小字 XS</div>
        </div>
      </div>
    </section>

    <!-- 按钮组件 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">按钮组件</h2>
      </div>
      <div class="card">
        <div class="card-body">
          <div class="flex flex-wrap gap-3 mb-4">
            <button class="btn btn-primary">主要按钮</button>
            <button class="btn btn-secondary">次要按钮</button>
            <button class="btn btn-success">成功按钮</button>
            <button class="btn btn-warning">警告按钮</button>
            <button class="btn btn-danger">危险按钮</button>
            <button class="btn btn-info">信息按钮</button>
          </div>

          <div class="flex flex-wrap gap-3 mb-4">
            <button class="btn btn-outline">轮廓按钮</button>
            <button class="btn btn-ghost">幽灵按钮</button>
            <button class="btn btn-link">链接按钮</button>
          </div>

          <div class="flex flex-wrap gap-3 mb-4">
            <button class="btn btn-primary btn-sm">小按钮</button>
            <button class="btn btn-primary">默认按钮</button>
            <button class="btn btn-primary btn-lg">大按钮</button>
            <button class="btn btn-primary btn-xl">超大按钮</button>
          </div>

          <div class="flex flex-wrap gap-3">
            <button class="btn btn-primary" disabled>禁用状态</button>
            <button class="btn btn-primary loading">
              <span class="loading-spinner"></span>
              加载中
            </button>
          </div>
        </div>
      </div>
    </section>

    <!-- 卡片组件 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">卡片组件</h2>
      </div>
      <div class="grid grid-cols-3">
        <div class="card">
          <div class="card-header">
            <h3 class="card-title">基础卡片</h3>
          </div>
          <div class="card-body">
            <p class="card-text">这是一个基础卡片组件，包含头部、内容和底部。</p>
          </div>
          <div class="card-footer">
            <button class="btn btn-primary btn-sm">操作</button>
          </div>
        </div>

        <div class="card">
          <div class="card-body">
            <h3 class="card-title">无边框卡片</h3>
            <p class="card-text">这是一个没有边框的卡片变体。</p>
          </div>
        </div>

        <div class="card card-flush">
          <div class="card-body">
            <h3 class="card-title">平卡片</h3>
            <p class="card-text">这是一个完全无阴影的平卡片。</p>
          </div>
        </div>
      </div>
    </section>

    <!-- 表单元素 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">表单元素</h2>
      </div>
      <div class="card">
        <div class="card-body">
          <div class="grid grid-cols-2">
            <div class="form-group">
              <label class="form-label">基础输入框</label>
              <input type="text" class="form-control" placeholder="请输入内容">
            </div>

            <div class="form-group">
              <label class="form-label">成功状态</label>
              <input type="text" class="form-control is-valid" value="验证通过">
              <div class="form-hint text-success">输入正确</div>
            </div>

            <div class="form-group">
              <label class="form-label">错误状态</label>
              <input type="text" class="form-control is-invalid" value="错误内容">
              <div class="form-error">请输入有效内容</div>
            </div>

            <div class="form-group">
              <label class="form-label">禁用状态</label>
              <input type="text" class="form-control" disabled value="禁用输入">
            </div>

            <div class="form-group">
              <label class="form-label">文本域</label>
              <textarea class="form-control" placeholder="请输入多行内容"></textarea>
            </div>

            <div class="form-group">
              <label class="form-label">不同尺寸</label>
              <div class="flex flex-column gap-2">
                <input type="text" class="form-control form-control-sm" placeholder="小输入框">
                <input type="text" class="form-control" placeholder="默认输入框">
                <input type="text" class="form-control form-control-lg" placeholder="大输入框">
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 徽章和标签 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">徽章和标签</h2>
      </div>
      <div class="card">
        <div class="card-body">
          <h3 class="text-lg font-semibold mb-3">徽章</h3>
          <div class="flex flex-wrap gap-2 mb-4">
            <span class="badge badge-primary">主要</span>
            <span class="badge badge-secondary">次要</span>
            <span class="badge badge-success">成功</span>
            <span class="badge badge-warning">警告</span>
            <span class="badge badge-danger">危险</span>
            <span class="badge badge-info">信息</span>
          </div>

          <h3 class="text-lg font-semibold mb-3">不同尺寸</h3>
          <div class="flex flex-wrap gap-2 mb-4">
            <span class="badge badge-primary badge-sm">小徽章</span>
            <span class="badge badge-primary">默认徽章</span>
            <span class="badge badge-primary badge-lg">大徽章</span>
          </div>

          <h3 class="text-lg font-semibold mb-3">标签</h3>
          <div class="tag-group">
            <span class="tag">标签1</span>
            <span class="tag">标签2</span>
            <span class="tag tag-closable">
              可关闭
              <span class="tag-remove">×</span>
            </span>
          </div>
        </div>
      </div>
    </section>

    <!-- 阴影系统 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">阴影系统</h2>
      </div>
      <div class="grid grid-cols-4">
        <div class="stat-card shadow-xs">
          <div class="text-sm text-muted">XS 阴影</div>
        </div>
        <div class="stat-card shadow-sm">
          <div class="text-sm text-muted">SM 阴影</div>
        </div>
        <div class="stat-card shadow">
          <div class="text-sm text-muted">MD 阴影</div>
        </div>
        <div class="stat-card shadow-lg">
          <div class="text-sm text-muted">LG 阴影</div>
        </div>
      </div>
    </section>

    <!-- 动画示例 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">动画示例</h2>
      </div>
      <div class="card">
        <div class="card-body">
          <div class="flex flex-wrap gap-4">
            <div class="stat-card animate-fade-in">
              <div class="text-sm text-muted">淡入</div>
            </div>
            <div class="stat-card animate-fade-in-up">
              <div class="text-sm text-muted">上移淡入</div>
            </div>
            <div class="stat-card animate-scale-in">
              <div class="text-sm text-muted">缩放进入</div>
            </div>
            <div class="stat-card animate-bounce-in">
              <div class="text-sm text-muted">弹跳进入</div>
            </div>
            <div class="stat-card animate-pulse">
              <div class="text-sm text-muted">脉冲</div>
            </div>
            <div class="stat-card">
              <div class="loading-spinner mx-auto"></div>
              <div class="text-sm text-muted text-center mt-2">加载旋转</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 进度条 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">进度条</h2>
      </div>
      <div class="card">
        <div class="card-body">
          <div class="mb-4">
            <div class="flex justify-between mb-2">
              <span class="text-sm font-medium">基础进度条</span>
              <span class="text-sm text-muted">60%</span>
            </div>
            <div class="progress">
              <div class="progress-bar" style="width: 60%"></div>
            </div>
          </div>

          <div class="mb-4">
            <div class="flex justify-between mb-2">
              <span class="text-sm font-medium">小进度条</span>
              <span class="text-sm text-muted">45%</span>
            </div>
            <div class="progress progress-sm">
              <div class="progress-bar" style="width: 45%"></div>
            </div>
          </div>

          <div class="mb-4">
            <div class="flex justify-between mb-2">
              <span class="text-sm font-medium">大进度条</span>
              <span class="text-sm text-muted">80%</span>
            </div>
            <div class="progress progress-lg">
              <div class="progress-bar" style="width: 80%"></div>
            </div>
          </div>

          <div>
            <div class="flex justify-between mb-2">
              <span class="text-sm font-medium">动画进度条</span>
              <span class="text-sm text-muted">加载中...</span>
            </div>
            <div class="progress">
              <div class="progress-bar progress-animated"></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 头像组 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">头像组件</h2>
      </div>
      <div class="card">
        <div class="card-body">
          <h3 class="text-lg font-semibold mb-3">不同尺寸</h3>
          <div class="flex items-center gap-3 mb-4">
            <div class="avatar avatar-sm">A</div>
            <div class="avatar">B</div>
            <div class="avatar avatar-lg">C</div>
            <div class="avatar avatar-xl">D</div>
          </div>

          <h3 class="text-lg font-semibold mb-3">头像组</h3>
          <div class="avatar-group">
            <div class="avatar">A</div>
            <div class="avatar">B</div>
            <div class="avatar">C</div>
            <div class="avatar">D</div>
            <div class="avatar">+3</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 骨架屏 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">骨架屏</h2>
      </div>
      <div class="card">
        <div class="card-body">
          <div class="flex gap-4">
            <div class="skeleton skeleton-avatar"></div>
            <div class="flex-1">
              <div class="skeleton skeleton-title"></div>
              <div class="skeleton skeleton-text"></div>
              <div class="skeleton skeleton-text" style="width: 80%"></div>
            </div>
          </div>
          <div class="skeleton skeleton-image mt-4"></div>
        </div>
      </div>
    </section>

    <!-- 筛选条 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">筛选条</h2>
      </div>
      <div class="filter-bar">
        <span class="filter-chip active">全部</span>
        <span class="filter-chip">进行中</span>
        <span class="filter-chip">已完成</span>
        <span class="filter-chip">已过期</span>
      </div>
    </section>

    <!-- 空状态 -->
    <section class="content-section">
      <div class="section-header">
        <h2 class="section-title">空状态</h2>
      </div>
      <div class="card">
        <div class="empty-state">
          <svg class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          <h3 class="empty-state-title">暂无数据</h3>
          <p class="empty-state-text">这里还没有任何内容，点击下方按钮开始创建</p>
          <button class="btn btn-primary">创建第一个任务</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
const colors = [
  { name: '主色', value: 'var(--primary)' },
  { name: '主色浅', value: '#EEF2FF' },
  { name: '成功', value: '#3F6212' },
  { name: '警告', value: '#F59E0B' },
  { name: '危险', value: '#B91C1C' },
  { name: '信息', value: 'var(--primary)' },
  { name: '灰色50', value: '#F9FAFB' },
  { name: '灰色100', value: '#E5E7EB' },
  { name: '灰色200', value: '#D1D5DB' },
  { name: '灰色400', value: '#9CA3AF' },
  { name: '灰色500', value: '#6B7280' },
  { name: '灰色900', value: '#111827' },
]
</script>

<style scoped>
.color-swatch {
  width: 100%;
  height: 80px;
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.color-info {
  padding: var(--spacing-3);
}

.color-name {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin-bottom: var(--spacing-1);
}

.color-value {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  font-family: var(--font-family-mono);
}

.font-sample {
  padding: var(--spacing-2);
  border-bottom: 1px solid var(--border-light);
}

.font-sample:last-child {
  border-bottom: none;
}
</style>
