<template>
  <div class="goals-container">

    <el-row :gutter="20" class="header">
      <el-col :span="24" class="text-right">
        <el-button type="primary" @click="handleCreate">新建目标</el-button>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col v-for="goal in goals" :key="goal.id" :xs="24" :sm="12" :md="8" :lg="6">

        <el-card class="goal-card" :class="goal.status.toLowerCase()">

          <div class="goal-header">
            <h3>{{ goal.title }}</h3>
            <el-tag :type="statusTagType(goal.status)" size="small">{{ goal.status }}</el-tag>
          </div>

          <p class="goal-description">{{ goal.description || '无描述' }}</p>

          <div class="goal-progress">
            <el-progress :percentage="goal.progress" :color="progressColor(goal.progress)" />
          </div>

          <div class="goal-dates">
            <span v-if="goal.startDate">开始: {{ formatDate(goal.startDate) }}</span>
            <span v-if="goal.targetDate">截止: {{ formatDate(goal.targetDate) }}</span>
          </div>

          <div class="goal-actions">
            <el-button size="small" @click="handleEdit(goal)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(goal)">删除</el-button>
            <el-button
              size="small"
              @click="viewRoadmap(goal.roadmapId)"
              :disabled="!goal.roadmapId"
            >查看路线图</el-button>
          </div>

        </el-card>

      </el-col>
    </el-row>

    <!-- 新建/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="50%">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">

        <el-form-item label="目标标题" prop="title">
          <el-input v-model="form.title" placeholder="例如：掌握Spring Boot" />
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选" />
        </el-form-item>

        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="form.startDate" type="date" placeholder="选择开始日期" format="YYYY-MM-DD" />
        </el-form-item>

        <el-form-item label="目标日期" prop="targetDate">
          <el-date-picker v-model="form.targetDate" type="date" placeholder="选择目标完成日期" format="YYYY-MM-DD" />
        </el-form-item>

        <el-form-item label="状态" prop="status" v-if="form.id">
          <el-select v-model="form.status" placeholder="选择状态">
            <el-option label="计划中" value="PLANNING" />
            <el-option label="进行中" value="IN_PROGRESS" />
            <el-option label="暂停" value="PAUSED" />
            <el-option label="已完成" value="COMPLETED" />
          </el-select>
        </el-form-item>

        <el-form-item label="进度 %" prop="progress" v-if="form.id">
          <el-slider v-model="form.progress" :min="0" :max="100" />
        </el-form-item>

        <el-form-item label="关联路线图" prop="roadmapId">
          <el-select v-model="form.roadmapId" placeholder="请选择路线图" clearable>
            <el-option
              v-for="item in roadmaps"
              :key="item.id"
              :label="item.title"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

interface LearningGoal {
  id: number
  title: string
  description: string
  status: 'PLANNING' | 'IN_PROGRESS' | 'PAUSED' | 'COMPLETED'
  progress: number
  startDate: string
  targetDate: string
}

const goals = ref<LearningGoal[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建目标')
const formRef = ref<FormInstance>()
const form = ref<Partial<LearningGoal>>({
  title: '',
  description: '',
  status: 'PLANNING',
  progress: 0,
  startDate: '',
  targetDate: ''
})
const submitting = ref(false)
const router = useRouter()
const roadmaps = ref([])

const rules: FormRules = {
  title: [
    { required: true, message: '请输入目标标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ]
}

const fetchRoadmaps = async () => {
  try {
    const res = await request.get('/roadmaps')
    roadmaps.value = res.data
  } catch {
    ElMessage.error('获取路线图列表失败')
  }
}

const fetchGoals = async () => {
  try {
    const res = await request.get('/goals')
    goals.value = res.data
  } catch (error) {
    ElMessage.error('获取目标列表失败')
  }
}

const viewRoadmap = (roadmapId) => {
  if (roadmapId) {
    router.push(`/dashboard/roadmap?roadmapId=${roadmapId}`)
  }
}

const handleCreate = () => {
  dialogTitle.value = '新建目标'
form.value = {
  id: 0,
  title: '',
  description: '',
  status: 'PLANNING',
  progress: 0,
  startDate: '',
  targetDate: '',
  roadmapId: null
}
  fetchRoadmaps()
  dialogVisible.value = true
}

const handleEdit = (goal) => {
  dialogTitle.value = '编辑目标'
  form.value = { ...goal }
  fetchRoadmaps()
  dialogVisible.value = true
}

const handleDelete = (goal: LearningGoal) => {
  ElMessageBox.confirm(`确定删除目标 "${goal.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/goals/${goal.id}`)
      ElMessage.success('删除成功')
      fetchGoals()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (form.value.id) {
          await request.put(`/goals/${form.value.id}`, form.value)
        } else {
          await request.post('/goals', form.value)
        }
        ElMessage.success('操作成功')
        dialogVisible.value = false
        fetchGoals()
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const statusTagType = (status: string) => {
  switch (status) {
    case 'PLANNING': return 'info'
    case 'IN_PROGRESS': return 'primary'
    case 'PAUSED': return 'warning'
    case 'COMPLETED': return 'success'
    default: return ''
  }
}

const progressColor = (progress: number) => {
  if (progress < 30) return '#f56c6c'
  if (progress < 70) return '#e6a23c'
  return '#67c23a'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

onMounted(fetchGoals)
</script>



<style scoped>
.goals-container {
  padding: 10px;
}
.header {
  margin-bottom: 20px;
}
.text-right {
  text-align: right;
}
.goal-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  background-color:rgb(157, 178, 191);
}
.goal-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
}
.goal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.goal-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
}
.goal-description {
  color: rgb(34, 40, 49);
  font-size: 14px;
  height: 60px;
  overflow: hidden;
  margin-bottom: 10px;
}
.goal-progress {
  margin: 15px 0;
}
.goal-dates {
  font-size: 12px;
  color: rgb(28, 7, 112);
  margin-bottom: 10px;
}
.goal-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style>
