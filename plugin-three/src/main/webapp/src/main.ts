import { createApp } from 'vue'
import PrimeVue from 'primevue/config'
import App from './App.vue'

// Import a theme just for local development so it doesn't look broken
import 'primevue/resources/themes/aura-light-green/theme.css'

const app = createApp(App)
app.use(PrimeVue)
app.mount('#app')