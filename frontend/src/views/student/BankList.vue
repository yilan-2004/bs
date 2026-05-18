<template>
  <div class="page page-stack">
    <PageHeader
      title="题库训练"
      subtitle="按知识模块进行系统化编程练习，让训练路径更清晰"
      eyebrow="Problem Banks"
      :icon="Collection"
    />

    <div class="toolbar bank-filter">
      <el-input v-model="query.keyword" clearable placeholder="搜索题库名称 / 简介" :prefix-icon="Search" @keyup.enter="loadBanks" />
      <el-select v-model="query.subjectId" clearable placeholder="学科">
        <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
      </el-select>
      <el-select v-model="query.difficulty" clearable placeholder="难度">
        <el-option label="基础" value="EASY" />
        <el-option label="进阶" value="MEDIUM" />
        <el-option label="挑战" value="HARD" />
        <el-option label="综合" value="MIXED" />
      </el-select>
      <el-input v-model="query.knowledgeTag" clearable placeholder="知识点，如 数组" @keyup.enter="loadBanks" />
      <el-button type="primary" :icon="Search" :loading="loading" @click="loadBanks">筛选</el-button>
    </div>

    <el-skeleton v-if="loading" :rows="8" animated />
    <div v-else-if="banks.length" class="card-grid">
      <BankCard v-for="bank in banks" :key="bank.id" :bank="bank" :progress="0" @enter="openBank" />
    </div>
    <EmptyState v-else title="暂无可练习题库" description="换一个关键词试试，或等待教师发布新的题库" :icon="Collection" />

    <div class="pager">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        background
        layout="total, prev, pager, next"
        :total="total"
        @current-change="loadBanks"
      />
    </div>
  </div>
</template>

<script setup>
import { Collection, Search } from '@element-plus/icons-vue'
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { problemBankApi } from '../../api/problemBank'
import { subjectApi } from '../../api/subject'
import BankCard from '../../components/BankCard.vue'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'

const router = useRouter()
const loading = ref(false)
const banks = ref([])
const subjects = ref([])
const total = ref(0)
const query = reactive({
  pageNum: 1,
  pageSize: 9,
  keyword: '',
  subjectId: '',
  difficulty: '',
  knowledgeTag: '',
  status: 1
})

async function loadBanks() {
  loading.value = true
  try {
    const data = await problemBankApi.list(query)
    banks.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function openBank(bank) {
  router.push(`/student/bank/${bank.id}`)
}

async function loadSubjects() {
  const data = await subjectApi.list({ pageNum: 1, pageSize: 100, status: 1 })
  subjects.value = data.records || []
}

watch(() => [query.subjectId, query.difficulty, query.knowledgeTag], () => {
  query.pageNum = 1
  loadBanks()
})

onMounted(() => {
  loadSubjects()
  loadBanks()
})
</script>

<style scoped>
.bank-filter {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 150px 160px 220px auto;
}

.pager {
  display: flex;
  justify-content: center;
}

@media (max-width: 900px) {
  .bank-filter {
    grid-template-columns: 1fr;
  }
}
</style>
