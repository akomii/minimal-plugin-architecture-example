<template>
  <div class="flex align-items-center justify-content-center min-h-screen surface-ground">
    <div class="surface-card p-5 shadow-4 border-round w-full md:w-6 lg:w-4 text-center">
      <i class="pi pi-info-circle text-5xl text-primary mb-3"></i>
      <h1 class="text-900 text-3xl font-medium mb-3">About Page</h1>
      <p class="text-700 line-height-3 mb-5">This is a fully functional Single-Page Vue application packaged inside a Spring Boot plugin</p>
      <router-link to="/" class="font-medium no-underline text-blue-500 hover:text-blue-700 cursor-pointer">
        <Button label="Back to Home" icon="pi pi-arrow-left" class="p-button-outlined w-full"/>
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import Button from "primevue/button"
</script>