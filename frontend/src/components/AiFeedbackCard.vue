<template>
  <section v-if="feedback" class="ai-card">
    <img class="ai-bg-image" src="/assets/ai-feedback-bg.png" alt="" aria-hidden="true" />

    <header class="ai-head">
      <div class="ai-orb">
        <MagicStick />
      </div>
      <div class="ai-title">
        <div class="ai-eyebrow">AgentEdu AI</div>
        <h3>AI 多智能体诊断结果</h3>
        <p>基于评测结果、失败用例和关键代码片段生成</p>
      </div>
      <div class="cache-pill" :class="{ hit: isFromCache }">
        {{ isFromCache ? '缓存命中' : 'AI 新生成' }}
      </div>
    </header>

    <div class="cache-note" :class="{ hit: isFromCache }">
      {{ isFromCache ? '已命中错误指纹缓存，复用历史相似错误反馈' : '本次由 AI 新生成反馈' }}
    </div>

    <div v-if="typeof feedback.score === 'number'" class="score-band">
      <span>AI 批改得分</span>
      <strong>{{ feedback.score }}</strong>
      <em>分</em>
    </div>

    <div class="agent-grid">
      <article class="agent-item diagnosis">
        <div class="agent-icon"><Search /></div>
        <h4>诊断智能体</h4>
        <b>{{ feedback.errorType || '综合诊断' }}</b>
        <p>{{ feedback.diagnosis || '暂无诊断内容' }}</p>
      </article>
      <article class="agent-item explanation">
        <div class="agent-icon"><Reading /></div>
        <h4>讲解智能体</h4>
        <p>{{ feedback.explanation || '暂无知识讲解' }}</p>
      </article>
      <article class="agent-item suggestion">
        <div class="agent-icon"><Tools /></div>
        <h4>建议智能体</h4>
        <p>{{ feedback.suggestion || '暂无修改建议' }}</p>
      </article>
      <article class="agent-item evaluation">
        <div class="agent-icon"><TrendCharts /></div>
        <h4>评价智能体</h4>
        <p>{{ feedback.evaluation || '暂无学习评价' }}</p>
      </article>
    </div>

    <div v-if="feedback.relatedKnowledge || feedback.nextPracticeAdvice" class="extra-grid">
      <article v-if="feedback.relatedKnowledge" class="extra-item knowledge">
        <div class="extra-head">
          <Collection />
          <span>相关知识点</span>
        </div>
        <p>{{ feedback.relatedKnowledge }}</p>
      </article>
      <article v-if="feedback.nextPracticeAdvice" class="extra-item practice">
        <div class="extra-head">
          <Flag />
          <span>下一步练习建议</span>
        </div>
        <p>{{ feedback.nextPracticeAdvice }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { Collection, Flag, MagicStick, Reading, Search, Tools, TrendCharts } from '@element-plus/icons-vue'
import { computed } from 'vue'

const props = defineProps({
  feedback: { type: Object, default: null }
})

const isFromCache = computed(() => props.feedback?.fromCache === true || props.feedback?.fromCache === 1)
</script>

<style scoped>
.ai-card {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(124, 58, 237, 0.20);
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 22px 58px rgba(124, 58, 237, 0.12);
}

.ai-bg-image {
  position: absolute;
  right: 24px;
  top: 24px;
  width: 220px;
  max-width: 30%;
  opacity: 0.08;
  pointer-events: none;
}

.ai-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 24px;
  color: #fff;
  background:
    radial-gradient(circle at 86% 0%, rgba(255, 255, 255, 0.24), transparent 28%),
    linear-gradient(135deg, #2563eb, #7c3aed);
}

.ai-orb {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 58px;
  height: 58px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 28px;
}

.ai-orb svg,
.agent-icon svg,
.extra-head svg {
  width: 1em;
  height: 1em;
}

.ai-title {
  min-width: 0;
}

.ai-eyebrow {
  color: #dbeafe;
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.ai-head h3 {
  margin: 4px 0 0;
  font-size: 24px;
  font-weight: 900;
}

.ai-head p {
  margin: 7px 0 0;
  color: #e0f2fe;
  font-size: 13px;
}

.cache-pill {
  flex: 0 0 auto;
  margin-left: auto;
  padding: 8px 12px;
  border-radius: 999px;
  color: #1e3a8a;
  background: #dbeafe;
  font-size: 12px;
  font-weight: 900;
}

.cache-pill.hit {
  color: #047857;
  background: #d1fae5;
}

.cache-note {
  position: relative;
  z-index: 1;
  margin: 20px 22px 0;
  padding: 12px 14px;
  border-radius: 14px;
  color: #1d4ed8;
  background: #eff6ff;
  font-weight: 800;
}

.cache-note.hit {
  color: #047857;
  background: #ecfdf5;
}

.score-band {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
  margin: 16px 22px 0;
  padding: 12px 16px;
  border-radius: 16px;
  color: #1d4ed8;
  background: linear-gradient(135deg, #dbeafe, #f5f3ff);
  font-weight: 900;
}

.score-band strong {
  font-size: 30px;
}

.score-band em {
  font-style: normal;
}

.agent-grid,
.extra-grid {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  padding: 22px;
}

.extra-grid {
  padding-top: 0;
}

.agent-item,
.extra-item {
  min-height: 210px;
  padding: 18px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 20px;
}

.extra-item {
  min-height: 120px;
}

.agent-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  margin-bottom: 12px;
  border-radius: 15px;
  font-size: 22px;
}

.agent-item h4 {
  margin: 0;
  color: #111827;
  font-size: 17px;
  font-weight: 900;
}

.agent-item b {
  display: block;
  margin-top: 10px;
  color: #2563eb;
}

.agent-item p,
.extra-item p {
  margin: 10px 0 0;
  color: #4b5563;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.extra-head {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #111827;
  font-size: 16px;
  font-weight: 900;
}

.extra-head :deep(.el-icon),
.extra-head svg {
  width: 20px;
  height: 20px;
  font-size: 20px;
}

.diagnosis {
  background: linear-gradient(180deg, #eff6ff, #fff);
}

.diagnosis .agent-icon {
  color: #2563eb;
  background: #dbeafe;
}

.explanation {
  background: linear-gradient(180deg, #ecfeff, #fff);
}

.explanation .agent-icon {
  color: #0891b2;
  background: #cffafe;
}

.suggestion {
  background: linear-gradient(180deg, #f5f3ff, #fff);
}

.suggestion .agent-icon {
  color: #7c3aed;
  background: #ede9fe;
}

.evaluation {
  background: linear-gradient(180deg, #f0fdf4, #fff);
}

.evaluation .agent-icon {
  color: #059669;
  background: #d1fae5;
}

.knowledge {
  background: linear-gradient(180deg, #fff7ed, #fff);
}

.practice {
  background: linear-gradient(180deg, #f8fafc, #fff);
}

@media (max-width: 900px) {
  .ai-head {
    align-items: flex-start;
    flex-direction: column;
  }

  .cache-pill {
    margin-left: 0;
  }

  .agent-grid,
  .extra-grid {
    grid-template-columns: 1fr;
  }
}
</style>
