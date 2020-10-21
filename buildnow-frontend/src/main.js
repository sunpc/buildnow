// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import axios from 'axios'
import ElementUI from 'element-ui'
import locale from 'element-ui/lib/locale/lang/en'    // added by Simon 2018/5/18
import VueContextMenu from '@xunlei/vue-context-menu'
import CodeMirror from 'vue-codemirror'
import ColorPicker from './components/common/vue-color-picker'  // added by Simon 2018/6/17

import 'element-ui/lib/theme-chalk/index.css'
//import 'handsontable/dist/handsontable.full.css'      // added by Simon 2018/5/26
import './assets/icon/iconfont.css'
import 'codemirror/lib/codemirror.css'

Vue.config.productionTip = false
Vue.use(ElementUI, { locale });    // added locale by Simon 2018/5/18
Vue.use(VueContextMenu);
Vue.use(ColorPicker);
Vue.use(CodeMirror);

Vue.prototype.$axios = axios;

/* eslint-disable no-new */
new Vue({
  router,
  render: h => h(App)
}).$mount('#app');
