<template>
  <div>
    <div class="settings-container">
      <h3><i class="el-icon-setting"></i> Settings</h3>
      <el-row style="margin-top: 20px;">
        <el-tabs type="border-card" @tab-click="handleTabClick">
          <el-tab-pane>
            <span slot="label"><i class="iconfont icon-general"></i> General</span>
            <el-form ref="envForm" :model="env" label-width="120px">
              <el-form-item label="Workspace">
                <el-input v-model="env.envCode" disabled></el-input>
              </el-form-item>
              <el-form-item label="Description">
                <el-input v-model="env.envDesc"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" @click="saveEnv" :loading="buttonLoading">Save</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          <el-tab-pane>
            <span slot="label"><i class="iconfont icon-people"></i> People</span>
            <el-form :inline="true">
              <el-form-item>
                <el-input
                  placeholder="Find a member..."
                  v-model="findMember"
                  style="width: 300px;" clearable>
                  <i slot="prefix" class="el-input__icon el-icon-search"></i>
                </el-input>
              </el-form-item>
              <el-form-item style="float: right;">
                <el-button type="primary" size="small" @click="showAddMemberDialog">Add member</el-button>
              </el-form-item>
            </el-form>
            <el-table
              :data="foundMembers"
              style="width: 100%"
              max-height="450">
              <el-table-column
                label="Name">
                <template slot-scope="scope">
                  <div class="user-avator">
                    <img :src="scope.row.img">
                    <span style="margin-left: 10px">{{ scope.row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                label="Email"
                width="300">
                <template slot-scope="scope">
                  {{ scope.row.email }}
                </template>
              </el-table-column>
              <el-table-column
                label="Role"
                width="150">
                <template slot-scope="scope">
                  <el-dropdown trigger="click" @command="handleChangeRole">
                    <span class="el-dropdown-link">                      
                      {{ scope.row.role }}<i class="el-icon-arrow-down el-icon--right"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item :command="{email:scope.row.email,role:'Admin'}">Admin</el-dropdown-item>
                      <el-dropdown-item :command="{email:scope.row.email,role:'Developer'}">Developer</el-dropdown-item>
                      <el-dropdown-item :command="{email:scope.row.email,role:'Viewer'}">Viewer</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
              <el-table-column
                label="Status"
                width="150">
                <template slot-scope="scope">
                  <el-dropdown trigger="click" @command="handleChangeStatus">
                    <span class="el-dropdown-link">                      
                      {{ scope.row.status }}<i class="el-icon-arrow-down el-icon--right"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item :command="{email:scope.row.email,status:'Approved'}">Approved</el-dropdown-item>
                      <el-dropdown-item :command="{email:scope.row.email,status:'Canceled'}">Canceled</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </el-row>
    </div>
    <!-- Add Member Dialog -->
    <el-dialog title="Add member" :visible.sync="addMemberDialogVisible" :close-on-click-modal="false"
               :show-close="true">
      <el-form :model="addMemberForm">
        <el-form-item label="Name" label-width="120px">
          <el-input v-model="addMemberForm.name" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="Email" label-width="120px">
          <el-input v-model="addMemberForm.email" auto-complete="off"></el-input>
        </el-form-item>
        <el-form-item label="Role" label-width="120px">
          <el-select v-model="addMemberForm.role">
            <el-option label="Admin" value="Admin"></el-option>
            <el-option label="Developer" value="Developer"></el-option>
            <el-option label="Viewer" value="Viewer"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="addMember" :loading="buttonLoading">Add</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  export default {
    data: function () {
      return {
        env: {
          envCode: sessionStorage.getItem("workspace"),
          envDesc: ""
        },
        findMember: '',
        foundMembers: [],
        findFeature: '',
        foundFeatures: [],
        allMembers: [],
        allFeatures: [],
        defaults: {
          serverIP: "",
          ftpsPort: "",
          codePrefix: "",
          jobPrefix: "",
          user: "",
          jclLib: "",
          db2Schema: ""
        },
        addMemberDialogVisible: false,
        addFeatureDialogVisible: false,
        addMemberForm: {
          email: "",
          role: "Developer"
        },
        addFeatureForm: {
          featureName: "F",
          featureDesc: ""
        },
        buttonLoading: false
      };
    },
    methods: {
      handleTabClick(tab, event) {
        // Handle Tab clicks
        switch (tab.index) {
          case '1':   // People
            this.getMembers();
            break;
        }
      },
      getEnv() {
        const promise = this.$axios({
          url: '/api/rest/getEnv',
          method: 'post',
          data: {
            "envCode": sessionStorage.getItem("workspace")
          }
        }).then((res) => {
          this.env.envDesc = res.data.result['ENV_DESP'].trim();
        });
      },
      saveEnv() {
        this.buttonLoading = true;
        const promise = this.$axios({
          url: '/api/rest/saveEnv',
          method: 'post',
          data: {
            "envCode": sessionStorage.getItem("workspace"),
            "envDesc": this.env.envDesc,
            "lastActUserId": parseInt(sessionStorage.getItem('userid'))
          }
        }).then((res) => {
          this.buttonLoading = false;
          this.$message({
            type: 'success',
            message: 'Saved!'
          });
        }, (err) => {
          this.buttonLoading = false;
          this.$message({
            type: 'error',
            message: 'Save failed. Please contact administrator.'
          });
        });
      },
      getMembers() {
        const promise = this.$axios({
          url: '/api/rest/getMembers',
          method: 'post',
          data: {
            "envCode": sessionStorage.getItem("workspace")
          }
        }).then((res) => {
          this.allMembers.splice(0, this.allMembers.length);
          this.foundMembers.splice(0, this.foundMembers.length);
          res.data.result.forEach(element => {
            let member = {
              name: element['USER_NAME'].trim(),
              email: element['USER_EMAIL'].trim(),
              role: element['USER_ROLE'].trim(),
              status: element['APPR_STATUS'].trim(),
              img: '/static/images/avatar.jpg?email=' + element['USER_EMAIL'].trim()
            };
            this.allMembers.push(member);
            this.foundMembers.push(member);
          });
        });
      },
      showAddMemberDialog() {
        this.addMemberDialogVisible = true;
      },
      addMember() {
        this.buttonLoading = true;
        // add member
        let userName = this.addMemberForm.name.trim();
        this.addMemberForm.email = this.addMemberForm.email.trim();
        const promise = this.$axios({
          url: '/api/rest/addMember',
          method: 'post',
          data: {
            "envCode": sessionStorage.getItem("workspace"),
            "userName": userName,
            "userEmail": this.addMemberForm.email,
            "userRole": this.addMemberForm.role,
            "lastActUserId": parseInt(sessionStorage.getItem('userid'))
          }
        }).then((res) => {
          if (res.data.result == "success") {
            // push to allMembers
            let member = {
              name: userName,
              email: this.addMemberForm.email,
              role: this.addMemberForm.role,
              status: 'Approved',
              img: '/static/images/avatar.jpg?email=' + this.addMemberForm.email
            };
            this.allMembers.push(member);
            // push to foundMembers if found
            if (member.name.toUpperCase().indexOf(this.findMember.toUpperCase()) >= 0
              || member.email.toUpperCase().indexOf(this.findMember.toUpperCase()) >= 0) {
              this.foundMembers.push(member);
            }
            // reset and show message
            this.addMemberForm.email = "";
            this.buttonLoading = false;
            this.$message({
              type: 'success',
              message: 'Member Added.'
            });
          }
        }, (err) => {
          this.buttonLoading = false;
          this.$message({
            type: 'error',
            message: 'Add failed. Please contact administrator.'
          });
        });
      },
      handleChangeRole(command) {
        const promise = this.$axios({
          url: '/api/rest/updateMemberRole',
          method: 'post',
          data: {
            "envCode": sessionStorage.getItem("workspace"),
            "userEmail": command.email,
            "userRole": command.role,
            "lastActUserId": parseInt(sessionStorage.getItem('userid'))
          }
        }).then((res) => {
          if (res.data.result == "success") {
            this.allMembers.forEach(member => {
              if (member.email == command.email) {
                member.role = command.role;
              }
            });
          }
        });
      },
      handleChangeStatus(command) {
        const promise = this.$axios({
          url: '/api/rest/updateMemberStatus',
          method: 'post',
          data: {
            "envCode": sessionStorage.getItem("workspace"),
            "userEmail": command.email,
            "apprStatus": command.status,
            "lastActUserId": parseInt(sessionStorage.getItem('userid'))
          }
        }).then((res) => {
          if (res.data.result == "success") {
            this.allMembers.forEach(member => {
              if (member.email == command.email) {
                member.status = command.status;
              }
            });
          }
        });
      },
    },
    mounted() {
      let userRole = sessionStorage.getItem("userrole");
      if (userRole != "Admin") {
        this.$router.push("/");
      }
      this.getEnv();
    },
    watch: {
      findMember: function (val) {
        this.foundMembers.splice(0, this.foundMembers.length);
        this.allMembers.forEach(element => {
          if (element.name.toUpperCase().indexOf(val.toUpperCase()) >= 0
            || element.email.toUpperCase().indexOf(val.toUpperCase()) >= 0) {
            this.foundMembers.push(element);
          }
        });
      }
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

  .settings-container {
    width: 1000px;
    margin-top: 20px;
    margin-right: auto;
    margin-left: auto;
  }

  .user-avator * {
    vertical-align: middle;
    font-size: 14px;
  }

  .user-avator img {
    width: 35px;
    height: 35px;
    border-radius: 50%;
  }
</style>
