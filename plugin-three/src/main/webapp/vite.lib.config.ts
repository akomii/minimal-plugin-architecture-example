import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    lib: {
      entry: 'src/App.vue',
      name: 'PluginThree',
      formats: ['es'],
      fileName: () => 'plugin-three.js'
    },
    outDir: '../../../target/classes/static/ui/plugin-three',
    emptyOutDir: false
  }
})