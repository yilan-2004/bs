<template>
  <div class="page page-stack">
    <PageHeader
      title="测试用例"
      subtitle="先选择题目，再进入该题目的测试用例管理页面"
      eyebrow="Test Case Entry"
      :icon="List"
    />

    <div class="toolbar">
      <el-input v-model="query.title" clearable placeholder="搜索题目标题" :prefix-icon="Search" style="width: 260px" @keyup.enter="loadProblems" />
      <el-button type="primary" :loading="loading" @click="loadProblems">查询</el-button>
    </div>

    <section class="surface-card">
      <el-table v-loading="loading" :data="problems" size="large">
        <el-table-column prop="title" label="题目标题" min-width="220" />
        <el-table-column prop="difficulty" label="难度" width="120">
          <template #default="{ row }">
            <span class="difficulty" :class="difficultyClass(row.difficulty)">{{ difficultyText(row.difficulty) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="knowledgeTags" label="知识点" min-width="220">
          <template #default="{ row }"><ProblemTag :tags="row.knowledgeTags" /></template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" text :icon="List" @click="router.push(`/teacher/testcases/${row.id}`)">管理用例</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无题目" description="创建题目后即可进入测试用例管理" :icon="List" />
        </template>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { List, Search } from '@element-plus/icons-vue'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { problemApi } from '../../api/problem'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import ProblemTag from '../../components/ProblemTag.vue'

const router = useRouter()
const loading = ref(false)
const problems = ref([])
const query = reactive({ pageNum: 1, pageSize: 50, title: '' })

function difficultyText(value) {
  return ({ EASY: '简单', MEDIUM: '中等', HARD: '困难' })[value] || value
}

function difficultyClass(value) {
  return ({ EASY: 'difficulty-easy', MEDIUM: 'difficulty-medium', HARD: 'difficulty-hard' })[value] || 'difficulty-mixed'
}

async function loadProblems() {
  loading.value = true
  try {
    const data = await problemApi.list(query)
    problems.value = data.records || []
  } finally {
    loading.value = false
  }
}

onMounted(loadProblems)
</script>

<style scoped>
.difficulty {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}
</style>
