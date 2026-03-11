<template>
  <div class="public-note">
    <el-card v-if="note">
      <h1>{{ note.title }}</h1>
      <div class="meta">
        创建于 {{ formatDate(note.createdAt) }}
      </div>
      <div class="content" v-html="note.content"></div>
      <div class="tags">
        <el-tag v-for="tag in note.tags" :key="tag.id">{{ tag.name }}</el-tag>
      </div>
    </el-card>
    <el-alert v-else-if="error" type="error" :title="error" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

const route = useRoute()
const note = ref(null)
const error = ref('')

const formatDate = (dateStr) => {
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${d.getMonth()+1}-${d.getDate()}`
}

onMounted(async () => {
  const shareId = route.params.shareId
  try {
    const res = await axios.get(`/api/share/note/${shareId}`)
    note.value = res.data.data
  } catch (err) {
    error.value = err.response?.data?.message || '获取笔记失败'
  }
})
</script>

<style scoped>
.public-note {
  max-width: 800px;
  margin: 20px auto;
}
.meta {
  color: #999;
  font-size: 14px;
  margin-bottom: 20px;
}
.content {
  line-height: 1.6;
}
.tags {
  margin-top: 20px;
}
</style>
