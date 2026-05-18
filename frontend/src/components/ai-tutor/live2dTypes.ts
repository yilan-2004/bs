export type Live2dAvatarStatus = 'idle' | 'thinking' | 'speaking' | 'error'

export interface Live2dAvatarStateConfig {
  motionGroup?: string
  expression?: string
}

export interface Live2dAvatarConfig {
  id: string
  name: string
  modelType: 'live2d'
  modelJsonPath: string
  layout: {
    width: number
    height: number
    scale: number
    x: number
    y: number
  }
  states: Record<Live2dAvatarStatus, Live2dAvatarStateConfig>
  options: {
    autoBlink?: boolean
    autoBreath?: boolean
    followPointer?: boolean
    transparent?: boolean
  }
}

export interface CreateLive2dAvatarOptions {
  container: HTMLElement
  config: Live2dAvatarConfig
  status?: Live2dAvatarStatus
}

export interface Live2dAvatarInstance {
  app: any
  model: any
  config: Live2dAvatarConfig
  currentStatus: Live2dAvatarStatus
  pointerHandler?: (event: PointerEvent) => void
}
