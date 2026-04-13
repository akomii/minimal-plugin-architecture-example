import {computed, ref, watch} from "vue"
import type {FrontendPluginManifest, LoadedFrontendPlugin} from "../plugin-types"
import type {FrontendPluginLoader} from "../services/frontend-plugin-loader"
import type {FrontendPluginManifestRepository} from "../services/frontend-plugin-manifest-repository"
import type {PluginActivationStorage} from "../services/plugin-activation-storage"
import {EsModuleFrontendPluginLoader} from "../services/frontend-plugin-loader"
import {HttpFrontendPluginManifestRepository} from "../services/frontend-plugin-manifest-repository"
import {LocalStoragePluginActivationStorage} from "../services/plugin-activation-storage"

/**
 * Central state management for the UI host
 * Coordinates fetching manifests, loading remote modules, and tracking active components
 */
export interface PluginStore {
  availablePlugins: Readonly<typeof availablePlugins>
  activePluginIds: Readonly<typeof activePluginIds>
  activePlugins: Readonly<typeof activePlugins>
  isRefreshingPlugins: Readonly<typeof isRefreshingPlugins>
  pluginError: Readonly<typeof pluginError>

  isPluginActive(pluginId: string): boolean

  refreshAvailablePlugins(): Promise<void>

  activatePlugin(pluginId: string): Promise<void>

  deactivatePlugin(pluginId: string): void

  togglePlugin(pluginId: string, enabled: boolean): Promise<void>
}

const availablePlugins = ref<FrontendPluginManifest[]>([])
const loadedPlugins = ref<Record<string, LoadedFrontendPlugin>>({})
const activePluginIds = ref<string[]>([])
const isRefreshingPlugins = ref(false)
const pluginError = ref<string | null>(null)

const activePlugins = computed(() =>
    activePluginIds.value
    .map((id) => loadedPlugins.value[id])
    .filter((plugin): plugin is LoadedFrontendPlugin => Boolean(plugin))
)

class DefaultPluginStore implements PluginStore {
  private readonly manifestRepository: FrontendPluginManifestRepository
  private readonly pluginLoader: FrontendPluginLoader
  private readonly activationStorage: PluginActivationStorage

  public constructor(
      manifestRepository: FrontendPluginManifestRepository,
      pluginLoader: FrontendPluginLoader,
      activationStorage: PluginActivationStorage
  ) {
    this.manifestRepository = manifestRepository
    this.pluginLoader = pluginLoader
    this.activationStorage = activationStorage
    activePluginIds.value = this.activationStorage.load()
    watch(activePluginIds, (value) => {
      this.activationStorage.save(value)
    }, {deep: true})
  }

  public get availablePlugins(): Readonly<typeof availablePlugins> {
    return availablePlugins
  }

  public get activePluginIds(): Readonly<typeof activePluginIds> {
    return activePluginIds
  }

  public get activePlugins(): Readonly<typeof activePlugins> {
    return activePlugins
  }

  public get isRefreshingPlugins(): Readonly<typeof isRefreshingPlugins> {
    return isRefreshingPlugins
  }

  public get pluginError(): Readonly<typeof pluginError> {
    return pluginError
  }

  public isPluginActive(pluginId: string): boolean {
    return activePluginIds.value.includes(pluginId)
  }

  public async refreshAvailablePlugins(): Promise<void> {
    isRefreshingPlugins.value = true
    pluginError.value = null
    try {
      const manifests = await this.manifestRepository.fetchAll()
      availablePlugins.value = manifests
      const availableIds = new Set(manifests.map((plugin) => plugin.id))
      activePluginIds.value = activePluginIds.value.filter((id) => availableIds.has(id))
      const nextLoadedPlugins: Record<string, LoadedFrontendPlugin> = {}
      for (const [pluginId, loadedPlugin] of Object.entries(loadedPlugins.value)) {
        if (availableIds.has(pluginId)) {
          nextLoadedPlugins[pluginId] = loadedPlugin
        }
      }
      loadedPlugins.value = nextLoadedPlugins
    } catch (error) {
      pluginError.value = error instanceof Error ? error.message : "Unknown error while refreshing plugins"
    } finally {
      isRefreshingPlugins.value = false
    }
  }

  public async activatePlugin(pluginId: string): Promise<void> {
    pluginError.value = null
    const manifest = availablePlugins.value.find((plugin) => plugin.id === pluginId)
    if (!manifest) {
      pluginError.value = `Plugin "${pluginId}" is not available`
      return
    }
    try {
      if (!loadedPlugins.value[pluginId]) {
        const loadedPlugin = await this.pluginLoader.load(manifest)
        loadedPlugins.value = {...loadedPlugins.value, [pluginId]: loadedPlugin}
      }
      if (!activePluginIds.value.includes(pluginId)) {
        activePluginIds.value = [...activePluginIds.value, pluginId]
      }
    } catch (error) {
      pluginError.value =
          error instanceof Error ? error.message : `Failed to activate plugin "${pluginId}"`
    }
  }

  public deactivatePlugin(pluginId: string): void {
    activePluginIds.value = activePluginIds.value.filter((id) => id !== pluginId)
  }

  public async togglePlugin(pluginId: string, enabled: boolean): Promise<void> {
    if (enabled) {
      await this.activatePlugin(pluginId)
      return
    }
    this.deactivatePlugin(pluginId)
  }
}

export const pluginStore: PluginStore = new DefaultPluginStore(
    new HttpFrontendPluginManifestRepository(),
    new EsModuleFrontendPluginLoader(),
    new LocalStoragePluginActivationStorage()
)

export const storeAvailablePlugins = availablePlugins
export const storeActivePluginIds = activePluginIds
export const storeActivePlugins = activePlugins
export const storeIsRefreshingPlugins = isRefreshingPlugins
export const storePluginError = pluginError
