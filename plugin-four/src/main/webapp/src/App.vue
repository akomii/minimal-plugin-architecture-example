<template>
  <div class="min-h-screen surface-ground">
    <div class="surface-section shadow-2 px-4 py-3 flex align-items-center justify-content-between">
      <div>
        <div class="text-xl font-bold">Plugin Four</div>
        <div class="text-sm text-600">Frontend plugin host demo</div>
      </div>
      <div class="flex gap-2">
        <RouterLink to="/plugins" class="no-underline">
          <Button label="Plugins" severity="secondary"/>
        </RouterLink>
        <RouterLink to="/settings" class="no-underline">
          <Button label="Settings" severity="secondary"/>
        </RouterLink>
      </div>
    </div>
    <main class="p-4">
      <RouterView/>
    </main>
  </div>
</template>

<script setup lang="ts">
import Button from "primevue/button"
import {RouterLink, RouterView} from "vue-router"
</script>
