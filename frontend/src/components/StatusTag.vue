<template>
  <span class="status-tag" :class="statusClass">
    <span class="status-dot"></span>
    {{ statusText }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, default: '' }
})

const statusMap = {
  ACCEPTED: { text: 'Accepted', className: 'accepted' },
  PARTIAL_ACCEPTED: { text: 'Partial Accepted', className: 'partial' },
  WRONG_ANSWER: { text: 'Wrong Answer', className: 'wrong' },
  RUNTIME_ERROR: { text: 'Runtime Error', className: 'runtime' },
  COMPILE_ERROR: { text: 'Compile Error', className: 'compile' },
  TIME_LIMIT_EXCEEDED: { text: 'Time Limit', className: 'tle' },
  JUDGING: { text: 'Judging', className: 'judging' },
  AI_EVALUATE_FAILED: { text: 'AI Evaluate Failed', className: 'runtime' },
  SYSTEM_ERROR: { text: 'System Error', className: 'runtime' }
}

const normalized = computed(() => String(props.status || '').toUpperCase())
const current = computed(() => statusMap[normalized.value] || { text: props.status || '未知状态', className: 'unknown' })
const statusText = computed(() => current.value.text)
const statusClass = computed(() => `status-${current.value.className}`)
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  min-height: 28px;
  padding: 6px 11px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 900;
  line-height: 1;
  white-space: nowrap;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 999px;
  background: currentColor;
  box-shadow: 0 0 0 4px rgba(255, 255, 255, 0.58);
}

.status-accepted {
  color: #047857;
  background: #d1fae5;
}

.status-wrong {
  color: #b91c1c;
  background: #fee2e2;
}

.status-partial {
  color: #0369a1;
  background: #e0f2fe;
}

.status-runtime {
  color: #c2410c;
  background: #ffedd5;
}

.status-compile {
  color: #6d28d9;
  background: #ede9fe;
}

.status-tle {
  color: #a16207;
  background: #fef3c7;
}

.status-judging {
  color: #1d4ed8;
  background: #dbeafe;
}

.status-unknown {
  color: #475569;
  background: #e2e8f0;
}
</style>
