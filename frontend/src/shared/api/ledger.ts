import { type ApiResult, http } from "./http"

/**
 * 分页基础响应结构
 */
export interface PageData<T> {
    items: T[]
    page: number
    size: number
    totalElements: number
    totalPages: number
}

/**
 * 账本明细数据结构
 */
export interface LedgerItem {
    id: string
    name: string
    createdAt: string
    updatedAt: string
}

/**
 * 获取账本列表 (分页)
 * @param page 页码，从 1 开始，默认 1
 * @param size 每页数量，默认 20
 * 
 * @returns 分页数据结构
 */
export async function getLedgers(page: number = 1, size: number = 20) {
    const response = await http.get<ApiResult<PageData<LedgerItem>>>(
        '/ledgers',
        {
            params: {
                page,
                size
            }
        }
    )
    return response.data.data
}