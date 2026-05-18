<template>
  <div class="page page-stack" v-loading="loading">
    <section v-if="bank" class="bank-hero">
      <div class="bank-hero-copy">
        <el-button text :icon="ArrowLeft" @click="router.push('/student/banks')">返回题库</el-button>
        <h1>{{ bank.name }}</h1>
        <p>{{ bank.description || '跟随题库路径完成系统化编程练习。' }}</p>
        <div class="hero-tags">
          <span class="difficulty" :class="difficultyClass">{{ difficultyText }}</span>
          <ProblemTag :tags="bank.knowledgeTags" />
        </div>
      </div>
      <div class="bank-hero-media">
        <img class="bank-detail-cover" :src="coverSrc" :alt="bank.name" @error="useFallbackCover" />
        <div class="hero-progress">
          <strong>{{ bank.problemCount || problems.length }}</strong>
          <span>题目数量</span>
          <el-progress :percentage="0" :stroke-width="10" />
        </div>
      </div>
    </section>

    <section>
      <div class="section-title"><Reading /> 题库题目</div>
      <div v-if="problems.length" class="card-grid">
        <ProblemCard v-for="problem in problems" :key="problem.id" :problem="problem" @start="openProblem" />
      </div>
      <EmptyState v-else title="该题库暂无题目" description="题目发布后可从这里进入原有代码提交页面" :icon="Reading" />
    </section>
  </div>
</template>

<script setup>
import { ArrowLeft, Reading } from '@element-plus/icons-vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { problemBankApi } from '../../api/problemBank'
import EmptyState from '../../components/EmptyState.vue'
import ProblemCard from '../../components/ProblemCard.vue'
import ProblemTag from '../../components/ProblemTag.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const bank = ref(null)
const problems = ref([])
const defaultCover = '/assets/default-bank-cover.png'
const coverSrc = ref(defaultCover)

watch(() => bank.value?.coverUrl, (value) => {
  coverSrc.value = value || defaultCover
})

const difficultyMap = {
  EASY: { text: '基础', className: 'difficulty-easy' },
  MEDIUM: { text: '进阶', className: 'difficulty-medium' },
  HARD: { text: '挑战', className: 'difficulty-hard' },
  MIXED: { text: '综合', className: 'difficulty-mixed' }
}
const difficultyInfo = computed(() => difficultyMap[bank.value?.difficulty] || difficultyMap.MIXED)
const difficultyText = computed(() => difficultyInfo.value.text)
const difficultyClass = computed(() => difficultyInfo.value.className)

function useFallbackCover() {
  coverSrc.value = defaultCover
}

async function loadDetail() {
  loading.value = true
  try {
    const id = route.params.id
    const [bankData, problemData] = await Promise.all([
      problemBankApi.detail(id),
      problemBankApi.problems(id, { pageNum: 1, pageSize: 100 })
    ])
    bank.value = bankData
    coverSrc.value = bankData.coverUrl || defaultCover
    problems.value = problemData.records || problemData || []
  } finally {
    loading.value = false
  }
}

function openProblem(problem) {
  router.push(`/student/problem/${problem.id}`)
}

onMounted(loadDetail)
</script>

<style scoped>
.bank-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  align-items: center;
  gap: 28px;
  padding: 30px;
  overflow: hidden;
  border: 1px solid rgba(37, 99, 235, 0.15);
  border-radius: 28px;
  background:
    radial-gradient(circle at 86% 12%, rgba(124, 58, 237, 0.12), transparent 30%),
    linear-gradient(135deg, #eff6ff, #fff);
  box-shadow: 0 18px 42px rgba(37, 99, 235, 0.08);
}

.bank-hero-copy {
  min-width: 0;
}

.bank-hero h1 {
  margin: 12px 0 0;
  color: #111827;
  font-size: 32px;
  font-weight: 900;
}

.bank-hero p {
  max-width: 720px;
  margin: 12px 0;
  color: #64748b;
  line-height: 1.8;
}

.hero-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.difficulty {
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}

.bank-hero-media {
  position: relative;
  display: grid;
  gap: 14px;
  max-height: 300px;
  overflow: hidden;
}

.bank-detail-cover {
  width: 100%;
  height: 210px;
  object-fit: cover;
  border-radius: 22px;
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.12);
}

.hero-progress {
  position: absolute;
  right: 18px;
  bottom: 18px;
  width: 180px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.58);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.10);
  backdrop-filter: blur(10px);
}

.hero-progress strong,
.hero-progress span {
  display: block;
}

.hero-progress strong {
  color: #2563eb;
  font-size: 30px;
  font-weight: 900;
}

.hero-progress span {
  margin: 4px 0 12px;
  color: #64748b;
}

@media (max-width: 1100px) {
  .bank-hero {
    grid-template-columns: 1fr;
  }
}
</style>
