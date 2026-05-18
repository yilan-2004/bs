<template>
  <article class="problem-card elevated-card" @click="$emit('start', problem)">
    <div class="problem-top">
      <span class="difficulty" :class="difficultyClass">{{ difficultyText }}</span>
      <StatusTag v-if="lastSubmitStatus" :status="lastSubmitStatus" />
    </div>
    <h3>{{ problem.title }}</h3>
    <p>{{ problem.description || '进入题目详情查看完整描述并开始编码训练。' }}</p>
    <ProblemTag :tags="problem.knowledgeTags" />
    <div class="type-row">
      <el-tag v-if="problem.subjectName" type="primary" effect="plain" round>{{ problem.subjectName }}</el-tag>
      <el-tag effect="plain" round>{{ questionTypeText }}</el-tag>
    </div>
    <div class="problem-meta">
      <span v-if="problem.bankName">题库：{{ problem.bankName }}</span>
      <span v-else>独立题目</span>
      <span>{{ problem.status === 0 ? '已禁用' : '可练习' }}</span>
    </div>
    <el-button type="primary" round @click.stop="$emit('start', problem)">
      {{ buttonText }}
    </el-button>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import ProblemTag from './ProblemTag.vue'
import StatusTag from './StatusTag.vue'

defineEmits(['start'])

const props = defineProps({
  problem: { type: Object, required: true },
  buttonText: { type: String, default: '开始练习' },
  lastSubmitStatus: { type: String, default: '' }
})

const difficultyMap = {
  EASY: { text: '简单', className: 'difficulty-easy' },
  MEDIUM: { text: '中等', className: 'difficulty-medium' },
  HARD: { text: '困难', className: 'difficulty-hard' }
}

const difficultyInfo = computed(() => difficultyMap[props.problem.difficulty] || { text: props.problem.difficulty || '未分级', className: 'difficulty-mixed' })
const difficultyText = computed(() => difficultyInfo.value.text)
const difficultyClass = computed(() => difficultyInfo.value.className)
const questionTypeText = computed(() => ({
  PROGRAMMING: '编程题',
  CHOICE: '选择题',
  FILL_BLANK: '填空题',
  SHORT_ANSWER: '简答题'
})[props.problem.questionType] || '编程题')
</script>

<style scoped>
.problem-card {
  display: grid;
  gap: 14px;
  padding: 20px;
  cursor: pointer;
  border: 1px solid rgba(148, 163, 184, 0.20);
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.problem-top,
.problem-meta,
.type-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.problem-top,
.problem-meta {
  justify-content: space-between;
}

.type-row {
  flex-wrap: wrap;
}

.difficulty {
  padding: 6px 11px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}

.problem-card h3 {
  margin: 0;
  color: #111827;
  font-size: 19px;
  font-weight: 900;
}

.problem-card p {
  display: -webkit-box;
  min-height: 48px;
  margin: 0;
  overflow: hidden;
  color: #6b7280;
  font-size: 14px;
  line-height: 1.7;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.problem-meta {
  color: #64748b;
  font-size: 13px;
}
</style>
