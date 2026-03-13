
<!--主布局页面-->

<template>
  <div class="dashboard-container">

    <el-container>

      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <div class="logo">学习型知识管理系统</div>
        <el-menu
          default-active="1"
          class="sidebar-menu"
          router
        >

          <el-menu-item index="/dashboard/overview">
            <el-icon><House /></el-icon>
            <span>概览</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/notes">
            <el-icon><Notebook /></el-icon>
            <span>我的笔记</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/roadmap">
            <el-icon><MapLocation /></el-icon>
            <span>学习路线</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/goals">
            <el-icon><Flag /></el-icon>
            <span>学习目标</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/tags">
            <el-icon><PriceTag /></el-icon>
            <span>标签管理</span>
          </el-menu-item>

          <el-menu-item index="/dashboard/profile">
            <el-icon><User /></el-icon>
            <span>个人中心</span>
          </el-menu-item>

        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>

        <el-header class="header">
          <div class="header-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item>首页</el-breadcrumb-item>
            </el-breadcrumb>
          </div>

          <div class="header-right">
            <el-dropdown>

              <span class="user-info">
                <el-avatar :size="32" :src="userInfo?.avatarUrl" />
                <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
              </span>

              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
                  <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view :key="$route.fullPath" />
        </el-main>

      </el-container>

    </el-container>

  </div>
</template>

<script setup lang="ts">
import { PriceTag,User,Flag,MapLocation,Notebook,House} from '@element-plus/icons-vue'
import { computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)


onMounted(() => {
  console.log('userInfo on mounted:', userInfo.value)
})

watch(userInfo, (newVal) => {
  console.log('userInfo changed:', newVal)
}, { immediate: true, deep: true })


const handleProfile = () => {
   router.push('/dashboard/profile')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    userStore.userLogout()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>



<style scoped>
.dashboard-container {
  height: 100vh;
     overflow: hidden;        /* 关键：禁止整个页面滚动 */
}

.sidebar {
  background-color: #304156;
  color: white;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  color: white;
  background-color: #27374D;
}

.sidebar-menu {
  border-right: none;
  background-color: #304156;
}

.sidebar-menu .el-menu-item {
  color: #bfcbd9;
}

.sidebar-menu .el-menu-item:hover,
.sidebar-menu .el-menu-item.is-active {
  background-color: #263445;
  color: #409EFF;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
  background-color: white;
  padding: 0 20px;

  height: 60px;
  flex-shrink: 0;           /* 防止在 flex 布局中被压缩 */
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 10px;
  font-size: 14px;
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  min-height: calc(100vh - 60px);

  height: calc(100vh - 60px); /* 减去头部高度 */
  verflow-y: auto;            /* 右侧内容可滚动 */
}
</style>
