<template>
    <div class="header">
        <div class="collapse-btn" @click="gotoHome">
            <i class="iconfont icon-workspace"></i>
        </div>
        <div class="logo">{{workspace}}</div>
        <div class="header-left">
            <el-menu
                class="el-menu-demo"
                mode="horizontal"
                @select="handleSelect"
                background-color="#242f42"
                text-color="#fff"
                active-text-color="#ffd04b"
                :default-active="$route.name" router>
                <el-menu-item index="/home">Home</el-menu-item>
                <el-menu-item index="/design">Design</el-menu-item>
                <el-menu-item index="/docs">Docs</el-menu-item>

          </el-menu>
        </div>
        <div class="header-right">
            <div class="header-user-con">
                <div class="btn-fullscreen" @click="handleFullScreen">
                    <el-tooltip effect="dark" :content="fullscreen?`Cancel Full Screen`:`Full Screen`" placement="bottom">
                        <i class="el-icon-rank"></i>
                    </el-tooltip>
                </div>
                <!-- User image -->
                <div class="user-avator"><img :src="userimg"></div>
                <!-- User menu -->
                <el-dropdown class="user-name" trigger="click" @command="handleCommand">
                    <span class="el-dropdown-link">
                        <i class="el-icon-caret-bottom"></i>
                    </span>
                    <el-dropdown-menu slot="dropdown">
                        <el-dropdown-item>Signed in as <b>{{username}}</b></el-dropdown-item>
                        <el-dropdown-item divided command="help">Help</el-dropdown-item>
                        <el-dropdown-item v-if="displaySettings" command="settings">Settings</el-dropdown-item>
                        <el-dropdown-item divided command="signout">Sign out</el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
            </div>
        </div>
    </div>
</template>
<script>
import Bus from "./common/Bus";
export default {
  data() {
    return {
      collapse: false,
      fullscreen: false,
      message: 2
    };
  },
  computed: {
    username() {
      return sessionStorage.getItem("fullname");
    },
    userimg() {
      return (
        "/static/images/avatar.jpg?email=" + sessionStorage.getItem("username")
      );
    },
    workspace() {
      let workspace = sessionStorage.getItem("workspace");
      return workspace ? workspace : "BuildNow";
    },
    displaySettings() {
      let userRole = sessionStorage.getItem("userrole");
      if (userRole == "Admin") {
        return true;
      }
      return false;
    }
  },
  methods: {
    handleCommand(command) {
      if (command == "help") {
        this.$router.push("/docs");
      } else if (command == "settings") {
        this.$router.push("/settings");
      } else if (command == "signout") {
        sessionStorage.removeItem("username");
        sessionStorage.removeItem("workspace");
        sessionStorage.removeItem("userid");
        sessionStorage.removeItem("fullname");
        sessionStorage.removeItem("userrole");
        this.$router.push("/login");
        this.$router.go(0);
      }
    },
    gotoHome() {
      this.$router.push("/home");
    },
    handleFullScreen() {
      let element = document.documentElement;
      if (this.fullscreen) {
        if (document.exitFullscreen) {
          document.exitFullscreen();
        } else if (document.webkitCancelFullScreen) {
          document.webkitCancelFullScreen();
        } else if (document.mozCancelFullScreen) {
          document.mozCancelFullScreen();
        } else if (document.msExitFullscreen) {
          document.msExitFullscreen();
        }
      } else {
        if (element.requestFullscreen) {
          element.requestFullscreen();
        } else if (element.webkitRequestFullScreen) {
          element.webkitRequestFullScreen();
        } else if (element.mozRequestFullScreen) {
          element.mozRequestFullScreen();
        } else if (element.msRequestFullscreen) {
          // IE11
          element.msRequestFullscreen();
        }
      }
      this.fullscreen = !this.fullscreen;
    },
    handleSelect() {
      
    }
  }
};
</script>
<style scoped>
.header {
  position: relative;
  box-sizing: border-box;
  width: 100%;
  height: 70px;
  font-size: 22px;
  color: #fff;
}
.collapse-btn {
  float: left;
  padding: 0 21px;
  cursor: pointer;
  line-height: 70px;
}
.header .iconfont {
  font-size: 24px;
}
.header .logo {
  font-family: Arial Black, Arial, sans-serif;
  float: left;
  margin-left: 20px;
  width: 300px;
  line-height: 70px;
}
.header-left {
  float: left;
  padding: 5px;
}
.header-right {
  float: right;
  padding-right: 50px;
}
.header-user-con {
  display: flex;
  height: 70px;
  align-items: center;
}
.btn-fullscreen {
  transform: rotate(45deg);
  margin-right: 5px;
  font-size: 24px;
}
.btn-bell,
.btn-fullscreen {
  position: relative;
  width: 30px;
  height: 30px;
  text-align: center;
  border-radius: 15px;
  cursor: pointer;
}
.btn-bell-badge {
  position: absolute;
  right: 0;
  top: -2px;
  width: 8px;
  height: 8px;
  border-radius: 4px;
  background: #f56c6c;
  color: #fff;
}
.btn-bell .el-icon-bell {
  color: #fff;
}
.user-name {
  margin-left: 10px;
}
.user-avator {
  margin-left: 20px;
}
.user-avator img {
  display: block;
  width: 40px;
  height: 40px;
  border-radius: 50%;
}
.el-dropdown-link {
  color: #fff;
  cursor: pointer;
}
.el-dropdown-menu__item {
  text-align: center;
}
.el-menu--horizontal {
  border-right: none;
  border-bottom: solid 0px;
}
</style>
