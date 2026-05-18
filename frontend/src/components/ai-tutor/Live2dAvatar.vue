<template>
  <div
    class="live2d-avatar"
    :style="{ width: `${mergedConfig.layout.width}px`, height: `${mergedConfig.layout.height}px` }"
  >
    <div v-show="loaded && !failed" ref="canvasHost" class="live2d-canvas"></div>

    <div v-if="!loaded || failed" class="avatar-fallback" :class="status">
      <div class="fallback-face">
        <span class="eye"></span>
        <span class="eye"></span>
        <em></em>
      </div>
      <strong>{{ mergedConfig.name || 'AI 学习助教' }}</strong>
      <p>{{ failed ? '虚拟助教暂不可用' : '虚拟助教加载中' }}</p>
    </div>

    <div v-if="message" class="avatar-message">
      {{ message }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import defaultConfig from '../../assets/live2d/airi/airi.config.json'
import { createLive2dAvatar, destroyLive2dAvatar, resizeAvatar, setAvatarStatus } from './live2dBridge'
import type { Live2dAvatarConfig, Live2dAvatarInstance, Live2dAvatarStatus } from './live2dTypes'

const props = defineProps<{
  status: Live2dAvatarStatus
  config?: Live2dAvatarConfig
  message?: string
}>()

const canvasHost = ref<HTMLElement | null>(null)
const instance = ref<Live2dAvatarInstance | null>(null)
const loaded = ref(false)
const failed = ref(false)

const mergedConfig = computed<Live2dAvatarConfig>(() => props.config || (defaultConfig as Live2dAvatarConfig))

async function mountAvatar() {
  await nextTick()
  if (!canvasHost.value) return
  loaded.value = false
  failed.value = false
  try {
    instance.value = await createLive2dAvatar({
      container: canvasHost.value,
      config: mergedConfig.value,
      status: props.status || 'idle'
    })
    loaded.value = true
  } catch (error) {
    console.warn('[Live2D] avatar load failed:', error)
    failed.value = true
  }
}

watch(() => props.status, async (status) => {
  if (!instance.value || failed.value) return
  await setAvatarStatus(instance.value, status || 'idle')
})

watch(() => [mergedConfig.value.layout.width, mergedConfig.value.layout.height], ([width, height]) => {
  if (instance.value) {
    resizeAvatar(instance.value, Number(width), Number(height))
  }
})

onMounted(mountAvatar)

onBeforeUnmount(() => {
  destroyLive2dAvatar(instance.value)
  instance.value = null
})
</script>

<style scoped>
.live2d-avatar {
  position: relative;
  max-width: 100%;
  overflow: hidden;
  border-radius: 22px;
  background:
    radial-gradient(circle at 50% 20%, rgba(37, 99, 235, 0.12), transparent 34%),
    linear-gradient(180deg, #f8fbff, #eef6ff);
}

.live2d-canvas {
  width: 100%;
  height: 100%;
}

.live2d-canvas :deep(canvas) {
  display: block;
  width: 100% !important;
  height: 100% !important;
}

.avatar-fallback {
  position: absolute;
  inset: 0;
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 10px;
  padding: 24px;
  text-align: center;
}

.fallback-face {
  position: relative;
  width: 96px;
  height: 96px;
  border-radius: 32px;
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  box-shadow: 0 18px 36px rgba(37, 99, 235, 0.22);
}

.fallback-face .eye {
  position: absolute;
  top: 34px;
  width: 12px;
  height: 18px;
  border-radius: 999px;
  background: #fff;
}

.fallback-face .eye:first-child {
  left: 28px;
}

.fallback-face .eye:nth-child(2) {
  right: 28px;
}

.fallback-face em {
  position: absolute;
  left: 34px;
  bottom: 28px;
  width: 28px;
  height: 10px;
  border-bottom: 4px solid #fff;
  border-radius: 0 0 999px 999px;
}

.avatar-fallback.thinking .fallback-face {
  background: linear-gradient(135deg, #06b6d4, #2563eb);
}

.avatar-fallback.speaking .fallback-face {
  background: linear-gradient(135deg, #10b981, #2563eb);
}

.avatar-fallback.error .fallback-face {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
}

.avatar-fallback strong {
  color: #0f172a;
  font-size: 18px;
  font-weight: 900;
}

.avatar-fallback p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
}

.avatar-message {
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 16px;
  padding: 10px 12px;
  border: 1px solid rgba(37, 99, 235, 0.14);
  border-radius: 14px;
  color: #1e3a8a;
  background: rgba(255, 255, 255, 0.86);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.08);
  font-size: 13px;
  line-height: 1.55;
}

@media (max-width: 820px) {
  .live2d-avatar {
    width: 100% !important;
    height: 260px !important;
  }
}
</style>
