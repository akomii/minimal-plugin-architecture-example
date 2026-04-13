import { createRouter, createWebHashHistory } from "vue-router"
import Settings from "./components/Settings.vue"
import PluginHostView from "./components/PluginHostView.vue"

/**
 * Defines the client-side routing structure using Vue Router
 * Maps URL paths to specific view components like Home and About
 */
export const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    { path: "/", redirect: "/plugins" },
    { path: "/plugins", component: PluginHostView },
    { path: "/settings", component: Settings }
  ]
})
