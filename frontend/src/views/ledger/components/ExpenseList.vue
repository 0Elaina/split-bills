<script setup lang="ts">
import type { ExpenseListItemVO } from '@/shared/api/expense'

defineProps<{
  expenses: ExpenseListItemVO[]
}>()

const emit = defineEmits<{
  (e: 'edit', expense: ExpenseListItemVO): void
  (e: 'delete', id: string): void
}>()
</script>

<template>
  <div class="flex flex-col gap-md">
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
              <v-list-item @click="emit('edit', expense)" class="hover:bg-surface-container-high transition-colors cursor-pointer">
                <template v-slot:prepend>
                  <span class="material-symbols-outlined mr-2 text-[18px]">edit</span>
                </template>
                <v-list-item-title class="font-label-lg text-label-lg">编辑</v-list-item-title>
              </v-list-item>
              <v-list-item @click="emit('delete', expense.id)" class="hover:bg-error-container text-error transition-colors cursor-pointer">
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
</template>

<style scoped>
.surface-card {
  background-color: #FFFDF8;
  border: 1px solid #E5E2D9;
  box-shadow: 0px 2px 8px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.2s ease-in-out;
}
.surface-card:hover {
  box-shadow: 0px 4px 12px rgba(0, 0, 0, 0.08);
}
.expense-divider:not(:last-child) {
  border-bottom: 1px solid #E5E2D9;
}
</style>
