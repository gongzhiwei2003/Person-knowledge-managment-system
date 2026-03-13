<template>
  <div class="public-roadmap">
    <el-card v-if="roadmap">
      <h1>{{ roadmap.title }}</h1>
      <p v-if="roadmap.description" class="description">{{ roadmap.description }}</p>
      <div class="meta">
        创建于 {{ formatDate(roadmap.createdAt) }}
      </div>
      <!-- 路线图可视化区域（只读） -->
      <div class="roadmap-viewer">
        <VueFlow
          v-if="nodes.length || edges.length"
          :nodes="nodes"
          :edges="edges"
          :fit-view-on-init="true"
          :nodes-draggable="false"
          :nodes-connectable="false"
          :elements-selectable="false"
        >
          <Background />
          <Controls />
        </VueFlow>
        <el-empty v-else description="该路线图暂无内容" />
      </div>
    </el-card>
    <el-alert v-else-if="error" type="error" :title="error" />
    <el-skeleton v-else :rows="10" animated />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { VueFlow, Background, Controls } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

const route = useRoute()
const roadmap = ref<any>(null)
const error = ref('')
const nodes = ref<any[]>([])
const edges = ref<any[]>([])

// 格式化日期
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${d.getMonth() + 1}-${d.getDate()}`
}

// 监听 roadmap 数据变化，解析 graphData 并更新 nodes/edges
watchEffect(() => {
  if (roadmap.value && roadmap.value.graphData) {
    try {
      const graphData = JSON.parse(roadmap.value.graphData)
      nodes.value = graphData.nodes || []
      edges.value = graphData.edges || []
    } catch (e) {
      console.error('解析路线图数据失败', e)
    }
  } else {
    nodes.value = []
    edges.value = []
  }
})

// 获取公开路线图
const fetchRoadmap = async () => {
  const shareId = route.params.shareId
  if (!shareId) {
    error.value = '无效的分享链接'
    return
  }
  try {
    const res = await axios.get(`/api/share/roadmap/${shareId}`)
    roadmap.value = res.data.data
  } catch (err: any) {
    error.value = err.response?.data?.message || '获取路线图失败'
  }
}

onMounted(fetchRoadmap)
</script>

<style scoped>
.public-roadmap {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 20px;
}
.description {
  color: #666;
  margin: 10px 0;
}
.meta {
  color: #999;
  font-size: 14px;
  margin-bottom: 20px;
}
.roadmap-viewer {
  width: 100%;
  height: 500px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>
