<template>
  <div class="flex align-items-center justify-content-center h-full p-4">
    <div class="surface-card p-5 shadow-4 border-round w-full md:w-8 lg:w-6">
      <div class="flex flex-column gap-3">
        <div v-for="plugin in availablePlugins" :key="plugin.id" class="flex align-items-center justify-content-between p-3 border-1 surface-border border-round">
          <span class="text-lg">{{ plugin.name }}</span>
          <Button
              v-if="!isActive(plugin.id)"
              label="Activate"
              icon="pi pi-check"
              class="p-button-success w-10rem"
              @click="toggle(plugin.id)"
          />
          <Button
              v-else
              label="Deactivate"
              icon="pi pi-times"
              class="p-button-danger p-button-outlined w-10rem"
              @click="toggle(plugin.id)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {availablePlugins} from '../router'
import {activePluginIds} from '../store'
import Button from 'primevue/button'

const isActive = (id: string) => activePluginIds.value.includes(id)

const toggle = (id: string) => {
  if (isActive(id)) {
    activePluginIds.value = activePluginIds.value.filter(pid => pid !== id)
  } else {
    activePluginIds.value.push(id)
  }
}
</script>
