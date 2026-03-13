<template>
  <div class="overview">
    <el-row :gutter="20">
      <!-- 笔记趋势图 -->
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>笔记数量趋势（近6个月）</span>
          </template>
          <div ref="noteTrendChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="mt-4">
      <!-- 标签统计 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>标签使用频率</span>
          </template>
          <div ref="tagChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <!-- 目标进度 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>学习目标完成情况</span>
          </template>
          <div ref="goalChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

interface Statistics {
  noteTrend: { month: string; count: number }[]
  tagCounts: { name: string; count: number }[]
  goalProgress: { totalGoals: number; completedGoals: number; completionRate: number }
}

const noteTrendChart = ref<HTMLElement>()
const tagChart = ref<HTMLElement>()
const goalChart = ref<HTMLElement>()

const fetchStatistics = async () => {
  try {
    const res = await request.get('/statistics')
    const data: Statistics = res.data
    nextTick(() => {
      initNoteTrendChart(data.noteTrend)
      initTagChart(data.tagCounts)
      initGoalChart(data.goalProgress)
    })
  } catch {
    ElMessage.error('获取统计数据失败')
  }
}

const initNoteTrendChart = (data: { month: string; count: number }[]) => {
  if (!noteTrendChart.value) return
  const chart = echarts.init(noteTrendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data.map(d => d.month) },
    yAxis: { type: 'value' },
    series: [{
      data: data.map(d => d.count),
      type: 'line',
      smooth: true,
      areaStyle: {}
    }]
  })
}

const initTagChart = (data: { name: string; count: number }[]) => {
  if (!tagChart.value) return
  const chart = echarts.init(tagChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      name: '标签频率',
      type: 'pie',
      radius: '50%',
      data: data.map(d => ({ name: d.name, value: d.count })),
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })
}

const initGoalChart = (data: { totalGoals: number; completedGoals: number; completionRate: number }) => {
  if (!goalChart.value) return
  const chart = echarts.init(goalChart.value)
  chart.setOption({
    tooltip: { formatter: '{b}: {c}%' },
    series: [{
      type: 'gauge',
      center: ['50%', '60%'],
      radius: '80%',
      startAngle: 180,
      endAngle: 0,
      min: 0,
      max: 100,
      splitNumber: 10,
      progress: {
        show: true,
        width: 30
      },
      axisLine: {
        lineStyle: {
          width: 30
        }
      },
      axisTick: {
        show: false
      },
      splitLine: {
        show: false
      },
      axisLabel: {
        show: false
      },
      detail: {
        valueAnimation: true,
        fontSize: 40,
        offsetCenter: [0, '20%'],
        formatter: '{value}%'
      },
      data: [{
        value: (data.completionRate * 100).toFixed(1),
        name: '完成率'
      }]
    }],
    title: {
      show: true,
      text: `目标总数: ${data.totalGoals}  已完成: ${data.completedGoals}`,
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 16
      }
    }
  })
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.overview {
  padding: 20px;
}
.mt-4 {
  margin-top: 20px;
}
</style>
