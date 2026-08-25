import { http } from './http'
import type { MemberVO } from './member'

export interface MemberBalanceVO {
    member: MemberVO
    paidAmount: string
    owedAmount: string
    netBalance: string
}

export interface TransferVO {
    fromMember: MemberVO
    toMember: MemberVO
    amount: string
}

export interface SettlementVO {
    balances: MemberBalanceVO[]
    transfers: TransferVO[]
}

/**
 * 获取账单结算结果
 * @param ledgerId 账本 ID
 */
export const getSettlement = (ledgerId: string): Promise<SettlementVO> => {
    return http.get(`/ledgers/${ledgerId}/settlement`).then(res => res.data.data)
}
