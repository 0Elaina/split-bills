import { createRouter, createWebHistory } from 'vue-router'

export const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'ledger-list',
            component: () => import('@/views/ledger/LedgerListView.vue')
        }
    ],
})
