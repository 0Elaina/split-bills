import { type ApiResult, http } from './http'

export interface MemberVO {
  id: string
  ledgerId: string
  name: string
  createdAt: string
}

export interface MemberSaveDTO {
  name: string
}

/**
 * 获取账本下的所有成员
 */
export const getMembers = async (ledgerId: string): Promise<MemberVO[]> => {
  const response = await http.get<ApiResult<MemberVO[]>>(`/ledgers/${ledgerId}/members`)
  return response.data.data
}

/**
 * 在指定账本下添加成员
 */
export const createMember = async (ledgerId: string, dto: MemberSaveDTO): Promise<string> => {
  const response = await http.post<ApiResult<string>>(`/ledgers/${ledgerId}/members`, dto)
  return response.data.data
}

/**
 * 修改成员名称
 */
export const updateMember = async (ledgerId: string, memberId: string, dto: MemberSaveDTO): Promise<MemberVO> => {
  const response = await http.patch<ApiResult<MemberVO>>(`/ledgers/${ledgerId}/members/${memberId}`, dto)
  return response.data.data
}

/**
 * 删除成员
 */
export const deleteMember = async (ledgerId: string, memberId: string): Promise<void> => {
  await http.delete<ApiResult<void>>(`/ledgers/${ledgerId}/members/${memberId}`)
}
