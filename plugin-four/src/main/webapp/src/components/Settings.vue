<template>
  <div class="grid">
    <div class="col-12">
      <Card>
        <template #title>Frontend Plugins</template>
        <template #subtitle>Discover frontend ES modules exposed by backend-loaded plugin JARs and activate or deactivate them in the browser</template>
        <template #content>
          <div class="flex justify-content-between align-items-center mb-4 gap-3 flex-wrap">
            <div class="text-sm text-600">
              Available from backend: {{ availablePlugins.length }}
            </div>
            <Button label="Refresh" icon="pi pi-refresh" :loading="isRefreshingPlugins" @click="handleRefresh"/>
          </div>
          <Message v-if="pluginError" severity="error" class="mb-4"> {{ pluginError }}</Message>
          <Message v-if="!pluginError && availablePlugins.length === 0" severity="warn" class="mb-4"> No frontend plugins are currently available from the backend</Message>
          <div v-if="availablePlugins.length > 0" class="flex flex-column gap-3">
            <div v-for="plugin in availablePlugins" :key="plugin.id"
                 class="border-1 surface-border border-round p-3 flex justify-content-between align-items-center gap-3 flex-wrap">
              <div>
                <div class="font-semibold">{{ plugin.id }}</div>
                <div class="text-sm text-600">Bundle: {{ plugin.url }}</div>
              </div>
              <div class="flex align-items-center gap-3">
                <Tag :severity="isPluginActive(plugin.id) ? 'success' : 'secondary'">
                  {{ isPluginActive(plugin.id) ? "Active" : "Inactive" }}
                </Tag>
                <ToggleSwitch :model-value="isPluginActive(plugin.id)" @update:model-value="(value) => handleToggle(plugin.id, value)"/>
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import {onMounted} from "vue"
import Button from "primevue/button"
import Card from "primevue/card"
import Message from "primevue/message"
import Tag from "primevue/tag"
import ToggleSwitch from "primevue/toggleswitch"
import {availablePlugins, isPluginActive, isRefreshingPlugins, pluginError, refreshAvailablePlugins, togglePlugin} from "../store"

async function handleRefresh(): Promise<void> {
  await refreshAvailablePlugins()
}

async function handleToggle(pluginId: string, enabled: boolean): Promise<void> {
  await togglePlugin(pluginId, enabled)
}

onMounted(async () => {
  if (availablePlugins.value.length === 0) {
    await refreshAvailablePlugins()
  }
})
</script>
