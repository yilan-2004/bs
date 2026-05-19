<template>
  <div class="airi-stage-avatar" :class="{ 'is-fallback': loadFailed }">
    <div ref="canvasHost" class="airi-canvas-host"></div>
    <div v-if="loadFailed || loading" class="airi-fallback">
      <img v-if="config?.model?.preview" :src="config.model.preview" alt="AIRI" />
      <div v-else class="fallback-face">A</div>
      <strong>{{ loading ? '正在加载 AIRI...' : 'AIRI 暂不可用' }}</strong>
      <span>{{ loading ? '正在准备虚拟学习助教形象' : '请确认 Live2D 模型文件已放入 public/live2d 目录' }}</span>
    </div>
    <div class="airi-status-pill">{{ statusText }}</div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
  status: {
    type: String,
    default: 'idle'
  },
  config: {
    type: Object,
    required: true
  }
})

const canvasHost = ref(null)
const loading = ref(true)
const loadFailed = ref(false)
let app = null
let model = null
let pointerHandler = null
let cubismCorePromise = null

const statusText = computed(() => {
  const map = {
    idle: '准备学习',
    thinking: '正在思考',
    speaking: '正在回答',
    error: '需要重试'
  }
  return map[props.status] || map.idle
})

function loadScript(src) {
  return new Promise((resolve, reject) => {
    const existing = document.querySelector(`script[data-airi-cubism-core="${src}"]`)
    if (existing) {
      existing.addEventListener('load', resolve, { once: true })
      existing.addEventListener('error', reject, { once: true })
      if (window.Live2DCubismCore) resolve()
      return
    }

    const script = document.createElement('script')
    script.src = src
    script.async = true
    script.dataset.airiCubismCore = src
    script.onload = resolve
    script.onerror = () => reject(new Error(`无法加载 Cubism Core: ${src}`))
    document.head.appendChild(script)
  })
}

async function ensureCubismCore() {
  if (window.Live2DCubismCore) return
  if (!cubismCorePromise) {
    cubismCorePromise = loadScript('/live2dcubismcore.min.js')
      .catch(() => loadScript('/assets/js/CubismSdkForWeb-5-r.3/Core/live2dcubismcore.min.js'))
  }
  await cubismCorePromise
}

async function loadRuntime() {
  await ensureCubismCore()
  const PIXI = await import('pixi.js')
  window.PIXI = PIXI
  const live2d = await import('pixi-live2d-display/cubism4')
  return { PIXI, Live2DModel: live2d.Live2DModel }
}

async function createAvatar() {
  if (!canvasHost.value || !props.config?.model?.modelJsonPath) return
  loading.value = true
  loadFailed.value = false

  try {
    const { PIXI, Live2DModel } = await loadRuntime()
    const layout = props.config.layout || {}
    const width = Number(layout.width || 760)
    const height = Number(layout.height || 880)

    app = new PIXI.Application({
      width,
      height,
      transparent: props.config.options?.transparent !== false,
      backgroundAlpha: 0,
      autoStart: true,
      antialias: true
    })

    canvasHost.value.innerHTML = ''
    canvasHost.value.appendChild(app.view)

    model = await Live2DModel.from(props.config.model.modelJsonPath, {
      autoInteract: props.config.options?.autoInteract !== false
    })

    model.anchor?.set?.(Number(layout.anchorX ?? 0.5), Number(layout.anchorY ?? 1))
    model.scale.set(Number(layout.scale || 0.44))
    model.x = width / 2 + Number(layout.x || 0)
    model.y = height + Number(layout.y || 0)
    app.stage.addChild(model)

    if (props.config.options?.followPointer !== false) {
      pointerHandler = (event) => {
        try {
          const rect = canvasHost.value.getBoundingClientRect()
          const px = event.clientX - rect.left
          const py = event.clientY - rect.top
          model.internalModel?.focusController?.focus?.(
            (px / Math.max(1, rect.width) - 0.5) * 2,
            (py / Math.max(1, rect.height) - 0.5) * -2
          )
        } catch {
          // Live2D pointer focus is optional.
        }
      }
      canvasHost.value.addEventListener('pointermove', pointerHandler)
    }

    await setStatus(props.status)
  } catch (error) {
    console.warn('[AIRI] Live2D load failed:', error?.message || error)
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

async function setStatus(status) {
  if (!model) return
  const state = props.config.states?.[status] || props.config.states?.idle
  if (!state?.motionGroup) return

  try {
    const motionManager = model.internalModel?.motionManager
    const definitions = motionManager?.definitions || motionManager?.motionGroups
    const group = definitions?.[state.motionGroup] ? state.motionGroup : 'Idle'
    await model.motion?.(group, Number(state.motionIndex || 0))
  } catch (error) {
    try {
      await model.motion?.('Idle')
    } catch {
      // Missing motions should not break the page.
    }
  }
}

function destroyAvatar() {
  try {
    if (pointerHandler && canvasHost.value) {
      canvasHost.value.removeEventListener('pointermove', pointerHandler)
    }
    pointerHandler = null
    model?.destroy?.()
    model = null
    app?.destroy?.(true, { children: true, texture: true, baseTexture: true })
    app = null
  } catch {
    // Best effort cleanup during route changes.
  }
}

onMounted(async () => {
  await nextTick()
  await createAvatar()
})

watch(
  () => props.status,
  (status) => setStatus(status)
)

onBeforeUnmount(destroyAvatar)
</script>

<style scoped>
.airi-stage-avatar {
  position: relative;
  width: min(48vw, 720px);
  height: min(84vh, 900px);
  min-height: 680px;
  pointer-events: auto;
}

.airi-canvas-host {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: end center;
  overflow: visible;
}

.airi-canvas-host :deep(canvas) {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain;
  display: block;
}

.airi-fallback {
  position: absolute;
  left: 50%;
  top: 50%;
  display: grid;
  width: 260px;
  transform: translate(-50%, -50%);
  place-items: center;
  gap: 10px;
  padding: 22px;
  border: 1px solid rgba(82, 202, 228, 0.35);
  border-radius: 22px;
  color: #cffafe;
  background: rgba(5, 32, 39, 0.82);
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.35);
  text-align: center;
}

.airi-fallback img {
  width: 118px;
  height: 118px;
  object-fit: contain;
}

.fallback-face {
  display: grid;
  width: 96px;
  height: 96px;
  place-items: center;
  border-radius: 28px;
  color: #dffbff;
  font-size: 48px;
  font-weight: 900;
  background: linear-gradient(135deg, #06b6d4, #2563eb);
}

.airi-fallback span {
  color: rgba(207, 250, 254, 0.72);
  font-size: 13px;
  line-height: 1.6;
}

.airi-status-pill {
  position: absolute;
  left: 50%;
  bottom: 26px;
  z-index: 5;
  transform: translateX(-50%);
  padding: 10px 18px;
  border: 1px solid rgba(93, 218, 244, 0.35);
  border-radius: 999px;
  color: #cdf6ff;
  background: rgba(5, 45, 55, 0.78);
  box-shadow: 0 14px 40px rgba(0, 0, 0, 0.35);
  font-size: 14px;
  font-weight: 800;
}

@media (max-width: 1100px) {
  .airi-stage-avatar {
    width: 100%;
    height: 520px;
    min-height: 520px;
  }
}
</style>
