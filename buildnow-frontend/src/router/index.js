import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/',
      component: resolve => require(['../components/Index.vue'], resolve),
      children: [
        {
          name: '/home',
          path: '/home',
          component: resolve => require(['../components/Home.vue'], resolve),
          meta: { title: 'Home' }
        },
        {
          name: '/design',
          path: '/design/:id?/:uniqueKey?',
          component: resolve => require(['../components/Design.vue'], resolve),
          meta: { title: 'Design' }
        },
        {
          name: '/docs',
          path: '/docs',
          component: resolve => require(['../components/Docs.vue'], resolve),
          meta: { title: 'Docs' }
        }
      ]
    },
    {
      path: '/login',
      component: resolve => require(['../components/Login.vue'], resolve)
    }
  ]
})
