<template>
  <div class="grid">
    <div class="col-12">
      <Card>
        <template #title>Active Plugins</template>
        <template #subtitle>Active plugins are dynamically imported ES modules mounted at runtime</template>
        <template #content>
          <Message v-if="pluginError" severity="error" class="mb-4"> {{ pluginError }}</Message>
          <Message v-if="activePlugins.length === 0" severity="info"> No plugins are active. Enable one in Settings</Message>
          <div v-else class="flex flex-column gap-4">
            <div v-for="plugin in activePlugins" :key="plugin.id" class="border-1 surface-border border-round p-3">
              <div class="flex justify-content-between align-items-center mb-3 gap-3 flex-wrap">
                <div>
                  <div class="font-semibold">{{ plugin.id }}</div>
                  <div class="text-sm text-600">Bundle: {{ plugin.url }}</div>
                </div>
              </div>
              <div class="border-1 surface-border border-round p-3">
                <component :is="plugin.component"/>
              </div>
            </div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import Card from "primevue/card"
import Message from "primevue/message"
import {activePlugins, pluginError} from "../store"
</script>
