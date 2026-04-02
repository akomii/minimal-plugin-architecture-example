/// <reference types="vite/client" />

/**
 * Build configuration for the Vite bundler
 * Defines proxy rules for local API development and configures the output directory for static UI assets
 */
declare module "*.vue" {
  import type {DefineComponent} from "vue"
  const component: DefineComponent<Record<string, never>, Record<string, never>, unknown>
  export default component
}
