<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { createExpense, updateExpense, type ExpenseSaveDTO, type ExpenseListItemVO } from '@/shared/api/expense'
import type { MemberVO } from '@/shared/api/member'

// 定义输入
const props = defineProps<{
  modelValue: boolean // 用于 v-model 控制弹窗开关
  ledgerId: string // 当前账本 ID
  members: MemberVO[] // 供下拉框和多选使用的成员列表
  editingExpense: ExpenseListItemVO | null // 修改模式的数据源
}>()

// 定义输出
const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void // 成功后通知外部刷新
}>()

const expenseSubmitting = ref(false)

// 表单状态
const expenseFormData = ref<ExpenseSaveDTO>({
  title: '',
  amount: '',
  expenseDate: new Date().toISOString().substring(0, 10),
  payerMemberId: '',
  participantMemberIds: []
})

// 计算属性：判断当前是新增还是修改
const isEditing = computed(() => !!props.editingExpense)

// 监听弹窗打开，回填数据或初始化
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    if (props.editingExpense) {
      expenseFormData.value = {
        title: props.editingExpense.title,
        amount: props.editingExpense.amount,
        expenseDate: props.editingExpense.expenseDate.substring(0, 10),
        payerMemberId: props.editingExpense.payer.id,
        participantMemberIds: props.editingExpense.participants.map(p => p.id)
      }
    } else {
      expenseFormData.value = {
        title: '',
        amount: '',
        expenseDate: new Date().toISOString().substring(0, 10),
        payerMemberId: '',
        participantMemberIds: []
      }
    }
  }
})

// 提交表单
const onSubmitExpense = async () => {
  if (expenseFormData.value.participantMemberIds.length === 0) return
  
  try {
    expenseSubmitting.value = true
    if (isEditing.value && props.editingExpense) {
      await updateExpense(props.ledgerId, props.editingExpense.id, expenseFormData.value)
    } else {
      await createExpense(props.ledgerId, expenseFormData.value)
    }
    // 关闭弹窗并通知父组件刷新
    emit('update:modelValue', false)
    emit('success')
  } finally {
    expenseSubmitting.value = false
  }
}
</script>

<template>
  <v-dialog :model-value="modelValue" @update:model-value="emit('update:modelValue', $event)" max-width="640" transition="dialog-bottom-transition">
    <form @submit.prevent="onSubmitExpense">
      <div class="bg-surface rounded-xl shadow-[0px_8px_24px_rgba(0,0,0,0.12)] flex flex-col border border-surface-variant overflow-hidden">
        <!-- Header -->
        <div class="px-md py-md border-b border-outline-variant flex items-center justify-between bg-surface">
          <h2 class="font-headline-md text-headline-md text-on-surface">{{ isEditing ? '修改消费' : '记一笔消费' }}</h2>
          <button type="button" @click="emit('update:modelValue', false)" class="text-on-surface-variant hover:text-on-surface transition-colors p-xs rounded-full hover:bg-surface-container-high focus:outline-none">
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
          <button type="button" @click="emit('update:modelValue', false)" class="px-md py-sm font-label-lg text-label-lg text-primary hover:bg-surface-container-highest rounded-lg transition-colors focus:outline-none focus:ring-2 focus:ring-primary-container">
            取消
          </button>
          <button type="submit" :disabled="expenseSubmitting" class="px-md py-sm font-label-lg text-label-lg bg-primary-container text-on-primary rounded-lg hover:bg-surface-tint transition-colors shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-container focus:ring-offset-1 focus:ring-offset-surface flex items-center gap-2">
            <v-progress-circular v-if="expenseSubmitting" indeterminate size="16" width="2"></v-progress-circular>
            {{ isEditing ? '保存修改' : '保存消费' }}
          </button>
        </div>
      </div>
    </form>
  </v-dialog>
</template>
