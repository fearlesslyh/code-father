
import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './styles/global.css'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)

// 初始化主题
import { useThemeStore } from './stores/theme'
useThemeStore() // 主题store会自动应用保存的主题或默认主题（白天模式）

app.use(router)
app.use(Antd)

app.mount('#app')
