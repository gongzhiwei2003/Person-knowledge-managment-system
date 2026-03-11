<template>
  <div class="editor-container">
    <el-page-header @back="goBack" content="编辑笔记" />

    <el-input v-model="title" placeholder="输入标题" class="title-input" />

    <div class="editor-wrapper">
      <EditorContent :editor="editor" />
    </div>

    <div class="tag-selector">
      <el-select
        v-model="selectedTags"
        multiple
        placeholder="选择标签"
        filterable
        allow-create
        @change="handleTagChange"
        @keydown.enter.prevent
      >
        <el-option
          v-for="tag in tagOptions"
          :key="tag.id"
          :label="tag.name"
          :value="tag.id"
        />
      </el-select>
    </div>

<div class="action-bar">
  <el-button @click="goBack">取消</el-button>
  <el-button type="primary" @click="saveNote" :loading="saving">保存</el-button>
  <!-- 添加删除按钮 -->
    <el-button
      v-if="noteId"
      type="danger"
      @click="handleDelete"
      :loading="deleting"
    >删除</el-button>
  <el-button v-if="noteId" @click="shareNote" :loading="sharing">分享</el-button>
</div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const noteId = route.query.id ? Number(route.query.id) : null
const title = ref('')
const content = ref('')
const selectedTags = ref<number[]>([])
const tagOptions = ref<any[]>([])
const saving = ref(false)
const sharing = ref(false)
const deleting = ref(false)

const handleDelete = async () => {
  if (!noteId) return
  try {
    // 确认对话框
    await ElMessageBox.confirm('确定要删除这篇笔记吗？此操作不可恢复。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    deleting.value = true
    await request.delete(`/notes/${noteId}`)
    ElMessage.success('删除成功')
    // 删除后返回列表页
    router.push('/dashboard/notes')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败', error)
      ElMessage.error('删除失败')
    }
  } finally {
    deleting.value = false
  }
}

const shareNote = async () => {
  if (!noteId) return
  sharing.value = true
  try {
    const res = await request.post(`/notes/${noteId}/share`)
    const shareId = res.data
    const shareUrl = `${window.location.origin}/share/note/${shareId}`
    // 复制到剪贴板
    await navigator.clipboard.writeText(shareUrl)
    ElMessage.success('分享链接已复制到剪贴板')
  } catch {
    ElMessage.error('生成分享链接失败')
  } finally {
    sharing.value = false
  }
}

const editor = useEditor({
  content: '',
  extensions: [StarterKit],
  onUpdate: ({ editor }) => {
    content.value = editor.getHTML()
  }
})

// 获取笔记详情（如果是编辑）
const fetchNote = async () => {
  if (!noteId) return
  try {
    const res = await request.get(`/notes/${noteId}`)
    const note = res.data
    title.value = note.title
    content.value = note.content
    editor.value?.commands.setContent(note.content)
    selectedTags.value = note.tags.map((t: any) => t.id)
  } catch (error) {
    ElMessage.error('获取笔记失败')
  }
}

// 获取所有标签供选择
const fetchTags = async () => {
  try {
    const res = await request.get('/tags')
    tagOptions.value = res.data
  } catch (error) {
    console.error('获取标签失败', error)
  }
}

// 处理标签变化（当用户创建新标签时，可能需要额外处理）
const handleTagChange = async (value: number[]) => {
  // 可选：当用户输入新标签（allow-create）时，后端尚未保存，需要手动创建
  // 但 el-select 的 allow-create 只会创建本地选项，不会自动保存到后端。
  // 这里可以在保存时统一处理，因为我们在保存时会将选中的标签ID发送给后端，后端会创建不存在的标签（通过 TagService.findOrCreateTag）。
  // 因此无需额外操作。
}

const saveNote = async () => {
  saving.value = true
  try {
    const noteData = {
      title: title.value,
      content: content.value,
      tags: selectedTags.value.map(id => ({ id })) // 传递标签对象数组（只需id）
    }
    if (noteId) {
      await request.put(`/notes/${noteId}`, noteData)
      ElMessage.success('更新成功')
    } else {
      await request.post('/notes', noteData)
      ElMessage.success('创建成功')
    }
    router.push('/dashboard/notes')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  router.push('/dashboard/notes')
}

onMounted(() => {
  fetchTags()
  if (noteId) fetchNote()
})
</script>


<style scoped>
.editor-container {
  padding: 20px;
}
.title-input {
  margin: 20px 0;
  font-size: 20px;
}
.editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  min-height: 400px;
}
.tag-selector {
  margin: 20px 0;
}
.action-bar {
  text-align: right;
}
</style>
