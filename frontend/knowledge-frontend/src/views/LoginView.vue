
<!--登录页面-->

<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">学习知识管理系统</h2>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-width="80px"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入您的用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入您的密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        <div class="register-wrapper">
           <el-button
              class="register-btn"
              @click="handleRegister"
           >
           没有账号？点击注册
           </el-button>
        </div>
      </el-form>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref<FormInstance>()

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const loading = ref(false)

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
    loading.value = true

    await userStore.userLogin(loginForm.username, loginForm.password)

    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  router.push('/register')
}
</script>

<style scoped>

/* 输入框样式优化 */
:deep(.el-input__wrapper) {
  background: #f8f9fa;
  border: none;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.2s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.2);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.3);
}

/* 注册按钮容器，靠右对齐 */
.register-wrapper {
  display: flex;
  justify-content: flex-end;
}

/* 注册按钮样式 */
.register-btn {
  background: transparent;
  border: 1px solid #667eea;
  color: #667eea;
  padding: 10px 20px;
  transition: all 0.2s;
}

.register-btn:hover {
  background: #667eea;
  color: white;
  border-color: #667eea;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: rgb(170, 196, 255) url("../images/大草原-天空.png") no-repeat center/cover fixed;
}

.login-card {
  width: 400px;
  padding: 40px;
  background: rgba(210, 218, 255,0.8);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
}

.login-btn {
  width: 100%;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

</style>
