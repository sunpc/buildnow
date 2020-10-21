<template>
  <el-tabs v-model="activeName" type="border-card" tab-position="left" style="height: 400px">
    <div id="raw-json">
      <pre>{{ attrObj }}</pre>
    </div>
  </el-tabs>
</template>

<script>  
  export default {
    name: "view-code",      
    components: {
    },
    data() {
      return {
        activeName: "member0"
      };
    },  
    props: {
      attrObj: {}
    },
    methods: {
    }  
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  #raw-json {
    height: 340px;
    overflow: auto;
  }
  #raw-json * {    
    -moz-user-select: text;
    -o-user-select: text;
    -khtml-user-select: text;
    -webkit-user-select: text;
    -ms-user-select: text;
    user-select: text;
  }
</style>
