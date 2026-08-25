import { http } from './http'
import type { MemberVO } from './member'

export interface ExpenseListItemVO {
    id: string
    title: string
    amount: string
    expenseDate: string
    payer: MemberVO
    participants: MemberVO[]
    createdAt: string
    updatedAt: string
}

export interface ExpensePageResponse {
    records: ExpenseListItemVO[]
    total: number
    size: number
    current: number
    pages: number
}

/**
 * 分页获取账本下的消费明细
 */
export function getExpenses(ledgerId: string, page = 1, size = 20) {
    return http.get<ExpensePageResponse>(`/ledgers/${ledgerId}/expenses`, {
        params: { current: page, size }
    })
}
