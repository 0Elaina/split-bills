<script setup lang="ts">
import { createLedger, getLedgers, type LedgerSaveDTO, type LedgerItem } from '@/shared/api/ledger';
import { onMounted, ref } from 'vue';


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
    name: ''
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
    <!-- 顶部导航条 -->
    <v-app-bar color="primary" elevation="2">
        <v-app-bar-title>合租账单结算器</v-app-bar-title>
    </v-app-bar>

    <!-- 页面主体容器 -->
    <v-main>
        <v-container class="py-6">
            <!-- 数据请求时的加载提示 -->
            <div v-if="loading" class="text-center mt-10">
                <v-progress-circular indeterminate color="primary" />
            </div>

            <!-- 数据就绪后的容器 -->
            <div v-else>
                <!-- 动作栏 (左右两端对齐布局) -->
                <v-row class="mb-6" align="center">
                    <v-col>
                        <h1 class="text-h4 font-weight-bold text-grey-darken-4 mb-1">我的账本</h1>
                        <div class="text-body-1 text-grey-darken-1">Manage your shared ledgers.</div>
                    </v-col>
                    <v-col cols="auto">
                        <v-btn color="primary" prepend-icon="mdi-plus" size="large" rounded="lg" elevation="2" @click="dialog = true">
                            新建账本
                        </v-btn>
                    </v-col>
                </v-row>

                <!-- 账本卡片网格 -->
                <v-row v-if="ledgers.length > 0">
                    <!-- 响应式列：手机占12格(全宽)，平板占6格(半宽)，桌面占4格(三分之一宽) -->
                    <v-col v-for="item in ledgers" :key="item.id" cols="12" sm="6" md="4">
                        <v-card hover class="h-100 d-flex flex-column rounded-xl" elevation="2">
                            <!-- 顶部：图标与更多按钮 -->
                            <div class="d-flex justify-space-between align-start pa-5 pb-2">
                                <v-avatar color="primary" variant="tonal" rounded="lg" size="52">
                                    <v-icon size="28">mdi-wallet</v-icon>
                                </v-avatar>
                                <v-btn icon="mdi-dots-vertical" variant="text" color="grey-darken-1" size="small" @click.stop></v-btn>
                            </div>
                            
                            <!-- 主体内容 -->
                            <div class="px-5 pt-2 pb-4 flex-grow-1">
                                <h3 class="text-h6 font-weight-bold text-grey-darken-4 text-truncate">{{ item.name }}</h3>
                                <div class="text-body-2 text-grey mt-1">Shared ledger for expenses.</div>
                            </div>
                            
                            <!-- 底部时间 -->
                            <v-divider></v-divider>
                            <div class="px-5 py-3 d-flex align-center text-caption text-grey-darken-1">
                                <v-icon size="small" class="mr-1">mdi-update</v-icon>
                                更新于 {{ new Date(item.updatedAt).toLocaleDateString() }}
                            </div>
                        </v-card>
                    </v-col>
                </v-row>

                <!-- 空状态提示 -->
                <v-row v-else>
                    <v-col class="text-center text-grey mt-10">
                        暂无账本，点击右上角新建一个吧！
                    </v-col>
                </v-row>
            </div>

            <!-- 新建账本弹窗 -->
            <v-dialog v-model="dialog" max-width="500">
                <v-card>
                    <v-card-title class="pt-4 px-6 text-h6 font-weight-bold">
                        新建账本
                    </v-card-title>
                    <v-card-text class="px-6 pb-2">
                        <!-- 提交时加上 .prevent 修饰符，阻止表单默认的刷新页面行为 -->
                        <v-form @submit.prevent="onSubmit">
                            <v-text-field v-model="formData.name" label="账本名称" variant="outlined" color="primary"
                                :rules="[
                                    v => !!v || '账本名称不能为空',
                                    v => v.length <= 50 || '最多不能超过50个字符'
                                ]" placeholder="请输入账本名称" autofocus>
                            </v-text-field>
                        </v-form>
                    </v-card-text>

                    <v-card-actions class="px-6 pb-4">
                        <v-spacer></v-spacer> <!-- 把按钮推到右边 -->
                        <v-btn variant="text" @click="dialog = false">取消</v-btn>
                        <v-btn color="primary" variant="flat" :loading="submitting" @click="onSubmit">
                            确定创建
                        </v-btn>
                    </v-card-actions>
                </v-card>
            </v-dialog>
        </v-container>
    </v-main>
</template>