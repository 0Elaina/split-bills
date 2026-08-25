import { ref } from 'vue'
import { getMembers, createMember, updateMember, deleteMember, type MemberVO, type MemberSaveDTO } from '@/shared/api/member'

export function useMembers(ledgerId: string) {
  const members = ref<MemberVO[]>([])
  
  const fetchMembers = async () => {
    members.value = await getMembers(ledgerId)
  }

  // 新增成员
  const memberDialog = ref(false)
  const memberFormData = ref<MemberSaveDTO>({ name: '' })
  const memberSubmitting = ref(false)

  const onSubmitMember = async () => {
    if (!memberFormData.value.name.trim()) return
    try {
      memberSubmitting.value = true
      await createMember(ledgerId, memberFormData.value)
      memberDialog.value = false
      memberFormData.value.name = ''
      await fetchMembers()
    } finally {
      memberSubmitting.value = false
    }
  }

  // 修改成员
  const editMemberDialog = ref(false)
  const memberEditing = ref<MemberVO | null>(null)
  const memberEditName = ref('')
  const memberEditSubmitting = ref(false)

  const openEditMember = (member: MemberVO) => {
    memberEditing.value = member
    memberEditName.value = member.name
    editMemberDialog.value = true
  }

  const onUpdateMember = async () => {
    if (!memberEditName.value.trim() || !memberEditing.value) return
    try {
      memberEditSubmitting.value = true
      await updateMember(ledgerId, memberEditing.value.id, { name: memberEditName.value })
      editMemberDialog.value = false
      await fetchMembers()
    } finally {
      memberEditSubmitting.value = false
    }
  }

  // 删除成员
  const deleteMemberDialog = ref(false)
  const memberDeleting = ref<MemberVO | null>(null)
  const memberDeleteSubmitting = ref(false)

  const openDeleteMember = (member: MemberVO) => {
    memberDeleting.value = member
    deleteMemberDialog.value = true
  }

  const confirmDeleteMember = async () => {
    if (!memberDeleting.value) return
    try {
      memberDeleteSubmitting.value = true
      await deleteMember(ledgerId, memberDeleting.value.id)
      deleteMemberDialog.value = false
      await fetchMembers()
    } finally {
      memberDeleteSubmitting.value = false
    }
  }

  return {
    members,
    fetchMembers,
    // Add
    memberDialog,
    memberFormData,
    memberSubmitting,
    onSubmitMember,
    // Edit
    editMemberDialog,
    memberEditing,
    memberEditName,
    memberEditSubmitting,
    openEditMember,
    onUpdateMember,
    // Delete
    deleteMemberDialog,
    memberDeleting,
    memberDeleteSubmitting,
    openDeleteMember,
    confirmDeleteMember
  }
}
