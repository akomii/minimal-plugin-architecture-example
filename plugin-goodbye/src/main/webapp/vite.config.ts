import {defineConfig} from "vite"
import vue from "@vitejs/plugin-vue"
import {dirname, resolve} from "path"
import {fileURLToPath} from "url"

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

export default defineConfig(({command}) => {
  const isDev = command === "serve"

  return {
    plugins: [vue()],
    base: "./",
    ...(isDev && {
      server: {
        proxy: {
          "/goodbye": {
            target: "http://localhost:8080",
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/goodbye/, '/api/plugins/goodbye')
          }
        }
      }
    }),
    build: {
      outDir: resolve(__dirname, "dist"),
      emptyOutDir: true
    }
  }
})
