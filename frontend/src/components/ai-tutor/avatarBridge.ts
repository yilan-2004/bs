import type { AvatarInstance, AvatarStatus, CreateAvatarOptions } from './avatarTypes'

type PixiModule = typeof import('pixi.js')

async function loadLive2dRuntime() {
  const PIXI = await import('pixi.js')
  ;(window as any).PIXI = PIXI
  const live2d = await import('pixi-live2d-display/cubism4')
  return { PIXI, Live2DModel: live2d.Live2DModel }
}

function supportsLive2dSource(url?: string) {
  return Boolean(url && /\.model3\.json(\?|#|$)/i.test(url))
}

export async function createAvatar(options: CreateAvatarOptions): Promise<AvatarInstance> {
  const { container, runtime } = options
  if (runtime.model.type === 'live2d' && supportsLive2dSource(runtime.sourceUrl)) {
    return createLive2dAvatar(options)
  }
  return createPlaceholderAvatar(options)
}

async function createLive2dAvatar(options: CreateAvatarOptions): Promise<AvatarInstance> {
  const { container, runtime } = options
  const { PIXI, Live2DModel } = await loadLive2dRuntime()
  const width = runtime.layout.width || 320
  const height = runtime.layout.height || 420

  const app = new PIXI.Application({
    width,
    height,
    transparent: runtime.layout.transparent !== false,
    autoStart: true,
    antialias: true
  } as any)

  container.innerHTML = ''
  container.appendChild(app.view as HTMLCanvasElement)

  const model = await Live2DModel.from(runtime.sourceUrl, {
    autoInteract: runtime.layout.followPointer !== false
  } as any)

  model.scale.set(runtime.layout.scale || 0.28)
  model.x = width / 2 + (runtime.layout.x || 0)
  model.y = height + (runtime.layout.y || 0)
  model.anchor?.set?.(0.5, 1)
  app.stage.addChild(model)

  const instance: AvatarInstance = {
    app,
    model,
    runtime,
    currentStatus: options.status || 'idle',
    kind: 'live2d'
  }

  if (runtime.layout.followPointer) {
    instance.pointerHandler = (event: PointerEvent) => {
      try {
        const rect = container.getBoundingClientRect()
        const px = event.clientX - rect.left
        const py = event.clientY - rect.top
        model.internalModel?.focusController?.focus?.(
          (px / Math.max(1, rect.width) - 0.5) * 2,
          (py / Math.max(1, rect.height) - 0.5) * -2
        )
      } catch {
        // Optional pointer focus must not break the tutor page.
      }
    }
    container.addEventListener('pointermove', instance.pointerHandler)
  }

  await setAvatarStatus(instance, instance.currentStatus)
  return instance
}

async function createPlaceholderAvatar(options: CreateAvatarOptions): Promise<AvatarInstance> {
  options.container.innerHTML = ''
  return {
    runtime: options.runtime,
    currentStatus: options.status || 'idle',
    kind: 'placeholder'
  }
}

export function destroyAvatar(instance?: AvatarInstance | null) {
  if (!instance) return
  try {
    const view = instance.app?.view as HTMLCanvasElement | undefined
    if (instance.pointerHandler && view?.parentElement) {
      view.parentElement.removeEventListener('pointermove', instance.pointerHandler)
    }
    instance.model?.destroy?.()
    instance.app?.destroy?.(true, { children: true, texture: true, baseTexture: true })
  } catch {
    // Best-effort destroy.
  }
}

export async function setAvatarStatus(instance: AvatarInstance, status: AvatarStatus) {
  instance.currentStatus = status
  if (instance.kind !== 'live2d') return
  const state = instance.runtime.model.states?.[status] || instance.runtime.model.states?.idle
  if (state?.expression) {
    await setExpression(instance, state.expression)
  }
  if (state?.motionGroup) {
    const played = await playMotion(instance, state.motionGroup)
    if (!played && state.motionGroup !== 'Idle') {
      await playMotion(instance, 'Idle')
    }
  }
}

export async function playMotion(instance: AvatarInstance, motionGroup?: string): Promise<boolean> {
  if (!motionGroup) return false
  try {
    const motionManager = instance.model?.internalModel?.motionManager
    const definitions = motionManager?.definitions || motionManager?.motionGroups
    if (definitions && !definitions[motionGroup]) return false
    await instance.model?.motion?.(motionGroup)
    return true
  } catch {
    return false
  }
}

export async function setExpression(instance: AvatarInstance, expression?: string): Promise<boolean> {
  if (!expression) return false
  try {
    const expressionManager = instance.model?.internalModel?.motionManager?.expressionManager
    const definitions = expressionManager?.definitions || expressionManager?.expressions
    if (Array.isArray(definitions)) {
      const exists = definitions.some((item: any) => item?.Name === expression || item?.name === expression)
      if (!exists) return false
    }
    await instance.model?.expression?.(expression)
    return true
  } catch {
    return false
  }
}

export function resizeAvatar(instance: AvatarInstance, width: number, height: number) {
  try {
    const PIXI = (window as any).PIXI as PixiModule | undefined
    instance.app?.renderer?.resize?.(width, height)
    if (PIXI && instance.model) {
      instance.model.x = width / 2 + (instance.runtime.layout.x || 0)
      instance.model.y = height + (instance.runtime.layout.y || 0)
    }
  } catch {
    // Resize is non-critical.
  }
}
