<template>
  <div class="page page-stack">
    <PageHeader
      title="我的提交"
      subtitle="回看评测结果、定位错误类型，并快速回到题目继续练习"
      eyebrow="Submission History"
      :icon="Tickets"
    />

    <section class="stat-grid">
      <StatCard :icon="Tickets" title="提交次数" :value="stats.total" type="blue" />
      <StatCard :icon="CircleCheck" title="通过次数" :value="stats.accepted" type="green" />
      <StatCard :icon="CircleClose" title="错误次数" :value="stats.failed" type="red" />
      <StatCard :icon="MagicStick" title="AI 诊断建议" :value="stats.ai" type="purple" />
    </section>

    <section class="surface-card">
      <el-table v-loading="loading" :data="records" size="large">
        <el-table-column prop="problemTitle" label="题目" min-width="180" />
        <el-table-column prop="judgeStatus" label="评测状态" width="170">
          <template #default="{ row }"><StatusTag :status="row.judgeStatus" /></template>
        </el-table-column>
        <el-table-column label="通过用例" width="120">
          <template #default="{ row }">{{ row.passCount || 0 }} / {{ row.totalCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="运行时间" width="110">
          <template #default="{ row }">{{ row.runTime || 0 }} ms</template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="190" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" @click="router.push(`/student/problem/${row.problemId}`)">重新练习</el-button>
            <el-button v-if="row.judgeStatus !== 'ACCEPTED'" text class="ai-link" @click="router.push(`/student/problem/${row.problemId}`)">AI反馈</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无提交记录" description="提交代码后，这里会沉淀你的练习轨迹" :icon="Tickets" />
        </template>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          background
          layout="total, prev, pager, next"
          :total="total"
          @current-change="loadRecords"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { CircleCheck, CircleClose, MagicStick, Tickets } from '@element-plus/icons-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { submitApi } from '../../api/submit'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import StatCard from '../../components/StatCard.vue'
import StatusTag from '../../components/StatusTag.vue'

const router = useRouter()
const loading = ref(false)
const records = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const stats = computed(() => {
  const accepted = records.value.filter(item => item.judgeStatus === 'ACCEPTED').length
  return {
    total: total.value || records.value.length,
    accepted,
    failed: records.value.filter(item => item.judgeStatus && item.judgeStatus !== 'ACCEPTED').length,
    ai: records.value.filter(item => item.needAiFeedback || item.judgeStatus !== 'ACCEPTED').length
  }
})

async function loadRecords() {
  loading.value = true
  try {
    const data = await submitApi.my(query)
    records.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadRecords)
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.ai-link {
  color: #7c3aed;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 18px;
}

@media (max-width: 1000px) {
  .stat-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
