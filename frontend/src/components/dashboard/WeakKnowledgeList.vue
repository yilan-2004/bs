<template>
  <section class="side-card">
    <div class="side-head">
      <h3>薄弱知识点</h3>
      <el-button text type="primary" @click="router.push('/student/report')">查看全部</el-button>
    </div>

    <div v-if="normalized.length" class="weak-list">
      <button v-for="item in normalized" :key="item.knowledgeTag" class="weak-item" @click="go(item.knowledgeTag)">
        <div class="weak-row">
          <strong>{{ item.knowledgeTag }}</strong>
          <span>错误率 {{ item.wrongRate }}%</span>
        </div>
        <el-progress :percentage="item.wrongRate" :show-text="false" :stroke-width="7" color="#f59e0b" />
      </button>
    </div>

    <EmptyState v-else description="暂无明显薄弱知识点" />
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import EmptyState from '../EmptyState.vue'

const props = defineProps({
  items: { type: Array, default: () => [] }
})

const router = useRouter()

const normalized = computed(() => props.items
  .map((item) => {
    const submitCount = Number(item.submitCount || 0)
    const wrongCount = Number(item.wrongCount || 0)
    return {
      knowledgeTag: item.knowledgeTag || item.tag || '未标注',
      wrongRate: submitCount ? Math.round((wrongCount * 100) / submitCount) : Number(item.wrongRate || 0)
    }
  })
  .filter((item) => item.wrongRate > 0)
  .sort((a, b) => b.wrongRate - a.wrongRate)
  .slice(0, 5))

function go(tag) {
  router.push(`/student/banks?knowledgeTag=${encodeURIComponent(tag)}`)
}
</script>

<style scoped>
.side-card {
  padding: 20px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.side-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 17px;
  font-weight: 900;
}

.weak-list {
  display: grid;
  gap: 14px;
}

.weak-item {
  width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
  text-align: left;
}

.weak-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}

.weak-row strong {
  color: #0f172a;
  font-size: 13px;
}

.weak-row span {
  color: #64748b;
  font-size: 12px;
}
</style>
