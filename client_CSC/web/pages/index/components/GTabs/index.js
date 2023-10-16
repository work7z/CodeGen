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
  Tree,
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
import React, { useRef, cloneElement } from "react";
import ReactDOM from "react-dom";
import gutils from "../../utils";
import { useState } from "react";
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
import Tabs, { TabPane } from "rc-tabs";
import "./rc-tabs.less";
import { Resizable } from "re-resizable";
import _ from "lodash";
import { DndProvider, useDrag, useDrop } from "react-dnd";
import { HTML5Backend } from "react-dnd-html5-backend";

const type = "DraggableTabNode";

const DraggableTabNode = ({ index, children, moveNode }) => {
  const ref = useRef();
  const [{ isOver, dropClassName }, drop] = useDrop({
    accept: type,
    collect: (monitor) => {
      const { index: dragIndex } = monitor.getItem() || {};
      if (dragIndex === index) {
        return {};
      }
      return {
        isOver: monitor.isOver(),
        dropClassName: "rc-dropping",
      };
    },
    drop: (item) => {
      moveNode(item.index, index);
    },
  });
  const [, drag] = useDrag({
    type,
    item: { index },
    collect: (monitor) => ({
      isDragging: monitor.isDragging(),
    }),
  });
  drop(drag(ref));
  return (
    <div ref={ref} className={isOver ? dropClassName : ""}>
      {children}
    </div>
  );
};

class DraggableTabs extends React.Component {
  state = {
    order: [],
  };

  moveTabNode = (dragKey, hoverKey) => {
    const newOrder = this.state.order.slice();
    const { children } = this.props;

    React.Children.forEach(children, (c) => {
      if (newOrder.indexOf(c.key) === -1) {
        newOrder.push(c.key);
      }
    });

    const dragIndex = newOrder.indexOf(dragKey);
    const hoverIndex = newOrder.indexOf(hoverKey);

    newOrder.splice(dragIndex, 1);
    newOrder.splice(hoverIndex, 0, dragKey);

    this.setState({
      order: newOrder,
    });
  };

  renderTabBar = (props, DefaultTabBar) => (
    <DefaultTabBar {...props}>
      {(node) => (
        <DraggableTabNode
          key={node.key}
          index={node.key}
          moveNode={this.moveTabNode}
        >
          {node}
        </DraggableTabNode>
      )}
    </DefaultTabBar>
  );

  render() {
    const { order } = this.state;
    const { children } = this.props;

    const tabs = [];
    React.Children.forEach(children, (c) => {
      tabs.push(c);
    });

    const orderTabs = tabs.slice().sort((a, b) => {
      const orderA = order.indexOf(a.key);
      const orderB = order.indexOf(b.key);

      if (orderA !== -1 && orderB !== -1) {
        return orderA - orderB;
      }
      if (orderA !== -1) {
        return -1;
      }
      if (orderB !== -1) {
        return 1;
      }

      const ia = tabs.indexOf(a);
      const ib = tabs.indexOf(b);

      return ia - ib;
    });

    return (
      <DndProvider backend={HTML5Backend}>
        <Tabs
          destroyInactiveTabPane={true}
          renderTabBar={this.renderTabBar}
          {...this.props}
        >
          {orderTabs}
        </Tabs>
      </DndProvider>
    );
  }
}

export default observer((props) => {
  const value = props.obj.value;
  const list = props.obj.list;
  return (
    <DraggableTabs
      animated={false}
      editable={{
        showAdd: false,
        onEdit(type, info) {
          if (type == "remove") {
            console.log("action remove", type, info);
            const findObjIdx = _.findIndex(list, (x) => x.id == info.key);
            const nextKey =
              findObjIdx + 1 >= _.size(list) ? findObjIdx - 1 : findObjIdx + 1;
            if (_.get(list, [findObjIdx, "id"]) == props.obj.value) {
              const nextKeyStr = _.get(list, [nextKey, "id"]);
              props.obj.value = nextKeyStr;
            }
            props.obj.list = _.filter(
              _.concat(
                _.slice(list, 0, findObjIdx),
                _.slice(list, findObjIdx + 1)
              ),
              (x) => !_.isNil(x)
            );
          }
        },
      }}
      activeKey={value}
      onChange={(e) => {
        props.obj.value = e;
        // console.log("chaging value", e);
        // props.onChange(e);
      }}
    >
      {_.map([...list], (x, d, n) => {
        return (
          <TabPane tab={x.label} key={x.id}>
            {props.renderTabPane(x, d, n)}
          </TabPane>
        );
      })}
    </DraggableTabs>
  );
});
