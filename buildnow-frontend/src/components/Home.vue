<template>
    <div class="home-bg">
        <div class="home-container">
            <el-row>
                <h1 align="center" style="padding-top: 50px; margin-bottom: 0.5rem; font-family: inherit; font-size:2rem; font-weight: 400; line-height: 1.2;">
                  Let&apos;s build a workflow chart now!
                </h1>
                <p align="center" style="margin-top: 50px; padding-bottom: 80px">
                    <el-button type="primary" size="large" style="width:180px;" @click="gotoDesign()">Build Now</el-button>
                </p>
            </el-row>
            <el-row>
                <el-col :span="12">
                    <el-card class="box-card">
                        <div slot="header" class="clearfix">
                            <span>Recent activities</span>
                        </div>
                        <div v-for="act in activities" :key="act.uuid" class="text item">
                            <a :href="'#/design/' + act.uuid">{{act.name}}</a>
                            <span style="float: right; padding: 3px 0">{{act.updateTime}}</span>
                        </div>
                    </el-card>
                </el-col>
                <el-col :span="12">
                    <el-card class="box-card">
                        <div slot="header" class="clearfix">
                            <span>Docs</span>
                        </div>
                        <div v-for="doc in docs" :key="doc.id" class="text item">
                            <a :href="'#/docs/' + doc.url">{{doc.name}}</a>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>
</template>

<script>
export default {
  data: function() {
    return {
      activities: [],
      docs: [
        { id: 1, name: "Quick Start Guide", url: "" },
        { id: 2, name: "Knowledge Center", url: "" }
      ]
    };
  },
  methods: {
    gotoDesign(designId) {
      sessionStorage.removeItem("designId");
      if (designId) {
        this.$router.push("/design/" + designId);
      } else {
        this.$router.push("/design");
      }
    }
  },
  mounted() {
    let that = this;
    const promise = this.$axios({
      url:'/api/rest/getRecentJobs',
      method: 'post',
      data: {
        envCode: sessionStorage.getItem("workspace")
      }
    }).then((res) => {
      res.data.result.forEach(element => {          
        that.activities.push({
            name: element.OBJ_NAME,
            uuid: element.OBJ_ID,
            updateTime: element.LAST_UPT_TIME
        });
      });
    });
  }
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
a:link {
  color: blue;
}
a:visited {
  color: blue;
}
a:hover {
  color: blue;
  text-decoration: underline;
}
a:active {
  color: blue;
}

.home-bg {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0) 60%, #fff),
    linear-gradient(70deg, #dbedff 32%, #ebfff0);
}
.home-container {
  width: 1000px;
  margin-right: auto;
  margin-left: auto;
}
.el-col {
  padding-left: 10px;
  padding-right: 10px;
}
.text {
  font-size: 14px;
}
.item {
  margin-bottom: 18px;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}
.box-card {
  width: 480px;
}
</style>