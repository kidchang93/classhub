import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import store from './plugins/store.js';

const app = createApp(App);

app.use(router);
app.use(store);
app.mount('#app');

export default app; // 애플리케이션 인스턴스를 내보냅니다.
