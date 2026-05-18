import type { CreateLive2dAvatarOptions, Live2dAvatarInstance, Live2dAvatarStatus } from './live2dTypes'

type PixiModule = typeof import('pixi.js')

async function loadRuntime() {
  const PIXI = await import('pixi.js')
  ;(window as any).PIXI = PIXI
  const live2d = await import('pixi-live2d-display/cubism4')
  return { PIXI, Live2DModel: live2d.Live2DModel }
}

export async function createLive2dAvatar(options: CreateLive2dAvatarOptions): Promise<Live2dAvatarInstance> {
  const { container, config } = options
  const { PIXI, Live2DModel } = await loadRuntime()
  const width = config.layout.width || 320
  const height = config.layout.height || 420

  const app = new PIXI.Application({
    width,
    height,
    transparent: config.options?.transparent !== false,
    autoStart: true,
    antialias: true
  } as any)

  container.innerHTML = ''
  container.appendChild(app.view as HTMLCanvasElement)

  const model = await Live2DModel.from(config.modelJsonPath, {
    autoInteract: config.options?.followPointer !== false
  } as any)

  model.scale.set(config.layout.scale || 0.28)
  model.x = width / 2 + (config.layout.x || 0)
  model.y = height + (config.layout.y || 0)
  model.anchor?.set?.(0.5, 1)
  app.stage.addChild(model)

  const instance: Live2dAvatarInstance = {
    app,
    model,
    config,
    currentStatus: options.status || 'idle'
  }

  if (config.options?.followPointer) {
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
        // Pointer focus is optional and should never affect page stability.
      }
    }
    container.addEventListener('pointermove', instance.pointerHandler)
  }

  await setAvatarStatus(instance, instance.currentStatus)
  return instance
}

export function destroyLive2dAvatar(instance?: Live2dAvatarInstance | null) {
  if (!instance) return
  try {
    const view = instance.app?.view as HTMLCanvasElement | undefined
    if (instance.pointerHandler && view?.parentElement) {
      view.parentElement.removeEventListener('pointermove', instance.pointerHandler)
    }
    instance.model?.destroy?.()
    instance.app?.destroy?.(true, { children: true, texture: true, baseTexture: true })
  } catch {
    // Destroy is best-effort to avoid blocking route changes.
  }
}

export async function setAvatarStatus(instance: Live2dAvatarInstance, status: Live2dAvatarStatus) {
  instance.currentStatus = status
  const state = instance.config.states?.[status] || instance.config.states?.idle
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

export async function playMotion(instance: Live2dAvatarInstance, motionGroup?: string): Promise<boolean> {
  if (!motionGroup) return false
  try {
    const motionManager = instance.model?.internalModel?.motionManager
    const definitions = motionManager?.definitions || motionManager?.motionGroups
    if (definitions && !definitions[motionGroup]) {
      return false
    }
    await instance.model?.motion?.(motionGroup)
    return true
  } catch {
    return false
  }
}

export async function setExpression(instance: Live2dAvatarInstance, expression?: string): Promise<boolean> {
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

export function resizeAvatar(instance: Live2dAvatarInstance, width: number, height: number) {
  try {
    const PIXI = (window as any).PIXI as PixiModule | undefined
    instance.app?.renderer?.resize?.(width, height)
    if (PIXI && instance.model) {
      instance.model.x = width / 2 + (instance.config.layout.x || 0)
      instance.model.y = height + (instance.config.layout.y || 0)
    }
  } catch {
    // Resize is non-critical.
  }
}
