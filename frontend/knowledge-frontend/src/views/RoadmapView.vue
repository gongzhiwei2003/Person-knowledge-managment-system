<template>
  <div class="roadmap-container">
    <div class="toolbar">
      <el-select
        v-if="roadmaps.length > 0"
        v-model="currentRoadmapId"
        placeholder="选择路线图"
        @change="loadRoadmap"
      >
        <el-option
          v-for="item in roadmaps"
          :key="item.id"
          :label="item.title"
          :value="item.id"
        />
      </el-select>
      <el-button type="primary" @click="addNode">添加节点</el-button>
      <el-button @click="saveRoadmap">保存</el-button>
      <el-button @click="createNewRoadmap">新建路线图</el-button>
    </div>
    <div class="vue-flow-wrapper">
     <VueFlow
       :nodes="nodes"
       :edges="edges"
       :node-types="nodeTypes"
       @connect="onConnect"
     >
       <Background />
       <Controls />
     </VueFlow>
    </div>

    <!-- 新建路线图对话框 -->
    <el-dialog v-model="dialogVisible" title="新建路线图" width="30%">
      <el-input v-model="newRoadmapTitle" placeholder="请输入路线图名称" />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitNewRoadmap">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>



<script setup lang="ts">
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { ref, onMounted, markRaw } from 'vue'
import { VueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import CustomNode from './CustomNode.vue'  // 稍后创建

// 拆分为独立的 ref
const nodes = ref([])
const edges = ref([])

const roadmaps = ref([])
const currentRoadmapId = ref('')
const dialogVisible = ref(false)
const newRoadmapTitle = ref('')
const route = useRoute()

// 定义节点类型变量
const nodeTypes = {
  custom: markRaw(CustomNode),
}

onMounted(async () => {
  await fetchRoadmaps()
  const roadmapId = route.query.roadmapId
  if (roadmapId) {
    currentRoadmapId.value = roadmapId
    await loadRoadmap(roadmapId)
  }
})

// 连接事件：自动添加边
const onConnect = (connection) => {
  // connection 对象包含 source 和 target 节点 ID
  edges.value.push({
    id: `e${connection.source}-${connection.target}`,
    source: connection.source,
    target: connection.target
  })
}

// 添加节点
const addNode = () => {
  if (!currentRoadmapId.value) {
    ElMessage.warning('请先选择或创建路线图')
    return
  }
  nodes.value.push({
    id: `node-${Date.now()}`,
    type: 'custom',
    position: { x: Math.random() * 300, y: Math.random() * 300 },
    data: {
      label: '新节点',
      completed: false,
      roadmapId: currentRoadmapId.value
    }
  })
}

// 获取路线图列表
const fetchRoadmaps = async () => {
  try {
    const res = await request.get('/roadmaps')
    roadmaps.value = res.data
  } catch {
    ElMessage.error('获取路线图列表失败')
  }
}

// 加载指定路线图
const loadRoadmap = async (id) => {
  try {
    const res = await request.get(`/roadmaps/${id}`)
    const data = JSON.parse(res.data.graphData || '{"nodes":[],"edges":[]}')
    nodes.value = (data.nodes || []).map(node => ({
      ...node,
      type: 'custom',   // 指定使用自定义节点
      data: {
        ...node.data,
        roadmapId: id   // 注入路线图ID
      }
    }))
    edges.value = data.edges || []
  } catch {
    ElMessage.error('加载路线图失败')
  }
}

// 保存路线图
const saveRoadmap = async () => {
  if (!currentRoadmapId.value) {
    ElMessage.warning('请先选择或创建路线图')
    return
  }
  try {
    await request.put(`/roadmaps/${currentRoadmapId.value}/graph`, {
      nodes: nodes.value,
      edges: edges.value
    })
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败')
  }
}

// 新建路线图
const createNewRoadmap = () => {
  dialogVisible.value = true
}

const submitNewRoadmap = async () => {
  if (!newRoadmapTitle.value.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  try {
    const res = await request.post('/roadmaps', { title: newRoadmapTitle.value })
    ElMessage.success('创建成功')
    dialogVisible.value = false
    newRoadmapTitle.value = ''
    await fetchRoadmaps()
    currentRoadmapId.value = res.data.id
    await loadRoadmap(res.data.id)
  } catch {
    ElMessage.error('创建失败')
  }
}

onMounted(fetchRoadmaps)
</script>


<style scoped>
.roadmap-container {
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}
.toolbar {
  padding: 10px;
  background: #f5f5f5;
  border-bottom: 1px solid #ddd;
  display: flex;
  gap: 10px;
  align-items: center;
}
.vue-flow-wrapper {
  flex: 1;
  background: #f0f2f5;
}
</style>
