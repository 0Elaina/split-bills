<script setup lang="ts">
const props = defineProps<{
  // 修改
  editDialog: boolean
  editName: string
  editSubmitting: boolean
  // 删除
  deleteDialog: boolean
  deleteSubmitting: boolean
}>()

const emit = defineEmits<{
  // 修改
  (e: 'update:editDialog', v: boolean): void
  (e: 'update:editName', v: string): void
  (e: 'submitEdit'): void
  // 删除
  (e: 'update:deleteDialog', v: boolean): void
  (e: 'submitDelete'): void
}>()
</script>

<template>
  <!-- 修改账本名称弹窗 -->
  <v-dialog :model-value="editDialog" @update:model-value="emit('update:editDialog', $event)" max-width="400">
    <v-card class="rounded-xl">
      <v-card-title class="font-headline-md pt-6 px-6">修改账本名称</v-card-title>
      <v-card-text class="px-6 pb-2 pt-4">
        <v-text-field
          :model-value="editName"
          @update:model-value="emit('update:editName', $event)"
          label="账本名称"
          variant="outlined"
          placeholder="请输入新的账本名称"
          hide-details="auto"
          autofocus
          @keyup.enter="emit('submitEdit')"
        ></v-text-field>
      </v-card-text>
      <v-card-actions class="px-6 pb-6 pt-4">
        <v-spacer></v-spacer>
        <v-btn variant="text" color="on-surface-variant" @click="emit('update:editDialog', false)">取消</v-btn>
        <v-btn variant="flat" color="primary-container" class="text-on-primary px-6" :loading="editSubmitting" @click="emit('submitEdit')">确认修改</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- 删除账本确认弹窗 -->
  <v-dialog :model-value="deleteDialog" @update:model-value="emit('update:deleteDialog', $event)" max-width="400">
    <v-card class="rounded-xl">
      <v-card-title class="font-headline-md pt-6 px-6 text-error">删除账本</v-card-title>
      <v-card-text class="px-6 py-4 text-body-md text-on-surface-variant">
        确定要删除这个账本吗？此操作不可恢复，且会删除账本下所有数据！
      </v-card-text>
      <v-card-actions class="px-6 pb-6">
        <v-spacer></v-spacer>
        <v-btn variant="text" color="on-surface-variant" @click="emit('update:deleteDialog', false)">取消</v-btn>
        <v-btn variant="flat" color="error" class="text-on-error px-6" :loading="deleteSubmitting" @click="emit('submitDelete')">确认删除</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
