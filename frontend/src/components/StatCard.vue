<template>
  <div class="stat-card elevated-card" :class="`stat-${type}`">
    <div class="stat-icon">
      <component :is="icon" v-if="icon" />
    </div>
    <div class="stat-content">
      <div class="stat-title">{{ title }}</div>
      <div class="stat-value">{{ value }}</div>
      <div class="stat-bottom">
        <span v-if="subText">{{ subText }}</span>
        <b v-if="trend">{{ trend }}</b>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: [Object, Function, String], default: null },
  title: { type: String, default: '' },
  value: { type: [String, Number], default: '-' },
  subText: { type: String, default: '' },
  trend: { type: String, default: '' },
  type: { type: String, default: 'blue' }
})
</script>

<style scoped>
.stat-card {
  position: relative;
  display: flex;
  gap: 16px;
  min-height: 132px;
  padding: 21px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.70);
  border-radius: 24px;
  background:
    radial-gradient(circle at 92% 10%, rgba(59, 130, 246, 0.11), transparent 32%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(248, 250, 252, 0.64));
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.07);
  backdrop-filter: blur(20px) saturate(1.12);
}

.stat-card::after {
  content: '';
  position: absolute;
  right: -40px;
  bottom: -60px;
  width: 150px;
  height: 150px;
  border-radius: 999px;
  background: rgba(147, 197, 253, 0.18);
  pointer-events: none;
}

.stat-icon {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  width: 52px;
  height: 52px;
  border-radius: 18px;
  font-size: 30px;
  line-height: 1;
  overflow: hidden;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.82), 0 14px 28px rgba(15, 23, 42, 0.08);
}

.stat-icon :deep(svg) {
  width: 1em;
  height: 1em;
  max-width: 30px;
  max-height: 30px;
}

.stat-content {
  position: relative;
  z-index: 1;
  min-width: 0;
}

.stat-title {
  color: #64748b;
  font-size: 13px;
  font-weight: 800;
}

.stat-value {
  margin-top: 8px;
  color: #0f172a;
  font-size: 32px;
  font-weight: 950;
  letter-spacing: -0.04em;
}

.stat-bottom {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
  color: #94a3b8;
  font-size: 12px;
}

.stat-bottom b {
  color: #10b981;
}

.stat-blue .stat-icon {
  color: #2563eb;
  background: linear-gradient(135deg, #dbeafe, #eff6ff);
}

.stat-purple .stat-icon {
  color: #7c3aed;
  background: linear-gradient(135deg, #ede9fe, #faf5ff);
}

.stat-cyan .stat-icon {
  color: #0891b2;
  background: linear-gradient(135deg, #cffafe, #ecfeff);
}

.stat-green .stat-icon {
  color: #059669;
  background: linear-gradient(135deg, #d1fae5, #ecfdf5);
}

.stat-orange .stat-icon {
  color: #d97706;
  background: linear-gradient(135deg, #fef3c7, #fffbeb);
}

.stat-red .stat-icon {
  color: #dc2626;
  background: linear-gradient(135deg, #fee2e2, #fff1f2);
}
</style>
