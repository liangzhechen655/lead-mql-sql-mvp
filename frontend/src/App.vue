<template>
  <el-config-provider>
    <main class="app-shell">
      <aside class="sidebar">
        <div class="brand">
          <div class="brand-mark">M</div>
          <div>
            <h1>MQL/SQL 线索流转</h1>
            <p>市场线索清洗与销售承接 MVP</p>
          </div>
        </div>

        <nav class="side-nav">
          <button
            v-for="item in tabOptions"
            :key="item.value"
            :class="{ active: activeTab === item.value }"
            type="button"
            @click="activeTab = item.value"
          >
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.label }}</span>
          </button>
        </nav>

        <section class="role-panel">
          <label>演示角色</label>
          <el-select v-model="roleMode" @change="refreshAll">
            <el-option label="主管：查看全部线索" value="MANAGER" />
            <el-option label="销售张三：仅看自己线索" value="SALES_ZHANG" />
            <el-option label="销售李四：仅看自己线索" value="SALES_LI" />
          </el-select>
        </section>

        <section class="sidebar-insight">
          <span>当前视角</span>
          <strong>{{ currentUser?.name || '加载中' }}</strong>
          <p>{{ roleMode === 'MANAGER' ? '主管全量漏斗' : '销售个人线索池' }}</p>
        </section>
      </aside>

      <section class="workspace">
        <header class="topbar">
          <div class="topbar-title">
            <span>LeadOps Console</span>
            <h2>{{ currentTitle }}</h2>
            <p>{{ currentSubtitle }}</p>
          </div>
          <div class="topbar-actions">
            <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="leadDialogVisible = true">新增线索</el-button>
          </div>
        </header>

        <section class="topbar-stats">
          <div v-for="item in quickStats" :key="item.label" class="stat-pill">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </section>

        <section v-if="activeTab === 'leads'" class="panel lead-panel">
          <div class="toolbar">
            <div class="toolbar-left">
              <el-input v-model="filters.keyword" placeholder="搜索姓名、手机号、渠道" clearable :prefix-icon="Search" @keyup.enter="loadLeads" />
              <el-select v-model="filters.status" placeholder="状态" clearable>
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
              <el-select v-model="filters.source" placeholder="来源" clearable>
                <el-option v-for="item in sourceOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </div>
            <div class="toolbar-right">
              <el-button :icon="Filter" @click="loadLeads">筛选</el-button>
              <el-upload :show-file-list="false" accept=".csv" :before-upload="importCsv">
                <el-button :icon="Upload">导入 CSV</el-button>
              </el-upload>
            </div>
          </div>

          <el-table :data="leads" v-loading="loading.leads" height="560" @row-click="openDetail">
            <el-table-column prop="customerName" label="客户" min-width="100" />
            <el-table-column prop="phone" label="手机号" min-width="118" />
            <el-table-column prop="source" label="来源" min-width="96" />
            <el-table-column prop="channel" label="渠道" min-width="118" />
            <el-table-column label="状态" min-width="96">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" effect="light">{{ row.statusLabel }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ownerName" label="负责人" min-width="90">
              <template #default="{ row }">{{ row.ownerName || '未分配' }}</template>
            </el-table-column>
            <el-table-column prop="lastFollowUpAt" label="最近跟进" min-width="132">
              <template #default="{ row }">{{ formatTime(row.lastFollowUpAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="228">
              <template #default="{ row }">
                <el-button size="small" :icon="User" :disabled="roleMode !== 'MANAGER'" @click.stop="openAssignDialog(row)">分配</el-button>
                <el-button size="small" :icon="EditPen" @click.stop="openStatusDialog(row)">状态</el-button>
                <el-button size="small" type="primary" :icon="ChatLineSquare" @click.stop="openFollowDialog(row)">跟进</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <DashboardPanel v-if="activeTab === 'dashboard'" :funnel="funnel" :loading="loading.dashboard" />

        <AnomalyPanel
          v-if="activeTab === 'anomalies'"
          :anomalies="anomalies"
          :loading="loading.anomalies"
          :format-time="formatTime"
          @refresh="loadAnomalies"
          @open-detail="openDetail"
        />

        <QueryPanel v-if="activeTab === 'query'" :funnel="funnel" :anomalies="anomalies" />

        <section v-if="activeTab === 'callbacks'" class="panel callback-grid">
          <div>
            <h3>模拟外呼系统回调</h3>
            <el-form label-width="100px">
              <el-form-item label="线索">
                <el-select v-model="callForm.leadId" filterable placeholder="选择线索">
                  <el-option v-for="lead in leads" :key="lead.id" :label="`${lead.customerName} ${lead.phone}`" :value="lead.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="是否接通">
                <el-switch v-model="callForm.connected" />
              </el-form-item>
              <el-form-item label="是否有效">
                <el-switch v-model="callForm.valid" />
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="callForm.rawResult" />
              </el-form-item>
              <el-button type="primary" :icon="Phone" @click="sendCallCallback">发送外呼回调</el-button>
            </el-form>
          </div>

          <div>
            <h3>模拟企微/SCRM 回调</h3>
            <el-form label-width="100px">
              <el-form-item label="线索">
                <el-select v-model="wechatForm.leadId" filterable placeholder="选择线索">
                  <el-option v-for="lead in leads" :key="lead.id" :label="`${lead.customerName} ${lead.phone}`" :value="lead.id" />
                </el-select>
              </el-form-item>
              <el-form-item label="加微成功">
                <el-switch v-model="wechatForm.added" />
              </el-form-item>
              <el-form-item label="企微ID">
                <el-input v-model="wechatForm.externalUserId" />
              </el-form-item>
              <el-form-item label="失败原因">
                <el-input v-model="wechatForm.failedReason" />
              </el-form-item>
              <el-button type="primary" :icon="Connection" @click="sendWechatCallback">发送加微回调</el-button>
            </el-form>
          </div>
        </section>
      </section>
    </main>

    <el-drawer v-model="detailVisible" size="46%" :title="detail?.lead?.customerName || '线索详情'">
      <template v-if="detail">
        <div class="detail-summary">
          <el-tag :type="statusType(detail.lead.status)">{{ detail.lead.statusLabel }}</el-tag>
          <span>{{ detail.lead.phone }}</span>
          <span>{{ detail.lead.source }} / {{ detail.lead.channel }}</span>
          <span>负责人：{{ detail.lead.ownerName || '未分配' }}</span>
        </div>
        <el-tabs>
          <el-tab-pane label="跟进记录">
            <el-timeline>
              <el-timeline-item v-for="item in detail.followUps" :key="item.id" :timestamp="formatTime(item.createdAt)">
                <b>{{ item.operatorName }} · {{ item.method }}</b>
                <p>{{ item.content }}</p>
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
          <el-tab-pane label="状态历史">
            <el-timeline>
              <el-timeline-item v-for="item in detail.statusHistory" :key="item.id" :timestamp="formatTime(item.changedAt)">
                {{ item.fromStatus || 'INIT' }} → {{ item.toStatus }}，{{ item.operatorName }}：{{ item.reason }}
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
          <el-tab-pane label="审计日志">
            <el-timeline>
              <el-timeline-item v-for="item in detail.operationLogs" :key="item.id" :timestamp="formatTime(item.createdAt)">
                {{ item.action }} / {{ item.operatorName }} / {{ item.detail }}
              </el-timeline-item>
            </el-timeline>
          </el-tab-pane>
        </el-tabs>
      </template>
    </el-drawer>

    <el-dialog v-model="leadDialogVisible" title="新增线索" width="520px">
      <el-form label-width="90px">
        <el-form-item label="客户姓名"><el-input v-model="leadForm.customerName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="leadForm.phone" /></el-form-item>
        <el-form-item label="来源"><el-input v-model="leadForm.source" placeholder="百度投放/抖音投放/官网表单" /></el-form-item>
        <el-form-item label="渠道"><el-input v-model="leadForm.channel" /></el-form-item>
        <el-form-item label="等级">
          <el-select v-model="leadForm.grade">
            <el-option label="A" value="A" />
            <el-option label="B" value="B" />
            <el-option label="C" value="C" />
            <el-option label="未知" value="UNKNOWN" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="leadForm.remark" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="leadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createLead">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statusDialogVisible" title="修改线索状态" width="520px">
      <el-form label-width="90px">
        <el-form-item label="当前状态">
          <el-tag :type="statusType(selectedLead?.status)">{{ selectedLead?.statusLabel || '-' }}</el-tag>
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="statusForm.status" placeholder="请选择可流转状态">
            <el-option v-for="item in availableStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因"><el-input v-model="statusForm.reason" /></el-form-item>
        <el-form-item label="无效原因"><el-input v-model="statusForm.invalidReason" placeholder="仅无效/失败时填写" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="updateStatus">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="分配负责人" width="520px">
      <el-form label-width="90px">
        <el-form-item label="线索">
          <el-input :model-value="selectedLead ? `${selectedLead.customerName} / ${selectedLead.phone}` : ''" disabled />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="assignForm.ownerId" placeholder="请选择销售负责人">
            <el-option
              v-for="user in salesUsers.filter((item) => item.role === 'SALES')"
              :key="user.id"
              :label="`${user.name}（${user.team || '销售组'}）`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="assignLead">保存分配</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="followDialogVisible" title="新增跟进记录" width="560px">
      <el-form label-width="90px">
        <el-form-item label="方式">
          <el-select v-model="followForm.method">
            <el-option label="电话" value="电话" />
            <el-option label="企业微信" value="企业微信" />
            <el-option label="短信" value="短信" />
            <el-option label="线下拜访" value="线下拜访" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="followForm.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="followDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="addFollowUp">保存</el-button>
      </template>
    </el-dialog>
  </el-config-provider>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatLineSquare,
  Connection,
  EditPen,
  Filter,
  Phone,
  Plus,
  Refresh,
  Search,
  Upload,
  User
} from '@element-plus/icons-vue'
import { api, errorMessage, setCurrentUserId } from './api'
import AnomalyPanel from './components/AnomalyPanel.vue'
import DashboardPanel from './components/DashboardPanel.vue'
import QueryPanel from './components/QueryPanel.vue'

const activeTab = ref('leads')
const roleMode = ref('MANAGER')
const leads = ref([])
const salesUsers = ref([])
const funnel = ref({})
const anomalies = ref([])
const detail = ref(null)
const detailVisible = ref(false)
const leadDialogVisible = ref(false)
const statusDialogVisible = ref(false)
const followDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const selectedLead = ref(null)

const loading = reactive({ leads: false, dashboard: false, anomalies: false })
const filters = reactive({ keyword: '', status: '', source: '' })

const leadForm = reactive({ customerName: '', phone: '', source: '', channel: '', grade: 'UNKNOWN', remark: '' })
const statusForm = reactive({ status: '', reason: '', invalidReason: '' })
const followForm = reactive({ method: '电话', content: '' })
const assignForm = reactive({ ownerId: null })
const callForm = reactive({ leadId: null, connected: true, valid: true, rawResult: '客户接通且表达兴趣' })
const wechatForm = reactive({ leadId: null, added: true, externalUserId: 'wm_demo_001', failedReason: '' })

const tabOptions = [
  { label: '线索工作台', value: 'leads', icon: 'List' },
  { label: '漏斗看板', value: 'dashboard', icon: 'DataAnalysis' },
  { label: '异常提醒', value: 'anomalies', icon: 'Warning' },
  { label: '自然语言查询', value: 'query', icon: 'Promotion' },
  { label: '三方回调', value: 'callbacks', icon: 'Connection' }
]

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

const transitionMap = {
  NEW: ['PENDING_CALL', 'VALID', 'CALLED_NOT_CONNECTED', 'INVALID'],
  PENDING_CALL: ['CALLED_CONNECTED', 'CALLED_NOT_CONNECTED', 'VALID', 'INVALID'],
  CALLED_CONNECTED: ['VALID', 'INVALID'],
  CALLED_NOT_CONNECTED: ['PENDING_CALL', 'INVALID'],
  VALID: ['PENDING_WECHAT', 'WECHAT_ADDED', 'WECHAT_FAILED', 'FOLLOWING', 'INVALID'],
  INVALID: [],
  PENDING_WECHAT: ['WECHAT_ADDED', 'WECHAT_FAILED'],
  WECHAT_ADDED: ['FOLLOWING'],
  WECHAT_FAILED: ['PENDING_WECHAT', 'INVALID'],
  FOLLOWING: ['MQL', 'INVALID'],
  MQL: ['SQL', 'FOLLOWING'],
  SQL: []
}


const currentTitle = computed(() => tabOptions.find((t) => t.value === activeTab.value)?.label || '')
const currentSubtitle = computed(() => {
  const map = {
    leads: '从线索进入、清洗、分配、跟进到 MQL/SQL 的主流程。',
    dashboard: '用漏斗和渠道转化率观察业务效果。',
    anomalies: '定位有效但超时未跟进的高风险线索。',
    query: '用规则版自然语言查询展示 Agent/Data Query 潜力。',
    callbacks: '模拟外呼系统和企微/SCRM 的状态回传。'
  }
  return map[activeTab.value]
})

const sourceOptions = computed(() => [...new Set(leads.value.map((item) => item.source).filter(Boolean))])
const availableStatusOptions = computed(() => {
  if (!selectedLead.value) return statusOptions
  const allowed = transitionMap[selectedLead.value.status] || []
  const options = statusOptions.filter((item) => allowed.includes(item.value))
  return options.length ? options : statusOptions.filter((item) => item.value === selectedLead.value.status)
})
const quickStats = computed(() => [
  { label: '总线索', value: funnel.value.total ?? leads.value.length },
  { label: '有效率', value: `${funnel.value.validRate || 0}%` },
  { label: 'SQL 转化', value: `${funnel.value.sqlRate || 0}%` },
  { label: '异常线索', value: anomalies.value.length }
])
const activeSalesUser = computed(() => {
  if (roleMode.value === 'SALES_ZHANG') return salesUsers.value.find((u) => u.name === '张三')
  if (roleMode.value === 'SALES_LI') return salesUsers.value.find((u) => u.name === '李四')
  return null
})
const currentUser = computed(() => {
  if (roleMode.value === 'MANAGER') return salesUsers.value.find((u) => u.role === 'MANAGER')
  return activeSalesUser.value
})

async function refreshAll() {
  await loadUsers()
  setCurrentUserId(currentUser.value?.id)
  await Promise.all([loadLeads(), loadDashboard(), loadAnomalies()])
}

async function loadUsers() {
  salesUsers.value = await api.salesUsers()
}

async function loadLeads() {
  loading.leads = true
  try {
    setCurrentUserId(currentUser.value?.id)
    const params = { ...filters }
    leads.value = await api.leads(params)
    const callableLead = leads.value.find((lead) => ['NEW', 'PENDING_CALL'].includes(lead.status))
    const wechatReadyLead = leads.value.find((lead) => ['VALID', 'PENDING_WECHAT'].includes(lead.status))
    if (!callForm.leadId && (callableLead || leads.value[0])) callForm.leadId = (callableLead || leads.value[0]).id
    if (!wechatForm.leadId && (wechatReadyLead || leads.value[0])) wechatForm.leadId = (wechatReadyLead || leads.value[0]).id
  } catch (error) {
    ElMessage.error(errorMessage(error))
  } finally {
    loading.leads = false
  }
}

async function loadDashboard() {
  loading.dashboard = true
  try {
    funnel.value = await api.funnel()
  } finally {
    loading.dashboard = false
  }
}

async function loadAnomalies() {
  loading.anomalies = true
  try {
    anomalies.value = await api.anomalies()
  } finally {
    loading.anomalies = false
  }
}

async function createLead() {
  try {
    await api.createLead({ ...leadForm })
    ElMessage.success('线索已新增')
    leadDialogVisible.value = false
    Object.assign(leadForm, { customerName: '', phone: '', source: '', channel: '', grade: 'UNKNOWN', remark: '' })
    refreshAll()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

async function importCsv(file) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('operatorName', roleMode.value === 'MANAGER' ? '王主管' : activeSalesUser.value?.name || '系统')
  try {
    const result = await api.importLeads(formData)
    ElMessage.success(`导入完成：新增 ${result.insertedRows}，重复 ${result.duplicateRows}，失败 ${result.failedRows}`)
    refreshAll()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
  return false
}

async function openDetail(row) {
  try {
    setCurrentUserId(currentUser.value?.id)
    detail.value = await api.leadDetail(row.id)
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

function openStatusDialog(row) {
  selectedLead.value = row
  Object.assign(statusForm, { status: '', reason: '', invalidReason: '' })
  statusDialogVisible.value = true
}

async function updateStatus() {
  if (!statusForm.status) {
    ElMessage.warning('请选择目标状态')
    return
  }
  try {
    await api.updateStatus(selectedLead.value.id, {
      ...statusForm,
      operatorName: roleMode.value === 'MANAGER' ? '王主管' : activeSalesUser.value?.name || '系统'
    })
    ElMessage.success('状态已更新')
    statusDialogVisible.value = false
    refreshAll()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

function openAssignDialog(row) {
  selectedLead.value = row
  assignForm.ownerId = row.ownerId || null
  assignDialogVisible.value = true
}

async function assignLead() {
  const target = salesUsers.value.find((u) => u.id === assignForm.ownerId)
  if (!target) return
  try {
    await api.assign(selectedLead.value.id, { ownerId: target.id, operatorName: '王主管' })
    ElMessage.success(`已分配给 ${target.name}`)
    assignDialogVisible.value = false
    refreshAll()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

function openFollowDialog(row) {
  selectedLead.value = row
  Object.assign(followForm, { method: '电话', content: '' })
  followDialogVisible.value = true
}

async function addFollowUp() {
  try {
    await api.addFollowUp(selectedLead.value.id, {
      ...followForm,
      operatorId: selectedLead.value.ownerId || activeSalesUser.value?.id
    })
    ElMessage.success('跟进记录已保存')
    followDialogVisible.value = false
    refreshAll()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

async function sendCallCallback() {
  try {
    await api.callCallback({
      leadId: callForm.leadId,
      connected: callForm.connected,
      valid: callForm.valid,
      rawResult: callForm.rawResult,
      invalidReason: '客户无需求或空号'
    })
    ElMessage.success('外呼回调已写入')
    refreshAll()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

async function sendWechatCallback() {
  try {
    await api.wechatCallback({ ...wechatForm })
    ElMessage.success('加微回调已写入')
    refreshAll()
  } catch (error) {
    ElMessage.error(errorMessage(error))
  }
}

function statusType(status) {
  if (status === 'SQL') return 'success'
  if (status === 'MQL' || status === 'FOLLOWING') return 'primary'
  if (status === 'INVALID' || status === 'WECHAT_FAILED') return 'danger'
  if (status === 'VALID' || status === 'WECHAT_ADDED') return 'warning'
  return 'info'
}

function formatTime(value) {
  if (!value) return '暂无'
  return value.replace('T', ' ').slice(0, 16)
}

onMounted(refreshAll)
</script>
