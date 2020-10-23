// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import axios from 'axios'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'    // added by Simon 2018/5/18
import VueContextMenu from '@xunlei/vue-context-menu'
import ColorPicker from './components/common/vue-color-picker'  // added by Simon 2018/6/17

import 'element-ui/lib/theme-chalk/index.css'
import './assets/icon/iconfont.css'

Vue.config.productionTip = false
Vue.use(ElementUI, { locale });    // added locale by Simon 2018/5/18
Vue.use(VueContextMenu);
Vue.use(ColorPicker);

Vue.prototype.$axios = axios;

router.beforeEach((to, from, next) => {
  const username = sessionStorage.getItem('username');
  if(!username && to.path !== '/login'){
      next('/login');
  } else {
      next();
  }
})

/* eslint-disable no-new */
new Vue({
  router,
  render: h => h(App)
}).$mount('#app');
