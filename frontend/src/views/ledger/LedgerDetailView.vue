<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLedger, updateLedger, deleteLedger, type LedgerItem } from '@/shared/api/ledger'
import { getMembers, createMember, updateMember, deleteMember, type MemberVO, type MemberSaveDTO } from '@/shared/api/member'
import { getExpenses, createExpense, updateExpense, deleteExpense, type ExpenseListItemVO, type ExpenseSaveDTO } from '@/shared/api/expense'

const route = useRoute()
const router = useRouter()
const ledgerId = route.params.id as string

const ledger = ref<LedgerItem | null>(null)
const loading = ref(true)

// 成员状态
const members = ref<MemberVO[]>([])
const memberDialog = ref(false)
const memberSubmitting = ref(false)
const memberFormData = ref<MemberSaveDTO>({ name: '' })

// 消费状态
const expenses = ref<ExpenseListItemVO[]>([])
const expenseDialog = ref(false)
const expenseSubmitting = ref(false)
const expenseForm = ref<any>(null)
const expenseFormData = ref<ExpenseSaveDTO>({
  title: '',
  amount: '',
  expenseDate: new Date().toISOString().substring(0, 10), // 默认今天
  payerMemberId: '',
  participantMemberIds: []
})

// 表单金额正则验证规则
const amountRules = [
  (v: string) => !!v || '金额不能为空',
  (v: string) => /^[1-9]\d*(\.\d{1,2})?$|^0\.\d{1,2}$/.test(v) || '金额格式不正确，必须大于 0 且最多两位小数'
]


const totalExpenseAmount = computed(() => {
  return expenses.value.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)
})

// 颜色映射池 (应对设计原型的甲乙丙颜色分配)
const colorClasses = [
  'bg-primary text-on-primary',
  'bg-secondary text-on-secondary',
  'bg-tertiary text-on-tertiary'
]

const getMemberColorClass = (index: number) => {
  return colorClasses[index % colorClasses.length]
}

// 辅助方法：通过 memberId 获取对应颜色，保持同一成员颜色一致
const getMemberColorById = (id: string) => {
  const index = members.value.findIndex(m => String(m.id) === String(id))
  return getMemberColorClass(index >= 0 ? index : 0)
}

// 修改名称弹窗状态
const editDialog = ref(false)
const editName = ref('')
const submitting = ref(false)

// 删除确认弹窗状态
const deleteDialog = ref(false)
const deleting = ref(false)

// 初始化获取数据
const fetchDetail = async () => {
  try {
    loading.value = true
    const [ledgerData, membersData, expensesData] = await Promise.all([
      getLedger(ledgerId),
      getMembers(ledgerId),
      getExpenses(ledgerId, 1, 100) // 暂取前100条
    ])
    ledger.value = ledgerData
    members.value = membersData
    expenses.value = expensesData.records || []
  } catch (error) {
    // 异常由全局拦截器处理
    router.replace('/') // 找不到则退回首页
  } finally {
    loading.value = false
  }
}

// 刷新成员
const refreshMembers = async () => {
  members.value = await getMembers(ledgerId)
}

// 提交新增成员
const onSubmitMember = async () => {
  if (!memberFormData.value.name.trim()) return
  try {
    memberSubmitting.value = true
    await createMember(ledgerId, memberFormData.value)
    memberDialog.value = false
    memberFormData.value.name = ''
    await refreshMembers()
  } finally {
    memberSubmitting.value = false
  }
}

// 刷新消费列表
const refreshExpenses = async () => {
  const expensesData = await getExpenses(ledgerId, 1, 100)
  expenses.value = expensesData.records || []
}

const expenseEditingId = ref<string>('')

// 打开记一笔弹窗 (新增)
const openExpenseDialog = () => {
  expenseEditingId.value = ''
  expenseFormData.value = {
    title: '',
    amount: '',
    expenseDate: new Date().toISOString().substring(0, 10),
    payerMemberId: '',
    participantMemberIds: []
  }
  expenseDialog.value = true
}

// 打开记一笔弹窗 (编辑)
const editExpense = (item: ExpenseListItemVO) => {
  expenseEditingId.value = item.id
  expenseFormData.value = {
    title: item.title,
    amount: item.amount,
    expenseDate: item.expenseDate.substring(0, 10),
    payerMemberId: item.payer.id,
    participantMemberIds: item.participants.map(p => p.id)
  }
  expenseDialog.value = true
}

// 删除消费
const confirmDeleteExpense = async (id: string) => {
  if (!confirm('确定要删除这笔消费吗？')) return
  await deleteExpense(ledgerId, id)
  await refreshExpenses()
}

// 提交记一笔 (新增/修改)
const onSubmitExpense = async () => {
  // 检查原生的 participantMemberIds 是否为空
  if (expenseFormData.value.participantMemberIds.length === 0) {
    return
  }
  
  try {
    expenseSubmitting.value = true
    if (expenseEditingId.value) {
      await updateExpense(ledgerId, expenseEditingId.value, expenseFormData.value)
    } else {
      await createExpense(ledgerId, expenseFormData.value)
    }
    expenseDialog.value = false
    await refreshExpenses()
  } finally {
    expenseSubmitting.value = false
  }
}

// 修改成员状态与逻辑
const editMemberDialog = ref(false)
const memberEditing = ref<MemberVO | null>(null)
const memberEditName = ref('')
const memberEditSubmitting = ref(false)

const openEditMember = (member: MemberVO) => {
  memberEditing.value = member
  memberEditName.value = member.name
  editMemberDialog.value = true
}

const onUpdateMember = async () => {
  if (!memberEditName.value.trim() || !memberEditing.value) return
  try {
    memberEditSubmitting.value = true
    await updateMember(ledgerId, memberEditing.value.id, { name: memberEditName.value })
    editMemberDialog.value = false
    await refreshMembers()
  } finally {
    memberEditSubmitting.value = false
  }
}

// 删除成员状态与逻辑
const deleteMemberDialog = ref(false)
const memberDeleting = ref<MemberVO | null>(null)
const memberDeleteSubmitting = ref(false)

const openDeleteMember = (member: MemberVO) => {
  memberDeleting.value = member
  deleteMemberDialog.value = true
}

const confirmDeleteMember = async () => {
  if (!memberDeleting.value) return
  try {
    memberDeleteSubmitting.value = true
    await deleteMember(ledgerId, memberDeleting.value.id)
    deleteMemberDialog.value = false
    await refreshMembers()
  } finally {
    memberDeleteSubmitting.value = false
  }
}

// 提交修改名称
const onUpdateName = async () => {
  if (!editName.value.trim()) return
  try {
    submitting.value = true
    const updated = await updateLedger(ledgerId, { name: editName.value })
    ledger.value = updated
    editDialog.value = false
  } finally {
    submitting.value = false
  }
}

// 触发修改弹窗
const openEditDialog = () => {
  if (ledger.value) {
    editName.value = ledger.value.name
    editDialog.value = true
  }
}

// 确认删除
const confirmDelete = async () => {
  try {
    deleting.value = true
    await deleteLedger(ledgerId)
    deleteDialog.value = false
    router.replace('/') // 删除成功，返回列表
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div
    class="text-on-background min-h-screen pb-xl flex flex-col font-body-md antialiased selection:bg-primary-container selection:text-on-primary-container"
  >
    <!-- 加载中 -->
    <main v-if="loading" class="flex-grow flex items-center justify-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
    </main>

    <template v-else-if="ledger">
      <!-- Top Navigation (Contextual) -->
      <header
        class="bg-background w-full sticky top-0 z-50 flat no-shadows border-b border-surface-variant"
      >
        <div class="flex flex-col max-w-container-max mx-auto px-lg py-md w-full gap-sm">
          <div class="flex justify-between items-center w-full">
            <div class="flex items-center gap-md">
              <button
                @click="router.push('/')"
                class="text-on-surface-variant hover:text-primary transition-colors flex items-center justify-center rounded-full p-2 hover:bg-surface-container-high"
              >
                <span class="material-symbols-outlined">arrow_back</span>
              </button>
              <h1 class="font-headline-lg text-headline-lg text-on-surface">{{ ledger.name }}</h1>
            </div>
            <div class="flex items-center gap-md">
              <!-- 更多操作菜单 -->
              <v-menu location="bottom end">
                <template v-slot:activator="{ props }">
                  <button
                    v-bind="props"
                    class="text-on-surface-variant hover:text-primary transition-colors flex items-center justify-center rounded-full p-2 hover:bg-surface-container-high"
                  >
                    <span class="material-symbols-outlined">more_vert</span>
                  </button>
                </template>
                <v-list>
                  <v-list-item
                    prepend-icon="mdi-pencil"
                    title="修改名称"
                    @click="openEditDialog"
                  ></v-list-item>
                  <v-list-item
                    prepend-icon="mdi-delete"
                    title="删除账本"
                    class="text-error"
                    @click="deleteDialog = true"
                  ></v-list-item>
                </v-list>
              </v-menu>

              <button
                @click="openExpenseDialog"
                class="bg-primary-container text-on-primary font-label-lg text-label-lg py-2 px-6 rounded-lg hover:bg-opacity-90 transition-all shadow-sm flex items-center gap-2"
              >
                <span class="material-symbols-outlined fill">add</span>
                记一笔
              </button>
            </div>
          </div>

          <!-- Summary Badges -->
          <div class="flex items-center gap-sm mt-xs pl-[60px]">
            <div
              class="bg-surface-container-high text-on-surface font-label-sm text-label-sm px-3 py-1.5 rounded-full flex items-center gap-1.5 border border-outline-variant"
            >
              <span class="material-symbols-outlined text-[16px]">account_balance_wallet</span>
              总支出 ¥{{ totalExpenseAmount }}
            </div>
            <div
              class="bg-surface-container-high text-on-surface font-label-sm text-label-sm px-3 py-1.5 rounded-full flex items-center gap-1.5 border border-outline-variant"
            >
              <span class="material-symbols-outlined text-[16px]">group</span>
              {{ members.length }} 位成员
            </div>
            <div
              class="bg-surface-container-high text-on-surface font-label-sm text-label-sm px-3 py-1.5 rounded-full flex items-center gap-1.5 border border-outline-variant"
            >
              <span class="material-symbols-outlined text-[16px]">receipt_long</span>
              {{ expenses.length }} 笔消费
            </div>
          </div>
        </div>
      </header>

      <!-- Main Content Area - Bento Grid -->
      <main class="max-w-container-max mx-auto px-lg mt-xl w-full flex-grow">
        <div class="grid grid-cols-12 gap-gutter">
          <!-- Left Column (8): Expense List -->
          <div class="col-span-12 lg:col-span-8 flex flex-col gap-md">
            <h2 class="font-headline-md text-headline-md text-on-surface mb-xs">消费明细</h2>
            
            <div v-if="expenses.length === 0"
              class="surface-card rounded-xl p-sm flex flex-col min-h-[200px] items-center justify-center text-on-surface-variant"
            >
              暂无消费明细，点击“记一笔”开始记录
            </div>

            <div v-else class="surface-card rounded-xl p-sm flex flex-col">
              <div
                v-for="expense in expenses"
                :key="expense.id"
                class="expense-divider p-sm flex items-center justify-between hover:bg-surface-container-low transition-colors rounded-lg cursor-pointer"
              >
                <div class="flex items-center gap-md">
                  <div class="w-12 h-12 rounded-full bg-secondary-container flex items-center justify-center text-on-secondary-container">
                    <span class="material-symbols-outlined">receipt_long</span>
                  </div>
                  <div class="flex flex-col">
                    <span class="font-body-lg text-body-lg text-on-surface font-medium">{{ expense.title }}</span>
                    <div class="flex items-center gap-2 mt-1">
                      <span class="font-label-sm text-label-sm text-on-surface-variant flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">calendar_today</span> {{ expense.expenseDate }}
                      </span>
                      <span class="font-label-sm text-label-sm text-on-surface-variant flex items-center gap-1">
                        <span class="material-symbols-outlined text-[14px]">person</span> {{ expense.payer?.name }} 支付
                      </span>
                    </div>
                    <div class="flex items-center flex-wrap gap-1 mt-2">
                      <span
                        v-for="participant in expense.participants"
                        :key="participant.id"
                        class="bg-surface-container-highest text-on-surface-variant text-[10px] px-2 py-0.5 rounded"
                      >
                        {{ participant.name }}
                      </span>
                    </div>
                  </div>
                </div>
                <div class="text-right flex items-center gap-sm">
                  <span class="font-headline-md text-headline-md text-on-surface">¥{{ expense.amount }}</span>
                  <v-menu location="bottom end">
                    <template v-slot:activator="{ props }">
                      <button v-bind="props" class="p-2 rounded-full hover:bg-surface-variant text-on-surface-variant transition-colors flex items-center justify-center" @click.stop>
                        <span class="material-symbols-outlined">more_vert</span>
                      </button>
                    </template>
                    <v-list class="bg-surface border border-outline-variant shadow-sm rounded-lg">
                      <v-list-item @click="editExpense(expense)" class="hover:bg-surface-container-high transition-colors cursor-pointer">
                        <template v-slot:prepend>
                          <span class="material-symbols-outlined mr-2 text-[18px]">edit</span>
                        </template>
                        <v-list-item-title class="font-label-lg text-label-lg">编辑</v-list-item-title>
                      </v-list-item>
                      <v-list-item @click="confirmDeleteExpense(expense.id)" class="hover:bg-error-container text-error transition-colors cursor-pointer">
                        <template v-slot:prepend>
                          <span class="material-symbols-outlined mr-2 text-[18px]">delete</span>
                        </template>
                        <v-list-item-title class="font-label-lg text-label-lg">删除</v-list-item-title>
                      </v-list-item>
                    </v-list>
                  </v-menu>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column (4): Members & Settlements -->
          <div class="col-span-12 lg:col-span-4 flex flex-col gap-xl">
            <!-- Members Card -->
            <div class="flex flex-col gap-sm">
              <h2 class="font-headline-md text-headline-md text-on-surface">参与成员</h2>
              <div
                class="bg-white border border-[#E5E2D9] shadow-sm rounded-xl p-md flex flex-wrap gap-4 items-center min-h-[100px]"
              >
                <!-- 渲染已有成员 -->
                <div v-for="(member, index) in members" :key="member.id" class="flex flex-col items-center gap-1">
                  <v-menu location="bottom center">
                    <template v-slot:activator="{ props }">
                      <div
                        v-bind="props"
                        :class="getMemberColorClass(index)"
                        class="w-12 h-12 rounded-full flex items-center justify-center font-headline-md text-headline-md font-bold shadow-sm cursor-pointer hover:opacity-80 transition-opacity"
                        :title="member.name"
                      >
                        {{ member.name.substring(0, 1) }}
                      </div>
                    </template>
                    <v-list>
                      <v-list-item prepend-icon="mdi-pencil" title="修改成员" @click="openEditMember(member)"></v-list-item>
                      <v-list-item prepend-icon="mdi-delete" title="删除成员" class="text-error" @click="openDeleteMember(member)"></v-list-item>
                    </v-list>
                  </v-menu>
                </div>

                <!-- 添加按钮 -->
                <button
                  @click="memberDialog = true"
                  class="w-12 h-12 rounded-full border-2 border-dashed border-outline-variant text-on-surface-variant flex items-center justify-center hover:bg-surface-container hover:text-primary transition-colors flex-shrink-0"
                  title="添加成员"
                >
                  <span class="material-symbols-outlined">add</span>
                </button>
              </div>
            </div>

            <!-- Settlement Card -->
            <div class="flex flex-col gap-sm">
              <h2 class="font-headline-md text-headline-md text-on-surface">结算方案</h2>
              <div
                class="bg-white border border-[#E5E2D9] shadow-sm rounded-xl p-md flex flex-col gap-md min-h-[100px] items-center justify-center text-on-surface-variant"
              >
                工作台占位区域：暂无结算方案
              </div>
            </div>
          </div>
        </div>
      </main>
    </template>

    <!-- Footer Component -->
    <footer
      class="w-full mt-xl bg-surface-container-low dark:bg-inverse-surface border-t border-outline-variant flat no-shadows transition-colors duration-200"
      v-if="!loading"
    >
      <div
        class="flex flex-col md:flex-row justify-between items-center px-lg py-md max-w-container-max mx-auto"
      >
        <div class="mb-4 md:mb-0 text-center md:text-left">
          <span class="font-headline-sm text-headline-sm text-primary block mb-2 font-bold"
            >Split Bills</span
          >
          <span class="text-secondary dark:text-secondary-fixed font-body-md text-body-md"
            >© 2024 Split Bills. Precision in every yen.</span
          >
        </div>
        <nav>
          <ul class="flex flex-wrap justify-center gap-6">
            <li>
              <a
                class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200 font-label-sm text-label-sm"
                href="#"
                >Privacy Policy</a
              >
            </li>
            <li>
              <a
                class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200 font-label-sm text-label-sm"
                href="#"
                >Terms of Service</a
              >
            </li>
            <li>
              <a
                class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200 font-label-sm text-label-sm"
                href="#"
                >Help Center</a
              >
            </li>
          </ul>
        </nav>
      </div>
    </footer>

    <!-- 修改名称弹窗 -->
    <v-dialog v-model="editDialog" max-width="500">
      <v-card>
        <v-card-title class="pt-4 px-6 text-h6 font-weight-bold">修改账本名称</v-card-title>
        <v-card-text class="px-6 pb-2">
          <v-form @submit.prevent="onUpdateName">
            <v-text-field
              v-model="editName"
              label="账本名称"
              variant="outlined"
              color="primary"
              :rules="[(v) => !!v || '不能为空', (v) => v.length <= 50 || '不能超过50个字符']"
              autofocus
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="editDialog = false">取消</v-btn>
          <v-btn color="primary" variant="flat" :loading="submitting" @click="onUpdateName"
            >保存</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除确认弹窗 -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card>
        <v-card-title class="pt-4 px-6 text-h6 font-weight-bold text-error">
          <v-icon color="error" class="mr-2">mdi-alert</v-icon>删除账本
        </v-card-title>
        <v-card-text class="px-6 py-4 text-body-1">
          确定要删除账本 <strong>{{ ledger?.name }}</strong> 吗？<br />
          <span class="text-caption text-grey"
            >注意：该操作不可恢复，删除后所有关联账单将一并清空。</span
          >
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" variant="flat" :loading="deleting" @click="confirmDelete"
            >确认删除</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 添加成员弹窗 -->
    <v-dialog v-model="memberDialog" max-width="500">
      <v-card>
        <v-card-title class="pt-4 px-6 text-h6 font-weight-bold">添加成员</v-card-title>
        <v-card-text class="px-6 pb-2">
          <v-form @submit.prevent="onSubmitMember">
            <v-text-field
              v-model="memberFormData.name"
              label="成员昵称"
              variant="outlined"
              color="primary"
              :rules="[(v) => !!v || '不能为空', (v) => v.length <= 10 || '不能超过10个字符']"
              autofocus
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="memberDialog = false">取消</v-btn>
          <v-btn color="primary" variant="flat" :loading="memberSubmitting" @click="onSubmitMember"
            >确定</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 修改成员弹窗 -->
    <v-dialog v-model="editMemberDialog" max-width="500">
      <v-card>
        <v-card-title class="pt-4 px-6 text-h6 font-weight-bold">修改成员名称</v-card-title>
        <v-card-text class="px-6 pb-2">
          <v-form @submit.prevent="onUpdateMember">
            <v-text-field
              v-model="memberEditName"
              label="成员昵称"
              variant="outlined"
              color="primary"
              :rules="[(v) => !!v || '不能为空', (v) => v.length <= 10 || '不能超过10个字符']"
              autofocus
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="editMemberDialog = false">取消</v-btn>
          <v-btn color="primary" variant="flat" :loading="memberEditSubmitting" @click="onUpdateMember"
            >保存</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除成员确认弹窗 -->
    <v-dialog v-model="deleteMemberDialog" max-width="400">
      <v-card>
        <v-card-title class="pt-4 px-6 text-h6 font-weight-bold text-error">
          <v-icon color="error" class="mr-2">mdi-alert</v-icon>删除成员
        </v-card-title>
        <v-card-text class="px-6 py-4 text-body-1">
          确定要删除成员 <strong>{{ memberDeleting?.name }}</strong> 吗？<br />
          <span class="text-caption text-grey"
            >注意：如果该成员已参与任何账单，将无法被删除。</span
          >
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="deleteMemberDialog = false">取消</v-btn>
          <v-btn color="error" variant="flat" :loading="memberDeleteSubmitting" @click="confirmDeleteMember"
            >确认删除</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>
    <!-- 记一笔消费弹窗 -->
    <v-dialog v-model="expenseDialog" max-width="640" transition="dialog-bottom-transition">
      <form @submit.prevent="onSubmitExpense">
        <div class="bg-surface rounded-xl shadow-[0px_8px_24px_rgba(0,0,0,0.12)] flex flex-col border border-surface-variant overflow-hidden">
          <!-- Header -->
          <div class="px-md py-md border-b border-outline-variant flex items-center justify-between bg-surface">
            <h2 class="font-headline-md text-headline-md text-on-surface">{{ expenseEditingId ? '修改消费' : '记一笔消费' }}</h2>
            <button type="button" @click="expenseDialog = false" class="text-on-surface-variant hover:text-on-surface transition-colors p-xs rounded-full hover:bg-surface-container-high focus:outline-none">
              <span class="material-symbols-outlined">close</span>
            </button>
          </div>
          <!-- Body -->
          <div class="p-md flex flex-col gap-md overflow-y-auto max-h-[70vh]">
            <!-- 消费名称 -->
            <div class="flex flex-col gap-xs">
              <label class="font-label-lg text-label-lg text-on-surface">消费名称</label>
              <input v-model="expenseFormData.title" class="w-full bg-surface-container-lowest border border-outline-variant rounded-lg px-sm py-sm text-on-surface font-body-md text-body-md focus:border-primary-container focus:outline-none focus:ring-1 focus:ring-primary-container transition-colors" placeholder="输入消费名称" type="text" required />
            </div>
            <!-- 金额 & 日期 Row -->
            <div class="grid grid-cols-2 gap-md">
              <!-- 金额 -->
              <div class="flex flex-col gap-xs">
                <label class="font-label-lg text-label-lg text-on-surface">金额</label>
                <div class="relative flex items-center">
                  <span class="absolute left-sm text-on-surface-variant font-body-md text-body-md">¥</span>
                  <input v-model="expenseFormData.amount" class="w-full bg-surface-container-lowest border border-outline-variant rounded-lg pl-[32px] pr-sm py-sm text-on-surface font-body-md text-body-md focus:border-primary-container focus:outline-none focus:ring-1 focus:ring-primary-container transition-colors" placeholder="0.00" type="text" required pattern="^[1-9]\d*(\.\d{1,2})?$|^0\.\d{1,2}$" title="金额格式不正确，必须大于 0 且最多两位小数" />
                </div>
              </div>
              <!-- 消费日期 -->
              <div class="flex flex-col gap-xs">
                <label class="font-label-lg text-label-lg text-on-surface">消费日期</label>
                <div class="relative flex items-center">
                  <input v-model="expenseFormData.expenseDate" class="w-full bg-surface-container-lowest border border-outline-variant rounded-lg px-sm py-sm text-on-surface font-body-md text-body-md focus:border-primary-container focus:outline-none focus:ring-1 focus:ring-primary-container transition-colors" type="date" required />
                </div>
              </div>
            </div>
            <!-- 付款人 -->
            <div class="flex flex-col gap-xs mt-base">
              <label class="font-label-lg text-label-lg text-on-surface">付款人</label>
              <div class="relative">
                <select v-model="expenseFormData.payerMemberId" class="w-full bg-surface-container-lowest border border-outline-variant rounded-lg px-sm py-sm text-on-surface font-body-md text-body-md appearance-none focus:border-primary-container focus:outline-none focus:ring-1 focus:ring-primary-container transition-colors pr-xl" required>
                  <option value="" disabled>请选择付款人</option>
                  <option v-for="m in members" :key="m.id" :value="m.id">{{ m.name }}</option>
                </select>
                <div class="pointer-events-none absolute inset-y-0 right-0 flex items-center px-sm text-on-surface-variant">
                  <span class="material-symbols-outlined">expand_more</span>
                </div>
              </div>
            </div>
            <!-- 参与人 -->
            <div class="flex flex-col gap-xs mt-base">
              <label class="font-label-lg text-label-lg text-on-surface">参与人</label>
              <div class="flex flex-wrap gap-sm items-center">
                <label v-for="m in members" :key="m.id" class="cursor-pointer">
                  <input type="checkbox" :value="m.id" v-model="expenseFormData.participantMemberIds" class="hidden" />
                  <div :class="[
                    'flex items-center gap-xs rounded-full pl-xs pr-sm py-xs border transition-colors',
                    expenseFormData.participantMemberIds.includes(m.id) 
                      ? 'bg-primary-container text-on-primary border-primary-container hover:bg-surface-tint' 
                      : 'bg-surface-container-lowest text-on-surface-variant border-outline-variant hover:border-primary'
                  ]">
                    <div :class="[
                      'w-6 h-6 rounded-full flex items-center justify-center font-label-sm text-label-sm font-bold',
                      expenseFormData.participantMemberIds.includes(m.id)
                        ? 'bg-surface-container-lowest text-primary'
                        : 'bg-surface-variant text-on-surface-variant'
                    ]">{{ m.name.charAt(0) }}</div>
                    <span class="font-label-sm text-label-sm">{{ m.name }}</span>
                    <span v-if="expenseFormData.participantMemberIds.includes(m.id)" class="material-symbols-outlined text-[16px]">close</span>
                    <span v-else class="material-symbols-outlined text-[16px]">add</span>
                  </div>
                </label>
              </div>
            </div>
            <!-- Note Text -->
            <div class="mt-xs">
              <p class="font-label-sm text-label-sm text-on-surface-variant flex items-center gap-xs">
                <span class="material-symbols-outlined text-[16px]">info</span>
                金额将由所选参与人平均分摊
              </p>
              <p v-if="expenseFormData.participantMemberIds.length === 0" class="text-error font-label-sm text-label-sm mt-1">请至少选择一位参与人</p>
            </div>
          </div>
          <!-- Footer Actions -->
          <div class="px-md py-md border-t border-outline-variant bg-surface flex justify-end gap-sm items-center">
            <button type="button" @click="expenseDialog = false" class="px-md py-sm font-label-lg text-label-lg text-primary hover:bg-surface-container-highest rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-primary-container">
              取消
            </button>
            <button type="submit" :disabled="expenseSubmitting" class="px-md py-sm font-label-lg text-label-lg bg-primary-container text-on-primary rounded-lg hover:bg-surface-tint transition-colors shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-container focus:ring-offset-1 focus:ring-offset-surface flex items-center gap-2">
              <v-progress-circular v-if="expenseSubmitting" indeterminate size="16" width="2"></v-progress-circular>
              {{ expenseEditingId ? '保存修改' : '保存消费' }}
            </button>
          </div>
        </div>
      </form>
    </v-dialog>
  </div>
</template>

<style scoped>
.surface-card {
  background-color: #FFFDF8; /* Level 1 Card */
  border: 1px solid #E5E2D9;
  box-shadow: 0px 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease-in-out;
}
.surface-card:hover {
  box-shadow: 0px 4px 12px rgba(0, 0, 0, 0.08); /* Level 2 Hover */
}
.expense-divider:not(:last-child) {
  border-bottom: 1px solid #E5E2D9;
}
</style>
