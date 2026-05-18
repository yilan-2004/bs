<template>
  <article class="bank-card elevated-card" @click="$emit('enter', bank)">
    <div class="cover-wrap">
      <img class="bank-cover" :src="coverSrc" :alt="`${bank.name || '题库'}封面`" @error="useFallbackCover" />
      <span class="difficulty" :class="difficultyClass">{{ difficultyText }}</span>
    </div>

    <div class="bank-body">
      <h3>{{ bank.name }}</h3>
      <p>{{ bank.description || '暂无简介，进入题库开始系统化编程训练。' }}</p>
      <el-tag v-if="bank.subjectName" type="primary" effect="plain" round>{{ bank.subjectName }}</el-tag>
      <ProblemTag :tags="bank.knowledgeTags" />

      <div class="bank-meta">
        <span>{{ bank.problemCount || 0 }} 道题</span>
        <span>学习进度 {{ progress }}%</span>
      </div>
      <el-progress :percentage="progress" :show-text="false" :stroke-width="8" />

      <div class="bank-footer">
        <el-button type="primary" round @click.stop="$emit('enter', bank)">开始练习</el-button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import ProblemTag from './ProblemTag.vue'

defineEmits(['enter'])

const props = defineProps({
  bank: { type: Object, required: true },
  progress: { type: Number, default: 0 }
})

const defaultCover = '/assets/default-bank-cover.png'
const coverSrc = ref(props.bank.coverUrl || defaultCover)

watch(() => props.bank.coverUrl, (value) => {
  coverSrc.value = value || defaultCover
})

function useFallbackCover() {
  coverSrc.value = defaultCover
}

const difficultyMap = {
  EASY: { text: '基础', className: 'difficulty-easy' },
  MEDIUM: { text: '进阶', className: 'difficulty-medium' },
  HARD: { text: '挑战', className: 'difficulty-hard' },
  MIXED: { text: '综合', className: 'difficulty-mixed' }
}

const difficultyInfo = computed(() => difficultyMap[props.bank.difficulty] || difficultyMap.MIXED)
const difficultyText = computed(() => difficultyInfo.value.text)
const difficultyClass = computed(() => difficultyInfo.value.className)
</script>

<style scoped>
.bank-card {
  overflow: hidden;
  cursor: pointer;
  border: 1px solid rgba(148, 163, 184, 0.20);
  border-radius: 20px;
  background: #fff;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.06);
}

.cover-wrap {
  position: relative;
  padding: 14px 14px 0;
}

.cover-wrap::after {
  position: absolute;
  inset: 14px 14px auto;
  height: 4px;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(90deg, #2563eb, #06b6d4, #7c3aed);
  content: "";
}

.difficulty {
  position: absolute;
  right: 24px;
  top: 24px;
  padding: 6px 11px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.12);
}

.bank-body {
  display: grid;
  gap: 14px;
  padding: 18px 20px 20px;
}

.bank-card h3 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 900;
}

.bank-card p {
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

.bank-meta,
.bank-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.bank-meta {
  color: #64748b;
  font-size: 13px;
  font-weight: 700;
}
</style>
