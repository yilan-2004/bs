export type AvatarStatus = 'idle' | 'thinking' | 'speaking' | 'error'
export type AvatarModelType = 'live2d' | 'vrm' | string

export interface AvatarModelSource {
  urls: string[]
}

export interface AvatarLayout {
  width: number
  height: number
  scale?: number
  x?: number
  y?: number
  transparent?: boolean
  followPointer?: boolean
}

export interface AvatarStateConfig {
  motionGroup?: string
  expression?: string
  message?: string
}

export interface AvatarModel {
  id: string
  name: string
  type: AvatarModelType
  description?: string
  config: Record<string, AvatarModelSource>
  layout?: Partial<AvatarLayout>
  states?: Partial<Record<AvatarStatus, AvatarStateConfig>>
}

export interface CharacterAvatar {
  id: string
  name: string
  description?: string
  defaultAvatarModelId?: string
  avatarModels: AvatarModel[]
}

export interface AvatarRuntimeConfig {
  model: AvatarModel
  layout: AvatarLayout
  sourceUrl?: string
}

export interface AvatarInstance {
  app?: any
  model?: any
  runtime: AvatarRuntimeConfig
  currentStatus: AvatarStatus
  pointerHandler?: (event: PointerEvent) => void
  kind: 'live2d' | 'placeholder'
}

export interface CreateAvatarOptions {
  container: HTMLElement
  runtime: AvatarRuntimeConfig
  status?: AvatarStatus
}
