<template>
  <section class="subject-section">
    <div class="section-head">
      <div>
        <h3>我的学科</h3>
        <p>按学科查看练习表现，优先补齐薄弱知识点。</p>
      </div>
      <el-button text type="primary" @click="router.push('/student/banks')">查看全部</el-button>
    </div>

    <div v-if="subjects.length" class="subject-grid">
      <article v-for="subject in displaySubjects" :key="subject.subjectId || subject.subjectName" class="subject-card">
        <div class="subject-top">
          <span class="subject-icon" :class="iconClass(subject.subjectName)">
            <el-icon><component :is="subjectIcon(subject.subjectName)" /></el-icon>
          </span>
          <div>
            <h4>{{ subject.subjectName || '未命名学科' }}</h4>
            <p>{{ subject.bankCount || 0 }} 个题库</p>
          </div>
        </div>

        <div class="subject-body">
          <el-progress
            type="circle"
            :width="66"
            :stroke-width="7"
            :percentage="Number(subject.accuracyRate || 0)"
            :color="progressColor(subject.accuracyRate)"
          />
          <div class="subject-metrics">
            <span>已练习 <strong>{{ subject.practicedQuestionCount || 0 }}</strong></span>
            <span>正确 <strong>{{ subject.acceptedCount || 0 }}</strong></span>
            <span>错误 <strong>{{ subject.wrongCount || 0 }}</strong></span>
          </div>
        </div>

        <div class="weak-tags">
          <span>薄弱知识点</span>
          <div>
            <el-tag v-for="tag in (subject.weakTags || []).slice(0, 3)" :key="tag" size="small" round>
              {{ tag }}
            </el-tag>
            <em v-if="!(subject.weakTags || []).length">暂无明显薄弱点</em>
          </div>
        </div>

        <el-button class="practice-button" @click="goPractice(subject)">去练习</el-button>
      </article>
    </div>

    <EmptyState v-else description="暂无学科练习数据，先从题库开始练习吧" />
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { Cpu, Collection, MagicStick, Reading, Notebook, TrendCharts } from '@element-plus/icons-vue'
import EmptyState from '../EmptyState.vue'

const props = defineProps({
  subjects: { type: Array, default: () => [] }
})

const router = useRouter()

const displaySubjects = computed(() => props.subjects.slice(0, 4))

function goPractice(subject) {
  const query = subject.subjectId ? `?subjectId=${subject.subjectId}` : ''
  router.push(`/student/banks${query}`)
}

function subjectIcon(name = '') {
  if (name.includes('编程')) return Cpu
  if (name.includes('数学')) return TrendCharts
  if (name.includes('英语')) return Reading
  if (name.includes('物理')) return MagicStick
  if (name.includes('语文')) return Notebook
  return Collection
}

function iconClass(name = '') {
  if (name.includes('编程')) return 'blue'
  if (name.includes('数学')) return 'green'
  if (name.includes('英语')) return 'purple'
  if (name.includes('物理')) return 'orange'
  return 'cyan'
}

function progressColor(value) {
  const rate = Number(value || 0)
  if (rate >= 80) return '#10b981'
  if (rate >= 60) return '#2563eb'
  if (rate >= 40) return '#f59e0b'
  return '#ef4444'
}
</script>

<style scoped>
.subject-section {
  padding: 22px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

.section-head p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.subject-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(230px, 1fr));
  gap: 14px;
}

.subject-card {
  padding: 18px;
  border: 1px solid #e8eef7;
  border-radius: 18px;
  background: linear-gradient(180deg, #ffffff, #f8fbff);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.subject-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 32px rgba(37, 99, 235, 0.12);
}

.subject-top {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.subject-icon {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.subject-icon.blue {
  background: linear-gradient(135deg, #2563eb, #60a5fa);
}

.subject-icon.green {
  background: linear-gradient(135deg, #10b981, #34d399);
}

.subject-icon.purple {
  background: linear-gradient(135deg, #7c3aed, #a78bfa);
}

.subject-icon.orange {
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
}

.subject-icon.cyan {
  background: linear-gradient(135deg, #06b6d4, #67e8f9);
}

.subject-top h4 {
  margin: 0;
  color: #0f172a;
  font-size: 16px;
  font-weight: 900;
  word-break: keep-all;
  overflow-wrap: normal;
}

.subject-top p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.subject-body {
  display: grid;
  grid-template-columns: 72px minmax(120px, 1fr);
  align-items: center;
  gap: 12px;
}

.subject-metrics {
  display: grid;
  gap: 7px;
  color: #64748b;
  font-size: 12px;
  word-break: keep-all;
  overflow-wrap: normal;
}

.subject-metrics strong {
  color: #0f172a;
}

.weak-tags {
  margin-top: 16px;
}

.weak-tags > span {
  display: block;
  margin-bottom: 8px;
  color: #64748b;
  font-size: 12px;
}

.weak-tags div {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-height: 24px;
}

.weak-tags em {
  color: #94a3b8;
  font-size: 12px;
  font-style: normal;
}

.practice-button {
  width: 100%;
  margin-top: 16px;
  border-radius: 12px;
  color: #2563eb;
  font-weight: 800;
  background: #eff6ff;
}

@media (max-width: 1280px) {
  .subject-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 700px) {
  .subject-grid {
    grid-template-columns: 1fr;
  }
}
</style>
