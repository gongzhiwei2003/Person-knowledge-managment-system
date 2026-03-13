import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/types/user'
import { login, getUserInfo } from '@/api/user'
import request from '@/utils/request'

//  配置 Pinia store（用户状态）

export const useUserStore = defineStore('user', () => {

  // 1. 先定义响应式变量
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const isLogin = ref(false)

  // 从 localStorage 恢复用户信息
  const savedUserInfo = localStorage.getItem('userInfo')
  if (savedUserInfo) {
    try {
      userInfo.value = JSON.parse(savedUserInfo)
    } catch (e) {
      console.error('解析 userInfo 失败', e)
    }
  }

 // 登录
   const userLogin = async (username: string, password: string) => {
     try {
       const res = await request.post('/users/login', { username, password })
       // 假设后端返回的数据结构为 { code:200, data: { token, id, username, nickname, avatarUrl } }
       const { token: newToken, id, username: name, nickname, avatarUrl } = res.data

       token.value = newToken
       userInfo.value = {
         id,
         username: name,
         nickname: nickname || name,   // 如果 nickname 为空，用 username 代替
         avatarUrl: avatarUrl || ''
       }
       // 保存到 localStorage
       localStorage.setItem('token', newToken)
       localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
       return res
     } catch (error) {
       throw error
     }
   }

  // 退出登录
  const userLogout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  // 获取用户信息
  const getUser = async () => {
    if (!token.value) return
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      isLogin.value = true
    } catch (error) {
      userLogout()
    }
  }

  return {
    token,
    userInfo,
    isLogin,
    userLogin,
    userLogout,
    getUser
  }

})
