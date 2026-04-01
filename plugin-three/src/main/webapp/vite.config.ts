import { defineConfig } from "vite"
import vue from "@vitejs/plugin-vue"
import { resolve } from "path"

export default defineConfig({
  plugins: [vue()],
  define: {
    process: "{}",
    "process.env": {},
    "process.env.NODE_ENV": JSON.stringify("production")
  },
  build: {
    lib: {
      entry: resolve(__dirname, "src/index.ts"),
      name: "PluginThree",
      formats: ["es"],
      fileName: () => "plugin-three.js"
    },
    outDir: "dist",
    emptyOutDir: true
  }
})
