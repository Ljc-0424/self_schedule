/**
 * 应用入口文件
 * 
 * 职责：
 * 1. 创建 Vue 应用实例
 * 2. 配置路由和状态管理
 * 3. 配置 Element Plus UI 组件库
 * 4. 挂载应用到 DOM
 * 
 * 技术栈：
 * - Vue 3 Composition API
 * - Vue Router 路由管理
 * - Vuex 状态管理
 * - Element Plus UI 组件库
 */

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import './styles/main.css'
import './styles/layout-override.css'  // 全局布局规范  // 导入设计系统

const passiveEvents = ['wheel', 'touchstart', 'touchmove']
const originalAddEventListener = EventTarget.prototype.addEventListener
EventTarget.prototype.addEventListener = function (
  type: string,
  listener: EventListenerOrEventListenerObject,
  options?: boolean | AddEventListenerOptions
) {
  if (passiveEvents.includes(type)) {
    if (typeof options === 'boolean') {
      options = { capture: options, passive: true }
    } else if (typeof options === 'object' && options !== null) {
      options = { ...options, passive: options.passive ?? true }
    } else {
      options = { passive: true }
    }
  }
  return originalAddEventListener.call(this, type, listener, options)
}

// 创建Vue应用实例
const app = createApp(App)

// 安装路由插件
app.use(router)

// 安装状态管理插件
app.use(store)

// 安装 Element Plus 组件库
app.use(ElementPlus)

// 挂载应用到index.html的#app元素
app.mount('#app')
