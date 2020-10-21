<template>
    <div class="wrapper">
        <v-head></v-head>
        <!-- <v-sidebar></v-sidebar> -->
        <div class="content-box" :class="{'content-collapse':collapse}">
            <div class="content">
                <transition name="move" mode="out-in">
                    <!-- by Simon 6/14/2018: Removed keep-alive so that the router view can be refreshed when route changed. -->
                    <!-- <keep-alive> -->
                    <router-view></router-view>
                    <!-- </keep-alive> -->
                </transition>
            </div>
        </div>

    </div>
</template>

<script>
    import Bus from './common/Bus';
    import vHead from './Header.vue';
    // import vSidebar from './Sidebar.vue';
    
    export default {
        data(){
            return {
                collapse: true
            }
        },
        components:{
            vHead
            // , vSidebar
        },
        created(){
            Bus.$on('collapse', msg => {
                this.collapse = msg;
            })
        }
    }

</script>


<style scoped>
    .content {
        width: "calc(100%)";
        height: "calc(100%)";
        padding: 0px;
    }
</style>
