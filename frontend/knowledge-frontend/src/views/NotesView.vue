<template>
  <div class="notes-container">

    <el-row :gutter="20" class="search-row">
      <!-- 搜索框 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索笔记..."
          :prefix-icon="Search"
          clearable
          @input="handleSearch"
          class="search-input"
        />
      </el-col>

      <!-- 标签筛选下拉框 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="6">
        <el-select
          v-model="selectedTagId"
          placeholder="按标签筛选"
          clearable
          @change="fetchNotes"
          class="tag-select"
        >
          <el-option
            v-for="tag in tagOptions"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          />
        </el-select>
      </el-col>

      <!-- 新建笔记按钮 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="12" class="text-right">
        <el-button type="primary" @click="handleCreate" class="create-btn">
          <el-icon><Plus /></el-icon> 新建笔记
        </el-button>
      </el-col>
    </el-row>


    <!-- 笔记卡片网格 -->
    <el-row :gutter="20" class="notes-grid">
      <el-col
        v-for="note in notes"
        :key="note.id"
        :xs="24"
        :sm="12"
        :md="8"
        :lg="6"
      >

        <el-card class="note-card" shadow="hover">
         <!-- 标题 -->
          <div class="note-header" @click="openNote(note.id)">
            <h3 class="note-title">{{ note.title || '无标题' }}</h3>
          </div>

           <!-- 内容区域 -->
           <div class="note-content" @click="openNote(note.id)">
             <p class="note-preview">{{ truncate(stripHtml(note.content), 500) }}</p>
           </div>

          <!-- 卡片底部 -->
          <div class="note-footer">
            <div class="footer-left">
              <!-- 日期 -->
              <span class="date">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(note.updatedAt) }}
              </span>
              <!-- 标签 -->
              <div class="tag-list">
                <el-tag
                  v-for="tag in note.tags"
                  :key="tag.id"
                  size="small"
                  class="note-tag"
                  effect="light"
                >
                  {{ tag.name }}
                </el-tag>
              </div>

            </div>
            <!-- 删除按钮 -->
            <div class="card-actions">
              <el-button
                type="danger"
                size="small"
                :icon="Delete"
                circle
                @click.stop="handleDelete(note.id, note.title)"
                class="delete-btn"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Plus, Calendar } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const notes = ref([])
const searchKeyword = ref('')
// 新增：当前选中的标签ID
const selectedTagId = ref<number | null>(null)
// 新增：所有标签选项
const tagOptions = ref<any[]>([])

// 获取所有标签（供下拉框使用）
const fetchTags = async () => {
  try {
    const res = await request.get('/tags')
    tagOptions.value = res.data
  } catch (error) {
    console.error('获取标签列表失败', error)
  }
}

const truncate = (text: string, length: number) => {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}

// 修改 fetchNotes，支持标签筛选
const fetchNotes = async () => {
  try {
    let url = '/notes'
    const params: any = {}
    if (selectedTagId.value) {
      // 如果选中了标签，则传递 tagId 参数
      params.tagId = selectedTagId.value
    } else if (searchKeyword.value) {
      // 如果有搜索关键词，则使用搜索接口
      url = '/notes/search'
      params.keyword = searchKeyword.value
    }
    // 将参数对象转换为查询字符串
    const queryString = new URLSearchParams(params).toString()
    const finalUrl = queryString ? `${url}?${queryString}` : url
    const res = await request.get(finalUrl)
    notes.value = res.data
  } catch (error) {
    ElMessage.error('获取笔记列表失败')
  }
}

const handleSearch = () => {
  fetchNotes()
}

const handleCreate = () => {
  router.push('/dashboard/notes/edit')
}

const handleDelete = async (id: number, title: string) => {
  try {
    await ElMessageBox.confirm(`确定要删除笔记《${title}》吗？`, '删除确认', { type: 'warning' })
    await request.delete(`/notes/${id}`)
    ElMessage.success('删除成功')
    await fetchNotes()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const openNote = (id: number) => {
  router.push(`/dashboard/notes/edit?id=${id}`)
}

const stripHtml = (html: string) => {
  return html.replace(/<[^>]*>/g, '')
}

const formatDate = (dateStr: string) => {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

onMounted(() => {
  fetchTags()
  fetchNotes()
})

</script>



<style scoped>
.tag-select {
  width: 100%;
}

.notes-container {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100%;
}

/* 搜索行 */
.search-row {
  margin-bottom: 24px;
}

.search-input {
  --el-input-border-radius: 20px;
  --el-input-height: 40px;
}

.create-btn {
  border-radius: 20px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 卡片网格 */
.notes-grid {
  margin: 0 -10px;
}

.note-card {
  height: auto;                /* 高度由内容决定 */
  display: flex;
  flex-direction: column;
  border-radius: 16px;
  transition: all 0.3s;
  margin-bottom: 20px;         /* 卡片间距 */
}

.note-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.note-header {
  cursor: pointer;
  padding: 16px 20px 8px 20px;
}

.note-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
  background: linear-gradient(135deg, #f8fafc 0%, #eef2f6 100%);
  padding: 8px 12px;
  border-radius: 8px;
  border-left: 4px solid rgb(156, 213, 255);
}

/* 内容区域：不再滚动，恢复普通布局 */
.note-content {
  cursor: pointer;
  padding: 0 20px 8px 20px;
  /* 移除所有滚动相关属性 */
}

.note-preview {
  margin: 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  /* 限制显示行数，超出省略 */
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.note-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px 20px 20px;
  border-top: 2px solid #edf2f7;
}

.footer-left {
  flex: 1;
  min-width: 0;
}

.date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: rgb(132, 148, 255);
  margin-bottom: 8px;
}

.date .el-icon {
  font-size: 14px;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.note-tag {
  border-radius: 12px;
  padding: 0 8px;
  height: 24px;
  line-height: 22px;
  border: 1px solid #e2e8f0;
  background: white;
  color: #475569;
  font-size: 12px;
}

.note-tag:hover {
  background: #f8fafc;
  border-color: #94a3b8;
}

.card-actions {
  margin-left: 12px;
  flex-shrink: 0;
}

.delete-btn {
  background: white;
  border: 1px solid #fee2e2;
  color: #ef4444;
}

.delete-btn:hover {
  background: #ef4444;
  color: white;
  border-color: #ef4444;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .notes-container {
    padding: 16px;
  }

  .text-right {
    text-align: left;
    margin-top: 12px;
  }

  .note-preview {
    -webkit-line-clamp: 2;   /* 小屏幕显示更少行 */
  }
}
</style>
