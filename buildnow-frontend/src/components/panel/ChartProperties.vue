<template>
  <el-tabs type="border-card" style="height: 400px">
    <el-tab-pane label="General">
      <el-form ref="form" :model="attrObj">
        <el-form-item label="Name:" prop="name" label-width="150px">
          <el-input v-model="attrObj.chartName" style="width:30%" maxlength="50"
                    placeholder="Chart name"></el-input>
        </el-form-item>
        <br>
        <el-form-item label="Description:" prop="desc" label-width="150px">
          <el-input v-model="attrObj.chartDesc" type="textarea" :rows=12 style="width:100%">
          </el-input>
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
    name: "chart-properties",
    components: {
      // HotTable
    },
    data() {
      return {
        labelPosition: 'left',
        actionType: 'add',
        changelogid:''
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
    height: 280px;
    overflow: hidden;
    display: none;
  }
  #raw-json {
    height: 340px;
    overflow: auto;
  }
</style>
