<template>
  <section class="section-card">
    <div class="section-head">
      <div>
        <h2>学科 / 题库入口</h2>
        <p>根据学科聚合题库和练习表现，点击进入对应训练区。</p>
      </div>
      <el-button text type="primary" @click="router.push('/student/banks')">
        全部题库
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <div v-if="subjects.length" class="subject-grid">
      <article v-for="(item, index) in subjects" :key="item.subjectId || index" class="subject-card" @click="openSubject(item)">
        <div class="subject-icon" :class="palette[index % palette.length]">
          <el-icon><component :is="iconFor(item.subjectName)" /></el-icon>
        </div>
        <div class="subject-content">
          <h3>{{ item.subjectName || '未分配学科' }}</h3>
          <p>{{ item.bankCount || 0 }} 个题库 · 已练 {{ item.practicedQuestionCount || 0 }} 题</p>
          <div class="mini-progress">
            <span :style="{ width: `${item.accuracyRate || 0}%` }"></span>
          </div>
          <small>正确率 {{ item.accuracyRate || 0 }}%</small>
          <div v-if="item.weakTags?.length" class="tag-row">
            <el-tag v-for="tag in item.weakTags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>
        </div>
      </article>
    </div>
    <EmptyState v-else title="暂无学科入口" description="完成题库和题目配置后，这里会显示可练习的学科入口。" :icon="Collection" />
  </section>
</template>

<script setup>
import { ArrowRight, Collection, DataAnalysis, EditPen, Notebook, Reading } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import EmptyState from '../EmptyState.vue'

defineProps({
  subjects: { type: Array, default: () => [] }
})

const router = useRouter()
const palette = ['blue', 'orange', 'green', 'purple', 'pink', 'cyan']

function iconFor(name = '') {
  if (name.includes('编程')) return EditPen
  if (name.includes('数学')) return DataAnalysis
  if (name.includes('英语')) return Reading
  if (name.includes('语文')) return Notebook
  return Collection
}

function openSubject(item) {
  router.push(item.subjectId ? `/student/banks?subjectId=${item.subjectId}` : '/student/banks')
}
</script>

<style scoped>
.section-card {
  padding: 24px;
  border: 1px solid #e8eef8;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.055);
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 900;
}

.section-head p {
  margin: 6px 0 0;
  color: #8190a8;
  font-size: 13px;
}

.subject-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 16px;
}

.subject-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  gap: 14px;
  padding: 18px;
  border: 1px solid #edf2fb;
  border-radius: 18px;
  background: linear-gradient(180deg, #fbfdff 0%, #fff 100%);
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.subject-card:hover {
  box-shadow: 0 16px 34px rgba(37, 99, 235, 0.12);
  transform: translateY(-3px);
}

.subject-icon {
  display: inline-flex;
  width: 46px;
  height: 46px;
  align-items: center;
  justify-content: center;
  border-radius: 15px;
  color: #fff;
  font-size: 24px;
}

.subject-icon.blue { background: linear-gradient(135deg, #2563eb, #60a5fa); }
.subject-icon.orange { background: linear-gradient(135deg, #f59e0b, #fb923c); }
.subject-icon.green { background: linear-gradient(135deg, #10b981, #34d399); }
.subject-icon.purple { background: linear-gradient(135deg, #7c3aed, #a78bfa); }
.subject-icon.pink { background: linear-gradient(135deg, #ec4899, #f472b6); }
.subject-icon.cyan { background: linear-gradient(135deg, #06b6d4, #22d3ee); }

.subject-content {
  min-width: 0;
}

.subject-content h3 {
  margin: 0;
  color: #111827;
  font-size: 17px;
  font-weight: 900;
}

.subject-content p,
.subject-content small {
  color: #718096;
  font-size: 12px;
}

.subject-content p {
  margin: 6px 0 10px;
}

.mini-progress {
  width: 100%;
  height: 7px;
  overflow: hidden;
  border-radius: 999px;
  background: #eef2f7;
}

.mini-progress span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #2563eb, #7c3aed);
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}
</style>
