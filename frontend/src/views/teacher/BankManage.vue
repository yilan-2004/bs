<template>
  <div class="page page-stack">
    <PageHeader
      title="题库管理"
      subtitle="按知识模块组织训练内容，沉淀可复用的教学题库"
      eyebrow="Bank Management"
      :icon="Collection"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增题库</el-button>
      </template>
    </PageHeader>

    <div class="toolbar bank-toolbar">
      <el-input v-model="query.keyword" clearable placeholder="搜索题库名称 / 描述" :prefix-icon="Search" @keyup.enter="loadBanks" />
      <el-select v-model="query.subjectId" clearable placeholder="学科">
        <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
      </el-select>
      <el-select v-model="query.difficulty" clearable placeholder="难度">
        <el-option label="基础" value="EASY" />
        <el-option label="进阶" value="MEDIUM" />
        <el-option label="挑战" value="HARD" />
        <el-option label="综合" value="MIXED" />
      </el-select>
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="loadBanks">查询</el-button>
    </div>

    <section class="surface-card">
      <el-table v-loading="loading" :data="banks" size="large">
        <el-table-column label="题库名称" min-width="220">
          <template #default="{ row }">
            <div class="bank-name-cell">
              <div class="bank-icon-cell"><el-icon><Collection /></el-icon></div>
              <div>
                <strong>{{ row.name }}</strong>
                <p>{{ row.description || '暂无题库简介' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="subjectName" label="学科" width="120">
          <template #default="{ row }">{{ row.subjectName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="difficulty" label="难度" width="110">
          <template #default="{ row }">
            <span class="difficulty" :class="difficultyClass(row.difficulty)">{{ difficultyText(row.difficulty) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="knowledgeTags" label="知识点" min-width="220">
          <template #default="{ row }"><ProblemTag :tags="row.knowledgeTags" /></template>
        </el-table-column>
        <el-table-column prop="problemCount" label="题目数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" round>{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button text type="primary" :icon="View" @click="router.push(`/student/bank/${row.id}`)">查看题目</el-button>
            <el-button text type="danger" :icon="Delete" :disabled="row.status === 0" @click="disableBank(row)">禁用</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无题库" description="点击新增题库，创建第一组编程训练内容" :icon="Collection" />
        </template>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑题库' : '新增题库'" width="640px">
      <el-form class="bank-form" label-position="top" :model="form">
        <el-form-item label="题库名称" required>
          <el-input v-model="form.name" placeholder="例如：Python 基础训练" />
        </el-form-item>
        <el-form-item label="题库简介">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="简要说明题库训练目标和适合学生" />
        </el-form-item>
        <el-form-item label="封面地址">
          <el-input v-model="form.coverUrl" placeholder="可选，图片 URL" />
        </el-form-item>
        <el-form-item label="所属学科">
          <el-select v-model="form.subjectId" clearable placeholder="选择学科" style="width: 100%">
            <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
          </el-select>
        </el-form-item>
        <div class="dialog-grid">
          <el-form-item label="难度">
            <el-select v-model="form.difficulty" style="width: 100%">
              <el-option label="基础" value="EASY" />
              <el-option label="进阶" value="MEDIUM" />
              <el-option label="挑战" value="HARD" />
              <el-option label="综合" value="MIXED" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
          </el-form-item>
        </div>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgeTags" placeholder="多个知识点用逗号分隔，如 数组,循环" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveBank">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Collection, Delete, Edit, Plus, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { problemBankApi } from '../../api/problemBank'
import { subjectApi } from '../../api/subject'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'
import ProblemTag from '../../components/ProblemTag.vue'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const banks = ref([])
const subjects = ref([])
const query = reactive({ pageNum: 1, pageSize: 50, keyword: '', subjectId: '', difficulty: '', status: '' })
const form = reactive({
  id: null,
  name: '',
  subjectId: '',
  description: '',
  coverUrl: '',
  difficulty: 'MIXED',
  knowledgeTags: '',
  sortOrder: 0
})

function difficultyText(value) {
  return ({ EASY: '基础', MEDIUM: '进阶', HARD: '挑战', MIXED: '综合' })[value] || value
}

function difficultyClass(value) {
  return ({
    EASY: 'difficulty-easy',
    MEDIUM: 'difficulty-medium',
    HARD: 'difficulty-hard',
    MIXED: 'difficulty-mixed'
  })[value] || 'difficulty-mixed'
}

async function loadBanks() {
  loading.value = true
  try {
    const data = await problemBankApi.list(query)
    banks.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    subjectId: row?.subjectId || '',
    description: row?.description || '',
    coverUrl: row?.coverUrl || '',
    difficulty: row?.difficulty || 'MIXED',
    knowledgeTags: row?.knowledgeTags || '',
    sortOrder: row?.sortOrder || 0
  })
  dialogVisible.value = true
}

async function saveBank() {
  if (!form.name?.trim()) {
    ElMessage.warning('请输入题库名称')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    if (form.id) {
      await problemBankApi.update(payload)
    } else {
      delete payload.id
      await problemBankApi.add(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadBanks()
  } finally {
    saving.value = false
  }
}

async function loadSubjects() {
  const data = await subjectApi.list({ pageNum: 1, pageSize: 100, status: 1 })
  subjects.value = data.records || []
}

async function disableBank(row) {
  await ElMessageBox.confirm(`确认禁用题库「${row.name}」吗？`, '操作确认', { type: 'warning' })
  await problemBankApi.delete(row.id)
  ElMessage.success('已禁用')
  loadBanks()
}

watch(() => [query.subjectId, query.difficulty, query.status], loadBanks)
onMounted(() => {
  loadSubjects()
  loadBanks()
})
</script>

<style scoped>
.bank-toolbar {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 150px 150px 140px auto;
}

.bank-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.bank-name-cell strong {
  color: #0f172a;
  font-size: 15px;
}

.bank-name-cell p {
  max-width: 380px;
  margin: 4px 0 0;
  overflow: hidden;
  color: #64748b;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.bank-icon-cell {
  display: inline-flex;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #2563eb;
  background: linear-gradient(135deg, #dbeafe, #ede9fe);
  font-size: 24px;
}

.difficulty {
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
</style>
