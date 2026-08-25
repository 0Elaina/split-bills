import { ref } from 'vue'
import { getSettlement, type SettlementVO } from '@/shared/api/settlement'

export function useSettlement(ledgerId: string) {
  const settlement = ref<SettlementVO | null>(null)
  
  const fetchSettlement = async () => {
    settlement.value = await getSettlement(ledgerId)
  }

  return {
    settlement,
    fetchSettlement
  }
}
