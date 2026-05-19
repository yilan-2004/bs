<template>
  <AvatarRenderer :model="resolvedModel" :status="status" :message="message" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import AvatarRenderer from './AvatarRenderer.vue'
import { fallbackCharacterAvatar, resolveDefaultAvatarModel } from './avatarPresets'
import type { AvatarModel, AvatarStatus, CharacterAvatar } from './avatarTypes'

const props = defineProps<{
  status: AvatarStatus
  message?: string
  character?: CharacterAvatar | null
  model?: AvatarModel | null
}>()

const resolvedModel = computed(() => {
  if (props.model) return props.model
  return resolveDefaultAvatarModel(props.character || fallbackCharacterAvatar)
})
</script>
