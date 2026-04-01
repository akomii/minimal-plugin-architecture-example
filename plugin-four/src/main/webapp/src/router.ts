import { createRouter, createWebHashHistory } from "vue-router"
import Settings from "./components/Settings.vue"
import PluginHostView from "./components/PluginHostView.vue"

export const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", redirect: "/plugins" },
    { path: "/plugins", component: PluginHostView },
    { path: "/settings", component: Settings }
  ]
})
