<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLedger, updateLedger, deleteLedger, type LedgerItem } from '@/shared/api/ledger'
import { getMembers, createMember, type MemberVO, type MemberSaveDTO } from '@/shared/api/member'

const route = useRoute()
const router = useRouter()
const ledgerId = route.params.id as string

const ledger = ref<LedgerItem | null>(null)
const loading = ref(true)

// 成员状态
const members = ref<MemberVO[]>([])
const memberDialog = ref(false)
const memberSubmitting = ref(false)
const memberFormData = ref<MemberSaveDTO>({ name: '' })

// 颜色映射池 (应对设计原型的甲乙丙颜色分配)
const colorClasses = [
  'bg-primary text-on-primary',
  'bg-secondary text-on-secondary',
  'bg-tertiary text-on-tertiary'
]

const getMemberColorClass = (index: number) => {
  return colorClasses[index % colorClasses.length]
}

// 修改名称弹窗状态
const editDialog = ref(false)
const editName = ref('')
const submitting = ref(false)

// 删除确认弹窗状态
const deleteDialog = ref(false)
const deleting = ref(false)

// 初始化获取数据
const fetchDetail = async () => {
  try {
    loading.value = true
    const [ledgerData, membersData] = await Promise.all([
      getLedger(ledgerId),
      getMembers(ledgerId)
    ])
    ledger.value = ledgerData
    members.value = membersData
  } catch (error) {
    // 异常由全局拦截器处理
    router.replace('/') // 找不到则退回首页
  } finally {
    loading.value = false
  }
}

// 刷新成员
const refreshMembers = async () => {
  members.value = await getMembers(ledgerId)
}

// 提交新增成员
const onSubmitMember = async () => {
  if (!memberFormData.value.name.trim()) return
  try {
    memberSubmitting.value = true
    await createMember(ledgerId, memberFormData.value)
    memberDialog.value = false
    memberFormData.value.name = ''
    await refreshMembers()
  } finally {
    memberSubmitting.value = false
  }
}

// 提交修改名称
const onUpdateName = async () => {
  if (!editName.value.trim()) return
  try {
    submitting.value = true
    const updated = await updateLedger(ledgerId, { name: editName.value })
    ledger.value = updated
    editDialog.value = false
  } finally {
    submitting.value = false
  }
}

// 触发修改弹窗
const openEditDialog = () => {
  if (ledger.value) {
    editName.value = ledger.value.name
    editDialog.value = true
  }
}

// 确认删除
const confirmDelete = async () => {
  try {
    deleting.value = true
    await deleteLedger(ledgerId)
    deleteDialog.value = false
    router.replace('/') // 删除成功，返回列表
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div
    class="text-on-background min-h-screen pb-xl flex flex-col font-body-md antialiased selection:bg-primary-container selection:text-on-primary-container"
  >
    <!-- 加载中 -->
    <main v-if="loading" class="flex-grow flex items-center justify-center">
      <v-progress-circular indeterminate color="primary"></v-progress-circular>
    </main>

    <template v-else-if="ledger">
      <!-- Top Navigation (Contextual) -->
      <header
        class="bg-background w-full sticky top-0 z-50 flat no-shadows border-b border-surface-variant"
      >
        <div class="flex flex-col max-w-container-max mx-auto px-lg py-md w-full gap-sm">
          <div class="flex justify-between items-center w-full">
            <div class="flex items-center gap-md">
              <button
                @click="router.push('/')"
                class="text-on-surface-variant hover:text-primary transition-colors flex items-center justify-center rounded-full p-2 hover:bg-surface-container-high"
              >
                <span class="material-symbols-outlined">arrow_back</span>
              </button>
              <h1 class="font-headline-lg text-headline-lg text-on-surface">{{ ledger.name }}</h1>
            </div>
            <div class="flex items-center gap-md">
              <!-- 更多操作菜单 -->
              <v-menu location="bottom end">
                <template v-slot:activator="{ props }">
                  <button
                    v-bind="props"
                    class="text-on-surface-variant hover:text-primary transition-colors flex items-center justify-center rounded-full p-2 hover:bg-surface-container-high"
                  >
                    <span class="material-symbols-outlined">more_vert</span>
                  </button>
                </template>
                <v-list>
                  <v-list-item
                    prepend-icon="mdi-pencil"
                    title="修改名称"
                    @click="openEditDialog"
                  ></v-list-item>
                  <v-list-item
                    prepend-icon="mdi-delete"
                    title="删除账本"
                    class="text-error"
                    @click="deleteDialog = true"
                  ></v-list-item>
                </v-list>
              </v-menu>

              <button
                class="bg-primary-container text-on-primary font-label-lg text-label-lg py-2 px-6 rounded-lg hover:bg-opacity-90 transition-all shadow-sm flex items-center gap-2"
              >
                <span class="material-symbols-outlined fill">add</span>
                记一笔
              </button>
            </div>
          </div>

          <!-- Summary Badges -->
          <div class="flex items-center gap-sm mt-xs pl-[60px]">
            <div
              class="bg-surface-container-high text-on-surface font-label-sm text-label-sm px-3 py-1.5 rounded-full flex items-center gap-1.5 border border-outline-variant"
            >
              <span class="material-symbols-outlined text-[16px]">account_balance_wallet</span>
              总支出 ¥0.00
            </div>
            <div
              class="bg-surface-container-high text-on-surface font-label-sm text-label-sm px-3 py-1.5 rounded-full flex items-center gap-1.5 border border-outline-variant"
            >
              <span class="material-symbols-outlined text-[16px]">group</span>
              {{ members.length }} 位成员
            </div>
            <div
              class="bg-surface-container-high text-on-surface font-label-sm text-label-sm px-3 py-1.5 rounded-full flex items-center gap-1.5 border border-outline-variant"
            >
              <span class="material-symbols-outlined text-[16px]">receipt_long</span>
              0 笔消费
            </div>
          </div>
        </div>
      </header>

      <!-- Main Content Area - Bento Grid -->
      <main class="max-w-container-max mx-auto px-lg mt-xl w-full flex-grow">
        <div class="grid grid-cols-12 gap-gutter">
          <!-- Left Column (8): Expense List -->
          <div class="col-span-12 lg:col-span-8 flex flex-col gap-md">
            <h2 class="font-headline-md text-headline-md text-on-surface mb-xs">消费明细</h2>
            <div
              class="bg-white border border-[#E5E2D9] shadow-sm rounded-xl p-sm flex flex-col min-h-[200px] items-center justify-center text-on-surface-variant"
            >
              工作台占位区域：暂无消费明细，后续在此添加
            </div>
          </div>

          <!-- Right Column (4): Members & Settlements -->
          <div class="col-span-12 lg:col-span-4 flex flex-col gap-xl">
            <!-- Members Card -->
            <div class="flex flex-col gap-sm">
              <h2 class="font-headline-md text-headline-md text-on-surface">参与成员</h2>
              <div
                class="bg-white border border-[#E5E2D9] shadow-sm rounded-xl p-md flex flex-wrap gap-4 items-center min-h-[100px]"
              >
                <!-- 渲染已有成员 -->
                <div v-for="(member, index) in members" :key="member.id" class="flex flex-col items-center gap-1">
                  <div
                    :class="getMemberColorClass(index)"
                    class="w-12 h-12 rounded-full flex items-center justify-center font-headline-md text-headline-md font-bold shadow-sm"
                  >
                    {{ member.name.substring(0, 1) }}
                  </div>
                </div>

                <!-- 添加按钮 -->
                <button
                  @click="memberDialog = true"
                  class="w-12 h-12 rounded-full border-2 border-dashed border-outline-variant text-on-surface-variant flex items-center justify-center hover:bg-surface-container hover:text-primary transition-colors flex-shrink-0"
                  title="添加成员"
                >
                  <span class="material-symbols-outlined">add</span>
                </button>
              </div>
            </div>

            <!-- Settlement Card -->
            <div class="flex flex-col gap-sm">
              <h2 class="font-headline-md text-headline-md text-on-surface">结算方案</h2>
              <div
                class="bg-white border border-[#E5E2D9] shadow-sm rounded-xl p-md flex flex-col gap-md min-h-[100px] items-center justify-center text-on-surface-variant"
              >
                工作台占位区域：暂无结算方案
              </div>
            </div>
          </div>
        </div>
      </main>
    </template>

    <!-- Footer Component -->
    <footer
      class="w-full mt-xl bg-surface-container-low dark:bg-inverse-surface border-t border-outline-variant flat no-shadows transition-colors duration-200"
      v-if="!loading"
    >
      <div
        class="flex flex-col md:flex-row justify-between items-center px-lg py-md max-w-container-max mx-auto"
      >
        <div class="mb-4 md:mb-0 text-center md:text-left">
          <span class="font-headline-sm text-headline-sm text-primary block mb-2 font-bold"
            >Split Bills</span
          >
          <span class="text-secondary dark:text-secondary-fixed font-body-md text-body-md"
            >© 2024 Split Bills. Precision in every yen.</span
          >
        </div>
        <nav>
          <ul class="flex flex-wrap justify-center gap-6">
            <li>
              <a
                class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200 font-label-sm text-label-sm"
                href="#"
                >Privacy Policy</a
              >
            </li>
            <li>
              <a
                class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200 font-label-sm text-label-sm"
                href="#"
                >Terms of Service</a
              >
            </li>
            <li>
              <a
                class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200 font-label-sm text-label-sm"
                href="#"
                >Help Center</a
              >
            </li>
          </ul>
        </nav>
      </div>
    </footer>

    <!-- 修改名称弹窗 -->
    <v-dialog v-model="editDialog" max-width="500">
      <v-card>
        <v-card-title class="pt-4 px-6 text-h6 font-weight-bold">修改账本名称</v-card-title>
        <v-card-text class="px-6 pb-2">
          <v-form @submit.prevent="onUpdateName">
            <v-text-field
              v-model="editName"
              label="账本名称"
              variant="outlined"
              color="primary"
              :rules="[(v) => !!v || '不能为空', (v) => v.length <= 50 || '不能超过50个字符']"
              autofocus
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="editDialog = false">取消</v-btn>
          <v-btn color="primary" variant="flat" :loading="submitting" @click="onUpdateName"
            >保存</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除确认弹窗 -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card>
        <v-card-title class="pt-4 px-6 text-h6 font-weight-bold text-error">
          <v-icon color="error" class="mr-2">mdi-alert</v-icon>删除账本
        </v-card-title>
        <v-card-text class="px-6 py-4 text-body-1">
          确定要删除账本 <strong>{{ ledger?.name }}</strong> 吗？<br />
          <span class="text-caption text-grey"
            >注意：该操作不可恢复，删除后所有关联账单将一并清空。</span
          >
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" variant="flat" :loading="deleting" @click="confirmDelete"
            >确认删除</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 添加成员弹窗 -->
    <v-dialog v-model="memberDialog" max-width="500">
      <v-card>
        <v-card-title class="pt-4 px-6 text-h6 font-weight-bold">添加成员</v-card-title>
        <v-card-text class="px-6 pb-2">
          <v-form @submit.prevent="onSubmitMember">
            <v-text-field
              v-model="memberFormData.name"
              label="成员昵称"
              variant="outlined"
              color="primary"
              :rules="[(v) => !!v || '不能为空', (v) => v.length <= 10 || '不能超过10个字符']"
              autofocus
            ></v-text-field>
          </v-form>
        </v-card-text>
        <v-card-actions class="px-6 pb-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="memberDialog = false">取消</v-btn>
          <v-btn color="primary" variant="flat" :loading="memberSubmitting" @click="onSubmitMember"
            >确定</v-btn
          >
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>
