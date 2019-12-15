<template>
  <el-tabs type="border-card" style="height: 400px">
    <!-- General -->
    <el-tab-pane label="General">
      <el-form ref="form" :model="attrObj" label-width="120px" :rules="validateRules">
        <el-form-item label="ID:" v-if="false">
          <el-input v-model="attrObj.id" disabled></el-input>
        </el-form-item>
        <el-form-item label="Name:" prop="name">
          <el-input v-model="attrObj.name" maxlength="8"></el-input>
        </el-form-item>
        <el-form-item label="Abstract:" style="margin-top:20px;">
          <el-input v-model="attrObj.desc"></el-input>
        </el-form-item>   
        <el-form-item label="Description:" style="margin-top:20px;">
          <el-input type="textarea" :rows="5" v-model="attrObj.longDesc"></el-input>
        </el-form-item>      
      </el-form>
    </el-tab-pane>    
    <!-- Raw -->
    <el-tab-pane label="Raw">
      <div id="raw-json">
        <pre>{{ attrObj }}</pre>
      </div>
    </el-tab-pane>
  </el-tabs>
</template>

<script>
  export default {
    name: "activity-properties",
    components: {
    },
    data() {
      return {
        validateRules: {
          name: [
            { required: true, message: "Name cannot be empty", trigger: "blur" },
            {
              pattern: /^[A-Za-z0-9@#\u4e00-\u9fa5]+$/,
              message: "Invalid name"
            }
          ],
        }
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
  #hot-layout {
    width: 100%;
    height: 220px;
    overflow: hidden;
    display: block;
  }
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
