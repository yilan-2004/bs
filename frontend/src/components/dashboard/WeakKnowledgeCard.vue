<template>
  <section class="side-card">
    <div class="side-head">
      <h3>薄弱知识点</h3>
      <el-button text type="primary" @click="router.push('/student/report')">报告</el-button>
    </div>

    <div v-if="items.length" class="weak-list">
      <article v-for="item in items" :key="item.knowledgeTag" class="weak-row" @click="openTag(item.knowledgeTag)">
        <div class="weak-name">
          <strong>{{ item.knowledgeTag }}</strong>
          <span>{{ item.wrongCount }}/{{ item.submitCount }} 次错误</span>
        </div>
        <el-progress :percentage="weakRate(item)" :show-text="false" :stroke-width="8" />
      </article>
    </div>
    <EmptyState v-else title="暂无薄弱知识点" description="提交练习后，系统会根据错误记录识别薄弱知识点。" :icon="TrendCharts" />
  </section>
</template>

<script setup>
import { TrendCharts } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import EmptyState from '../EmptyState.vue'

defineProps({
  items: { type: Array, default: () => [] }
})

const router = useRouter()

function weakRate(item) {
  if (!item.submitCount) return 0
  return Math.min(100, Math.round((item.wrongCount / item.submitCount) * 100))
}

function openTag(tag) {
  router.push(`/student/banks?knowledgeTag=${encodeURIComponent(tag)}`)
}
</script>

<style scoped>
.side-card {
  padding: 22px;
  border: 1px solid #e8eef8;
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.055);
}

.side-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.side-head h3 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 900;
}

.weak-list {
  display: grid;
  gap: 15px;
}

.weak-row {
  display: grid;
  gap: 8px;
  cursor: pointer;
}

.weak-name {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #718096;
  font-size: 12px;
}

.weak-name strong {
  color: #111827;
  font-size: 14px;
}
</style>
