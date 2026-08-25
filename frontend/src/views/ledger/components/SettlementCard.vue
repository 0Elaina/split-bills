<script setup lang="ts">
import type { SettlementVO } from '@/shared/api/settlement'
import type { MemberVO } from '@/shared/api/member'

const props = defineProps<{
  settlement: SettlementVO | null
  members: MemberVO[]
}>()

// 颜色映射池
const colorClasses = [
  'bg-primary text-on-primary',
  'bg-secondary text-on-secondary',
  'bg-tertiary text-on-tertiary'
]

const getMemberColorClass = (index: number) => {
  return colorClasses[index % colorClasses.length]
}

const getMemberColorById = (id: string) => {
  const index = props.members.findIndex(m => String(m.id) === String(id))
  return getMemberColorClass(index >= 0 ? index : 0)
}
</script>

<template>
  <div class="flex flex-col gap-sm">
    <h2 class="font-headline-md text-headline-md text-on-surface">结算方案</h2>
    <div class="surface-card rounded-xl p-md flex flex-col gap-md min-h-[200px]">
      <template v-if="settlement && (settlement.balances.length > 0 || settlement.transfers.length > 0)">
        <!-- Net Balances -->
        <div class="flex flex-col gap-xs" v-if="settlement.balances.length > 0">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant mb-2">个人净余额</h3>
          <div v-for="bal in settlement.balances" :key="bal.member.id" class="flex justify-between items-center py-1">
            <div class="flex items-center gap-2">
              <div :class="['w-6 h-6 rounded-full flex items-center justify-center font-label-sm text-label-sm font-bold shadow-sm', getMemberColorById(bal.member.id)]">
                {{ bal.member.name.substring(0, 1) }}
              </div>
              <span class="font-body-md text-body-md text-on-surface">{{ bal.member.name }}</span>
            </div>
            <span class="font-body-md text-body-md font-medium" :class="parseFloat(bal.netBalance) > 0 ? 'text-primary-container' : parseFloat(bal.netBalance) < 0 ? 'text-error' : 'text-on-surface-variant'">
              {{ parseFloat(bal.netBalance) > 0 ? '+' : '' }}{{ bal.netBalance === '0.00' ? '' : '¥' }}{{ bal.netBalance === '0.00' ? '已结清' : bal.netBalance }}
            </span>
          </div>
        </div>

        <div class="h-px bg-outline-variant w-full opacity-50" v-if="settlement.transfers.length > 0"></div>

        <!-- Settlement Suggestions -->
        <div class="flex flex-col gap-sm" v-if="settlement.transfers.length > 0">
          <h3 class="font-label-lg text-label-lg text-on-surface-variant mb-1">建议转账</h3>
          <div v-for="(t, idx) in settlement.transfers" :key="idx" class="bg-surface-container-low rounded-lg p-3 border border-outline-variant flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div :class="['w-8 h-8 rounded-full flex items-center justify-center font-label-sm text-label-sm font-bold shadow-sm', getMemberColorById(t.fromMember.id)]">
                {{ t.fromMember.name.substring(0, 1) }}
              </div>
              <span class="material-symbols-outlined text-outline">arrow_forward</span>
              <div :class="['w-8 h-8 rounded-full flex items-center justify-center font-label-sm text-label-sm font-bold shadow-sm', getMemberColorById(t.toMember.id)]">
                {{ t.toMember.name.substring(0, 1) }}
              </div>
            </div>
            <span class="font-body-lg text-body-lg text-on-surface font-medium">¥{{ t.amount }}</span>
          </div>
        </div>
        
        <div v-if="settlement.transfers.length === 0 && settlement.balances.some(b => parseFloat(b.netBalance) !== 0)" class="flex items-center justify-center p-4">
          <span class="text-on-surface-variant text-label-md">无需转账</span>
        </div>
      </template>
      
      <div v-else class="flex-grow flex flex-col items-center justify-center text-center p-6 text-on-surface-variant opacity-70">
        <span class="material-symbols-outlined text-4xl mb-2">account_balance</span>
        <p class="font-label-lg">暂无结算数据</p>
        <p class="text-label-sm mt-1">添加成员并记录消费后即可看到结算建议</p>
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
</style>
