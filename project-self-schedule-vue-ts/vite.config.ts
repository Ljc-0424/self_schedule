import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [vue(), VitePWA({
    registerType: 'autoUpdate',
    devOptions: {
      enabled: true
    },
    manifest: {
      name: 'SelfSchedule - 智能任务管家',
      short_name: 'SelfSchedule',
      description: '个人智能任务安排系统，AI驱动的任务管理、专注计时、提醒通知',
      start_url: '/',
      display: 'standalone',
      background_color: '#ffffff',
      theme_color: '#409eff',
      orientation: 'portrait-primary',
      icons: [
        {
          src: '/SelfSchedule.svg',
          sizes: 'any',
          type: 'image/svg+xml',
          purpose: 'any maskable'
        }
      ],
      categories: ['productivity', 'utilities'],
      lang: 'zh-CN',
      dir: 'ltr',
      scope: '/',
      prefer_related_applications: false
    }
  })],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  base: '/',
  build: {
    outDir: '../self_schedule/src/main/resources/static',
    emptyOutDir: true
  },
  server: {
    proxy: {
      '/api/sse': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        headers: {
          'Cache-Control': 'no-cache',
          'Connection': 'keep-alive'
        }
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
