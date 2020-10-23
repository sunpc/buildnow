<template>
    <div class="login-wrap">
        <div class="ms-title">BuildNow 2020.2</div>
        <div class="ms-login">
            <el-form :model="loginForm" status-icon :show-message="false" :rules="rules" ref="loginForm" label-width="0px">
                <el-form-item prop="username">
                    <el-input v-model="loginForm.username" placeholder="Your email address" @blur="getDefaultWorkspace"></el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input type="password" placeholder="Your password" v-model="loginForm.password" @keyup.enter.native="submitForm('loginForm')"></el-input>
                </el-form-item>
                <el-form-item prop="workspace">
                    <el-select v-model="loginForm.workspace" placeholder="Your development workspace">
                        <el-option
                        v-for="item in workspaces"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                        </el-option>
                    </el-select>
                </el-form-item>
                <div class="login-btn">
                    <el-button type="primary" @click="submitForm('loginForm')">Sign In</el-button>
                </div>
                <p style="font-size:12px;line-height:30px;"><a href="javascript:void(0);">Forget password?</a></p>
            </el-form>
        </div>
    </div>
</template>

<script>
export default {
  data: function() {
    return {
      loginForm: {
        workspace: "",
        username: "",
        password: ""
      },
      rules: {
        username: [{ required: true, message: "", trigger: "blur" }],
        password: [{ required: true, message: "", trigger: "blur" }]
      },
      workspaces: []
    };
  },
  methods: {
    getDefaultWorkspace() {
      const promise = this.$axios({
        url: "/api/rest/getDefaultEnv",
        method: "post",
        data: {
          userEmail: this.loginForm.username
        }
      }).then(
        res => {
          if (res.data.envCode && res.data.envCode != "") {
            this.loginForm.workspace = res.data.envCode;
          }
        }
      );
    },
    submitForm(formName) {
      this.$refs[formName].validate(valid => {
        if (valid) {
          const loading = this.$loading({
            lock: true,
            text: "w3ID authenticating...",
            spinner: "el-icon-loading",
            background: "rgba(0, 0, 0, 0.7)"
          });

          const promise = this.$axios({
            url: "/api/rest/auth",
            method: "post",
            data: {
              username: this.loginForm.username,
              password: this.loginForm.password,
              envcode: this.loginForm.workspace
            }
          }).then(
            res => {
              if (res.data.authenticated) {
                sessionStorage.setItem("workspace", this.loginForm.workspace);
                sessionStorage.setItem("username", this.loginForm.username);
                sessionStorage.setItem("userid", res.data.userId);
                sessionStorage.setItem("fullname", res.data.userName);
                sessionStorage.setItem("userrole", res.data.userRole);
                loading.close();
                this.$router.push("/");
              } else {
                this.$message.error(res.data.message);
                loading.close();
              }
            },
            err => {
              this.$message.error("System error. Please retry.");
              loading.close();
            }
          );
        } else {
          this.$message.error("Invalid input. Please retry.");
          return false;
        }
      });
    }
  },
  mounted() {
    const promise = this.$axios({
      url: "/api/rest/getEnvs",
      method: "post",
      data: {}
    }).then(res => {
      res.data.result.forEach(element => {
        this.workspaces.push({
          value: element.ENV_CODE.trim(),
          label: element.ENV_CODE.trim() + " - " + element.ENV_DESP.trim()
        });
        if (element.DEFAULT_INDC == 1) {
          this.loginForm.workspace = element.ENV_CODE.trim();
        }
      });
    });
  }
};
</script>

<style scoped>
.login-wrap {
  background: url(../assets/images/bg_signin.jpg);
  position: relative;
  width: 100%;
  height: 100%;
}
.ms-title {
  position: absolute;
  margin: 0;
  padding: 0;
  width: 500px; 
  height: 100px;
  left: 50%; 
  top: 50%; 
  margin-left: -250px; 
  margin-top: -180px;
  text-align: center;
  font-family: Arial, sans-serif;
  font-size: 32px;
  color: #fff;
}
.ms-login {
  position: absolute;
  margin: 0;
  padding: 40px;
  width: 500px; 
  height: 200px;
  left: 50%; 
  top: 50%; 
  margin-left: -290px; 
  margin-top: -100px;
  border-radius: 5px;
  background: #fff;
}
.login-btn {
  text-align: center;
}
.login-btn button {
  width: 100%;
  height: 36px;
}
.el-select {
  width: 100%;
}
</style>
