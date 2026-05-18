<template>
  <div class="empty-state">
    <img v-if="image" class="empty-image" :src="image" :alt="title" />
    <div v-else class="empty-icon">
      <component :is="icon" v-if="icon" />
      <span v-else>∅</span>
    </div>
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
    <slot name="action"></slot>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: [Object, Function, String], default: null },
  image: { type: String, default: '' },
  title: { type: String, default: '暂无数据' },
  description: { type: String, default: '当前没有可展示的内容' }
})
</script>

<style scoped>
.empty-state {
  display: grid;
  place-items: center;
  min-height: 220px;
  padding: 34px 20px;
  overflow: hidden;
  border: 1px dashed rgba(148, 163, 184, 0.42);
  border-radius: 20px;
  color: #6b7280;
  background:
    radial-gradient(circle at center, rgba(37, 99, 235, 0.07), transparent 42%),
    rgba(255, 255, 255, 0.72);
  text-align: center;
}

.empty-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 62px;
  height: 62px;
  margin-bottom: 14px;
  overflow: hidden;
  border-radius: 20px;
  color: #2563eb;
  background: #dbeafe;
  font-size: 28px;
  font-weight: 900;
}

.empty-icon :deep(svg) {
  width: 1em;
  height: 1em;
  max-width: 30px;
  max-height: 30px;
}

.empty-state h3 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  font-weight: 900;
}

.empty-state p {
  max-width: 420px;
  margin: 8px 0 18px;
  line-height: 1.7;
}
</style>
