<template>
  <header class="dashboard-topbar">
    <div class="welcome-block">
      <h1>你好，{{ studentName }}</h1>
      <p>坚持学习，未来可期！今天也要加油哦！</p>
    </div>

    <el-input
      v-model="keyword"
      class="dashboard-search"
      size="large"
      clearable
      placeholder="搜索课程、题目、知识点..."
      @keyup.enter="handleSearch"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <div class="topbar-actions">
      <button class="round-action" title="学习提醒" @click="$emit('open-reminders')">
        <el-badge :value="reminderCount" :hidden="!reminderCount" class="action-badge">
          <el-icon><Bell /></el-icon>
        </el-badge>
      </button>
      <button class="round-action" title="AI 助教" @click="router.push('/student/report')">
        <el-icon><ChatDotRound /></el-icon>
      </button>
      <div class="profile-pill">
        <el-avatar :size="44" class="profile-avatar">{{ studentName.slice(0, 1) }}</el-avatar>
        <div class="profile-text">
          <strong>{{ studentName }}</strong>
          <span>学生</span>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { Bell, ChatDotRound, Search } from '@element-plus/icons-vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

defineProps({
  studentName: { type: String, default: '同学' },
  reminderCount: { type: Number, default: 0 }
})

defineEmits(['open-reminders'])

const router = useRouter()
const keyword = ref('')

function handleSearch() {
  const value = keyword.value.trim()
  router.push(value ? `/student/banks?keyword=${encodeURIComponent(value)}` : '/student/banks')
}
</script>

<style scoped>
.dashboard-topbar {
  display: grid;
  grid-template-columns: minmax(220px, 320px) minmax(320px, 560px) auto;
  align-items: center;
  gap: 24px;
  max-width: 1440px;
  margin: 0 auto 24px;
}

.welcome-block h1 {
  margin: 0;
  color: #111827;
  font-size: 24px;
  font-weight: 900;
  letter-spacing: 0;
}

.welcome-block p {
  margin: 8px 0 0;
  color: #64748b;
  font-size: 14px;
}

.dashboard-search :deep(.el-input__wrapper) {
  min-height: 52px;
  border: 1px solid #e5eaf2;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.topbar-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  min-width: 0;
}

.round-action {
  display: inline-flex;
  width: 48px;
  height: 48px;
  align-items: center;
  justify-content: center;
  border: 1px solid #e5eaf2;
  border-radius: 16px;
  color: #334155;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
  cursor: pointer;
  font-size: 20px;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.round-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(37, 99, 235, 0.14);
}

.action-badge {
  display: inline-flex;
}

.profile-pill {
  display: grid;
  grid-template-columns: 44px minmax(0, auto);
  align-items: center;
  gap: 10px;
  padding: 6px 12px 6px 6px;
  border: 1px solid #e5eaf2;
  border-radius: 999px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.profile-avatar {
  background: linear-gradient(135deg, #2563eb, #7c3aed);
  color: #fff;
  font-weight: 900;
}

.profile-text strong,
.profile-text span {
  display: block;
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.profile-text strong {
  color: #111827;
  font-size: 14px;
}

.profile-text span {
  margin-top: 2px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 800;
}

@media (max-width: 1180px) {
  .dashboard-topbar {
    grid-template-columns: 1fr;
  }

  .topbar-actions {
    justify-content: flex-start;
  }
}
</style>
