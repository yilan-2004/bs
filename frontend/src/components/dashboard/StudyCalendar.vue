<template>
  <section class="calendar-card">
    <div class="calendar-head">
      <h3>学习日历</h3>
      <div class="calendar-nav">
        <button @click="shiftMonth(-1)"><el-icon><ArrowLeft /></el-icon></button>
        <span>{{ monthLabel }}</span>
        <button @click="shiftMonth(1)"><el-icon><ArrowRight /></el-icon></button>
      </div>
    </div>

    <div class="week-row">
      <span v-for="day in weeks" :key="day">{{ day }}</span>
    </div>

    <div class="calendar-grid">
      <button
        v-for="cell in calendarCells"
        :key="cell.key"
        class="day-cell"
        :class="{ muted: !cell.current, today: cell.isToday, active: cell.date === selectedDate }"
        @click="openDay(cell)"
      >
        <span>{{ cell.day }}</span>
        <em v-if="cell.stats?.submitCount" class="dot practice"></em>
        <em v-if="cell.stats?.wrongCount" class="dot wrong"></em>
        <em v-if="cell.stats?.aiFeedbackCount" class="dot ai"></em>
      </button>
    </div>

    <div class="legend">
      <span><em class="dot practice"></em>有练习</span>
      <span><em class="dot wrong"></em>有错误</span>
      <span><em class="dot ai"></em>有 AI 诊断</span>
    </div>

    <el-dialog v-model="dialogVisible" :title="`${selectedDate || ''} 练习记录`" width="560px">
      <div v-if="dayRecords.length" class="day-records">
        <article v-for="record in dayRecords" :key="record.submitId" class="day-record">
          <div>
            <strong>{{ record.problemTitle || '未命名题目' }}</strong>
            <p>{{ record.subjectName || '未分类' }} · {{ record.bankName || '未归属题库' }}</p>
          </div>
          <StatusTag :status="record.judgeStatus" />
          <el-button link type="primary" @click="goProblem(record.problemId)">查看详情</el-button>
        </article>
      </div>
      <EmptyState v-else description="当天暂无练习记录" />
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { dashboardApi } from '../../api/dashboard'
import EmptyState from '../EmptyState.vue'
import StatusTag from '../StatusTag.vue'

const router = useRouter()
const currentMonth = ref(new Date())
const stats = ref([])
const selectedDate = ref('')
const dayRecords = ref([])
const dialogVisible = ref(false)
const weeks = ['日', '一', '二', '三', '四', '五', '六']

const monthValue = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = String(currentMonth.value.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
})

const monthLabel = computed(() => `${currentMonth.value.getFullYear()}年${currentMonth.value.getMonth() + 1}月`)

const statsMap = computed(() => {
  const map = new Map()
  stats.value.forEach((item) => map.set(item.date, item))
  return map
})

const calendarCells = computed(() => {
  const year = currentMonth.value.getFullYear()
  const month = currentMonth.value.getMonth()
  const first = new Date(year, month, 1)
  const start = new Date(year, month, 1 - first.getDay())
  const today = toDateString(new Date())
  return Array.from({ length: 42 }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    const value = toDateString(date)
    return {
      key: `${value}-${index}`,
      date: value,
      day: date.getDate(),
      current: date.getMonth() === month,
      isToday: value === today,
      stats: statsMap.value.get(value)
    }
  })
})

async function loadCalendar() {
  stats.value = await dashboardApi.studentCalendar({ month: monthValue.value })
}

async function openDay(cell) {
  selectedDate.value = cell.date
  dayRecords.value = await dashboardApi.studentDayRecords({ date: cell.date })
  dialogVisible.value = true
}

function shiftMonth(offset) {
  const next = new Date(currentMonth.value)
  next.setMonth(next.getMonth() + offset)
  currentMonth.value = next
  loadCalendar()
}

function toDateString(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function goProblem(problemId) {
  if (problemId) {
    dialogVisible.value = false
    router.push(`/student/problem/${problemId}`)
  }
}

onMounted(loadCalendar)
</script>

<style scoped>
.calendar-card {
  padding: 20px;
  border: 1px solid #e5eaf2;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(15, 23, 42, 0.06);
}

.calendar-head,
.calendar-nav,
.week-row,
.legend {
  display: flex;
  align-items: center;
}

.calendar-head {
  justify-content: space-between;
  margin-bottom: 16px;
}

.calendar-head h3 {
  margin: 0;
  color: #0f172a;
  font-size: 17px;
  font-weight: 900;
}

.calendar-nav {
  gap: 8px;
  color: #475569;
  font-size: 13px;
  font-weight: 800;
}

.calendar-nav button {
  display: inline-flex;
  width: 26px;
  height: 26px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 8px;
  background: #f1f5f9;
  color: #475569;
  cursor: pointer;
  font-size: 14px;
}

.week-row,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}

.week-row {
  margin-bottom: 8px;
  color: #64748b;
  font-size: 12px;
  text-align: center;
}

.day-cell {
  position: relative;
  display: inline-flex;
  height: 38px;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 12px;
  color: #334155;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
}

.day-cell:hover {
  background: #eff6ff;
}

.day-cell.muted {
  color: #cbd5e1;
}

.day-cell.today,
.day-cell.active {
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #60a5fa);
}

.dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 999px;
}

.day-cell .dot {
  position: absolute;
  bottom: 5px;
}

.day-cell .dot.practice {
  left: calc(50% - 10px);
}

.day-cell .dot.wrong {
  left: calc(50% - 2px);
}

.day-cell .dot.ai {
  left: calc(50% + 6px);
}

.practice {
  background: #2563eb;
}

.wrong {
  background: #f59e0b;
}

.ai {
  background: #7c3aed;
}

.legend {
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
  color: #64748b;
  font-size: 12px;
}

.legend span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.day-records {
  display: grid;
  gap: 10px;
}

.day-record {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid #edf2f7;
  border-radius: 14px;
  background: #f8fafc;
}

.day-record strong {
  color: #0f172a;
  font-size: 14px;
}

.day-record p {
  margin: 4px 0 0;
  color: #64748b;
  font-size: 12px;
}
</style>
