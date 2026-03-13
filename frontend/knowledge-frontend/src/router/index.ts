import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/RegisterView.vue')
    },
    // 公开分享路由
    {
      path: '/share/note/:shareId',
      name: 'PublicNote',
      component: () => import('@/views/PublicNoteView.vue')
    },
    {
      path: '/share/roadmap/:shareId',
      name: 'PublicRoadmap',
      component: () => import('@/views/PublicRoadmapView.vue')
    },
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/DashboardView.vue'),
      children: [
        {
          path: 'overview',
          name: 'Overview',
          component: () => import('@/views/OverviewView.vue')
        },
        {
          path: 'notes',
          name: 'Notes',
          component: () => import('@/views/NotesView.vue')
        },
        {
          path: 'notes/edit',
          name: 'NoteEditor',
          component: () => import('@/views/NoteEditor.vue')
        },
        {
          path: 'tags',
          name: 'Tags',
          component: () => import('@/views/TagsView.vue')
        },
        {
          path: 'goals',
          name: 'Goals',
          component: () => import('@/views/GoalsView.vue')
        },
        {
          path: 'roadmap',
          name: 'Roadmap',
          component: () => import('@/views/RoadmapView.vue')
        },
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/ProfileView.vue')
        }
      ]
    }
  ]
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path.startsWith('/dashboard') && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
