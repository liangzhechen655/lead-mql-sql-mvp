<template>
  <section class="panel">
    <div class="section-heading">
      <div>
        <span class="eyebrow">Risk Queue</span>
        <h3>异常线索提醒</h3>
        <p>有效线索、待加微线索、已加微线索和 MQL 线索超过 48 小时未跟进会进入此列表。</p>
      </div>
      <el-button :icon="Refresh" @click="$emit('refresh')">刷新异常</el-button>
    </div>
    <div class="anomaly-strip">
      <span>待处理异常 <b>{{ anomalies.length }}</b></span>
      <span>响应 SLA <b>48h</b></span>
      <span>处理建议 <b>优先跟进</b></span>
    </div>
    <el-table :data="anomalies" v-loading="loading" height="560" @row-click="$emit('open-detail', $event)">
      <el-table-column prop="customerName" label="客户" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="statusLabel" label="当前状态" />
      <el-table-column prop="ownerName" label="负责人" />
      <el-table-column label="最近跟进">
        <template #default="{ row }">{{ formatTime(row.lastFollowUpAt) }}</template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup>
import { Refresh } from '@element-plus/icons-vue'

defineProps({
  anomalies: { type: Array, required: true },
  loading: { type: Boolean, default: false },
  formatTime: { type: Function, required: true }
})

defineEmits(['refresh', 'open-detail'])
</script>
