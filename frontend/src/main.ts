import './shared/styles/main.css'

import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './app/App.vue'
import { router } from './app/router'
import { vuetify } from './app/vuetify'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(vuetify)

app.mount('#app')
