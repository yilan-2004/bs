<template>
  <section class="side-card">
    <div class="side-head">
      <div>
        <h3>学习排行榜</h3>
        <p>本周</p>
      </div>
      <el-button text type="primary" @click="dialogVisible = true">查看全部</el-button>
    </div>

    <div v-if="list.length" class="rank-list">
      <article v-for="item in list.slice(0, 5)" :key="`${item.rank}-${item.studentName}`" class="rank-item" :class="{ me: item.isMe }">
        <span class="rank-index" :class="`top-${item.rank}`">{{ item.rank }}</span>
        <el-avatar :size="30" class="rank-avatar">{{ (item.studentName || '同').slice(0, 1) }}</el-avatar>
        <div class="rank-user">
          <strong>{{ item.studentName || '匿名学生' }}<em v-if="item.isMe">（你）</em></strong>
          <span>{{ item.submitCount || 0 }} 次练习 · 正确率 {{ item.accuracyRate || 0 }}%</span>
        </div>
        <b>{{ item.score || 0 }}分</b>
      </article>
    </div>

    <EmptyState v-else description="暂无排行数据" />

    <el-dialog v-model="dialogVisible" title="学习排行榜" width="640px">
      <el-table :data="list" size="small">
        <el-table-column prop="rank" label="排名" width="80" />
        <el-table-column prop="studentName" label="学生" />
        <el-table-column prop="submitCount" label="练习次数" width="100" />
        <el-table-column prop="accuracyRate" label="正确率" width="100">
          <template #default="{ row }">{{ row.accuracyRate || 0 }}%</template>
        </el-table-column>
        <el-table-column prop="score" label="积分" width="100" />
      </el-table>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, ref } from 'vue'
import EmptyState from '../EmptyState.vue'

const props = defineProps({
  ranking: { type: Object, default: () => ({ list: [] }) }
})

const dialogVisible = ref(false)
const list = computed(() => props.ranking?.list || [])
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
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 14px;
}

.side-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 17px;
  font-weight: 900;
}

.side-head p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}

.rank-list {
  display: grid;
  gap: 10px;
}

.rank-item {
  display: grid;
  grid-template-columns: 26px 30px minmax(0, 1fr) auto;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 14px;
}

.rank-item.me {
  background: #eff6ff;
}

.rank-index {
  display: inline-flex;
  width: 24px;
  height: 24px;
  align-items: center;
  justify-content: center;
  border-radius: 9px;
  color: #64748b;
  background: #f1f5f9;
  font-size: 12px;
  font-weight: 900;
}

.rank-index.top-1 {
  color: #fff;
  background: #f59e0b;
}

.rank-index.top-2 {
  color: #fff;
  background: #94a3b8;
}

.rank-index.top-3 {
  color: #fff;
  background: #f97316;
}

.rank-avatar {
  background: linear-gradient(135deg, #bfdbfe, #ddd6fe);
  color: #1e3a8a;
  font-weight: 900;
}

.rank-user {
  min-width: 0;
}

.rank-user strong,
.rank-user span {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-user strong {
  color: #0f172a;
  font-size: 13px;
}

.rank-user em {
  color: #2563eb;
  font-style: normal;
}

.rank-user span {
  margin-top: 3px;
  color: #64748b;
  font-size: 11px;
}

.rank-item b {
  color: #2563eb;
  font-size: 13px;
}
</style>
