<template>
  <div class="custom-node" :class="{ completed: data.completed }">
    <!-- 左侧手柄：作为目标（target） -->
    <Handle type="target" :position="Position.Left" />

    <el-checkbox
      v-model="data.completed"
      @change="handleCheckboxChange"
      size="small"
    />
    <div v-if="isEditing" class="node-edit">
      <el-input
        v-model="editLabel"
        size="small"
        @blur="saveEdit"
        @keyup.enter="saveEdit"
        ref="inputRef"
        autofocus
      />
    </div>
    <span v-else @dblclick="startEdit" class="node-label">{{ data.label }}</span>

    <!-- 右侧手柄：作为源（source） -->
    <Handle type="source" :position="Position.Right" />

  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { ref, nextTick } from 'vue'
import { Handle, Position } from '@vue-flow/core'

// 定义组件的 props
const props = defineProps<{
  id: string           // 节点ID
  data: any            // 节点数据，包含 label, completed, roadmapId 等
}>()

// 编辑状态
const isEditing = ref(false)
const editLabel = ref(props.data.label || '')
const inputRef = ref<HTMLInputElement>()

// 开始编辑
const startEdit = () => {
  editLabel.value = props.data.label
  isEditing.value = true
  nextTick(() => {
    inputRef.value?.focus()
  })
}

// 保存编辑
const saveEdit = async () => {
  if (!editLabel.value.trim()) {
    ElMessage.warning('节点名称不能为空')
    return
  }
  // 更新当前节点的 data.label
  props.data.label = editLabel.value
  isEditing.value = false
  // 这里为了简单不立即保存，依赖用户点击“保存”按钮
}

// 复选框变化事件
const handleCheckboxChange = async (value: boolean) => {
  const roadmapId = props.data.roadmapId
  if (!roadmapId) {
    ElMessage.warning('未关联路线图，无法更新状态')
    return
  }
  try {
    await request.post(`/roadmaps/${roadmapId}/nodes/complete`, {
      nodeId: props.id,
      completed: value
    })
  } catch {
    ElMessage.error('更新节点状态失败')
    props.data.completed = !value // 回滚
  }
}

</script>


<style scoped>


/* 手柄默认是圆点，可以自定义样式 */
.handle {
  width: 16px !important;
  height: 16px !important;
  background-color: red !important;
  border: 2px solid white !important;
  border-radius: 50%;
  z-index: 100;
}

.node-label {
  cursor: pointer;
  user-select: none;
}
.node-edit {
  width: 100%;
}
.custom-node {
  position: relative; /* 使手柄可以相对于节点定位 */
  padding: 8px 12px;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  min-width: 150px;
}
.custom-node.completed {
  background: #f0f9eb;
  border-color: #67c23a;
}
</style>
