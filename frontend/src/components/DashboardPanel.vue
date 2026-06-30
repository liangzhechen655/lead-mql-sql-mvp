<template>
  <section class="dashboard-grid">
    <div class="metric" v-loading="loading">
      <span>总线索</span>
      <strong>{{ funnel.total || 0 }}</strong>
    </div>
    <div class="metric">
      <span>有效率</span>
      <strong>{{ funnel.validRate || 0 }}%</strong>
    </div>
    <div class="metric">
      <span>加微率</span>
      <strong>{{ funnel.wechatRate || 0 }}%</strong>
    </div>
    <div class="metric">
      <span>SQL 转化率</span>
      <strong>{{ funnel.sqlRate || 0 }}%</strong>
    </div>

    <section class="panel wide">
      <h3>状态漏斗</h3>
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
      <h3>渠道 SQL 转化</h3>
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
