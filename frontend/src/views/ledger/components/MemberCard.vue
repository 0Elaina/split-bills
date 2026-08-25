<script setup lang="ts">
import type { MemberVO } from '@/shared/api/member'

const props = defineProps<{
  members: MemberVO[]
}>()

const emit = defineEmits<{
  (e: 'add'): void
  (e: 'edit', member: MemberVO): void
  (e: 'delete', member: MemberVO): void
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
</script>

<template>
  <div class="flex flex-col gap-sm">
    <h2 class="font-headline-md text-headline-md text-on-surface">参与成员</h2>
    <div
      class="bg-white border border-[#E5E2D9] shadow-sm rounded-xl p-md flex flex-wrap gap-4 items-center min-h-[100px]"
    >
      <!-- 渲染已有成员 -->
      <div v-for="(member, index) in members" :key="member.id" class="flex flex-col items-center gap-1">
        <v-menu location="bottom center">
          <template v-slot:activator="{ props }">
            <div
              v-bind="props"
              :class="getMemberColorClass(index)"
              class="w-12 h-12 rounded-full flex items-center justify-center font-headline-md text-headline-md font-bold shadow-sm cursor-pointer hover:opacity-80 transition-opacity"
              :title="member.name"
            >
              {{ member.name.substring(0, 1) }}
            </div>
          </template>
          <v-list>
            <v-list-item prepend-icon="mdi-pencil" title="修改成员" @click="emit('edit', member)"></v-list-item>
            <v-list-item prepend-icon="mdi-delete" title="删除成员" class="text-error" @click="emit('delete', member)"></v-list-item>
          </v-list>
        </v-menu>
      </div>

      <!-- 添加按钮 -->
      <button
        @click="emit('add')"
        class="w-12 h-12 rounded-full border-2 border-dashed border-outline-variant text-on-surface-variant flex items-center justify-center hover:bg-surface-container hover:text-primary transition-colors flex-shrink-0"
        title="添加成员"
      >
        <span class="material-symbols-outlined">add</span>
      </button>
    </div>
  </div>
</template>
