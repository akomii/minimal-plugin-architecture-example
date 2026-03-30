<template>
  <div class="flex h-screen w-screen m-0 p-0 font-sans">
    <div class="w-15rem bg-gray-900 p-4 flex flex-column gap-2 shadow-2 z-1">
      <h2 class="mt-0 mb-4 text-center">App Host</h2>

      <router-link to="/settings" class="p-button p-component p-button-secondary text-left w-full mb-3 text-decoration-none">
        <span class="flex align-items-center"><i class="pi pi-cog mr-2"></i> Settings</span>
      </router-link>

      <div class="flex flex-column gap-2 border-top-1 border-gray-700 pt-3">
        <div class="text-sm mb-2 text-center uppercase font-bold">Active Plugins</div>
        <router-link
            v-for="plugin in activePlugins"
            :key="plugin.id"
            :to="plugin.path"
            class="p-button p-component p-button-info p-button-outlined text-left w-full text-decoration-none"
        >
          <span class="flex align-items-center"><i class="pi pi-star mr-2"></i> {{ plugin.name }}</span>
        </router-link>
      </div>
    </div>

    <div class="flex-1 surface-ground overflow-auto">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import {availablePlugins} from './router'
import {activePluginIds} from './store'

const activePlugins = computed(() => {
  return availablePlugins.filter(p => activePluginIds.value.includes(p.id))
})
</script>

<style>
body {
  margin: 0;
  padding: 0;
}

.text-decoration-none {
  text-decoration: none;
}
</style>