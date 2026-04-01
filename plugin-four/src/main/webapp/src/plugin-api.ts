import type {
  FrontendPluginDefinition,
  FrontendPluginManifest,
  FrontendPluginModule,
  LoadedFrontendPlugin
} from "./plugin-types"

function isFrontendPluginDefinition(value: unknown): value is FrontendPluginDefinition {
  return typeof value === "object" && value !== null && "component" in value
}

function normalizeModuleExport(
    manifest: FrontendPluginManifest,
    mod: FrontendPluginModule
): LoadedFrontendPlugin {
  const directDefault = mod.default

  if (isFrontendPluginDefinition(directDefault)) {
    return {
      id: directDefault.id ?? manifest.id,
      name: directDefault.name ?? manifest.name,
      path: directDefault.path ?? manifest.path,
      url: manifest.url,
      component: directDefault.component
    }
  }

  if (directDefault) {
    return {
      id: manifest.id,
      name: manifest.name,
      path: manifest.path,
      url: manifest.url,
      component: directDefault
    }
  }

  if (mod.component) {
    return {
      id: mod.meta?.id ?? manifest.id,
      name: mod.meta?.name ?? manifest.name,
      path: mod.meta?.path ?? manifest.path,
      url: manifest.url,
      component: mod.component
    }
  }

  throw new Error(`Plugin "${manifest.id}" did not export a Vue component`)
}

export async function fetchAvailableFrontendPlugins(): Promise<FrontendPluginManifest[]> {
  const response = await fetch("/api/plugins/frontend", {
    headers: {
      Accept: "application/json"
    }
  })

  if (!response.ok) {
    throw new Error(`Failed to fetch frontend plugins: HTTP ${response.status}`)
  }

  const data = (await response.json()) as FrontendPluginManifest[]

  if (!Array.isArray(data)) {
    throw new Error("Frontend plugin manifest response is not an array")
  }

  return data
}

export async function loadFrontendPlugin(
    manifest: FrontendPluginManifest
): Promise<LoadedFrontendPlugin> {
  const mod = (await import(/* @vite-ignore */ manifest.url)) as FrontendPluginModule
  return normalizeModuleExport(manifest, mod)
}