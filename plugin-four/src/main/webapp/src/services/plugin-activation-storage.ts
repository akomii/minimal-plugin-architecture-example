/**
 * Local storage persistence layer
 * Saves and loads the user's active plugin selections across browser sessions
 */
export interface PluginActivationStorage {
  load(): string[]
  save(pluginIds: string[]): void
}

export class LocalStoragePluginActivationStorage implements PluginActivationStorage {
  private readonly storageKey: string

  public constructor(storageKey = "activeFrontendPluginIds") {
    this.storageKey = storageKey
  }

  public load(): string[] {
    const saved = localStorage.getItem(this.storageKey)
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

  public save(pluginIds: string[]): void {
    localStorage.setItem(this.storageKey, JSON.stringify(pluginIds))
  }
}
