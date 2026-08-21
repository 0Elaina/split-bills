<script setup lang="ts">
import { getLedgers, type LedgerItem } from '@/shared/api/ledger';
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
                <v-row class="mb-4" align="center">
                    <v-col>
                        <h2 class="text-h5 font-weight-bold">我的账本</h2>
                    </v-col>
                    <v-col cols="auto">
                        <v-btn color="primary" prepend-icon="mdi-plus">
                            新建账本
                        </v-btn>
                    </v-col>
                </v-row>

                <!-- 账本卡片网络 -->
                <v-row v-if="ledgers.length > 0">
                    <!-- 响应式列：手机占12格(全宽)，平板占6格(半宽)，桌面占4格(三分之一宽) -->
                    <v-col v-for="item in ledgers" :key="item.id" cols="12" sm="6" md="4">
                        <!-- 带有 hover 悬浮升起效果的卡片 -->
                        <v-card hover class="h-100 d-flex flex-column">
                            <!-- 卡片标题 -->
                            <v-card-title class="text-h6 font-weight-bold pt-4">
                                {{ item.name }}
                            </v-card-title>

                            <!-- 卡片内容区域：显示创建时间 -->
                            <v-card-text class="text-body-2 text-grey-darken-1 mt-auto">
                                <v-icon size="small" class="mr-1">mdi-clock-outline</v-icon>
                                创建于 {{ new Date(item.createdAt).toLocaleDateString() }}
                            </v-card-text>
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
        </v-container>
    </v-main>
</template>