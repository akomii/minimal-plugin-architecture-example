<template>
  <div class="flex align-items-center justify-content-center min-h-screen surface-ground">
    <div class="surface-card p-5 shadow-4 border-round w-full md:w-6 lg:w-4">
      <div class="text-center mb-5">
        <i class="pi pi-home text-5xl text-primary mb-3"></i>
        <h1 class="text-900 text-3xl font-medium mb-3">Goodbye Plugin</h1>
      </div>
      <div class="flex flex-column gap-3">
        <InputText v-model="name" placeholder="Name" class="w-full mb-3"/>
        <Button label="Send to Backend" icon="pi pi-send" @click="callBackend" class="w-full"/>
        <Message v-if="message" severity="success" :closable="false">{{ message }}</Message>
        <router-link to="/about" class="font-medium no-underline text-blue-500 hover:text-blue-700 cursor-pointer">
          <Button label="Go to About" icon="pi pi-arrow-right" iconPos="right" class="p-button-outlined w-full"/>
        </router-link>

        <a v-if="isWelcomeLoaded" :href="welcomeUrl" class="font-medium no-underline text-blue-500 hover:text-blue-700 cursor-pointer">
          <Button label="Open Welcome Plugin" icon="pi pi-external-link" class="p-button-secondary w-full"/>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref} from "vue"
import InputText from "primevue/inputtext"
import Button from "primevue/button"
import Message from "primevue/message"

const name = ref("")
const message = ref("")
const isWelcomeLoaded = ref(false)
const welcomeUrl = ref("")

const getApiBase = () => {
  let base = window.location.pathname.replace(/\/ui\/.*$/, "")
  if (base.endsWith("/")) base = base.slice(0, -1)
  return base || "/api/plugins"
}

const callBackend = async () => {
  const r = await fetch(`${getApiBase()}/goodbye?input=${encodeURIComponent(name.value)}`)
  message.value = await r.text()
}

onMounted(async () => {
  try {
    const apiBase = getApiBase()
    const r = await fetch(apiBase)
    if (r.ok) {
      const plugins = await r.json()
      const welcome = plugins.find((p: any) => p.id === "plugin-welcome")
      if (welcome && welcome.loaded) {
        isWelcomeLoaded.value = true
        welcomeUrl.value = `${apiBase}/ui/plugin-welcome/index.html`
      }
    }
  } catch (e) {
    console.error("Failed to fetch plugins", e)
  }
})
</script>