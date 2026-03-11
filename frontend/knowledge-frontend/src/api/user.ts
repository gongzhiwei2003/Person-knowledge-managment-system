import request from '@/utils/request'

// 创建 API 接口

// 用户登录
export const login = (data: { username: string; password: string }) => {
  return request({
    url: '/users/login',
    method: 'post',
    data
  })
}

// 用户注册
export const register = (data: { username: string; password: string; email?: string }) => {
  return request({
    url: '/users/register',
    method: 'post',
    data
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/users/1', // 暂时写死，实际应从token解析
    method: 'get'
  })
}
