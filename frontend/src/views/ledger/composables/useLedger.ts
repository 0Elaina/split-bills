import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getLedger, updateLedger, deleteLedger, type LedgerItem } from '@/shared/api/ledger'

export function useLedger(ledgerId: string) {
  const router = useRouter()
  const ledger = ref<LedgerItem | null>(null)
  
  // 修改名称状态
  const editDialog = ref(false)
  const editName = ref('')
  const submitting = ref(false)

  // 删除状态
  const deleteDialog = ref(false)
  const deleting = ref(false)

  const fetchLedger = async () => {
    try {
      ledger.value = await getLedger(ledgerId)
    } catch (e) {
      router.replace('/')
    }
  }

  const openEditDialog = () => {
    if (ledger.value) {
      editName.value = ledger.value.name
      editDialog.value = true
    }
  }

  const onUpdateName = async () => {
    if (!editName.value.trim()) return
    try {
      submitting.value = true
      ledger.value = await updateLedger(ledgerId, { name: editName.value })
      editDialog.value = false
    } finally {
      submitting.value = false
    }
  }

  const confirmDelete = async () => {
    try {
      deleting.value = true
      await deleteLedger(ledgerId)
      deleteDialog.value = false
      router.replace('/')
    } finally {
      deleting.value = false
    }
  }

  return {
    ledger,
    fetchLedger,
    // Edit
    editDialog,
    editName,
    submitting,
    openEditDialog,
    onUpdateName,
    // Delete
    deleteDialog,
    deleting,
    confirmDelete
  }
}
