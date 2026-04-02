import {createApp} from "vue"
import PrimeVue from "primevue/config"
import Aura from "@primevue/themes/aura"
import "primeflex/primeflex.css"
import "primeicons/primeicons.css"
import App from "./App.vue"

/**
 * Entry point for the standalone Vue application
 * Initializes the Vue instance, configures PrimeVue, and injects the routing system
 */
const app = createApp(App)

app.use(PrimeVue, {
  theme: {
    preset: Aura
  }
})
app.mount("#app")
