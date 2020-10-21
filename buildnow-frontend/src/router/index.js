import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/design'
    },
    {
      path: '/',
      component: resolve => require(['../components/Index.vue'], resolve),
      children:[
        {
          name: '/design',
          path: '/design/:id?/:uniqueKey?',
          component: resolve => require(['../components/Design.vue'], resolve),
          meta: { title: 'Design' }
        }
      ]
    }
  ]
})
