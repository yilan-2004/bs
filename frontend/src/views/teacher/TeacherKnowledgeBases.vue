<template>
  <div class="page page-stack">
    <PageHeader
      title="知识库管理"
      subtitle="维护课程知识文档，为 AI 诊断提供可检索的教学依据。"
      eyebrow="Knowledge Base"
      :icon="Files"
    >
      <template #actions>
        <el-button type="primary" :icon="Plus" @click="openBaseDialog()">新增知识库</el-button>
      </template>
    </PageHeader>

    <section class="toolbar kb-toolbar">
      <el-input
        v-model="baseQuery.keyword"
        clearable
        placeholder="搜索知识库名称或说明"
        :prefix-icon="Search"
        @keyup.enter="loadBases"
      />
      <el-select v-model="baseQuery.subjectId" clearable placeholder="学科">
        <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
      </el-select>
      <el-select v-model="baseQuery.status" clearable placeholder="状态">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :loading="baseLoading" @click="loadBases">查询</el-button>
    </section>

    <section class="knowledge-layout">
      <div class="surface-card base-panel">
        <div class="section-head">
          <div>
            <h2>知识库</h2>
            <p>选择一个知识库后，可在右侧维护文档并生成知识切片。</p>
          </div>
          <el-tag round type="info">{{ bases.length }} 个</el-tag>
        </div>

        <el-table
          v-loading="baseLoading"
          :data="bases"
          row-key="id"
          highlight-current-row
          class="base-table"
          @current-change="selectBase"
        >
          <el-table-column label="知识库" min-width="260">
            <template #default="{ row }">
              <div class="base-name">
                <div class="base-icon"><el-icon><Files /></el-icon></div>
                <div>
                  <strong>{{ row.name }}</strong>
                  <p>{{ row.description || '暂无说明' }}</p>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="学科" width="110">
            <template #default="{ row }">{{ row.subjectName || '-' }}</template>
          </el-table-column>
          <el-table-column label="文档/切片" width="120">
            <template #default="{ row }">
              <span class="count-text">{{ row.documentCount || 0 }} / {{ row.chunkCount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" round>
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" :icon="Edit" @click.stop="openBaseDialog(row)">编辑</el-button>
              <el-button text type="danger" :icon="Delete" :disabled="row.status === 0" @click.stop="disableBase(row)">
                禁用
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyState title="暂无知识库" description="新增知识库后，即可录入课程知识文档。" :icon="Files" />
          </template>
        </el-table>
      </div>

      <div class="surface-card document-panel">
        <div class="section-head">
          <div>
            <h2>{{ currentBase?.name || '知识文档' }}</h2>
            <p>{{ currentBase ? '录入文本知识文档，切片后可用于 AI 诊断增强。' : '请先在左侧选择一个知识库。' }}</p>
          </div>
          <el-button type="primary" :icon="Plus" :disabled="!currentBase" @click="openDocDialog()">新增文档</el-button>
        </div>

        <div v-if="currentBase" class="doc-toolbar">
          <el-input
            v-model="docQuery.keyword"
            clearable
            placeholder="搜索文档标题或知识点"
            :prefix-icon="Search"
            @keyup.enter="loadDocuments"
          />
          <el-select v-model="docQuery.status" clearable placeholder="状态">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button :loading="docLoading" @click="loadDocuments">刷新</el-button>
        </div>

        <el-table v-if="currentBase" v-loading="docLoading" :data="documents" row-key="id" class="doc-table">
          <el-table-column label="文档" min-width="260">
            <template #default="{ row }">
              <div class="doc-title">
                <strong>{{ row.title }}</strong>
                <p>{{ row.knowledgeTags || '未设置知识点' }}</p>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="切片数" width="90">
            <template #default="{ row }">{{ row.chunkCount || 0 }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" round>
                {{ row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180" prop="createTime" />
          <el-table-column label="操作" width="310" fixed="right">
            <template #default="{ row }">
              <el-button text type="primary" :icon="Edit" @click="openDocDialog(row)">编辑</el-button>
              <el-button text type="primary" :icon="Scissor" :loading="chunkingId === row.id" @click="chunkDocument(row)">
                切片
              </el-button>
              <el-button text type="primary" :icon="View" @click="openChunks(row)">查看切片</el-button>
              <el-button text type="danger" :icon="Delete" :disabled="row.status === 0" @click="disableDocument(row)">
                禁用
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyState title="暂无知识文档" description="录入课程知识文本后，可一键生成检索切片。" :icon="Document" />
          </template>
        </el-table>

        <EmptyState
          v-else
          title="请选择知识库"
          description="左侧选择知识库后，这里会展示对应文档。"
          :icon="Files"
        />
      </div>
    </section>

    <el-dialog v-model="baseDialogVisible" :title="baseForm.id ? '编辑知识库' : '新增知识库'" width="620px">
      <el-form label-position="top" :model="baseForm">
        <el-form-item label="知识库名称" required>
          <el-input v-model="baseForm.name" placeholder="例如：Python 基础语法知识库" />
        </el-form-item>
        <el-form-item label="所属学科">
          <el-select v-model="baseForm.subjectId" clearable placeholder="选择学科" style="width: 100%">
            <el-option v-for="subject in subjects" :key="subject.id" :label="subject.name" :value="subject.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="知识库说明">
          <el-input v-model="baseForm.description" type="textarea" :rows="4" placeholder="说明该知识库覆盖的课程内容" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="baseForm.sortOrder" :min="0" style="width: 180px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="baseDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="baseSaving" @click="saveBase">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="docDialogVisible" :title="docForm.id ? '编辑知识文档' : '新增知识文档'" width="760px">
      <el-form label-position="top" :model="docForm">
        <el-form-item label="文档标题" required>
          <el-input v-model="docForm.title" placeholder="例如：循环结构与边界条件" />
        </el-form-item>
        <el-form-item label="知识点标签">
          <el-input v-model="docForm.knowledgeTags" placeholder="多个标签用逗号分隔，例如：循环,边界条件,输入输出" />
        </el-form-item>
        <el-form-item label="文档内容" required>
          <el-input
            v-model="docForm.content"
            type="textarea"
            :rows="12"
            placeholder="请录入课程知识文本。系统会按段落切片，用于 AI 反馈检索增强。"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="docDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="docSaving" @click="saveDocument">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="chunkDrawerVisible" title="知识切片" size="640px">
      <div v-loading="chunkLoading" class="chunk-list">
        <div v-for="chunk in chunks" :key="chunk.id" class="chunk-card">
          <div class="chunk-meta">
            <el-tag round>第 {{ chunk.chunkOrder + 1 }} 段</el-tag>
            <span>{{ chunk.knowledgeTags || '未设置标签' }}</span>
          </div>
          <p>{{ chunk.chunkText }}</p>
        </div>
        <EmptyState v-if="!chunks.length && !chunkLoading" title="暂无切片" description="点击文档的“切片”按钮后再查看。" :icon="Scissor" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { Delete, Document, Edit, Files, Plus, Scissor, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, reactive, ref, watch } from 'vue'
import { knowledgeApi } from '../../api/knowledge'
import { subjectApi } from '../../api/subject'
import EmptyState from '../../components/EmptyState.vue'
import PageHeader from '../../components/PageHeader.vue'

const subjects = ref([])
const bases = ref([])
const documents = ref([])
const chunks = ref([])
const currentBase = ref(null)

const baseLoading = ref(false)
const docLoading = ref(false)
const chunkLoading = ref(false)
const baseSaving = ref(false)
const docSaving = ref(false)
const chunkingId = ref(null)
const baseDialogVisible = ref(false)
const docDialogVisible = ref(false)
const chunkDrawerVisible = ref(false)

const baseQuery = reactive({ pageNum: 1, pageSize: 50, keyword: '', subjectId: '', status: '' })
const docQuery = reactive({ pageNum: 1, pageSize: 50, baseId: null, keyword: '', status: '' })
const baseForm = reactive({ id: null, name: '', description: '', subjectId: '', sortOrder: 0 })
const docForm = reactive({ id: null, baseId: null, title: '', content: '', knowledgeTags: '' })

async function loadSubjects() {
  const data = await subjectApi.list({ pageNum: 1, pageSize: 100, status: 1 })
  subjects.value = data.records || []
}

async function loadBases() {
  baseLoading.value = true
  try {
    const data = await knowledgeApi.listBases(baseQuery)
    bases.value = data.records || []
    if (currentBase.value) {
      const latest = bases.value.find((item) => item.id === currentBase.value.id)
      currentBase.value = latest || null
    }
  } finally {
    baseLoading.value = false
  }
}

async function loadDocuments() {
  if (!currentBase.value) return
  docLoading.value = true
  try {
    docQuery.baseId = currentBase.value.id
    const data = await knowledgeApi.listDocuments(docQuery)
    documents.value = data.records || []
  } finally {
    docLoading.value = false
  }
}

function selectBase(row) {
  if (!row) return
  currentBase.value = row
  documents.value = []
  docQuery.keyword = ''
  docQuery.status = ''
  loadDocuments()
}

function openBaseDialog(row) {
  Object.assign(baseForm, {
    id: row?.id || null,
    name: row?.name || '',
    description: row?.description || '',
    subjectId: row?.subjectId || '',
    sortOrder: row?.sortOrder || 0
  })
  baseDialogVisible.value = true
}

async function saveBase() {
  if (!baseForm.name?.trim()) {
    ElMessage.warning('请输入知识库名称')
    return
  }
  baseSaving.value = true
  try {
    const payload = { ...baseForm }
    if (payload.id) {
      await knowledgeApi.updateBase(payload)
    } else {
      delete payload.id
      await knowledgeApi.addBase(payload)
    }
    ElMessage.success('保存成功')
    baseDialogVisible.value = false
    await loadBases()
  } finally {
    baseSaving.value = false
  }
}

async function disableBase(row) {
  await ElMessageBox.confirm(`确认禁用知识库「${row.name}」吗？`, '操作确认', { type: 'warning' })
  await knowledgeApi.deleteBase(row.id)
  ElMessage.success('已禁用')
  if (currentBase.value?.id === row.id) {
    currentBase.value = null
    documents.value = []
  }
  loadBases()
}

function openDocDialog(row) {
  if (!currentBase.value) return
  Object.assign(docForm, {
    id: row?.id || null,
    baseId: currentBase.value.id,
    title: row?.title || '',
    content: row?.content || '',
    knowledgeTags: row?.knowledgeTags || ''
  })
  docDialogVisible.value = true
}

async function saveDocument() {
  if (!docForm.title?.trim() || !docForm.content?.trim()) {
    ElMessage.warning('请填写文档标题和内容')
    return
  }
  docSaving.value = true
  try {
    const payload = { ...docForm }
    if (payload.id) {
      await knowledgeApi.updateDocument(payload)
    } else {
      delete payload.id
      await knowledgeApi.addDocument(payload)
    }
    ElMessage.success('保存成功')
    docDialogVisible.value = false
    await loadDocuments()
    await loadBases()
  } finally {
    docSaving.value = false
  }
}

async function disableDocument(row) {
  await ElMessageBox.confirm(`确认禁用文档「${row.title}」吗？`, '操作确认', { type: 'warning' })
  await knowledgeApi.deleteDocument(row.id)
  ElMessage.success('已禁用')
  await loadDocuments()
  await loadBases()
}

async function chunkDocument(row) {
  chunkingId.value = row.id
  try {
    const count = await knowledgeApi.chunkDocument(row.id)
    ElMessage.success(`切片完成，共生成 ${count || 0} 个片段`)
    await loadDocuments()
    await loadBases()
  } finally {
    chunkingId.value = null
  }
}

async function openChunks(row) {
  chunkDrawerVisible.value = true
  chunkLoading.value = true
  chunks.value = []
  try {
    chunks.value = await knowledgeApi.listChunks(row.id)
  } finally {
    chunkLoading.value = false
  }
}

watch(() => [baseQuery.subjectId, baseQuery.status], loadBases)
watch(() => docQuery.status, loadDocuments)

onMounted(async () => {
  await loadSubjects()
  await loadBases()
})
</script>

<style scoped>
.kb-toolbar {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) 160px 140px auto;
}

.knowledge-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.02fr) minmax(0, 1.25fr);
  gap: 22px;
}

.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head h2 {
  margin: 0;
  color: #0f172a;
  font-size: 20px;
  font-weight: 950;
}

.section-head p {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.6;
}

.base-name {
  display: flex;
  align-items: center;
  gap: 12px;
}

.base-icon {
  display: inline-flex;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #2563eb;
  background: linear-gradient(135deg, #dbeafe, #ede9fe);
  font-size: 22px;
}

.base-name strong,
.doc-title strong {
  color: #0f172a;
  font-size: 15px;
  font-weight: 900;
}

.base-name p,
.doc-title p {
  max-width: 360px;
  margin: 4px 0 0;
  overflow: hidden;
  color: #64748b;
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.count-text {
  color: #2563eb;
  font-weight: 900;
}

.doc-toolbar {
  display: grid;
  grid-template-columns: minmax(240px, 1fr) 120px auto;
  gap: 12px;
  margin-bottom: 14px;
}

.chunk-list {
  display: grid;
  gap: 14px;
}

.chunk-card {
  padding: 16px;
  border: 1px solid #e5eaf2;
  border-radius: 16px;
  background: #f8fafc;
}

.chunk-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  color: #64748b;
  font-size: 12px;
}

.chunk-card p {
  margin: 0;
  color: #334155;
  line-height: 1.75;
  white-space: pre-wrap;
}

@media (max-width: 1180px) {
  .knowledge-layout,
  .kb-toolbar,
  .doc-toolbar {
    grid-template-columns: 1fr;
  }
}
</style>
