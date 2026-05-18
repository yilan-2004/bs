<template>
  <section class="character-panel" @mousemove="handleMouseMove" @mouseleave="resetEyes">
    <div class="brand-row">
      <div class="brand-logo">AI</div>
      <div>
        <strong>AgentEdu</strong>
        <span>多学科个性化学习平台</span>
      </div>
    </div>

    <div class="character-stage" :class="[`mode-${mode}`, `mood-${mood}`]">
      <div class="halo halo-one"></div>
      <div class="halo halo-two"></div>
      <div class="character-shadow"></div>
      <div class="character">
        <div class="antenna left"></div>
        <div class="antenna right"></div>
        <div class="head">
          <div class="face">
            <span class="eye eye-left"></span>
            <span class="eye eye-right"></span>
            <span class="mouth"></span>
          </div>
        </div>
        <div class="body">
          <div class="badge">AI</div>
        </div>
        <div class="arm arm-left"></div>
        <div class="arm arm-right"></div>
      </div>

      <div class="bubble bubble-one">错因诊断</div>
      <div class="bubble bubble-two">个性练习</div>
      <div class="bubble bubble-three">学习报告</div>
    </div>

    <div class="panel-copy">
      <p>{{ eyebrow }}</p>
      <h2>{{ title }}</h2>
      <span>{{ description }}</span>
    </div>
  </section>
</template>

<script setup>
defineProps({
  mode: { type: String, default: 'login' },
  mood: { type: String, default: 'idle' },
  eyebrow: { type: String, default: 'AI Learning Assistant' },
  title: { type: String, default: '让每一次练习都有反馈' },
  description: { type: String, default: '连接题库、评测、AI 诊断和学习报告，形成完整学习闭环。' }
})

function handleMouseMove(event) {
  const rect = event.currentTarget.getBoundingClientRect()
  const x = ((event.clientX - rect.left) / rect.width - 0.5) * 12
  const y = ((event.clientY - rect.top) / rect.height - 0.5) * 10
  event.currentTarget.style.setProperty('--eye-x', `${x}px`)
  event.currentTarget.style.setProperty('--eye-y', `${y}px`)
}

function resetEyes(event) {
  event.currentTarget.style.setProperty('--eye-x', '0px')
  event.currentTarget.style.setProperty('--eye-y', '0px')
}
</script>

<style scoped>
.character-panel {
  --eye-x: 0px;
  --eye-y: 0px;
  position: relative;
  display: grid;
  align-content: space-between;
  min-height: 620px;
  overflow: hidden;
  padding: 30px;
  border-radius: 34px;
  color: #fff;
  background:
    radial-gradient(circle at 28% 18%, rgba(255, 255, 255, 0.28), transparent 20%),
    radial-gradient(circle at 76% 72%, rgba(6, 182, 212, 0.28), transparent 24%),
    linear-gradient(135deg, #2563eb 0%, #4f46e5 46%, #7c3aed 100%);
  box-shadow: 0 32px 90px rgba(37, 99, 235, 0.24);
}

.brand-row {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  border-radius: 16px;
  color: #2563eb;
  background: #fff;
  font-weight: 950;
}

.brand-row strong,
.brand-row span {
  display: block;
}

.brand-row strong {
  font-size: 20px;
  font-weight: 950;
}

.brand-row span {
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
}

.character-stage {
  position: relative;
  z-index: 1;
  display: grid;
  min-height: 360px;
  place-items: center;
}

.halo {
  position: absolute;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  animation: float 7s ease-in-out infinite;
}

.halo-one {
  width: 210px;
  height: 210px;
  right: 14%;
  top: 14%;
}

.halo-two {
  width: 118px;
  height: 118px;
  left: 16%;
  bottom: 18%;
  animation-delay: -2s;
}

.character {
  position: relative;
  width: 210px;
  height: 250px;
  animation: bob 4.8s ease-in-out infinite;
}

.head {
  position: absolute;
  left: 22px;
  top: 24px;
  width: 166px;
  height: 132px;
  border: 8px solid rgba(255, 255, 255, 0.9);
  border-radius: 52px;
  background: linear-gradient(180deg, #f8fbff, #dbeafe);
  box-shadow: 0 20px 42px rgba(15, 23, 42, 0.18);
}

.face {
  position: absolute;
  inset: 26px 22px;
  border-radius: 34px;
  background: #0f172a;
}

.eye {
  position: absolute;
  top: 30px;
  width: 20px;
  height: 26px;
  border-radius: 999px;
  background: #22d3ee;
  box-shadow: 0 0 18px rgba(34, 211, 238, 0.9);
  transform: translate(var(--eye-x), var(--eye-y));
  transition: transform 0.08s ease, height 0.2s ease;
}

.eye-left {
  left: 32px;
}

.eye-right {
  right: 32px;
}

.mouth {
  position: absolute;
  left: 50%;
  bottom: 26px;
  width: 34px;
  height: 14px;
  border-bottom: 4px solid #e0f2fe;
  border-radius: 0 0 999px 999px;
  transform: translateX(-50%);
}

.mood-focus .eye,
.mood-loading .eye {
  height: 18px;
}

.mood-loading .character {
  animation-duration: 1.6s;
}

.body {
  position: absolute;
  left: 55px;
  top: 142px;
  width: 100px;
  height: 94px;
  border-radius: 36px 36px 44px 44px;
  background: linear-gradient(180deg, #f8fbff, #bfdbfe);
  box-shadow: 0 20px 34px rgba(15, 23, 42, 0.16);
}

.badge {
  display: grid;
  width: 42px;
  height: 42px;
  margin: 22px auto;
  place-items: center;
  border-radius: 999px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  font-weight: 950;
}

.antenna {
  position: absolute;
  top: 4px;
  width: 4px;
  height: 38px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.86);
  transform-origin: bottom;
}

.antenna::before {
  content: '';
  position: absolute;
  left: 50%;
  top: -8px;
  width: 14px;
  height: 14px;
  border-radius: 999px;
  background: #e0f2fe;
  transform: translateX(-50%);
}

.antenna.left {
  left: 62px;
  transform: rotate(-18deg);
}

.antenna.right {
  right: 62px;
  transform: rotate(18deg);
}

.arm {
  position: absolute;
  top: 158px;
  width: 34px;
  height: 76px;
  border-radius: 999px;
  background: #dbeafe;
}

.arm-left {
  left: 25px;
  transform: rotate(24deg);
}

.arm-right {
  right: 25px;
  transform: rotate(-24deg);
}

.character-shadow {
  position: absolute;
  bottom: 38px;
  width: 180px;
  height: 28px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.15);
  filter: blur(6px);
}

.bubble {
  position: absolute;
  z-index: 2;
  padding: 11px 16px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 16px;
  color: #fff;
  background: rgba(255, 255, 255, 0.16);
  font-size: 14px;
  font-weight: 900;
  backdrop-filter: blur(10px);
}

.bubble-one {
  left: 34px;
  top: 88px;
}

.bubble-two {
  right: 26px;
  top: 146px;
}

.bubble-three {
  left: 58px;
  bottom: 70px;
}

.panel-copy {
  position: relative;
  z-index: 2;
  max-width: 430px;
}

.panel-copy p {
  margin: 0 0 10px;
  color: rgba(255, 255, 255, 0.72);
  font-size: 13px;
  font-weight: 900;
  letter-spacing: 0.05em;
  text-transform: uppercase;
}

.panel-copy h2 {
  margin: 0;
  font-size: 34px;
  font-weight: 950;
  line-height: 1.18;
}

.panel-copy span {
  display: block;
  margin-top: 14px;
  color: rgba(255, 255, 255, 0.78);
  line-height: 1.75;
}

@keyframes bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-12px); }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-18px) scale(1.04); }
}

@media (max-width: 980px) {
  .character-panel {
    min-height: 420px;
  }

  .character-stage {
    min-height: 260px;
  }

  .character {
    transform: scale(0.82);
  }
}
</style>
