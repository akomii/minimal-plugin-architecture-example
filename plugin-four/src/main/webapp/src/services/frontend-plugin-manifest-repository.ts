import type {FrontendPluginManifest} from "../plugin-types"

/**
 * API client for plugin discovery
 * Calls the host backend to retrieve the list of available frontend plugin URLs
 */
export interface FrontendPluginManifestRepository {
  fetchAll(): Promise<FrontendPluginManifest[]>
}

export class HttpFrontendPluginManifestRepository implements FrontendPluginManifestRepository {
  private readonly endpoint: string

  public constructor(endpoint = "/api/plugins/frontend") {
    this.endpoint = endpoint
  }

  public async fetchAll(): Promise<FrontendPluginManifest[]> {
    const response = await fetch(this.endpoint, {
      headers: {Accept: "application/json"}
    })
    if (!response.ok) {
      throw new Error(`Failed to fetch frontend plugins: HTTP ${response.status}`)
    }
    const data = (await response.json()) as unknown
    if (!Array.isArray(data)) {
      throw new Error("Frontend plugin manifest response is not an array")
    }
    return data.map(this.toManifest)
  }

  private toManifest(value: unknown): FrontendPluginManifest {
    if (
        typeof value !== "object" ||
        value === null ||
        !("id" in value) ||
        !("url" in value) ||
        typeof value.id !== "string" ||
        typeof value.url !== "string"
    ) {
      throw new Error("Frontend plugin manifest item is invalid")
    }
    return {id: value.id, url: value.url}
  }
}
