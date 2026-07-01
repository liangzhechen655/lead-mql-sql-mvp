<template>
  <section class="dashboard-grid">
    <div v-for="item in metricCards" :key="item.label" class="metric" :class="item.tone" v-loading="item.loading">
      <span>{{ item.label }}</span>
      <strong>{{ item.value }}</strong>
      <small>{{ item.caption }}</small>
    </div>

    <section class="panel wide">
      <div class="section-heading compact">
        <div>
          <span class="eyebrow">Lifecycle</span>
          <h3>状态漏斗</h3>
        </div>
      </div>
      <div class="funnel">
        <div v-for="item in funnelItems" :key="item.key" class="funnel-row">
          <span>{{ item.label }}</span>
          <div class="funnel-bar">
            <i :style="{ width: item.width + '%' }"></i>
          </div>
          <b>{{ item.count }}</b>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="section-heading compact">
        <div>
          <span class="eyebrow">Channel</span>
          <h3>渠道 SQL 转化</h3>
        </div>
      </div>
      <el-table :data="funnel.channelConversions || []" height="300">
        <el-table-column prop="channel" label="渠道" />
        <el-table-column prop="total" label="总数" width="80" />
        <el-table-column prop="sqlCount" label="SQL" width="80" />
        <el-table-column label="转化率" width="100">
          <template #default="{ row }">{{ row.sqlRate }}%</template>
        </el-table-column>
      </el-table>
    </section>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  funnel: { type: Object, required: true },
  loading: { type: Boolean, default: false }
})

const statusOptions = [
  { label: '新线索', value: 'NEW' },
  { label: '待外呼', value: 'PENDING_CALL' },
  { label: '已接通', value: 'CALLED_CONNECTED' },
  { label: '未接通', value: 'CALLED_NOT_CONNECTED' },
  { label: '有效', value: 'VALID' },
  { label: '无效', value: 'INVALID' },
  { label: '待加微', value: 'PENDING_WECHAT' },
  { label: '已加微', value: 'WECHAT_ADDED' },
  { label: '加微失败', value: 'WECHAT_FAILED' },
  { label: '跟进中', value: 'FOLLOWING' },
  { label: 'MQL', value: 'MQL' },
  { label: 'SQL', value: 'SQL' }
]

const metricCards = computed(() => [
  { label: '总线索', value: props.funnel.total || 0, caption: '当前线索池', tone: 'tone-slate', loading: props.loading },
  { label: '有效率', value: `${props.funnel.validRate || 0}%`, caption: '清洗后有效', tone: 'tone-emerald' },
  { label: '加微率', value: `${props.funnel.wechatRate || 0}%`, caption: '私域承接', tone: 'tone-amber' },
  { label: 'SQL 转化率', value: `${props.funnel.sqlRate || 0}%`, caption: '销售商机', tone: 'tone-cyan' }
])

const funnelItems = computed(() => {
  const counts = props.funnel.counts || {}
  const max = Math.max(1, ...Object.values(counts))
  return statusOptions.map((item) => ({
    ...item,
    key: item.value,
    count: counts[item.value] || 0,
    width: Math.max(6, ((counts[item.value] || 0) / max) * 100)
  }))
})
</script>
