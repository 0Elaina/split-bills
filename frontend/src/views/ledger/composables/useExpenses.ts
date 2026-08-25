import { ref, computed } from 'vue'
import { getExpenses, deleteExpense, type ExpenseListItemVO } from '@/shared/api/expense'

export function useExpenses(ledgerId: string) {
  const expenses = ref<ExpenseListItemVO[]>([])
  
  const fetchExpenses = async () => {
    const expensesData = await getExpenses(ledgerId, 1, 100)
    expenses.value = expensesData.records || []
  }

  const totalExpenseAmount = computed(() => {
    return expenses.value.reduce((sum, item) => sum + parseFloat(item.amount), 0).toFixed(2)
  })

  // 记一笔弹窗状态
  const expenseDialog = ref(false)
  const expenseEditing = ref<ExpenseListItemVO | null>(null)

  const openExpenseDialog = () => {
    expenseEditing.value = null
    expenseDialog.value = true
  }

  const editExpense = (item: ExpenseListItemVO) => {
    expenseEditing.value = item
    expenseDialog.value = true
  }

  const confirmDeleteExpense = async (id: string) => {
    if (!confirm('确定要删除这笔消费吗？')) return
    await deleteExpense(ledgerId, id)
    await fetchExpenses()
  }

  const onExpenseSuccess = async () => {
    await fetchExpenses()
  }

  return {
    expenses,
    fetchExpenses,
    totalExpenseAmount,
    // Dialog
    expenseDialog,
    expenseEditing,
    openExpenseDialog,
    editExpense,
    confirmDeleteExpense,
    onExpenseSuccess
  }
}
