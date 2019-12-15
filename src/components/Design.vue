<template>
  <div class="main">
    <el-container>
      <!-- Top toolbar -->
      <el-header>
        <div class="subheader editor-toolbar">
          <el-row>
            <el-col :span="16">
              <el-tooltip class="item" effect="dark" content="New" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-new" size="mini" class="toolbar-button" @click="handleNew" :disabled="readOnly"></el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="Save" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-save" size="mini" class="toolbar-button" @click="handleSave" :disabled="readOnly"></el-button>
              </el-tooltip>
              <div class="divider"></div>
              <el-tooltip class="item" effect="dark" content="Undo" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-undo" size="mini" class="toolbar-button" :disabled="readOnly"></el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="Redo" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-redo" size="mini" class="toolbar-button" :disabled="readOnly"></el-button>
              </el-tooltip>
              <div class="divider"></div>
              <el-tooltip class="item" effect="dark" content="Cut" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-cut" size="mini" class="toolbar-button" :disabled="readOnly"></el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="Copy" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-copy" size="mini" class="toolbar-button" :disabled="readOnly"></el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="Paste" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-paste" size="mini" class="toolbar-button" :disabled="readOnly"></el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="Delete" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-delete" size="mini" class="toolbar-button" @click="handleDeleteNode" :disabled="readOnly"></el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="Color" placement="bottom" :hide-after="hideafter">
                <el-button size="mini" class="toolbar-button" @click="handleColorChange">
                  <colorPicker v-model="shapeColor" defaultColor="#ffffff" @change="handleColorChange" :disabled="readOnly"></colorPicker>
                </el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="Properties" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-properties" size="mini" class="toolbar-button" @click="handleNodeMenuProp" :disabled="readOnly"></el-button>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="View Code" placement="bottom" :hide-after="hideafter">
                <el-button icon="iconfont icon-code" size="mini" class="toolbar-button" @click="handleViewCode" :disabled="readOnly"></el-button>
              </el-tooltip>
            </el-col>
            <el-col :span="8" style="text-align:right">
              <el-autocomplete
                class="search"
                popper-class="search-autocomplete"
                v-model="searchText"
                :fetch-suggestions="handleQuerySearch"
                placeholder="Search Charts"
                @select="handleQuerySelect"
                @focus="handleQueryFocus"
                @blur="handleQueryBlur">
                <i class="el-icon-search el-input__icon" slot="suffix" @click="handleQueryIconClick" style="line-height: 32px;"></i>
                <template slot-scope="{ item }">
                  <div class="name">{{ item.name }}</div>
                  <span class="addr">{{ item.text }}</span>
                </template>
              </el-autocomplete>
              <el-tooltip class="item" effect="dark" content="Version History" placement="bottom" :hide-after="hideafter" v-show="savedDesign">
                <el-dropdown trigger="click" @command="handleVersionHistory">
                  <el-button icon="iconfont icon-version-history" size="mini" class="toolbar-button"></el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item v-for="item in jobHistory" :key="item.uniqueKey" :command="item.uniqueKey" divided>
                      <el-tag>{{item.version}}</el-tag> <b>{{item.objectName}}</b> {{item.lastUptTime}} <i>by {{item.userName}}</i>
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </el-tooltip>
              <el-tooltip class="item" effect="dark" content="Make Current" placement="bottom" :hide-after="hideafter" v-show="viewHistory">
                <el-button icon="iconfont icon-make-current" size="mini" class="toolbar-button" @click="handleMakeCurrent"></el-button>
              </el-tooltip>
            </el-col>
          </el-row>
        </div>
      </el-header>
      <el-container>
        <!-- Left toolbar -->
        <el-aside v-if="!readOnly">
          <div id="flowComponents" class="full-left">
            <el-collapse v-model="activeLeftToolbarNames">
              <el-collapse-item title="Cursors" name="cursors">
                <div class="components-btn noComponent active" name="selectBtn">
                  <div><i class="iconfont icon-move"></i><span>Move</span></div>
                </div>
                <div class="components-btn noComponent" name="NOROUTING">
                  <div><i class="iconfont icon-line"></i><span>Straight</span></div>
                </div>
                <div class="components-btn noComponent" name="SIMPLEROUTING">
                  <div><i class="iconfont icon-polyline"></i><span>Orthogonal</span></div>
                </div>
              </el-collapse-item>
              <el-collapse-item title="General" name="general">
                <div class="components-btn" name="startComponent" type="start" draggable="true" category="general" data-show="S">
                  <div><i class="iconfont icon-begin"></i><span>Start</span></div>
                </div>
                <div class="components-btn" name="endComponent" type="end" draggable="true" category="general" data-show="E">
                  <div><i class="iconfont icon-end"></i><span>End</span></div>
                </div>
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="general" data-show="DATASET" activityType="FILE" codeType="ACTIVITY">
                  <div><i class="iconfont icon-parallelogram"></i><span>File</span></div>
                </div>
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="general" data-show="TABLE" activityType="TABLE" codeType="ACTIVITY">
                  <div><i class="iconfont icon-column"></i><span>Table</span></div>
                </div>                
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="processes" data-show="PROC" activityType="PROC" codeType="ACTIVITY">
                  <div><i class="iconfont icon-procedure"></i><span>Procedure</span></div>
                </div>
              </el-collapse-item>
              <el-collapse-item title="Transform" name="transform">
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="processes" data-show="SORT" activityType="SORT" codeType="ACTIVITY">
                  <div><i class="iconfont icon-rectangle"></i><span>Sort</span></div>
                </div>
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="processes" data-show="JOIN" activityType="JOIN" codeType="ACTIVITY">
                  <div><i class="iconfont icon-join"></i><span>Join</span></div>
                </div>
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="processes" data-show="SPLIT" activityType="SPLIT" codeType="ACTIVITY">
                  <div><i class="iconfont icon-split"></i><span>Split</span></div>
                </div>
              </el-collapse-item>
              <el-collapse-item title="Database" name="database">
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="processes" data-show="UNLOAD" activityType="UNLOAD" codeType="ACTIVITY">
                  <div><i class="iconfont icon-trapezium"></i><span>Unload</span></div>
                </div>
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="processes" data-show="LOAD" activityType="LOAD" codeType="ACTIVITY">
                  <div><i class="iconfont icon-trapezium-inverse"></i><span>Load</span></div>
                </div>
                <div class="components-btn" name="ordinaryActivity" type="activity" draggable="true" category="processes" data-show="SQL" activityType="SQL" codeType="ACTIVITY">
                  <div><i class="iconfont icon-sql"></i><span>SQL</span></div>
                </div>
              </el-collapse-item>
            </el-collapse>
          </div>
        </el-aside>
        <!-- Main panel -->
        <el-main>
          <div class="full-right tab">
            <div id="svgbg" class="svgbg"></div>
          </div>
        </el-main>
      </el-container>
    </el-container>
    <!-- Context Menu -->
    <context-menu class="right-menu" :target="contextMenuTarget" :show="contextMenuVisible" @update:show="(show) => contextMenuVisible = show">
      <a href="javascript:;" @click="handleDeleteNode" v-show="handleDeleteNodeVisible()"><i class="iconfont icon-delete"></i>&nbsp;&nbsp;Delete</a>
      <a href="javascript:;" @click="handleNodeMenuProp" v-show="handleNodeMenuPropVisible()"><i class="iconfont icon-properties"></i>&nbsp;&nbsp;<b>Properties</b></a>
    </context-menu>
    <!-- Properties Dialog -->
    <el-dialog :title="dialogTitle" :width="dialogWidth" :visible.sync="dialogShow" :close-on-click-modal="false" :show-close="true" :before-close="handleDialogCancel">
      <component ref="attrDialog" :attr-obj="attrObj" v-bind:is="dialogComponent" v-if="dialogReset"></component>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleDialogCancel">Cancel</el-button>
        <el-button type="primary" @click="handleDialogOK">OK</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import $ from "jquery";
  import * as d3 from "d3";
  import { uuid } from "vue-uuid";

  import ActivityProperties from './panel/ActivityProperties.vue';
  import ChartProperties from './panel/ChartProperties.vue';
  import ViewCode from './panel/ViewCode.vue';

  import GraphEditor from './common/GraphEditor'

  import "../assets/css/flowchart.css";

  export default {
    name: "design",
    inject: ["reload"],
    data () {
      return {
        jobHistory: [],
        activeLeftToolbarNames: ['cursors', 'general', 'transform', 'database'],
        hideafter: 1000,
        searchText: "",
        dialogWidth: "75%",
        dialogComponent: "",
        dialogTitle: "",
        dialogShow: false,
        dialogReset: true,
        attrObj: {},
        attrObjSave: {},
        contextMenuTarget: document.body,
        contextMenuVisible: false,
        shapeColor: "#ffffff",
        code: [],
        codeDDL: [],
        downloadConfig: {
          uuid: "",
          name: ""
        }
      };
    },
    components: {
      ActivityProperties,
      ChartProperties,
      ViewCode
    },
    computed: {
      readOnly() {
        let userRole = sessionStorage.getItem("userrole");
        let uniqueKey = this.$route.params.uniqueKey;
        if (userRole == "Viewer" || uniqueKey) {
          return true;
        }
        return false;
      },
      savedDesign() {
        let designId = this.$route.params.id;
        if (designId) {
          return true;
        }
        return false;
      },
      viewHistory() {
        let uniqueKey = this.$route.params.uniqueKey;
        if (uniqueKey) {
          return true;
        }
        return false;
      }
    },
    methods: {
      initFlowChart(cb) {
        // Get current date - added by Simon 9/4/2018
        let myDate = new Date();
        let nowDate = myDate.getDate() < 10 ? "0" + myDate.getDate() : myDate.getDate(),
          nowMonth = (myDate.getMonth() + 1) < 10 ? "0" + (myDate.getMonth() + 1) : (myDate.getMonth() + 1),
          nowYear = myDate.getFullYear().toString().substring(2);
        // Set initData
        let initData = {
          nodes: [],
          edges: [],
          authors: [],
          properties: {
            chartName: ""
          }
        };
        // Get saved graph - added by Simon 7/17/2018
        let designId = this.$route.params.id;
        let uniqueKey = this.$route.params.uniqueKey;
        // Query API
        if (designId) {
          sessionStorage.setItem('designId', designId);
          const loading = this.$loading({
            lock: true,
            text: 'Loading...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          const promise = this.$axios({
            url:'/api/rest/getObject',
            method: 'post',
            data: {
              objId: designId,
              uniqueKey: uniqueKey
            }
          }).then((res) => {
            if (res.data.result.length > 0) {
              initData = JSON.parse(res.data.result[0].JSON_FILE);
              // 根据edge.source.id重新关联node对象
              initData.edges.map(function(edge) {
                var source = initData.nodes.find(function(node) {
                  return node.id === edge.source.id;
                });
                var target = initData.nodes.find(function(node) {
                  return node.id === edge.target.id;
                });
                edge.source = source;
                edge.target = target;
                return edge;
              });
              // get job history - added by Simon 9/16/2018
              const promise = this.$axios({
                url:'/api/rest/getObjectHistory',
                method: 'post',
                data: {
                  objId: designId
                }
              }).then((res) => {
                this.jobHistory = [];
                for (let x in res.data.result) {
                  this.jobHistory.push({
                    version: res.data.result[x].VERSION.trim(),
                    objectName: res.data.result[x].OBJ_NAME.trim(),
                    uniqueKey: x > 0 ? res.data.result[x].UNIQUE_KEY.trim() : '',
                    userName: res.data.result[x].USER_NAME.trim(),
                    lastUptTime: res.data.result[x].LAST_UPT_TIME.trim()
                  });
                }
                loading.close();
                cb(initData);
              }, (err) => {
                loading.close();
                cb(initData);
              });
            }
          }, (err) => {
            loading.close();
            cb(initData);
          });
        } else if (sessionStorage.getItem('designId')) {
          this.$router.replace('/design/' + sessionStorage.getItem('designId'));
          this.reload();
        } else {
          cb(initData);
        }
      },
      initCanvas() {
        let that = this;
        /**
         * 左侧组件
         */
        function handleComponentsBtn() {
          $(this).siblings().removeClass("active").end().addClass("active");
          var graph_active = that.graphPool.getGraphByActiveEdit(),
            state = graph_active.state,
            nodeName = $(this).attr("name"),
            container = $(".svgbg");
          if (nodeName === "NOROUTING" || nodeName === "SIMPLEROUTING") {
            state.drawLine = nodeName;
            container.on("mouseover mouseout", ".conceptG", function(e) {
              if (e.type === "mouseover") {
                this.style.cursor = "crosshair";
              } else if (e.type == "mouseout") {
                this.style.cursor = "move";
              }
            });
          } else {
            container.off("mouseover mouseout", ".conceptG");
            state.drawLine = null;
          }
        }

        /**
         * 存放所有 GraphEditor 对象及方法
         */
        that.graphPool = {
          pools: [],
          updateGraphActiveById: function(containerId) {
            this.pools.forEach(function(graph) {
              if (graph.containerId === containerId) {
                graph.state.activeEdit = true;
              } else {
                graph.state.activeEdit = false;
              }
            });
          },
          getGraphByActiveEdit: function() {
            var graph_active = this.pools.find(function(graph) {
              return graph.state.activeEdit;
            });
            return graph_active;
          },
          removeGraphFromPools: function(containerId) {
            var pools = this.pools;
            for (var i = 0; i < pools.length; i++) {
              if (pools[i].containerId === containerId) {
                pools.splice(i, 1);
              }
            }
          }
        };

        /**
         * MAIN LOGIC
         */
        var container = d3.select(".svgbg").node(),
          containerId = "tab_main";
        var svg = d3.select(".svgbg")
          .append("svg")
          .attr("id", "svgpanel") // by Simon 2018/5/15
          .attr("style", "width: 100%; height: 100%; display: block; position: absolute; background-image: none;"); // by Simon 2018/7/8
        // Initialize the chart
        this.initFlowChart(function(initData) {
          // Define GraphEditor
          window.graph_main = new GraphEditor(
            containerId,
            svg,
            initData.nodes,
            initData.edges,
            initData.authors,
            initData.properties
          );
          that.graphPool.pools.push(graph_main);
          graph_main.updateGraph();
          // Leftbar buttons
          $("#flowComponents .components-btn").on("click", handleComponentsBtn);
          // Context menu // by Simon 2018/5/15
          that.contextMenuTarget = document.getElementById("svgpanel");
          // Double clicks // by Simon 2018/5/15
          $("#svgpanel").on("dblclick", function() {
            var graph_active = that.graphPool.getGraphByActiveEdit();
            var selectedNode = graph_active.state.selectedNode,
              selectedEdge = graph_active.state.selectedEdge;
            that.handleAttrObject(graph_active, selectedNode, selectedEdge);
          });
        });
      },  // End initCanvas
      handleAttrObject(graph_active, selectedNode, selectedEdge) {
        // Handle the attribute object // added by Simon 5/19/2018
        let that = this;
        // Disable graph keydown overrides // added by Simon 6/14/2018
        graph_active.state.graphKeyDown = false;
        if (selectedNode && selectedNode.type == "activity") {
          // Set dialog component // by Simon 7/4/2018
          console.log(selectedNode)
          switch (selectedNode.codeType) {
            case "ACTIVITY":
              that.dialogComponent = "ActivityProperties";
              that.dialogTitle = "Activity Properties";
              break;
          }
          // Set attributes
          that.attrObj = JSON.parse(JSON.stringify(selectedNode));
          that.attrObjSave = selectedNode;
          // Show dialog
          that.dialogShow = true;
        } else if (selectedEdge) {
          // that.attrdialog = true;
          // that.attrObj = selectedEdge;
          // graph_active.state.mouseUpLink = null;
        } else {
          // show Chart Properties dialog
          that.dialogShow = true;
          that.dialogComponent = "ChartProperties";
          that.dialogTitle = "Chart Properties";
          that.attrObj = JSON.parse(JSON.stringify(graph_active.properties));
          that.attrObjSave = graph_active.properties;
        }
      },
      handleNew() {
        // Create a new graph // added by Simon 2018/8/20
        sessionStorage.removeItem("designId");
        this.$router.push("/design");
        this.reload();
      },
      handleSave() {
        // Save the graph // added by Simon 2018/5/16
        let that = this;
        let graph_active = that.graphPool.getGraphByActiveEdit();
        // Prompt job name // added by Simon 2018/6/21
        if (graph_active.properties.chartName == "") {
          graph_active.state.graphKeyDown = false;
          this.$prompt('Please enter a chart name', 'New', {
            confirmButtonText: 'OK',
            cancelButtonText: 'Cancel',
            inputPattern: /^[a-zA-Z][0-9a-zA-Z]{2,7}$/,
            inputErrorMessage: 'Chart name must be 3-8 alphanumeric characters, starts with an alphabet.'
          }).then(({ value }) => {
            graph_active.state.graphKeyDown = true;
            graph_active.properties.chartName = value.toUpperCase();
            graph_active.properties.jobSymbolics[0].symValue = value.toUpperCase();
            that.handleSaveCallback();
          }).catch(() => {
            graph_active.state.graphKeyDown = true;
          });
        } else {
          that.handleSaveCallback();
        }
      },
      handleSaveCallback() {
        // Save the graph
        let that = this;
        let graph_active = that.graphPool.getGraphByActiveEdit();
        let requestObjs = [];
        // prepare graph json
        let graphJson = {
          nodes: graph_active.nodes,
          edges: graph_active.edges,
          authors: graph_active.authors,
          properties: graph_active.properties
        };
        requestObjs.push({
          "objectId": graphJson.properties.chartId,
          "objectType": "JOB",
          "objectName": graphJson.properties.chartName,
          "objectJson": JSON.stringify(graphJson),
          "userId": parseInt(sessionStorage.getItem('userid')),
          "workspace": sessionStorage.getItem("workspace")
        });
        const loading = this.$loading({
          lock: true,
          text: 'Saving...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        const promise = this.$axios({
          url:'/api/rest/saveJson',
          method: 'post',
          data: requestObjs
        }).then((res) => {
          if (res.data.result == "success") {
            // call back
            that.$message({
              message: "Saved!",
              type: "success"
            });
            loading.close();
            // process router
            that.$router.replace('/design/' + graphJson.properties.chartId);
          } else {
            that.$message.error('Save failed!');
            loading.close();
          }
        }, (err) => {
          that.$message.error('Save failed!');
          loading.close();
        });
      },
      handleViewCode() {
        // disable keydown
        let graph_active = this.graphPool.getGraphByActiveEdit();
        graph_active.state.graphKeyDown = false;
        // show View Code dialog
        console.log(this.graphPool.getGraphByActiveEdit());
        this.dialogShow = true;
        this.dialogComponent = "ViewCode";
        this.dialogTitle = "View Code";
        this.attrObj = this.graphPool.getGraphByActiveEdit();
        this.attrObjSave = null;
      },
      handleDeleteNodeVisible() {
        // If DELETE is visible // added by Simon 2018/5/15
        let that = this;
        if (that.graphPool) {
          var graph_active = that.graphPool.getGraphByActiveEdit();
          var selectedNode = graph_active.state.selectedNode,
            selectedEdge = graph_active.state.selectedEdge;
          if (selectedNode || selectedEdge) {
            return true;
          }
        }
        return false;
      },
      handleDeleteNode() {
        // Delete a node
        let that = this;
        var graph_active = that.graphPool.getGraphByActiveEdit();
        var selectedNode = graph_active.state.selectedNode,
          selectedEdge = graph_active.state.selectedEdge;
        // Hide context menu
        this.contextMenuVisible = false;
        // Delete the component
        if (selectedNode) {
          var nodes = graph_active.nodes;
          nodes.splice(nodes.indexOf(selectedNode), 1);
          graph_active.spliceLinksForNode(selectedNode);
          // if (selectedNode.component === "blockActivity") {
          //   var containerId = "tab_" + selectedNode.id;
          //   $(".full-right [data-tab=" + containerId + "]").remove();
          //   that.graphPool.removeGraphFromPools(containerId);
          // }
          graph_active.state.selectedNode = null;
          graph_active.updateGraph();
        } else if (selectedEdge) {
          var edges = graph_active.edges;
          edges.splice(edges.indexOf(selectedEdge), 1);
          graph_active.state.selectedEdge = null;
          graph_active.updateGraph();
        }
      },
      handleNodeMenuPropVisible() {
        // If PROPERTIES is visible // added by Simon 2018/5/16
        let that = this;
        if (that.graphPool) {
          var graph_active = that.graphPool.getGraphByActiveEdit();
          var selectedNode = graph_active.state.selectedNode;
          if (selectedNode && selectedNode.type == "activity") {
            return true;
          } else if (!selectedNode) {
            return true;
          }
        }
        return false;
      },
      handleNodeMenuProp() {
        // Hide context menu
        this.contextMenuVisible = false;
        // Show Properties dialog
        let that = this;
        var graph_active = that.graphPool.getGraphByActiveEdit();
        var selectedNode = graph_active.state.selectedNode,
          selectedEdge = graph_active.state.selectedEdge;
        that.handleAttrObject(graph_active, selectedNode, selectedEdge);
      },
      handleColorChange() {
        // 修改颜色
        var graph_active = this.graphPool.getGraphByActiveEdit();
        var selectedNode = graph_active.state.selectedNode;
        if (selectedNode) {
          selectedNode.fillColor = this.shapeColor;
          graph_active.updateGraph();
        }
      },
      handleDialogOK() {
        // Data Validation - Added by Simon 2018/8/30
        if (typeof this.$refs.attrDialog.beforeSave === "function") {
          let validateResult = this.$refs.attrDialog.beforeSave();
          if (!validateResult.success) {
            this.$message.error(validateResult.message);
            return;
          }
        }
        // 合并attrObj - Redesigned by Simon 2018/7/24
        // deepClone is defined here to replace jquery.extend to remove redundant array items.
        // $.extend(true, this.attrObjSave, this.attrObj);
        if (this.attrObjSave != null) {
          let deepClone = function(copy, obj) {
            // Handle Array or Object
            if (obj instanceof Array || obj instanceof Object) {
              for (var attr in obj) {
                if (obj[attr] && obj[attr] instanceof Array || obj[attr] instanceof Object) {                
                  if (!copy[attr]) {
                    if (obj[attr] instanceof Array) {
                      copy[attr] = [];
                    } else if (obj[attr] instanceof Object) {
                      copy[attr] = {};
                    }
                  }
                  deepClone(copy[attr], obj[attr]);
                } else if (obj[attr] != null) {
                  copy[attr] = obj[attr];
                }
              }
            }
            // Remove redundant array items
            if (copy instanceof Array && obj instanceof Array) {
              let copyLen = copy.filter(function(item) {
                return !item.hasOwnProperty("name") || item.name != null;
              }).length;
              let objLen = obj.filter(function(item) {
                return !item.hasOwnProperty("name") || item.name != null;
              }).length;
              if (copyLen > objLen) {
                copy.splice(objLen, copyLen - objLen);
              }
            }
          };
          deepClone(this.attrObjSave, this.attrObj);
        }
        // 关闭对话框
        this.dialogShow = false;
        // 刷新画板
        let graph_active = this.graphPool.getGraphByActiveEdit();
        graph_active.state.graphKeyDown = true;
        graph_active.updateGraph();
        // 强制刷新组件
        this.dialogReset = false;
        this.$nextTick(() => {
          this.dialogReset = true
        });
      },
      handleDialogCancel() {
        // 刷新画板
        let graph_active = this.graphPool.getGraphByActiveEdit();
        graph_active.state.graphKeyDown = true;
        // 关闭对话框
        this.dialogShow = false;
        // 强制刷新组件
        this.dialogReset = false;
        this.$nextTick(() => {
          this.dialogReset = true
        });
      },
      handleQuerySearch(queryString, cb) {
        // 模糊查询Jobs
        const promise = this.$axios({
          url:'/api/rest/searchJobs',
          method: 'post',
          data: {
            queryString: queryString,
            envCode: sessionStorage.getItem("workspace")
          }
        }).then((res) => {
          cb(res.data.result);
        });
      },
      handleQueryFilter(queryString) {
        return (restaurant) => {
          return (restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
        };
      },
      handleQuerySelect(item) {
        this.$router.replace('/design/' + item.uuid);
        this.reload();
      },
      handleQueryFocus(item) {
        var graph_active = this.graphPool.getGraphByActiveEdit();
        graph_active.state.graphKeyDown = false;
      },
      handleQueryBlur(item) {
        var graph_active = this.graphPool.getGraphByActiveEdit();
        graph_active.state.graphKeyDown = true;
      },
      handleQueryIconClick(ev) {
        // console.log(ev);
      },
      handleVersionHistory(uniqueKey) {
        // Handle version history - added by Simon 9/15/2018
        let designId = this.$route.params.id;
        if (uniqueKey && uniqueKey != "") {
          this.$router.replace('/design/' + designId + "/" + uniqueKey);
          this.reload();
        } else {
          this.$router.replace('/design/' + designId);
          this.reload();
        }
      },
      handleMakeCurrent() {
        // Handle make current - added by Simon 9/16/2018
        let designId = this.$route.params.id;
        let uniqueKey = this.$route.params.uniqueKey;
        let userId = parseInt(sessionStorage.getItem('userid'));
        const loading = this.$loading({
          lock: true,
          text: 'Updating...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.7)'
        });
        const promise = this.$axios({
          url:'/api/rest/makeObjectCurrent',
          method: 'post',
          data: {
            "objId": designId,
            "uniqueKey": uniqueKey,
            "userId": userId
          }
        }).then((res) => {
          if (res.data.result == "success") {
            this.$router.replace('/design/' + designId);
            this.reload();
          } else {
            this.$message.error('Update failed!');
            loading.close();
          }
        }, (err) => {
          this.$message.error('Update failed!');
          loading.close();
        });
      }
    },
    mounted() {
      this.initCanvas();
    }
  };
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  #rMenu {
    position: absolute;
    width: 82px !important;
    display: none;
  }
  .right-menu {
    position: fixed;
    background: #fff;
    border: solid 1px rgba(0, 0, 0, 0.2);
    border-radius: 3px;
    z-index: 999;
    display: none;
    border: 1px solid #eee;
    box-shadow: 0 0.5em 1em 0 rgba(0, 0, 0, 0.1);
    border-radius: 1px;
  }
  .right-menu a {
    width: 100px;
    height: 28px;
    line-height: 28px;
    text-align: left;
    font-size: 12px;
    display: block;
    color: #1a1a1a;
    text-decoration: none;
    padding: 2px;
  }
  .right-menu a:hover {
    color: #fff;
    background: #409eff;
  }
  .subheader {
    box-sizing: border-box;
    border-bottom: 1px solid #ccc;
    padding: 5px 0 5px 10px;
  }
  .subheader .divider {
    display: inline-block;
    height: 14px;
    border-right: 1px solid #c7cacd;
  }
  .svgbg {
    background-image: url(data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGRlZnM+PHBhdHRlcm4gaWQ9ImdyaWQiIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHBhdGggZD0iTSAwIDEwIEwgNDAgMTAgTSAxMCAwIEwgMTAgNDAgTSAwIDIwIEwgNDAgMjAgTSAyMCAwIEwgMjAgNDAgTSAwIDMwIEwgNDAgMzAgTSAzMCAwIEwgMzAgNDAiIGZpbGw9Im5vbmUiIHN0cm9rZT0iI2UwZTBlMCIgb3BhY2l0eT0iMC4yIiBzdHJva2Utd2lkdGg9IjEiLz48cGF0aCBkPSJNIDQwIDAgTCAwIDAgMCA0MCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjZTBlMGUwIiBzdHJva2Utd2lkdGg9IjEiLz48L3BhdHRlcm4+PC9kZWZzPjxyZWN0IHdpZHRoPSIxMDAlIiBoZWlnaHQ9IjEwMCUiIGZpbGw9InVybCgjZ3JpZCkiLz48L3N2Zz4=) !important;
    box-sizing: border-box;
    border: 1px dashed #fff;
    height: 88vh;
  }
  .full-left {
    height: 93vh;
    padding: 5px;
    box-sizing: border-box;
    border-right: 1px solid #ccc;
    background-color: #eee;
  }
  .full-right {
    height: 88vh;
  }
  /* .svgcontain .el-tab-pane {
    height: 80vh;
  } */
  .conceptG text {
    pointer-events: none;
  }
  .el-header {
    height: auto !important;
    padding: 0 !important;
  }
  .el-aside {
    width: 120px !important;
  }
  .el-main {
    padding: 0 !important;
  }
  .el-button+.el-tooltip {
    margin-left: 0 !important;
  }
  .editor-toolbar {
    background-color: #eee;
  }
  .toolbar-button {
    background-color: #eee;
  }
</style>
