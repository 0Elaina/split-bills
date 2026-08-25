<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useLedger } from './composables/useLedger'
import { useMembers } from './composables/useMembers'
import { useExpenses } from './composables/useExpenses'
import { useSettlement } from './composables/useSettlement'

import ExpenseList from './components/ExpenseList.vue'
import MemberCard from './components/MemberCard.vue'
import SettlementCard from './components/SettlementCard.vue'
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

const { settlement, fetchSettlement } = useSettlement(ledgerId)

const loading = ref(true)

// 初始化获取数据
const fetchDetail = async () => {
  try {
    loading.value = true
    await Promise.all([
      fetchLedger(),
      fetchMembers(),
      fetchExpenses(),
      fetchSettlement()
    ])
  } finally {
    loading.value = false
  }
}

const handleExpenseSuccess = async () => {
  await onExpenseSuccess()
  await fetchSettlement()
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
            <ExpenseList
              :expenses="expenses"
              @edit="editExpense"
              @delete="async (id) => { await confirmDeleteExpense(id); fetchSettlement(); }"
            />
          </div>

          <!-- Right Column (4): Members & Settlements -->
          <div class="col-span-12 lg:col-span-4 flex flex-col gap-xl">
            <!-- Members Card -->
            <MemberCard
              :members="members"
              @add="memberDialog = true"
              @edit="openEditMember"
              @delete="openDeleteMember"
            />

            <!-- Settlement Card -->
            <SettlementCard
              :settlement="settlement"
              :members="members"
            />
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
      @submit-add="async () => { await onSubmitMember(); fetchSettlement(); }"

      v-model:edit-dialog="editMemberDialog"
      v-model:edit-name="memberEditName"
      :edit-submitting="memberEditSubmitting"
      @submit-edit="async () => { await onUpdateMember(); fetchSettlement(); }"

      v-model:delete-dialog="deleteMemberDialog"
      :deleting-member="memberDeleting"
      :delete-submitting="memberDeleteSubmitting"
      @submit-delete="async () => { await confirmDeleteMember(); fetchSettlement(); }"
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
      @success="handleExpenseSuccess"
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
