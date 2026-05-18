<template>
  <div class="page-header">
    <div class="page-header-main">
      <div v-if="icon" class="page-header-icon">
        <component :is="icon" />
      </div>
      <div>
        <div v-if="eyebrow" class="page-header-eyebrow">{{ eyebrow }}</div>
        <h1>{{ title }}</h1>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div v-if="$slots.actions" class="page-header-actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  eyebrow: { type: String, default: '' },
  icon: { type: [Object, Function, String], default: null }
})
</script>

<style scoped>
.page-header {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding: 26px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.70);
  border-radius: 26px;
  background:
    radial-gradient(circle at 90% 6%, rgba(124, 58, 237, 0.14), transparent 30%),
    radial-gradient(circle at 12% 0%, rgba(59, 130, 246, 0.13), transparent 34%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.90), rgba(248, 250, 252, 0.66));
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(22px) saturate(1.18);
}

.page-header::before {
  content: '';
  position: absolute;
  inset: 0 0 auto 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.95), transparent);
}

.page-header-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.page-header-icon {
  display: inline-flex;
  flex: 0 0 58px;
  align-items: center;
  justify-content: center;
  width: 58px;
  height: 58px;
  border-radius: 20px;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #4f46e5 48%, #7c3aed);
  box-shadow: 0 16px 34px rgba(79, 70, 229, 0.28);
  font-size: 26px;
}

.page-header-icon :deep(svg) {
  width: 1em;
  height: 1em;
  max-width: 28px;
  max-height: 28px;
}

.page-header h1 {
  margin: 0;
  color: #0f172a;
  font-size: clamp(24px, 3vw, 32px);
  font-weight: 950;
  letter-spacing: -0.035em;
}

.page-header p {
  max-width: 760px;
  margin: 9px 0 0;
  color: #64748b;
  font-size: 14px;
  line-height: 1.75;
}

.page-header-eyebrow {
  margin-bottom: 7px;
  color: #4f46e5;
  font-size: 12px;
  font-weight: 950;
  letter-spacing: 0.11em;
  text-transform: uppercase;
}

.page-header-actions {
  position: relative;
  z-index: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 900px) {
  .page-header {
    align-items: flex-start;
    flex-direction: column;
    padding: 20px;
  }

  .page-header-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
