import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        // 后端接口已有 /api 前缀，所以不需要 rewrite
      },
      '/uploads': {      // 新增：代理静态资源请求
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
