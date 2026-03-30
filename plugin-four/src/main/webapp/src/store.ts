import {ref, watch} from 'vue'

const saved = localStorage.getItem('activePlugins')
export const activePluginIds = ref<string[]>(saved ? JSON.parse(saved) : [])

watch(activePluginIds, (newVal) => {
  localStorage.setItem('activePlugins', JSON.stringify(newVal))
}, {deep: true})
