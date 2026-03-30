import {createRouter, createWebHashHistory} from 'vue-router'
import {defineAsyncComponent} from 'vue'
import Settings from './components/Settings.vue'
import PluginA from './components/PluginA.vue'
import PluginB from './components/PluginB.vue'

const ExternalPluginThree = defineAsyncComponent(() =>
    // @ts-ignore
    import(/* @vite-ignore */ '/api/plugins/ui/plugin-three/plugin-three.js')
)
export const availablePlugins = [
  {id: 'plug-a', name: 'Plugin A', path: '/plugin-a', component: PluginA},
  {id: 'plug-b', name: 'Plugin B', path: '/plugin-b', component: PluginB},
  {id: 'plug-three', name: 'Plugin 3', path: '/plugin-three', component: ExternalPluginThree}
]

export const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {path: '/', redirect: '/settings'},
    {path: '/settings', component: Settings},
    ...availablePlugins.map(p => ({path: p.path, component: p.component}))
  ]
})
