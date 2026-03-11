<template>
  <div class="tags-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>标签管理</span>
          <el-button type="primary" size="small" @click="handleAdd">新建标签</el-button>
        </div>
      </template>

      <el-table :data="tags" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="标签名称" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 标签编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="30%">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

interface Tag {
  id: number
  name: string
}

const tags = ref<Tag[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('新建标签')
const formRef = ref<FormInstance>()
const form = ref<Tag>({ id: 0, name: '' })
const submitting = ref(false)

const rules: FormRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ]
}

const fetchTags = async () => {
  try {
    const res = await request.get('/tags')
    tags.value = res.data
  } catch (error) {
    ElMessage.error('获取标签列表失败')
  }
}

const handleAdd = () => {
  dialogTitle.value = '新建标签'
  form.value = { id: 0, name: '' }
  dialogVisible.value = true
}

const handleEdit = (row: Tag) => {
  dialogTitle.value = '编辑标签'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleDelete = (row: Tag) => {
  ElMessageBox.confirm(`确定删除标签 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/tags/${row.id}`)
      ElMessage.success('删除成功')
      fetchTags()
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
          await request.put(`/tags/${form.value.id}`, form.value)
          ElMessage.success('更新成功')
        } else {
          await request.post('/tags', form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        fetchTags()
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

onMounted(fetchTags)
</script>

<style scoped>
.tags-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
