import { createRouter, createWebHistory } from 'vue-router'
import LedgerListView from '@/views/ledger/LedgerListView.vue'
import LedgerDetailView from '@/views/ledger/LedgerDetailView.vue'

export const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'ledger-list',
      component: LedgerListView,
    },
    {
      path: '/ledgers/:id',
      name: 'ledger-detail',
      component: LedgerDetailView,
    },
  ],
})
