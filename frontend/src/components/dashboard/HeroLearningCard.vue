<template>
  <section class="hero-card">
    <div class="hero-copy">
      <span class="hero-kicker">个性化学习建议</span>
      <h2>继续你的学习计划</h2>
      <p>根据练习记录和评测结果，为你推荐下一步任务。</p>
      <div class="hero-actions">
        <el-button type="primary" size="large" @click="router.push('/student/banks')">去学习</el-button>
        <el-button size="large" @click="router.push('/student/report')">查看学习计划</el-button>
      </div>
    </div>

    <div class="hero-art" aria-hidden="true">
      <div class="book-stack">
        <span class="book book-blue"></span>
        <span class="book book-green"></span>
        <span class="book book-purple"></span>
      </div>
      <div class="study-board">
        <div class="board-top"></div>
        <div class="code-line long"></div>
        <div class="code-line"></div>
        <div class="code-line short"></div>
      </div>
      <div class="check-card">
        <span></span>
        <span></span>
        <span></span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.hero-card {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 380px;
  min-height: 206px;
  overflow: hidden;
  padding: 38px 42px;
  border: 1px solid #dde8fb;
  border-radius: 22px;
  background:
    radial-gradient(circle at 85% 12%, rgba(124, 58, 237, 0.14), transparent 28%),
    linear-gradient(135deg, #eff6ff 0%, #eef7ff 48%, #f7fbff 100%);
  box-shadow: 0 20px 48px rgba(37, 99, 235, 0.12);
}

.hero-copy {
  position: relative;
  z-index: 1;
  align-self: center;
}

.hero-kicker {
  display: inline-flex;
  margin-bottom: 12px;
  padding: 6px 12px;
  border-radius: 999px;
  color: #2563eb;
  background: rgba(37, 99, 235, 0.1);
  font-size: 13px;
  font-weight: 800;
}

.hero-copy h2 {
  margin: 0;
  color: #0f172a;
  font-size: 34px;
  font-weight: 900;
  letter-spacing: 0;
}

.hero-copy p {
  max-width: 520px;
  margin: 14px 0 26px;
  color: #475569;
  font-size: 16px;
  line-height: 1.7;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.hero-actions :deep(.el-button) {
  min-width: 118px;
  border-radius: 12px;
  font-weight: 800;
}

.hero-actions :deep(.el-button--primary) {
  border: 0;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  box-shadow: 0 14px 24px rgba(37, 99, 235, 0.24);
}

.hero-art {
  position: relative;
  min-height: 170px;
}

.study-board,
.check-card,
.book-stack {
  position: absolute;
}

.study-board {
  right: 110px;
  top: 18px;
  width: 190px;
  height: 124px;
  padding: 20px;
  border-radius: 20px;
  background: linear-gradient(145deg, #1d4ed8, #7c3aed);
  box-shadow: 0 22px 42px rgba(37, 99, 235, 0.28);
  transform: rotate(3deg);
}

.board-top {
  width: 42px;
  height: 8px;
  margin-bottom: 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.76);
}

.code-line {
  height: 8px;
  margin-top: 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.58);
}

.code-line.long {
  width: 132px;
}

.code-line:not(.long):not(.short) {
  width: 108px;
}

.code-line.short {
  width: 78px;
}

.check-card {
  right: 20px;
  top: 42px;
  width: 118px;
  height: 146px;
  padding: 22px 18px;
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 18px 36px rgba(37, 99, 235, 0.12);
}

.check-card span {
  display: block;
  height: 12px;
  margin-bottom: 18px;
  border-radius: 999px;
  background: linear-gradient(90deg, #bfdbfe, #93c5fd);
}

.check-card span:nth-child(2) {
  width: 72%;
}

.check-card span:nth-child(3) {
  width: 88%;
}

.book-stack {
  right: 210px;
  bottom: 5px;
  width: 150px;
  height: 58px;
}

.book {
  position: absolute;
  display: block;
  width: 120px;
  height: 16px;
  border-radius: 10px;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
}

.book-blue {
  right: 0;
  bottom: 0;
  background: #60a5fa;
}

.book-green {
  right: 16px;
  bottom: 18px;
  background: #34d399;
}

.book-purple {
  right: 32px;
  bottom: 36px;
  background: #a78bfa;
}

@media (max-width: 920px) {
  .hero-card {
    grid-template-columns: 1fr;
  }

  .hero-art {
    display: none;
  }
}
</style>
