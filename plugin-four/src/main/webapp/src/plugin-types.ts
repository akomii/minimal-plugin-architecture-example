import type { Component } from "vue"

export interface FrontendPluginManifest {
  id: string
  url: string
}

export interface LoadedFrontendPlugin {
  id: string
  url: string
  component: Component
}

export interface FrontendPluginDefinition {
  id?: string
  component: Component
}

export interface FrontendPluginModuleMeta {
  id?: string
}

export interface FrontendPluginModule {
  default?: Component | FrontendPluginDefinition
  component?: Component
  meta?: FrontendPluginModuleMeta
}
