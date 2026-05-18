<template>
  <div class="page page-stack">
    <PageHeader
      title="题目管理"
      subtitle="维护编程题内容、难度、知识点和测试用例入口"
      eyebrow="Problem Management"
      :icon="Notebook"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="router.push('/teacher/problem/new')">新增题目</el-button>
      </template>
    </PageHeader>

    <div class="toolbar">
      <el-input v-model="query.title" clearable placeholder="搜索题目标题" :prefix-icon="Search" style="width: 240px" @keyup.enter="loadProblems" />
      <el-select v-model="query.subjectId" clearable placeholder="学科" style="width: 140px">
        <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
      </el-select>
      <el-select v-model="query.questionType" clearable placeholder="题型" style="width: 150px">
        <el-option label="编程题" value="PROGRAMMING" />
        <el-option label="选择题" value="CHOICE" />
        <el-option label="填空题" value="FILL_BLANK" />
        <el-option label="简答题" value="SHORT_ANSWER" />
      </el-select>
      <el-select v-model="query.difficulty" clearable placeholder="难度" style="width: 140px">
        <el-option label="简单" value="EASY" />
        <el-option label="中等" value="MEDIUM" />
        <el-option label="困难" value="HARD" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态" style="width: 130px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="loadProblems">查询</el-button>
    </div>

    <section class="surface-card">
      <el-table v-loading="loading" :data="problems" size="large">
        <el-table-column prop="title" label="题目标题" min-width="220" />
        <el-table-column prop="subjectName" label="学科" width="110">
          <template #default="{ row }">{{ row.subjectName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="questionType" label="题型" width="120">
          <template #default="{ row }">{{ questionTypeText(row.questionType) }}</template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="110">
          <template #default="{ row }">
            <span class="difficulty" :class="difficultyClass(row.difficulty)">{{ difficultyText(row.difficulty) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="knowledgeTags" label="知识点" min-width="220">
          <template #default="{ row }"><ProblemTag :tags="row.knowledgeTags" /></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" round>{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="测试用例数" width="120">
          <template #default="{ row }">{{ row.testCaseCount ?? row.caseCount ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="Edit" @click="router.push(`/teacher/problem/${row.id}/edit`)">编辑</el-button>
            <el-button text type="primary" :icon="List" @click="router.push(`/teacher/testcases/${row.id}`)">测试用例</el-button>
            <el-dropdown>
              <el-button text>更多</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="router.push(`/teacher/submits?problemId=${row.id}`)">提交记录</el-dropdown-item>
                  <el-dropdown-item divided @click="disableProblem(row)">禁用题目</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无题目" description="创建题目后即可添加测试用例并开放给学生练习" :icon="Notebook" />
        </template>
      </el-table>
    </section>
  </div>
</template>

<script setup>
import { Edit, List, Notebook, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { problemApi } from '../../api/problem'
import { subjectApi } from '../../api/subject'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import ProblemTag from '../../components/ProblemTag.vue'

const router = useRouter()
const loading = ref(false)
const problems = ref([])
const subjects = ref([])
const query = reactive({ pageNum: 1, pageSize: 50, title: '', subjectId: '', questionType: '', difficulty: '', status: '' })

function difficultyText(value) {
  return ({ EASY: '简单', MEDIUM: '中等', HARD: '困难' })[value] || value
}

function difficultyClass(value) {
  return ({ EASY: 'difficulty-easy', MEDIUM: 'difficulty-medium', HARD: 'difficulty-hard' })[value] || 'difficulty-mixed'
}

function questionTypeText(value) {
  return ({ PROGRAMMING: '编程题', CHOICE: '选择题', FILL_BLANK: '填空题', SHORT_ANSWER: '简答题' })[value] || value || '-'
}

async function loadSubjects() {
  const data = await subjectApi.list({ pageNum: 1, pageSize: 100, status: 1 })
  subjects.value = data.records || []
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

async function disableProblem(row) {
  await ElMessageBox.confirm(`确认禁用题目「${row.title}」吗？`, '操作确认', { type: 'warning' })
  await problemApi.delete(row.id)
  ElMessage.success('已禁用')
  loadProblems()
}

watch(() => [query.subjectId, query.questionType, query.difficulty, query.status], loadProblems)
onMounted(() => {
  loadSubjects()
  loadProblems()
})
</script>

<style scoped>
.difficulty {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}
</style>
