<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <span>个人中心</span>
      </template>

      <el-form label-width="100px">
        <!-- 头像 -->
        <el-form-item label="头像">
          <div class="avatar-upload">
            <el-avatar :size="80" :src="avatarUrl" />
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :http-request="uploadAvatar"
            >
              <el-button size="small" type="primary">上传头像</el-button>
            </el-upload>
          </div>
        </el-form-item>

        <!-- 用户名（不可修改） -->
        <el-form-item label="用户名">
          <el-input v-model="profile.username" disabled />
        </el-form-item>

        <!-- 昵称 -->
        <el-form-item label="昵称">
          <el-input v-model="profile.nickname" placeholder="请输入昵称" />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱">
          <el-input v-model="profile.email" placeholder="请输入邮箱" />
        </el-form-item>

        <!-- 修改密码 -->
        <el-divider>修改密码</el-divider>
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存资料</el-button>
          <el-button @click="changePassword" :loading="changingPwd">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import type { UploadRequestOptions } from 'element-plus'
import { useUserStore } from '@/stores/user'

// 在 script 中获取 store
const userStore = useUserStore()

const profile = ref({
  username: '',
  nickname: '',
  email: '',
  avatarUrl: ''
})
const avatarUrl = ref('')
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const saving = ref(false)
const changingPwd = ref(false)

// 获取用户资料
const fetchProfile = async () => {
  try {
    const res = await request.get('/users/profile')
    profile.value = res.data
    avatarUrl.value = res.data.avatarUrl || ''
    // 如果昵称为空，则默认用用户名
    if (!profile.value.nickname) {
    profile.value.nickname = profile.value.username
    }
  } catch {
    ElMessage.error('获取资料失败')
  }
}

// 保存资料
const saveProfile = async () => {
  saving.value = true
  try {
    await request.put('/users/profile', {
      nickname: profile.value.nickname,
      email: profile.value.email
    })
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 头像上传前的校验
const beforeAvatarUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

// 自定义上传
const uploadAvatar = async (options: UploadRequestOptions) => {
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post('/users/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    // 更新当前页面头像
    avatarUrl.value = res.data
    profile.value.avatarUrl = res.data

    // 更新 store 中的用户头像（右上角会用）
    if (userStore.userInfo) {
      userStore.userInfo.avatarUrl = res.data
      // 同时更新 localStorage
      localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
    }

    ElMessage.success('头像上传成功')
  } catch {
    ElMessage.error('头像上传失败')
  }
}

// 修改密码
const changePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次密码不一致')
    return
  }
  changingPwd.value = true
  try {
    await request.put('/users/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    })
    ElMessage.success('密码修改成功')
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '密码修改失败')
  } finally {
    changingPwd.value = false
  }
}

onMounted(fetchProfile)
</script>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 20px auto;
}
.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}
.avatar-uploader {
  display: inline-block;
}
</style>
