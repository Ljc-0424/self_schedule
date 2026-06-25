import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/home/Home.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/auth/ForgotPassword.vue')
  },
  {
    path: '/appeal',
    name: 'Appeal',
    component: () => import('../views/auth/Appeal.vue')
  },
  {
    path: '/tasks',
    name: 'Tasks',
    component: () => import('../views/tasks/Tasks.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('../views/user/User.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/focus',
    name: 'Focus',
    component: () => import('../views/focus/Focus.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reminders',
    name: 'Reminders',
    component: () => import('../views/reminders/Reminders.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/feedback',
    name: 'Feedback',
    component: () => import('../views/feedback/Feedback.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'Messages',
    component: () => import('../views/messages/MessageCenter.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/feedbacks',
    name: 'AdminFeedbacks',
    component: () => import('../views/admin/AdminFeedbacks.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/online-users',
    name: 'AdminOnlineUsers',
    component: () => import('../views/admin/AdminOnlineUsers.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/appeals',
    name: 'AdminAppeals',
    component: () => import('../views/admin/AdminAppeals.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/help',
    name: 'Help',
    component: () => import('../views/help/Help.vue')
  },
  {
    path: '/design-system',
    name: 'DesignSystem',
    component: () => import('../views/design-system/DesignSystem.vue'),
    meta: { requiresAuth: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token

  if (to.meta.requiresAuth && !isLoggedIn) {
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && isLoggedIn) {
    next('/tasks')
  } else {
    next()
  }
})

export default router
