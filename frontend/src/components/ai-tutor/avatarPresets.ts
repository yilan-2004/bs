import type { AvatarLayout, AvatarModel, CharacterAvatar } from './avatarTypes'

export const defaultAvatarLayout: AvatarLayout = {
  width: 320,
  height: 420,
  scale: 0.28,
  x: 0,
  y: 0,
  transparent: true,
  followPointer: true
}

export const localAiriAvatarModel: AvatarModel = {
  id: 'airi-local-live2d',
  name: 'AIRI 学习助教',
  type: 'live2d',
  description: '项目本地 Live2D 资源，采用 AIRI avatarModels.config.live2d.urls 结构。',
  config: {
    live2d: {
      urls: ['/live2d/airi/model/airi.model3.json']
    }
  },
  layout: defaultAvatarLayout,
  states: {
    idle: { motionGroup: 'Idle', expression: 'normal', message: '准备好一起学习了。' },
    thinking: { motionGroup: 'Thinking', expression: 'thinking', message: '我正在分析你的问题...' },
    speaking: { motionGroup: 'TapBody', expression: 'happy', message: '这是我的学习建议。' },
    error: { motionGroup: 'Error', expression: 'sad', message: '刚才出了一点小问题。' }
  }
}

export const fallbackCharacterAvatar: CharacterAvatar = {
  id: 'student-ai-tutor',
  name: 'AI 学习助教',
  description: '本地默认形象配置；后端 /avatars/student-tutor 可覆盖。',
  defaultAvatarModelId: localAiriAvatarModel.id,
  avatarModels: [localAiriAvatarModel]
}

export function resolveDefaultAvatarModel(character?: CharacterAvatar | null): AvatarModel {
  const models = character?.avatarModels?.length ? character.avatarModels : fallbackCharacterAvatar.avatarModels
  return models.find((item) => item.id === character?.defaultAvatarModelId) || models[0] || localAiriAvatarModel
}

export function toRuntimeConfig(model: AvatarModel) {
  const layout = { ...defaultAvatarLayout, ...(model.layout || {}) }
  const sourceUrl = model.config?.[model.type]?.urls?.[0]
    || model.config?.live2d?.urls?.[0]
    || model.config?.vrm?.urls?.[0]
  return { model, layout, sourceUrl }
}
