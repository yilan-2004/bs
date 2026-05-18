<template>
  <section class="side-card">
    <div class="side-head">
      <h3>学习提醒</h3>
      <span>{{ reminders.length }} 条</span>
    </div>

    <div v-if="reminders.length" class="reminder-list">
      <article v-for="item in reminders" :key="`${item.type}-${item.title}`" class="reminder-item" @click="openReminder(item)">
        <div class="reminder-icon" :class="item.type">
          <el-icon><component :is="iconFor(item.type)" /></el-icon>
        </div>
        <div>
          <strong>{{ item.title }}</strong>
          <p>{{ item.content }}</p>
        </div>
      </article>
    </div>
    <EmptyState v-else title="暂无提醒" description="暂无提醒，继续保持学习状态。" :icon="Bell" />
  </section>
</template>

<script setup>
import { Bell, MagicStick, TrendCharts, Warning } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import EmptyState from '../EmptyState.vue'

defineProps({
  reminders: { type: Array, default: () => [] }
})

const router = useRouter()

function iconFor(type) {
  if (type === 'AI_FEEDBACK_PENDING') return MagicStick
  if (type === 'TODAY_NOT_PRACTICED') return Bell
  if (type === 'WEAK_KNOWLEDGE') return TrendCharts
  return Warning
}

function openReminder(item) {
  router.push(item.targetUrl || '/student/banks')
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

.side-head span {
  color: #8190a8;
  font-size: 13px;
}

.reminder-list {
  display: grid;
  gap: 12px;
}

.reminder-item {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  padding: 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: background 0.18s ease;
}

.reminder-item:hover {
  background: #f8fbff;
}

.reminder-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #2563eb;
  background: #dbeafe;
  font-size: 22px;
}

.reminder-icon.AI_FEEDBACK_PENDING {
  color: #7c3aed;
  background: #ede9fe;
}

.reminder-icon.WEAK_KNOWLEDGE,
.reminder-icon.LATEST_WRONG {
  color: #f97316;
  background: #ffedd5;
}

.reminder-item strong {
  color: #111827;
  font-size: 14px;
}

.reminder-item p {
  display: -webkit-box;
  margin: 5px 0 0;
  overflow: hidden;
  color: #718096;
  font-size: 12px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
</style>
