<template>
  <div class="grid">
    <div class="col-12">
      <Card>
        <template #subtitle>
          <div class="flex justify-content-between align-items-center mb-4 gap-3 flex-wrap">
            <div class="text-sm text-600"> Available from backend: {{ storeAvailablePlugins.length }}</div>
            <Button label="Refresh" icon="pi pi-refresh" :loading="storeIsRefreshingPlugins" @click="handleRefresh"/>
          </div>
        </template>
        <template #content>
          <Message v-if="storePluginError" severity="error" class="mb-4"> {{ storePluginError }}</Message>
          <Message v-if="!storePluginError && storeAvailablePlugins.length === 0" severity="warn" class="mb-4">No frontend plugins are currently available from the backend</Message>
          <div v-if="storeAvailablePlugins.length > 0" class="flex flex-column gap-3">
            <div v-for="plugin in storeAvailablePlugins" :key="plugin.id" class="flex justify-content-between align-items-center gap-3 flex-wrap">
              <div>
                <div class="font-semibold">{{ plugin.id }}</div>
                <div class="text-sm text-600">Bundle: {{ plugin.url }}</div>
              </div>
              <div class="flex align-items-center gap-3">
                <Tag :severity="pluginStore.isPluginActive(plugin.id) ? 'success' : 'secondary'">
                  {{ pluginStore.isPluginActive(plugin.id) ? "Active" : "Inactive" }}
                </Tag>
                <ToggleSwitch :model-value="pluginStore.isPluginActive(plugin.id)" @update:model-value="(value) => handleToggle(plugin.id, value)"/>
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
import {pluginStore, storeAvailablePlugins, storeIsRefreshingPlugins, storePluginError} from "../stores/plugin-store"

async function handleRefresh(): Promise<void> {
  await pluginStore.refreshAvailablePlugins()
}

async function handleToggle(pluginId: string, enabled: boolean): Promise<void> {
  await pluginStore.togglePlugin(pluginId, enabled)
}

onMounted(async () => {
  if (storeAvailablePlugins.value.length === 0) {
    await pluginStore.refreshAvailablePlugins()
  }
})
</script>
