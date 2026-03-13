<template>
  <div class="editor-container">
    <el-page-header @back="goBack" content="编辑笔记" />

    <!-- 上传工具栏 -->
    <div class="upload-toolbar">
      <!-- 上传图片 -->
      <el-upload
        class="upload-btn"
        action="#"
        :show-file-list="false"
        :before-upload="beforeImageUpload"
        :http-request="uploadImage"
      >
        <el-button type="primary" size="small" :loading="uploading">📷 图片</el-button>
      </el-upload>

      <!-- 上传文件（音频、视频、文档等） -->
      <el-upload
        class="upload-btn"
        action="#"
        :show-file-list="false"
        :before-upload="beforeFileUpload"
        :http-request="uploadFile"
      >
        <el-button type="primary" size="small" :loading="uploading">📁 文件</el-button>
      </el-upload>

      <!-- 上传视频（单独按钮，也可以合并到文件，但为了让用户更清晰，保留） -->
      <el-upload
        class="upload-btn"
        action="#"
        :show-file-list="false"
        :before-upload="beforeVideoUpload"
        :http-request="uploadVideo"
      >
        <el-button type="primary" size="small" :loading="uploading">🎥 视频</el-button>
      </el-upload>

      <!-- 插入外部链接 -->
      <el-button type="primary" size="small" @click="insertLink">🔗 链接</el-button>
    </div>

    <el-input v-model="title" placeholder="输入标题" class="title-input" />
    <!-- 编辑器 -->
    <div class="editor-wrapper">
      <EditorContent :editor="editor" />
    </div>

    <!-- 附件区域 -->
    <div class="attachments-section" v-if="attachments.length > 0">
      <el-divider>附件</el-divider>
      <div class="attachment-list">
        <div v-for="(att, index) in attachments" :key="index" class="attachment-item">
          <span class="attachment-icon">
            <el-icon v-if="att.type === 'file'"><Document /></el-icon>
            <el-icon v-else-if="att.type === 'video'"><VideoCamera /></el-icon>
            <el-icon v-else><Link /></el-icon>
          </span>
          <a :href="att.url" target="_blank" class="attachment-name">{{ att.name }}</a>
          <span v-if="att.size" class="attachment-size">({{ formatFileSize(att.size) }})</span>
          <el-button type="danger" size="small" circle :icon="Delete" @click="removeAttachment(index)" class="attachment-delete" />
        </div>
      </div>
    </div>

    <!-- 标签选择器 -->
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
      <el-button v-if="noteId" @click="shareNote" :loading="sharing">分享</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Image from '@tiptap/extension-image'
import type { UploadRequestOptions } from 'element-plus'
import request from '@/utils/request'
import type { AttachmentDTO } from '@/types/attachment' // 需要创建类型
import { Document, VideoCamera, Link, Delete } from '@element-plus/icons-vue'

// 附件列表
const attachments = ref<AttachmentDTO[]>([])
const route = useRoute()
const router = useRouter()
const noteId = route.query.id ? Number(route.query.id) : null
const title = ref('')
const content = ref('')
const selectedTags = ref<number[]>([])
const tagOptions = ref<any[]>([])
const saving = ref(false)
const sharing = ref(false)
const uploading = ref(false)

const removeAttachment = async (index: number) => {
  const att = attachments.value[index]
  if (att.type !== 'link') {
    // 如果是文件或视频，需要从服务器删除文件
    try {
      // 这里使用原始 URL（上传时记录的）来删除，因为 att.url 是下载接口的 URL
      // 我们需要根据 att.url 构造原始 URL
      const originalUrl = `/uploads/${att.type}s/${extractFileNameFromDownloadUrl(att.url)}` // 注意复数 s
      await request.delete('/upload/file', { params: { url: originalUrl } })
      // 同时从临时文件列表中移除（如果有）
      const idx = uploadedTempUrls.value.indexOf(originalUrl)
      if (idx !== -1) uploadedTempUrls.value.splice(idx, 1)
    } catch (e) {
      console.warn('删除文件失败', e)
    }
  }
  attachments.value.splice(index, 1)
}

const formatFileSize = (size: number) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const extractFileNameFromDownloadUrl = (downloadUrl: string) => {
  return downloadUrl.substring(downloadUrl.lastIndexOf('/') + 1)
}

// 从完整 URL 中提取文件名（如 /uploads/images/xxx.png → xxx.png）
const extractFileName = (url: string): string => {
  return url.substring(url.lastIndexOf('/') + 1);
}

// 初始化编辑器
const editor = useEditor({
  content: '',
  extensions: [
    StarterKit,
    Image.configure({
      inline: false,           // 设置为块级元素，独占一行
      allowBase64: false,
      HTMLAttributes: {
        style: 'display: block; max-width: 50%; height: auto; margin: 10px 0;',
      },
    }),
  ],
  onUpdate: ({ editor }) => {
    content.value = editor.getHTML()
  }
})

// ---------- 文件上传相关 ----------


// 记录本次编辑会话中上传的所有临时文件URL（包括图片、文件、视频）
const uploadedTempUrls = ref<string[]>([])

// 清理所有未保存的临时文件
const cleanupTempFiles = async () => {
  if (uploadedTempUrls.value.length === 0) return
  try {
    await request.delete('/upload/files', {
      data: uploadedTempUrls.value  // 注意：DELETE 请求使用 data 发送 body
    })
    uploadedTempUrls.value = []
  } catch (e) {
    console.error('清理临时文件失败', e)
  }
}

// 图片上传前校验
const beforeImageUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

// 普通文件上传前校验（不限类型，只限制大小）
const beforeFileUpload = (file: File) => {
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isLt20M) {
    ElMessage.error('文件大小不能超过 20MB')
    return false
  }
  return true
}

// 视频上传前校验
const beforeVideoUpload = (file: File) => {
  const isVideo = file.type.startsWith('video/')
  const isLt50M = file.size / 1024 / 1024 < 50
  if (!isVideo) {
    ElMessage.error('只能上传视频文件')
    return false
  }
  if (!isLt50M) {
    ElMessage.error('视频大小不能超过 50MB')
    return false
  }
  return true
}

// 上传图片
const uploadImage = async (options: UploadRequestOptions) => {
  uploading.value = true
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post('/upload/image', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const imageUrl = res.data
    console.log('新图片URL:', imageUrl)
    // 记录到临时列表
    uploadedTempUrls.value.push(imageUrl)

    if (!editor.value) {
      ElMessage.error('编辑器未初始化')
      return
    }
    // 聚焦到文档末尾
    editor.value.commands.focus('end')
    // 插入图片
    const imageHtml = `<img src="${imageUrl}" style="display: block; max-width: 100%; height: auto; margin: 10px 0;">`
    const inserted = editor.value.commands.insertContent(imageHtml)

    if (inserted) {
      // 获取当前文档末尾的位置
      const endPos = editor.value.state.doc.content.size
      // 在文档末尾插入空段落（不影响前面的图片）
      editor.value.commands.insertContentAt(endPos, '<p><br></p>')
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error('图片插入失败')
    }
  } catch (error) {
    console.error('上传请求失败:', error)
    ElMessage.error('图片上传失败')
  } finally {
    uploading.value = false
  }
}

// 上传文件（非图片）
const uploadFile = async (options: UploadRequestOptions) => {
  uploading.value = true
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post('/upload/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const fileUrl = res.data          // 例如 /uploads/files/xxx.pdf
    const fileName = options.file.name
    const fileSize = options.file.size

    // 记录到临时文件列表（用于页面刷新/取消时清理）
    uploadedTempUrls.value.push(fileUrl)

    // 添加到附件列表
    attachments.value.push({
      type: 'file',
      name: fileName,
      url: `/api/upload/download/${extractFileName(fileUrl)}`,
      size: fileSize
    })

    ElMessage.success('文件上传成功')
  } catch (error) {
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
  }
}

// 上传视频
const uploadVideo = async (options: UploadRequestOptions) => {
  uploading.value = true
  const formData = new FormData()
  formData.append('file', options.file)
  try {
    const res = await request.post('/upload/file', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    const videoUrl = res.data
    const fileName = options.file.name
    const fileSize = options.file.size

    uploadedTempUrls.value.push(videoUrl)

    attachments.value.push({
      type: 'video',
      name: fileName,
      url: `/api/upload/download/${extractFileName(videoUrl)}`,
      size: fileSize
    })

    ElMessage.success('视频上传成功')
  } catch (error) {
    ElMessage.error('视频上传失败')
  } finally {
    uploading.value = false
  }
}

// 插入外部链接
const insertLink = async () => {
  const { value: url } = await ElMessageBox.prompt('请输入链接地址', '插入链接', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?$/,
    inputErrorMessage: '请输入有效的链接地址'
  }).catch(() => ({ value: null }))
  if (!url) return

  const { value: text } = await ElMessageBox.prompt('请输入链接文字（可选）', '链接文字', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '默认为链接地址'
  }).catch(() => ({ value: null }))

  const linkText = text || url

  attachments.value.push({
    type: 'link',
    name: linkText,
    url: url,
    size: undefined
  })

  ElMessage.success('链接已添加')
}


// ---------- 原有笔记功能 ----------
const shareNote = async () => {
  if (!noteId) return
  sharing.value = true
  try {
    const res = await request.post(`/notes/${noteId}/share`)
    const shareId = res.data
    const shareUrl = `${window.location.origin}/share/note/${shareId}`
    await navigator.clipboard.writeText(shareUrl)
    ElMessage.success('分享链接已复制到剪贴板')
  } catch {
    ElMessage.error('生成分享链接失败')
  } finally {
    sharing.value = false
  }
}

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
    if (note.attachments) {
      attachments.value = note.attachments;  // 直接赋值，因为已经是数组
    } else {
      attachments.value = [];
    }
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

const handleTagChange = async (value: number[]) => {
  // 可留空
}

const saveNote = async () => {
  saving.value = true
  try {
    const noteData = {
      title: title.value,
      content: content.value,
      tags: selectedTags.value.map(id => ({ id })),
      attachments: JSON.stringify(attachments.value)   // 转为 JSON 字符串
    }
    if (noteId) {
      await request.put(`/notes/${noteId}`, noteData)
    } else {
      await request.post('/notes', noteData)
    }
    ElMessage.success('保存成功')
    uploadedTempUrls.value = []
    router.push('/dashboard/notes')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const goBack = async () => {
  await cleanupTempFiles()
  router.push('/dashboard/notes')
}



// 页面卸载时用 sendBeacon 发送批量删除请求
let beforeUnloadHandler: (event: BeforeUnloadEvent) => void

onMounted(() => {
  fetchTags()
  if (noteId) fetchNote()

  beforeUnloadHandler = () => {
    if (uploadedTempUrls.value.length > 0) {
      fetch('/api/upload/files', {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(uploadedTempUrls.value),
        keepalive: true
      }).catch(e => console.warn('清理请求失败', e))
    }
  }
  window.addEventListener('beforeunload', beforeUnloadHandler)
})

onUnmounted(() => {
  window.removeEventListener('beforeunload', beforeUnloadHandler)
})


</script>

<style scoped>
.editor-container {
  padding: 20px;
}
.upload-toolbar {
  margin: 10px 0;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.upload-btn {
  display: inline-block;
}
.title-input {
  margin: 20px 0;
  font-size: 20px;
}

.editor-wrapper {
  height: 400px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
}

.tag-selector {
  margin: 20px 0;
}
.action-bar {
  text-align: right;
}

.attachments-section {
  margin: 20px 0;
}
.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.attachment-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f9f9f9;
  border-radius: 6px;
  border: 1px solid #eee;
}
.attachment-icon {
  margin-right: 8px;
  font-size: 18px;
}
.attachment-name {
  flex: 1;
  text-decoration: none;
  color: #409EFF;
}
.attachment-name:hover {
  text-decoration: underline;
}
.attachment-size {
  color: #999;
  margin: 0 12px;
}
.attachment-delete {
  margin-left: auto;
}

</style>
