<template>
  <section ref="rootRef" class="side-card">
    <div class="side-head">
      <h3>今日任务</h3>
      <el-button text type="primary" @click="router.push('/student/banks')">查看全部</el-button>
    </div>

    <div v-if="tasks.length" class="task-list">
      <button v-for="task in tasks" :key="`${task.type}-${task.title}`" class="task-item" @click="go(task.targetUrl)">
        <span class="task-icon" :class="taskClass(task.type)">
          <el-icon><component :is="taskIcon(task.type)" /></el-icon>
        </span>
        <span class="task-copy">
          <strong>{{ task.title }}</strong>
          <em>{{ task.content }}</em>
        </span>
      </button>
    </div>

    <EmptyState v-else description="暂无提醒，继续保持学习状态" />
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ChatLineRound, Collection, Flag, Warning } from '@element-plus/icons-vue'
import EmptyState from '../EmptyState.vue'

defineProps({
  tasks: { type: Array, default: () => [] }
})

const router = useRouter()
const rootRef = ref(null)

defineExpose({
  scrollIntoView: () => rootRef.value?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
})

function go(targetUrl) {
  router.push(targetUrl || '/student/banks')
}

function taskIcon(type) {
  if (type === 'AI_FEEDBACK_PENDING') return ChatLineRound
  if (type === 'WEAK_KNOWLEDGE') return Warning
  if (type === 'LATEST_WRONG') return Flag
  return Collection
}

function taskClass(type) {
  if (type === 'AI_FEEDBACK_PENDING') return 'purple'
  if (type === 'WEAK_KNOWLEDGE') return 'orange'
  if (type === 'LATEST_WRONG') return 'red'
  return 'blue'
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

.task-list {
  display: grid;
  gap: 12px;
}

.task-item {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 12px;
  width: 100%;
  padding: 12px;
  border: 1px solid #edf2f7;
  border-radius: 14px;
  background: #f8fafc;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.task-item:hover {
  transform: translateY(-2px);
  border-color: #bfdbfe;
}

.task-icon {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.task-icon.blue {
  background: #2563eb;
}

.task-icon.purple {
  background: #7c3aed;
}

.task-icon.orange {
  background: #f59e0b;
}

.task-icon.red {
  background: #ef4444;
}

.task-copy strong,
.task-copy em {
  display: block;
}

.task-copy strong {
  overflow: hidden;
  color: #0f172a;
  font-size: 14px;
  font-style: normal;
  font-weight: 800;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-copy em {
  display: -webkit-box;
  margin-top: 5px;
  overflow: hidden;
  color: #64748b;
  font-size: 12px;
  font-style: normal;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
