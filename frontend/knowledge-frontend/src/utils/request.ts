import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'

//创建 API 请求工具

const service: AxiosInstance = axios.create({
  baseURL: '/api',    // 重要：所有请求都会自动加上 /api 前缀
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器：自动携带 token
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理错误
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  (error) => {
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service
