<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLedger } from './composables/useLedger'
import { useMembers } from './composables/useMembers'
import { useExpenses } from './composables/useExpenses'

import ExpenseFormDialog from './components/ExpenseFormDialog.vue'
import MemberModals from './components/MemberModals.vue'
import LedgerModals from './components/LedgerModals.vue'

const route = useRoute()
const router = useRouter()
const ledgerId = route.params.id as string

const {
  ledger, fetchLedger,
  editDialog: ledgerEditDialog, editName: ledgerEditName, submitting: ledgerSubmitting,
  openEditDialog: openLedgerEditDialog, onUpdateName: onLedgerUpdateName,
  deleteDialog: ledgerDeleteDialog, deleting: ledgerDeleting, confirmDelete: confirmLedgerDelete
} = useLedger(ledgerId)

const {
  members, fetchMembers,
  memberDialog, memberFormData, memberSubmitting, onSubmitMember,
  editMemberDialog, memberEditing, memberEditName, memberEditSubmitting, openEditMember, onUpdateMember,
  deleteMemberDialog, memberDeleting, memberDeleteSubmitting, openDeleteMember, confirmDeleteMember
} = useMembers(ledgerId)

const {
  expenses, fetchExpenses, totalExpenseAmount,
  expenseDialog, expenseEditing, openExpenseDialog, editExpense, confirmDeleteExpense, onExpenseSuccess
} = useExpenses(ledgerId)

// 颜色映射池
const colorClasses = [
  'bg-primary text-on-primary',
  'bg-secondary text-on-secondary',
  'bg-tertiary text-on-tertiary'
]

const getMemberColorClass = (index: number) => {
  return colorClasses[index % colorClasses.length]
}

const loading = ref(true)

// 初始化获取数据
const fetchDetail = async () => {
  try {
    loading.value = true
    await Promise.all([
      fetchLedger(),
      fetchMembers(),
      fetchExpenses()
    ])
  } finally {
    loading.value = false
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
                    title="修改账本名称"
                    @click="openLedgerEditDialog"
                  ></v-list-item>
                  <v-list-item
                    prepend-icon="mdi-delete"
                    title="删除账本"
                    class="text-error"
                    @click="ledgerDeleteDialog = true"
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

    <!-- 成员管理弹窗组件 -->
    <MemberModals
      v-model:add-dialog="memberDialog"
      v-model:add-form-data="memberFormData"
      :add-submitting="memberSubmitting"
      @submit-add="onSubmitMember"

      v-model:edit-dialog="editMemberDialog"
      v-model:edit-name="memberEditName"
      :edit-submitting="memberEditSubmitting"
      @submit-edit="onUpdateMember"

      v-model:delete-dialog="deleteMemberDialog"
      :deleting-member="memberDeleting"
      :delete-submitting="memberDeleteSubmitting"
      @submit-delete="confirmDeleteMember"
    />

    <!-- 账本管理弹窗组件 -->
    <LedgerModals
      v-model:edit-dialog="ledgerEditDialog"
      v-model:edit-name="ledgerEditName"
      :edit-submitting="ledgerSubmitting"
      @submit-edit="onLedgerUpdateName"

      v-model:delete-dialog="ledgerDeleteDialog"
      :delete-submitting="ledgerDeleting"
      @submit-delete="confirmLedgerDelete"
    />
    <!-- 记一笔消费弹窗组件 -->
    <ExpenseFormDialog
      v-model="expenseDialog"
      :ledger-id="ledgerId"
      :members="members"
      :editing-expense="expenseEditing"
      @success="onExpenseSuccess"
    />
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
