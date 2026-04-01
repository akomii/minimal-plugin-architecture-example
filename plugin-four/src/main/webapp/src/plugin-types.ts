import type { Component } from "vue"

export interface FrontendPluginManifest {
  id: string
  name: string
  path: string
  url: string
}

export interface LoadedFrontendPlugin {
  id: string
  name: string
  path: string
  url: string
  component: Component
}

export interface FrontendPluginModule {
  default?: Component | FrontendPluginDefinition
  component?: Component
  meta?: {
    id?: string
    name?: string
    path?: string
  }
}

export interface FrontendPluginDefinition {
  id?: string
  name?: string
  path?: string
  component: Component
}