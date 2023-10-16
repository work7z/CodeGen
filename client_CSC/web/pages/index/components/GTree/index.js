import {
  Callout,
  PanelStack,
  ProgressBar,
  AnchorButton,
  Tooltip,
  Dialog,
  Drawer,
  Overlay,
  Alert,
  RadioGroup,
  Radio,
  ButtonGroup,
  TextArea,
  Intent,
  Position,
  Toaster,
  Checkbox,
  ContextMenu,
  NumericInput,
  FormGroup,
  HTMLSelect,
  ControlGroup,
  InputGroup,
  Navbar,
  NavbarHeading,
  NonIdealState,
  NavbarDivider,
  NavbarGroup,
  Alignment,
  Classes,
  Icon,
  Card,
  Elevation,
  Button,
  PanelStack2,
} from "@blueprintjs/core";
import { Example, IExampleProps } from "@blueprintjs/docs-theme";
import {
  ColumnHeaderCell,
  Cell,
  Column,
  Table,
  Regions,
} from "@blueprintjs/table";
import React from "react";
import ReactDOM from "react-dom";
import gutils from "../../utils";
import { useState, useEffect } from "react";
import {
  useStores,
  useAsObservableSource,
  useLocalStore,
  useObserver,
} from "mobx-react-lite";
import { Provider, observer, inject } from "mobx-react";
var createHistory = require("history").createBrowserHistory;
import {
  withRouter,
  HashRouter as Router,
  Switch,
  Route,
  Link,
  useHistory,
} from "react-router-dom";
var { autorun, observable } = require("mobx");
import gstore from "../../store.js";
import "./index.less";
import {
  Classes as Popover2Classes,
  ContextMenu2,
  Tooltip2,
} from "@blueprintjs/popover2";
import Tree from "rc-tree";
import "./rc-tree.less";
import _ from "lodash";

function generateData(x = 3, y = 2, z = 1, gData = []) {
  // x：每一级下的节点总数。y：每级节点里有y个节点、存在子节点。z：树的level层级数（0表示一级）
  function _loop(_level, _preKey, _tns) {
    const preKey = _preKey || "0";
    const tns = _tns || gData;

    const children = [];
    for (let i = 0; i < x; i++) {
      const key = `${preKey}-${i}`;
      tns.push({ title: `${key}-label`, key: `${key}-key` });
      if (i < y) {
        children.push(key);
      }
    }
    if (_level < 0) {
      return tns;
    }
    const __level = _level - 1;
    children.forEach((key, index) => {
      tns[index].children = [];
      return _loop(__level, key, tns[index].children);
    });

    return null;
  }
  _loop(z);
  return gData;
}
export function calcTotal(x = 3, y = 2, z = 1) {
  /* eslint no-param-reassign:0 */
  const rec = (n) => (n >= 0 ? x * y ** n-- + rec(n) : 0);
  return rec(z + 1);
}
console.log("总节点数（单个tree）：", calcTotal());
// 性能测试：总节点数超过 2000（z要小）明显感觉慢。z 变大时，递归多，会卡死。

export const gData = generateData();

function isPositionPrefix(smallPos, bigPos) {
  if (bigPos.length < smallPos.length) {
    return false;
  }
  // attention: "0-0-1" "0-0-10"
  if (
    bigPos.length > smallPos.length &&
    bigPos.charAt(smallPos.length) !== "-"
  ) {
    return false;
  }
  return bigPos.substr(0, smallPos.length) === smallPos;
}
// console.log(isPositionPrefix("0-1", "0-10-1"));

// arr.length === 628, use time: ~20ms
export function filterParentPosition(arr) {
  const levelObj = {};
  arr.forEach((item) => {
    const posLen = item.split("-").length;
    if (!levelObj[posLen]) {
      levelObj[posLen] = [];
    }
    levelObj[posLen].push(item);
  });
  const levelArr = Object.keys(levelObj).sort();
  for (let i = 0; i < levelArr.length; i += 1) {
    if (levelArr[i + 1]) {
      levelObj[levelArr[i]].forEach((ii) => {
        for (let j = i + 1; j < levelArr.length; j += 1) {
          levelObj[levelArr[j]].forEach((_i, index) => {
            if (isPositionPrefix(ii, _i)) {
              levelObj[levelArr[j]][index] = null;
            }
          });
          levelObj[levelArr[j]] = levelObj[levelArr[j]].filter((p) => p);
        }
      });
    }
  }
  let nArr = [];
  levelArr.forEach((i) => {
    nArr = nArr.concat(levelObj[i]);
  });
  return nArr;
}
// console.log(filterParentPosition(
//   ['0-2', '0-3-3', '0-10', '0-10-0', '0-0-1', '0-0', '0-1-1', '0-1']
// ));

function loopData(data, callback) {
  const loop = (d, level = 0) => {
    d.forEach((item, index) => {
      const pos = `${level}-${index}`;
      if (item.children) {
        loop(item.children, pos);
      }
      callback(item, index, pos);
    });
  };
  loop(data);
}

function spl(str) {
  return str.split("-");
}
function splitLen(str) {
  return str.split("-").length;
}

export function getFilterExpandedKeys(data, expandedKeys) {
  const expandedPosArr = [];
  loopData(data, (item, index, pos) => {
    if (expandedKeys.indexOf(item.key) > -1) {
      expandedPosArr.push(pos);
    }
  });
  const filterExpandedKeys = [];
  loopData(data, (item, index, pos) => {
    expandedPosArr.forEach((p) => {
      if (
        ((splitLen(pos) < splitLen(p) && p.indexOf(pos) === 0) || pos === p) &&
        filterExpandedKeys.indexOf(item.key) === -1
      ) {
        filterExpandedKeys.push(item.key);
      }
    });
  });
  return filterExpandedKeys;
}

function isSibling(pos, pos1) {
  pos.pop();
  pos1.pop();
  return pos.join(",") === pos1.join(",");
}

export function getRadioSelectKeys(data, selectedKeys, key) {
  const res = [];
  const pkObjArr = [];
  const selPkObjArr = [];
  loopData(data, (item, index, pos) => {
    if (selectedKeys.indexOf(item.key) > -1) {
      pkObjArr.push([pos, item.key]);
    }
    if (key && key === item.key) {
      selPkObjArr.push(pos, item.key);
    }
  });
  const lenObj = {};
  const getPosKey = (pos, k) => {
    const posLen = splitLen(pos);
    if (!lenObj[posLen]) {
      lenObj[posLen] = [[pos, k]];
    } else {
      lenObj[posLen].forEach((pkArr, i) => {
        if (isSibling(spl(pkArr[0]), spl(pos))) {
          // 后来覆盖前者
          lenObj[posLen][i] = [pos, k];
        } else if (spl(pkArr[0]) !== spl(pos)) {
          lenObj[posLen].push([pos, k]);
        }
      });
    }
  };
  pkObjArr.forEach((pk) => {
    getPosKey(pk[0], pk[1]);
  });
  if (key) {
    getPosKey(selPkObjArr[0], selPkObjArr[1]);
  }

  Object.keys(lenObj).forEach((item) => {
    lenObj[item].forEach((i) => {
      if (res.indexOf(i[1]) === -1) {
        res.push(i[1]);
      }
    });
  });
  return res;
}

export default observer((props) => {
  const treeData = props.obj[props.index];
  console.log("treeData", treeData, props);

  const key_sltKeys = props.unikey + "sltk";
  const key_expKeys = props.unikey + "expk";

  const onDrop = (info) => {
    console.log(info);
    const dropKey = info.node.key;
    const dragKey = info.dragNode.key;
    const dropPos = info.node.pos.split("-");
    const dropPosition =
      info.dropPosition - Number(dropPos[dropPos.length - 1]);

    const loop = (data, key, callback) => {
      for (let i = 0; i < data.length; i++) {
        if (data[i].key === key) {
          return callback(data[i], i, data);
        }
        if (data[i].children) {
          loop(data[i].children, key, callback);
        }
      }
    };
    const data = [...treeData];

    // Find dragObject
    let dragObj;
    loop(data, dragKey, (item, index, arr) => {
      arr.splice(index, 1);
      dragObj = item;
    });

    debugger;

    if (!info.dropToGap) {
      // Drop on the content
      loop(data, dropKey, (item) => {
        item.children = item.children || [];
        // where to insert 示例添加到头部，可以是随意位置
        item.children.unshift(dragObj);
      });
    } else if (
      // (info.node.props.children || []).length >= 0 && // Has children
      !_.isNil(info.node.props.children) &&
      info.node.props.isLeaf != true &&
      info.node.props.expanded && // Is expanded
      dropPosition === 1 // On the bottom gap
    ) {
      loop(data, dropKey, (item) => {
        item.children = item.children || [];
        // where to insert 示例添加到头部，可以是随意位置
        item.children.unshift(dragObj);
        // in previous version, we use item.children.push(dragObj) to insert the
        // item to the tail of the children
      });
    } else {
      let ar;
      let i;
      loop(data, dropKey, (item, index, arr) => {
        ar = arr;
        i = index;
      });
      if (dropPosition === -1) {
        ar.splice(i, 0, dragObj);
      } else {
        ar.splice(i + 1, 0, dragObj);
      }
    }

    props.obj[props.index] = data;

    if (props.chgfunc) {
      props.chgfunc(data);
    }
  };
  let m_sltKeys = gutils.safeparsenow(localStorage.getItem(key_sltKeys));
  if (_.isNil(m_sltKeys) || m_sltKeys == "null") {
    m_sltKeys = [];
  }
  let [sltKeys = [], onSltKeys] = useState(m_sltKeys);
  if (_.isNil(sltKeys)) {
    sltKeys = [];
  }
  let m_expKeys = gutils.safeparsenow(localStorage.getItem(key_expKeys));
  if (_.isNil(m_expKeys) || m_expKeys == "null") {
    m_expKeys = [];
  }
  let [expKeys = [], onExpKeys] = useState(m_expKeys);
  if (_.isNil(expKeys)) {
    expKeys = [];
  }
  useEffect(() => {
    localStorage.setItem(key_expKeys, JSON.stringify(expKeys));
    localStorage.setItem(key_sltKeys, JSON.stringify(sltKeys));
  }, [sltKeys, expKeys]);
  console.log("expKeys", { expKeys, sltKeys });

  useEffect(() => {
    function cancelSelect() {
      gutils.defer(() => {
        if (window.treeClick) {
          window.treeClick = false;
        } else {
          onSltKeys([]);
        }
      }, 30);
    }
    window.addEventListener("click", cancelSelect);

    return () => {
      window.removeEventListener("click", cancelSelect);
    };
  }, []);

  return (
    <div>
      <Tree
        showLine={true}
        allowDrop={(options) => {
          return true;
        }}
        onRightClick={(e, k) => {
          window.treeClick = true;
          console.log("right click", e, k);
          const node = e.node;
          e.event.preventDefault();
          if (props.onRightClick) {
            props.onRightClick(e);
          }
          if (_.indexOf(sltKeys, node.key) == -1) {
            onSltKeys([node.key]);
            console.log("you click me PUSH", e, node, sltKeys);
          }
          // $(e.event.target).click();
          console.log("on right click", e);
        }}
        defaultExpandAll={true}
        defaultExpandParent={true}
        selectedKeys={sltKeys}
        expandedKeys={expKeys}
        onExpand={(xxxe, e) => {
          window.treeClick = true;
          let node = e.node;
          console.log("on expand", e);
          let idxarr = _.indexOf(expKeys, node.key);
          if (idxarr == -1) {
            onExpKeys([...expKeys, node.key]);
          } else {
            onExpKeys(gutils.pickArr(expKeys, idxarr));
          }
          if (_.indexOf(sltKeys, node.key) == -1) {
            onSltKeys([node.key]);
            console.log("you click me PUSH", e, node, sltKeys);
          }
        }}
        onDoubleClick={(e, node) => {
          console.log("double click", props, node);
          console.log("you click me", e, node, sltKeys);
          e.expanded = !e.expanded;
          // already selected, and it's a folder
          if (!node.isLeaf) {
            let idxarr = _.indexOf(expKeys, node.key);
            if (idxarr == -1) {
              onExpKeys([...expKeys, node.key]);
            } else {
              onExpKeys(gutils.pickArr(expKeys, idxarr));
            }
          }
        }}
        onClick={(e, node) => {
          window.treeClick = true;
          if (_.indexOf(sltKeys, node.key) == -1) {
            onSltKeys([node.key]);
            console.log("you click me PUSH", e, node, sltKeys);
          }
        }}
        draggable
        onDrop={onDrop}
        treeData={treeData}
        switcherIcon={(props) => {
          // || _.isEmpty(props.children)
          if (props.isLeaf) {
            return "";
          }
          return (
            <Icon
              intent="none"
              style={{
                color: "#5c7080",
              }}
              size={12}
              icon={
                props.isLeaf
                  ? "document"
                  : props.expanded
                  ? "chevron-down"
                  : "chevron-right"
              }
            />
          );
        }}
        icon={(props, a, b, c) => {
          // console.log("rendering icon", props, a, b, c);
          return (
            <Icon
              size={12}
              intent="none"
              style={{
                color: props.isConnect ? "green" : "#5c7080",
              }}
              icon={
                props.isLeaf
                  ? props.isConnect
                    ? "data-connection"
                    : "database"
                  : props.expanded
                  ? "folder-open"
                  : "folder-close"
              }
            />
          );
        }}
      />
    </div>
  );
});
