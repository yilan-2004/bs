<template>
  <div class="problem-tags">
    <span v-for="tag in tagList" :key="tag" class="problem-tag">{{ tag }}</span>
    <span v-if="!tagList.length" class="problem-tag muted-tag">未设置知识点</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: String, default: '' },
  tags: { type: [String, Array], default: '' }
})

const source = computed(() => props.tags || props.value || '')
const tagList = computed(() => {
  if (Array.isArray(source.value)) {
    return source.value.map(item => String(item).trim()).filter(Boolean)
  }
  return String(source.value).split(',').map(item => item.trim()).filter(Boolean)
})
</script>

<style scoped>
.problem-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.problem-tag {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  color: #1d4ed8;
  background: #eff6ff;
  font-size: 12px;
  font-weight: 800;
}

.muted-tag {
  color: #64748b;
  background: #f1f5f9;
}
</style>
