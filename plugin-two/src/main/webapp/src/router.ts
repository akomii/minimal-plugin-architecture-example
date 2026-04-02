import {createRouter, createWebHashHistory} from "vue-router"
import Home from "./Home.vue"
import About from "./About.vue"

/**
 * Defines the client-side routing structure using Vue Router
 * Maps URL paths to specific view components like Home and About
 */
export const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {path: "/", component: Home},
    {path: "/about", component: About}
  ]
})
