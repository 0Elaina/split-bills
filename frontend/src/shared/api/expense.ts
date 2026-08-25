import { http, type ApiResult } from './http'
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
export async function getExpenses(ledgerId: string, page = 1, size = 20) {
  const response = await http.get<ApiResult<ExpensePageResponse>>(`/ledgers/${ledgerId}/expenses`, {
    params: { current: page, size }
  })
  return response.data.data
}

export interface ExpenseSaveDTO {
    title: string
    amount: string
    expenseDate: string
    payerMemberId: string | number
    participantMemberIds: (string | number)[]
}

/**
 * 新增记一笔
 */
export async function createExpense(ledgerId: string, data: ExpenseSaveDTO) {
  const response = await http.post<ApiResult<void>>(`/ledgers/${ledgerId}/expenses`, data)
  return response.data.data
}

/**
 * 修改记一笔
 */
export async function updateExpense(ledgerId: string, expenseId: string, data: ExpenseSaveDTO) {
  const response = await http.put<ApiResult<void>>(`/ledgers/${ledgerId}/expenses/${expenseId}`, data)
  return response.data.data
}

/**
 * 删除记一笔
 */
export async function deleteExpense(ledgerId: string, expenseId: string) {
  const response = await http.delete<ApiResult<void>>(`/ledgers/${ledgerId}/expenses/${expenseId}`)
  return response.data.data
}
