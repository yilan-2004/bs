<template>
  <div class="page page-stack" v-loading="loading">
    <PageHeader
      title="测试用例管理"
      subtitle="样例用例展示给学生，隐藏用例只参与代码评测"
      eyebrow="Test Cases"
      :icon="List"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增用例</el-button>
      </template>
    </PageHeader>

    <section v-if="problem" class="problem-info surface-card">
      <div>
        <span class="bank-name">{{ problem.bankName || '未归入题库' }}</span>
        <h2>{{ problem.title }}</h2>
        <ProblemTag :tags="problem.knowledgeTags" />
      </div>
      <span class="difficulty" :class="difficultyClass(problem.difficulty)">{{ difficultyText(problem.difficulty) }}</span>
    </section>

    <section class="surface-card">
      <el-table :data="cases" size="large">
        <el-table-column prop="sortOrder" label="排序" width="90" />
        <el-table-column prop="isSample" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.isSample === 1 ? 'success' : 'warning'" round>{{ row.isSample === 1 ? '样例' : '隐藏' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="输入数据" min-width="240">
          <template #default="{ row }"><pre class="case-code">{{ row.inputData || '无输入' }}</pre></template>
        </el-table-column>
        <el-table-column label="期望输出" min-width="240">
          <template #default="{ row }"><pre class="case-code">{{ row.expectedOutput || '无输出' }}</pre></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" round>{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button text type="danger" :icon="Delete" @click="deleteCase(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无测试用例" description="至少添加一个启用测试用例后，学生提交才能评测" :icon="List" />
        </template>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑测试用例' : '新增测试用例'" width="720px">
      <el-form label-position="top" :model="form">
        <div class="form-grid">
          <el-form-item label="用例类型">
            <el-radio-group v-model="form.isSample">
              <el-radio-button :label="1">样例用例</el-radio-button>
              <el-radio-button :label="0">隐藏用例</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" />
          </el-form-item>
        </div>
        <el-form-item label="输入数据">
          <el-input v-model="form.inputData" type="textarea" :rows="7" placeholder="支持多行输入；无输入可留空" />
        </el-form-item>
        <el-form-item label="期望输出" required>
          <el-input v-model="form.expectedOutput" type="textarea" :rows="7" placeholder="支持多行输出" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCase">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Delete, Edit, List, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { problemApi } from '../../api/problem'
import { testCaseApi } from '../../api/testcase'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import ProblemTag from '../../components/ProblemTag.vue'

const route = useRoute()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const problem = ref(null)
const cases = ref([])
const form = reactive({ id: null, problemId: Number(route.params.problemId), inputData: '', expectedOutput: '', isSample: 0, sortOrder: 0 })

function difficultyText(value) {
  return ({ EASY: '简单', MEDIUM: '中等', HARD: '困难' })[value] || value
}

function difficultyClass(value) {
  return ({ EASY: 'difficulty-easy', MEDIUM: 'difficulty-medium', HARD: 'difficulty-hard' })[value] || 'difficulty-mixed'
}

async function loadData() {
  loading.value = true
  try {
    const problemId = route.params.problemId
    const [detail, list] = await Promise.all([problemApi.detail(problemId), testCaseApi.list(problemId)])
    problem.value = detail
    cases.value = Array.isArray(list) ? list : (list.records || [])
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id || null,
    problemId: Number(route.params.problemId),
    inputData: row?.inputData || '',
    expectedOutput: row?.expectedOutput || '',
    isSample: row?.isSample ?? 0,
    sortOrder: row?.sortOrder || 0
  })
  dialogVisible.value = true
}

async function saveCase() {
  saving.value = true
  try {
    if (form.id) {
      await testCaseApi.update({ ...form })
    } else {
      const payload = { ...form }
      delete payload.id
      await testCaseApi.add(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

async function deleteCase(row) {
  await ElMessageBox.confirm('确认删除/禁用该测试用例吗？', '操作确认', { type: 'warning' })
  await testCaseApi.delete(row.id)
  ElMessage.success('已删除')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.problem-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.problem-info h2 {
  margin: 6px 0 12px;
  color: #111827;
  font-size: 24px;
  font-weight: 900;
}

.bank-name {
  color: #2563eb;
  font-weight: 900;
}

.difficulty {
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}

.case-code {
  max-height: 128px;
  margin: 0;
  padding: 11px 12px;
  overflow: auto;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 12px;
  color: #334155;
  background: #f8fafc;
  font-family: Consolas, "JetBrains Mono", monospace;
  font-size: 12px;
  line-height: 1.65;
  white-space: pre-wrap;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 18px;
}
</style>
