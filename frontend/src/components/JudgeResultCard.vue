<template>
  <section v-if="result" class="judge-card" :class="{ success: isAccepted }">
    <header class="judge-head">
      <div>
        <div class="judge-eyebrow">Judge Result</div>
        <h3>评测结果</h3>
      </div>
      <StatusTag :status="result.judgeStatus" />
    </header>

    <div class="judge-metrics">
      <div class="metric">
        <CircleCheck />
        <div>
          <b>{{ result.passCount || 0 }} / {{ result.totalCount || 0 }}</b>
          <span>通过数量</span>
        </div>
      </div>
      <div class="metric">
        <Timer />
        <div>
          <b>{{ result.runTime || 0 }} ms</b>
          <span>耗时</span>
        </div>
      </div>
      <div class="metric">
        <MagicStick />
        <div>
          <b>{{ result.needAiFeedback ? '建议诊断' : '无需诊断' }}</b>
          <span>AI 助教</span>
        </div>
      </div>
      <div v-if="typeof result.score === 'number'" class="metric">
        <TrendCharts />
        <div>
          <b>{{ result.score }} pts</b>
          <span>AI 得分</span>
        </div>
      </div>
    </div>

    <el-alert
      v-if="result.errorMessage"
      class="judge-error"
      :title="result.errorMessage"
      type="error"
      :closable="false"
      show-icon
    />

    <div v-if="firstFailed" class="failed-case">
      <div class="failed-title">
        <CircleClose />
        首个未通过结果
      </div>
      <div class="case-grid">
        <div>
          <span>输入 / 题干</span>
          <pre>{{ firstFailed.inputData || '无' }}</pre>
        </div>
        <div>
          <span>期望 / 参考答案</span>
          <pre>{{ firstFailed.expectedOutput || '无' }}</pre>
        </div>
        <div>
          <span>实际答案 / 错误信息</span>
          <pre>{{ firstFailed.actualOutput || firstFailed.errorOutput || firstFailed.errorMessage || '无' }}</pre>
        </div>
      </div>
    </div>

    <el-table
      v-if="caseResults.length"
      class="case-table"
      :data="caseResults"
      size="small"
      border
      :row-class-name="caseRowClass"
    >
      <el-table-column type="index" label="#" width="58" />
      <el-table-column prop="judgeStatus" label="状态" width="170">
        <template #default="{ row }">
          <StatusTag :status="row.judgeStatus" />
        </template>
      </el-table-column>
      <el-table-column prop="runTime" label="耗时" width="100">
        <template #default="{ row }">{{ row.runTime || 0 }} ms</template>
      </el-table-column>
      <el-table-column prop="expectedOutput" label="期望/参考" min-width="180" show-overflow-tooltip />
      <el-table-column prop="actualOutput" label="实际/作答" min-width="180" show-overflow-tooltip />
      <el-table-column prop="errorOutput" label="错误信息" min-width="180" show-overflow-tooltip />
    </el-table>
  </section>
</template>

<script setup>
import { CircleCheck, CircleClose, MagicStick, Timer, TrendCharts } from '@element-plus/icons-vue'
import { computed } from 'vue'
import StatusTag from './StatusTag.vue'

const props = defineProps({
  result: { type: Object, default: null }
})

const caseResults = computed(() => props.result?.testCaseResults || props.result?.caseResults || [])
const isAccepted = computed(() => ['ACCEPTED', 'PARTIAL_ACCEPTED'].includes(props.result?.judgeStatus))
const firstFailed = computed(() => caseResults.value.find(item => item.judgeStatus && !['ACCEPTED', 'PARTIAL_ACCEPTED'].includes(item.judgeStatus)))

function caseRowClass({ row }) {
  return row.judgeStatus && !['ACCEPTED', 'PARTIAL_ACCEPTED'].includes(row.judgeStatus) ? 'failed-row' : ''
}
</script>

<style scoped>
.judge-card {
  padding: 22px;
  border: 1px solid rgba(239, 68, 68, 0.18);
  border-radius: 22px;
  background: #fff;
  box-shadow: 0 18px 44px rgba(239, 68, 68, 0.08);
}

.judge-card.success {
  border-color: rgba(16, 185, 129, 0.24);
  box-shadow: 0 18px 44px rgba(16, 185, 129, 0.08);
}

.judge-head,
.failed-title {
  display: flex;
  align-items: center;
}

.judge-head {
  justify-content: space-between;
  gap: 16px;
}

.judge-eyebrow {
  color: #2563eb;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.judge-head h3 {
  margin: 4px 0 0;
  color: #111827;
  font-size: 22px;
  font-weight: 900;
}

.judge-metrics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.metric {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 16px;
  background: #f8fafc;
}

.metric svg {
  width: 24px;
  height: 24px;
  color: #2563eb;
}

.metric b {
  display: block;
  color: #111827;
  font-size: 18px;
  font-weight: 900;
}

.metric span {
  color: #6b7280;
  font-size: 12px;
}

.judge-error {
  margin-top: 16px;
  border-radius: 14px;
}

.failed-case {
  margin-top: 18px;
  padding: 16px;
  border: 1px solid rgba(239, 68, 68, 0.18);
  border-radius: 18px;
  background: #fff7f7;
}

.failed-title {
  gap: 8px;
  color: #b91c1c;
  font-weight: 900;
}

.failed-title svg {
  width: 20px;
  height: 20px;
}

.case-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.case-grid span {
  display: block;
  margin-bottom: 6px;
  color: #6b7280;
  font-size: 12px;
  font-weight: 800;
}

.case-grid pre {
  min-height: 86px;
  margin: 0;
  padding: 12px;
  overflow: auto;
  border: 1px solid rgba(239, 68, 68, 0.10);
  border-radius: 12px;
  color: #334155;
  background: #fff;
  font-family: Consolas, "JetBrains Mono", monospace;
  font-size: 12px;
  line-height: 1.65;
  white-space: pre-wrap;
}

.case-table {
  margin-top: 18px;
}

.case-table :deep(.failed-row) {
  background: #fff7f7;
}

@media (max-width: 900px) {
  .case-grid {
    grid-template-columns: 1fr;
  }
}
</style>
