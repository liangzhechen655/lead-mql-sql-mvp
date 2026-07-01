<template>
  <section class="query-layout">
    <div class="panel query-main">
      <div class="section-heading compact">
        <div>
          <span class="eyebrow">Rule Agent</span>
          <h3>自然语言业务查询</h3>
          <p>把常见运营问题映射到漏斗、异常和渠道转化查询。</p>
        </div>
      </div>
      <div class="query-command">
        <el-input v-model="question" type="textarea" :rows="5" placeholder="例如：本周哪个渠道 SQL 转化率最高？当前有多少异常线索？整体漏斗转化率是多少？" />
      </div>
      <div class="query-actions">
        <el-button type="primary" :icon="Promotion" @click="askQuestion">查询</el-button>
        <el-button @click="askPreset('当前有多少异常线索？')">查异常</el-button>
        <el-button @click="askPreset('整体漏斗转化率是多少？')">查漏斗</el-button>
      </div>
      <div v-if="queryResult" class="answer-box">
        <span>命中规则 · {{ queryResult.matchedRule }}</span>
        <p>{{ queryResult.answer }}</p>
      </div>
    </div>

    <aside class="query-side">
      <div class="panel mini-panel">
        <span class="eyebrow">Prompt Library</span>
        <h3>可问问题</h3>
        <button v-for="item in sampleQuestions" :key="item" type="button" @click="askPreset(item)">
          {{ item }}
        </button>
      </div>
      <div class="panel mini-panel">
        <span class="eyebrow">Snapshot</span>
        <h3>当前概览</h3>
        <div class="side-metrics">
          <span>总线索 <b>{{ funnel.total || 0 }}</b></span>
          <span>有效率 <b>{{ funnel.validRate || 0 }}%</b></span>
          <span>SQL 转化 <b>{{ funnel.sqlRate || 0 }}%</b></span>
          <span>异常线索 <b>{{ anomalies.length }}</b></span>
        </div>
      </div>
    </aside>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Promotion } from '@element-plus/icons-vue'
import { api, errorMessage } from '../api'

defineProps({
  funnel: { type: Object, required: true },
  anomalies: { type: Array, required: true }
})

const question = ref('本周哪个渠道 SQL 转化率最高？')
const queryResult = ref(null)

const sampleQuestions = [
  '本周哪个渠道 SQL 转化率最高？',
  '当前有多少异常线索？',
  '整体漏斗转化率是多少？'
]

async function askQuestion() {
  try {
    queryResult.value = await api.ask(question.value)
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

function askPreset(value) {
  question.value = value
  askQuestion()
}
</script>
