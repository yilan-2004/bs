<template>
  <section class="editor-panel">
    <header class="editor-topbar">
      <div class="editor-title-block">
        <div class="editor-title">{{ title }}</div>
        <div class="editor-meta">
          <span>{{ languageLabel }}</span>
          <span>运行限制 3s</span>
          <span>代码限制 50KB</span>
        </div>
      </div>
      <div class="editor-actions">
        <el-button v-if="showReset" :icon="Refresh" @click="$emit('reset')">重置代码</el-button>
        <el-button
          v-if="showSubmit"
          type="primary"
          :icon="UploadFilled"
          :loading="loading"
          :disabled="loading"
          @click="$emit('submit')"
        >
          {{ loading ? loadingText : submitText }}
        </el-button>
      </div>
    </header>
    <div ref="containerRef" class="code-editor"></div>
  </section>
</template>

<script setup>
import loader from '@monaco-editor/loader'
import { Refresh, UploadFilled } from '@element-plus/icons-vue'
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: String, default: '' },
  language: { type: String, default: 'python' },
  height: { type: String, default: '560px' },
  title: { type: String, default: 'Python 代码编辑器' },
  showSubmit: { type: Boolean, default: false },
  showReset: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  submitText: { type: String, default: '提交代码' },
  loadingText: { type: String, default: '评测中...' }
})

const emit = defineEmits(['update:modelValue', 'submit', 'reset'])
const containerRef = ref(null)
let editor

const languageLabel = computed(() => props.language === 'python' ? 'Python' : props.language)

onMounted(async () => {
  const monaco = await loader.init()
  editor = monaco.editor.create(containerRef.value, {
    value: props.modelValue,
    language: props.language,
    theme: 'vs-dark',
    fontSize: 14,
    lineHeight: 23,
    minimap: { enabled: false },
    automaticLayout: true,
    scrollBeyondLastLine: false,
    tabSize: 4,
    padding: { top: 16, bottom: 16 },
    roundedSelection: true,
    wordWrap: 'on'
  })
  editor.onDidChangeModelContent(() => emit('update:modelValue', editor.getValue()))
})

watch(() => props.modelValue, (value) => {
  if (editor && value !== editor.getValue()) {
    editor.setValue(value)
  }
})

onBeforeUnmount(() => {
  editor?.dispose()
})
</script>

<style scoped>
.editor-panel {
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.16);
  border-radius: 22px;
  background: #0f172a;
  box-shadow: 0 24px 56px rgba(15, 23, 42, 0.20);
}

.editor-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 78px;
  gap: 16px;
  padding: 14px 16px 14px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.09);
  color: #fff;
  background:
    radial-gradient(circle at 94% 0%, rgba(124, 58, 237, 0.32), transparent 30%),
    linear-gradient(90deg, #0f172a, #172554);
}

.editor-title-block {
  min-width: 0;
}

.editor-title {
  font-size: 16px;
  font-weight: 900;
}

.editor-meta,
.editor-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 9px;
}

.editor-meta {
  margin-top: 8px;
}

.editor-meta span {
  padding: 4px 9px;
  border-radius: 999px;
  color: #bfdbfe;
  background: rgba(37, 99, 235, 0.24);
  font-size: 12px;
  font-weight: 800;
}

.editor-actions :deep(.el-button:not(.el-button--primary)) {
  border-color: rgba(255, 255, 255, 0.18);
  color: #e0f2fe;
  background: rgba(255, 255, 255, 0.08);
}

.code-editor {
  width: 100%;
  height: v-bind(height);
}

@media (max-width: 900px) {
  .editor-topbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
