<template>
  <div class="flex align-items-center justify-content-center min-h-screen surface-ground">
    <div class="surface-card p-5 shadow-4 border-round w-full md:w-6 lg:w-4">
      <div class="text-center mb-5">
        <i class="pi pi-star text-5xl text-primary mb-3"></i>
        <h1 class="text-900 text-3xl font-medium mb-3">Welcome Plugin</h1>
      </div>
      <div class="flex flex-column gap-3">
        <InputText v-model="name" placeholder="Name" class="w-full mb-3"/>
        <Button label="Send to Backend" icon="pi pi-send" @click="callBackend" class="w-full"/>
        <Message v-if="message" severity="success" :closable="false">{{ message }}</Message>
      </div>
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

const callBackend = async () => {
  let apiBase = window.location.pathname.replace(/\/ui\/.*$/, "")
  if (apiBase.endsWith("/")) {
    apiBase = apiBase.slice(0, -1)
  }

  const r = await fetch(`${apiBase}/welcome?input=${encodeURIComponent(name.value)}`)
  message.value = await r.text()
}
</script>
