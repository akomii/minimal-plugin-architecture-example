<template>
  <div class="surface-card p-5 shadow-4 border-round w-full">
    <div class="text-center mb-5">
      <i class="pi pi-star text-5xl text-primary mb-3"></i>
      <h1 class="text-900 text-3xl font-medium mb-3">Plugin Three</h1>
      <p class="text-500">Dynamically loaded library component</p>
    </div>
    <div class="flex flex-column gap-3 mt-4">
      <InputText v-model="name" placeholder="Enter your name" class="w-full"/>
      <Button label="Send to Backend" icon="pi pi-send" @click="callBackend" class="w-full"/>
      <Message v-if="message" severity="success" :closable="false">{{ message }}</Message>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref} from "vue"
import InputText from "primevue/inputtext"
import Button from "primevue/button"
import Message from "primevue/message"

const name = ref("")
const message = ref("")

const getApiBase = () => {
  let base = window.location.pathname.replace(/\/ui\/.*$/, "")
  if (base.endsWith("/")) base = base.slice(0, -1)
  return base || "/api/plugins"
}

const callBackend = async () => {
  const r = await fetch(`${getApiBase()}/three?input=${encodeURIComponent(name.value)}`)
  message.value = await r.text()
}
</script>
