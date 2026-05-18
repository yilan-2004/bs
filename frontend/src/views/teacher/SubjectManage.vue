<template>
  <div class="page page-stack">
    <PageHeader
      title="学科管理"
      subtitle="维护平台可选择的学科，为题库和题目提供统一分类"
      eyebrow="Subject Management"
      :icon="Collection"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增学科</el-button>
      </template>
    </PageHeader>

    <div class="toolbar subject-toolbar">
      <el-input v-model="query.keyword" clearable placeholder="搜索学科名称" :prefix-icon="Search" @keyup.enter="loadSubjects" />
      <el-select v-model="query.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="loadSubjects">查询</el-button>
    </div>

    <section class="surface-card">
      <el-table v-loading="loading" :data="subjects" size="large">
        <el-table-column label="学科" min-width="220">
          <template #default="{ row }">
            <div class="subject-name">
              <div class="subject-icon">{{ row.icon || 'S' }}</div>
              <div>
                <strong>{{ row.name }}</strong>
                <p>{{ row.description || '暂无描述' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" round>{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="Edit" @click="openDialog(row)">编辑</el-button>
            <el-button text type="danger" :icon="Delete" :disabled="row.status === 0" @click="disableSubject(row)">禁用</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState title="暂无学科" description="新增学科后即可在题库和题目中选择" :icon="Collection" />
        </template>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学科' : '新增学科'" width="560px">
      <el-form label-position="top" :model="form">
        <el-form-item label="学科名称" required>
          <el-input v-model="form.name" placeholder="例如：编程、数学、英语" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="form.icon" placeholder="可填写简短字符，如 💻 或 PY" />
        </el-form-item>
        <el-form-item label="学科描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="简要说明该学科覆盖的学习内容" />
        </el-form-item>
        <div class="dialog-grid">
          <el-form-item label="状态">
            <el-select v-model="form.status" style="width: 100%">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveSubject">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { Collection, Delete, Edit, Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref, watch } from 'vue'
import { subjectApi } from '../../api/subject'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const subjects = ref([])
const query = reactive({ pageNum: 1, pageSize: 50, keyword: '', status: '' })
const form = reactive({
  id: null,
  name: '',
  description: '',
  icon: '',
  status: 1,
  sortOrder: 0
})

async function loadSubjects() {
  loading.value = true
  try {
    const data = await subjectApi.list(query)
    subjects.value = data.records || []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    description: row?.description || '',
    icon: row?.icon || '',
    status: row?.status ?? 1,
    sortOrder: row?.sortOrder || 0
  })
  dialogVisible.value = true
}

async function saveSubject() {
  if (!form.name?.trim()) {
    ElMessage.warning('请输入学科名称')
    return
  }
  saving.value = true
  try {
    const payload = { ...form }
    if (form.id) {
      await subjectApi.update(payload)
    } else {
      delete payload.id
      await subjectApi.add(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadSubjects()
  } finally {
    saving.value = false
  }
}

async function disableSubject(row) {
  await ElMessageBox.confirm(`确认禁用学科“${row.name}”吗？`, '操作确认', { type: 'warning' })
  await subjectApi.delete(row.id)
  ElMessage.success('已禁用')
  loadSubjects()
}

watch(() => query.status, loadSubjects)
onMounted(loadSubjects)
</script>

<style scoped>
.subject-toolbar {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 140px auto;
}

.subject-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.subject-name strong {
  color: #0f172a;
  font-size: 15px;
}

.subject-name p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 13px;
}

.subject-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #2563eb;
  background: linear-gradient(135deg, #dbeafe, #ede9fe);
  font-size: 18px;
  font-weight: 900;
}

.dialog-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
</style>
