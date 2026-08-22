<script setup lang="ts">
import { createLedger, getLedgers, type LedgerSaveDTO, type LedgerItem } from '@/shared/api/ledger'
import { onMounted, ref } from 'vue'

// 定义响应式状态
const ledgers = ref<LedgerItem[]>([])
const loading = ref<boolean>(true)

// 获取账本列表
const fetchLedgers = async () => {
  try {
    loading.value = true
    const data = await getLedgers()
    ledgers.value = data.items
  } catch (error) {
  } finally {
    loading.value = false
  }
}

// 控制弹窗可见性
const dialog = ref<boolean>(false)
// 控制按钮 loading（防连击）
const submitting = ref<boolean>(false)

// 表单绑定的数据源
const formData = ref<LedgerSaveDTO>({
  name: '',
})

// 提交表单方法
const onSubmit = async () => {
  // 基本的安全兜底，防止纯空格被提交
  if (!formData.value.name.trim()) return

  try {
    submitting.value = true
    // 调用 API 创建账本
    await createLedger(formData.value)

    // 成功后更改状态
    dialog.value = false
    formData.value.name = ''

    // 重新获取账本列表
    await fetchLedgers()
  } catch (error) {
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchLedgers()
})
</script>

<template>
  <div
    class="text-on-background min-h-screen flex flex-col font-body-md antialiased selection:bg-primary-container selection:text-on-primary-container"
  >
    <!-- TopNavBar -->
    <header class="bg-background dark:bg-background docked full-width top-0 z-50">
      <div class="flex justify-between items-center px-lg py-md max-w-container-max mx-auto w-full">
        <div
          class="text-headline-md font-headline-md font-bold text-primary dark:text-primary-fixed"
        >
          Split Bills
        </div>
        <div class="flex items-center gap-sm">
          <!-- Notifications -->
          <button
            aria-label="Notifications"
            class="text-primary dark:text-primary-fixed hover:text-primary-container transition-colors duration-200 p-sm rounded-full hover:bg-surface-variant focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 0"
              >notifications</span
            >
          </button>
          <!-- Account -->
          <button
            aria-label="Account"
            class="text-primary dark:text-primary-fixed hover:text-primary-container transition-colors duration-200 p-sm rounded-full hover:bg-surface-variant focus:outline-none focus:ring-2 focus:ring-primary"
          >
            <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 0"
              >account_circle</span
            >
          </button>
        </div>
      </div>
    </header>

    <!-- Main Content -->
    <main class="flex-grow w-full max-w-container-max mx-auto px-lg py-xl">
      <!-- 数据请求时的加载提示 -->
      <div v-if="loading" class="text-center mt-10">
        <v-progress-circular indeterminate color="primary"></v-progress-circular>
      </div>

      <template v-else>
        <!-- Header Section -->
        <div
          class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-xl gap-md"
        >
          <div>
            <h1 class="font-headline-lg text-headline-lg text-on-surface mb-xs">我的账本</h1>
            <p class="font-body-md text-body-md text-on-surface-variant">
              Manage your shared ledgers.
            </p>
          </div>
          <button
            @click="dialog = true"
            aria-label="New Ledger"
            class="bg-primary-container text-on-primary font-label-lg text-label-lg px-md py-sm rounded-lg flex items-center gap-sm hover:bg-primary transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2 shadow-sm"
          >
            <span
              class="material-symbols-outlined text-[20px]"
              style="font-variation-settings: 'FILL' 0"
              >add</span
            >
            新建账本
          </button>
        </div>

        <!-- Ledgers Grid -->
        <div
          v-if="ledgers.length > 0"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-gutter"
        >
          <div
            v-for="item in ledgers"
            :key="item.id"
            @click="$router.push(`/ledgers/${item.id}`)"
            class="card-surface rounded-xl p-md flex flex-col group cursor-pointer bg-white border border-[#E5E2D9] shadow-sm hover:shadow-md transition-all hover:-translate-y-0.5"
          >
            <div class="flex justify-between items-start mb-md">
              <div class="bg-surface-container p-sm rounded-lg text-primary flex-shrink-0">
                <span
                  class="material-symbols-outlined text-[28px]"
                  style="font-variation-settings: 'FILL' 0"
                  >account_balance_wallet</span
                >
              </div>
              <button
                aria-label="More options"
                class="text-on-surface-variant hover:text-primary p-xs rounded-full hover:bg-surface-variant transition-colors focus:outline-none focus:ring-2 focus:ring-primary"
                @click.stop
              >
                <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 0"
                  >more_vert</span
                >
              </button>
            </div>
            <div class="flex-grow">
              <h3
                class="font-headline-md text-headline-md text-on-surface mb-xs line-clamp-1 group-hover:text-primary transition-colors"
              >
                {{ item.name }}
              </h3>
              <p class="font-body-md text-body-md text-on-surface-variant line-clamp-2">
                Shared ledger for expenses.
              </p>
            </div>
            <div
              class="mt-md pt-sm border-t border-outline-variant flex items-center justify-between text-on-surface-variant font-label-sm text-label-sm"
            >
              <div class="flex items-center gap-xs">
                <span
                  class="material-symbols-outlined text-[16px]"
                  style="font-variation-settings: 'FILL' 0"
                  >update</span
                >
                <span>更新于 {{ new Date(item.updatedAt).toLocaleDateString() }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-else class="text-center text-on-surface-variant mt-10">
          暂无账本，点击右上角新建一个吧！
        </div>
      </template>

      <!-- 新建账本弹窗 (保留 Vuetify 交互) -->
      <v-dialog v-model="dialog" max-width="500">
        <v-card>
          <v-card-title class="pt-4 px-6 text-h6 font-weight-bold"> 新建账本 </v-card-title>
          <v-card-text class="px-6 pb-2">
            <v-form @submit.prevent="onSubmit">
              <v-text-field
                v-model="formData.name"
                label="账本名称"
                variant="outlined"
                color="primary"
                :rules="[
                  (v) => !!v || '账本名称不能为空',
                  (v) => v.length <= 50 || '最多不能超过50个字符',
                ]"
                placeholder="请输入账本名称"
                autofocus
              >
              </v-text-field>
            </v-form>
          </v-card-text>

          <v-card-actions class="px-6 pb-4">
            <v-spacer></v-spacer>
            <v-btn variant="text" @click="dialog = false">取消</v-btn>
            <v-btn color="primary" variant="flat" :loading="submitting" @click="onSubmit">
              确定创建
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </main>

    <!-- Footer -->
    <footer
      class="bg-surface-container-low dark:bg-inverse-surface w-full mt-xl border-t border-outline-variant z-10"
    >
      <div
        class="flex flex-col md:flex-row justify-between items-center px-lg py-md max-w-container-max mx-auto gap-md"
      >
        <div class="font-headline-sm text-headline-sm text-primary">Split Bills</div>
        <div
          class="font-body-md text-body-md text-secondary dark:text-secondary-fixed text-center md:text-left"
        >
          © 2024 Split Bills. Precision in every yen.
        </div>
        <div class="flex items-center gap-md font-label-sm text-label-sm">
          <a
            class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200"
            href="#"
            >Privacy Policy</a
          >
          <a
            class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200"
            href="#"
            >Terms of Service</a
          >
          <a
            class="text-on-surface-variant hover:text-primary dark:hover:text-primary-fixed transition-colors duration-200"
            href="#"
            >Help Center</a
          >
        </div>
      </div>
    </footer>
  </div>
</template>
