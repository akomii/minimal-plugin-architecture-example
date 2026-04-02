import type {FrontendPluginDefinition, FrontendPluginManifest, FrontendPluginModule, LoadedFrontendPlugin} from "../plugin-types"

/**
 * Dynamic module loader
 * Fetches compiled ES modules via URL and normalizes their exports into valid Vue components
 */
export interface FrontendPluginLoader {
  load(manifest: FrontendPluginManifest): Promise<LoadedFrontendPlugin>
}

export class EsModuleFrontendPluginLoader implements FrontendPluginLoader {
  public async load(manifest: FrontendPluginManifest): Promise<LoadedFrontendPlugin> {
    const mod = (await import(/* @vite-ignore */ manifest.url)) as FrontendPluginModule
    return this.normalizeModuleExport(manifest, mod)
  }

  private normalizeModuleExport(manifest: FrontendPluginManifest, mod: FrontendPluginModule): LoadedFrontendPlugin {
    const directDefault = mod.default
    if (this.isFrontendPluginDefinition(directDefault)) {
      return {
        id: directDefault.id ?? manifest.id,
        url: manifest.url,
        component: directDefault.component
      }
    }
    if (directDefault) {
      return {
        id: manifest.id,
        url: manifest.url,
        component: directDefault
      }
    }
    if (mod.component) {
      return {
        id: mod.meta?.id ?? manifest.id,
        url: manifest.url,
        component: mod.component
      }
    }
    throw new Error(`Plugin "${manifest.id}" did not export a Vue component`)
  }

  private isFrontendPluginDefinition(value: unknown): value is FrontendPluginDefinition {
    return typeof value === "object" && value !== null && "component" in value
  }
}
