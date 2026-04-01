import {computed, ref, watch} from "vue"
import {fetchAvailableFrontendPlugins, loadFrontendPlugin} from "./plugin-api"
import type {FrontendPluginManifest, LoadedFrontendPlugin} from "./plugin-types"

const STORAGE_KEY = "activeFrontendPluginIds"

function parseSavedActivePluginIds(): string[] {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (!saved) {
    return []
  }
  try {
    const parsed = JSON.parse(saved) as unknown
    return Array.isArray(parsed) ? parsed.filter((item): item is string => typeof item === "string") : []
  } catch {
    return []
  }
}

export const availablePlugins = ref<FrontendPluginManifest[]>([])
export const loadedPlugins = ref<Record<string, LoadedFrontendPlugin>>({})
export const activePluginIds = ref<string[]>(parseSavedActivePluginIds())

export const isRefreshingPlugins = ref(false)
export const pluginError = ref<string | null>(null)

watch(activePluginIds, (value) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
}, {deep: true})

export const activePlugins = computed(() =>
    activePluginIds.value
    .map((id) => loadedPlugins.value[id])
    .filter((plugin): plugin is LoadedFrontendPlugin => Boolean(plugin))
)

export function isPluginActive(pluginId: string): boolean {
  return activePluginIds.value.includes(pluginId)
}

export async function refreshAvailablePlugins(): Promise<void> {
  isRefreshingPlugins.value = true
  pluginError.value = null
  try {
    const manifests = await fetchAvailableFrontendPlugins()
    availablePlugins.value = manifests
    const availableIds = new Set(manifests.map((plugin) => plugin.id))
    activePluginIds.value = activePluginIds.value.filter((id) => availableIds.has(id))
    for (const loadedId of Object.keys(loadedPlugins.value)) {
      if (!availableIds.has(loadedId)) {
        const next = {...loadedPlugins.value}
        delete next[loadedId]
        loadedPlugins.value = next
      }
    }
  } catch (error) {
    pluginError.value = error instanceof Error ? error.message : "Unknown error while refreshing plugins"
  } finally {
    isRefreshingPlugins.value = false
  }
}

export async function activatePlugin(pluginId: string): Promise<void> {
  pluginError.value = null
  const manifest = availablePlugins.value.find((plugin) => plugin.id === pluginId)
  if (!manifest) {
    pluginError.value = `Plugin "${pluginId}" is not available`
    return
  }
  try {
    if (!loadedPlugins.value[pluginId]) {
      const loadedPlugin = await loadFrontendPlugin(manifest)
      loadedPlugins.value = {
        ...loadedPlugins.value,
        [pluginId]: loadedPlugin
      }
    }
    if (!activePluginIds.value.includes(pluginId)) {
      activePluginIds.value = [...activePluginIds.value, pluginId]
    }
  } catch (error) {
    pluginError.value = error instanceof Error ? error.message : `Failed to activate plugin "${pluginId}"`
  }
}

export function deactivatePlugin(pluginId: string): void {
  activePluginIds.value = activePluginIds.value.filter((id) => id !== pluginId)
}

export async function togglePlugin(pluginId: string, enabled: boolean): Promise<void> {
  if (enabled) {
    await activatePlugin(pluginId)
  } else {
    deactivatePlugin(pluginId)
  }
}
