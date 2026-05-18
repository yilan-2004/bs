<template>
  <section class="recent-card">
    <div class="card-head">
      <div>
        <h3>最近练习记录</h3>
        <p>展示最近 4 条作答状态，完整记录可进入提交页查看。</p>
      </div>
      <el-button text type="primary" @click="router.push('/student/submissions')">查看更多记录</el-button>
    </div>

    <el-table v-if="displayRecords.length" :data="displayRecords" class="recent-table" size="small">
      <el-table-column label="题目" min-width="180">
        <template #default="{ row }">
          <button class="link-button" @click="goProblem(row.problemId)">{{ row.problemTitle || '未命名题目' }}</button>
          <p class="bank-name">{{ row.bankName || '未归属题库' }}</p>
        </template>
      </el-table-column>
      <el-table-column prop="subjectName" label="学科" width="82" />
      <el-table-column label="状态" width="116">
        <template #default="{ row }">
          <StatusTag :status="row.judgeStatus" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="138">
        <template #default="{ row }">
          <div class="actions">
            <el-button link type="primary" @click="goProblem(row.problemId)">重练</el-button>
            <el-button
              v-if="row.judgeStatus !== 'ACCEPTED'"
              link
              type="primary"
              @click="router.push('/student/submissions')"
            >
              解析
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <EmptyState v-else description="暂无练习记录，先去题库开始第一道题吧" />
  </section>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import EmptyState from '../EmptyState.vue'
import StatusTag from '../StatusTag.vue'

const props = defineProps({
  records: { type: Array, default: () => [] }
})

const router = useRouter()
const displayRecords = computed(() => props.records.slice(0, 4))

function goProblem(problemId) {
  if (problemId) {
    router.push(`/student/problem/${problemId}`)
  }
}
</script>

<style scoped>
.recent-card {
  min-width: 0;
  padding: 22px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.card-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

.card-head p,
.bank-name {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 12px;
}

.recent-table {
  --el-table-border-color: #eef2f7;
  --el-table-header-bg-color: #f8fafc;
  border-radius: 14px;
  overflow: hidden;
}

.recent-table :deep(.el-table__cell) {
  vertical-align: middle;
}

.link-button {
  max-width: 100%;
  padding: 0;
  border: 0;
  color: #0f172a;
  background: transparent;
  font-weight: 800;
  text-align: left;
  cursor: pointer;
}

.link-button:hover {
  color: #2563eb;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 8px;
}
</style>
