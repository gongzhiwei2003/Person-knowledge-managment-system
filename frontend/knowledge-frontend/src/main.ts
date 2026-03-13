import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import '@vue-flow/core/dist/style.css';
import '@vue-flow/core/dist/theme-default.css'; // 可选，默认主题样式
import '@vue-flow/controls/dist/style.css';      // 控制器样式
// import '@vue-flow/background/dist/style.css';    // 背景样式


const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
