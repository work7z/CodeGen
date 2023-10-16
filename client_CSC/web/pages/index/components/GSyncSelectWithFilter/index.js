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
  Menu,
  MenuItem,
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
import React from "react";
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
import { Select } from "@blueprintjs/select";
import _ from "lodash";

export default observer((props) => {
  const value = props.obj[props.index];
  const chgFunc = (val) => {
    props.obj[props.index] = val;
  };
  let list = props.list;
  if (_.isNil(value) && !_.isEmpty(list)) {
    // gutils.defer(() => {
    //   chgFunc[list[0].value];
    // });
  }
  if (_.isEmpty(list)) {
    // list = [
    //   {
    //     label: "Loading...",
    //     value: -1,
    //   },
    // ];
  }

  const crtViewLabel = _.chain(list)
    .find((x) => x.value == value)
    .get("label")
    .value();
  function escapeRegExpChars(text) {
    return text.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
  }
  function highlightText(text = "", query) {
    let lastIndex = 0;
    const words = query
      .split(/\s+/)
      .filter((word) => word.length > 0)
      .map(escapeRegExpChars);
    if (words.length === 0) {
      return [text];
    }
    const regexp = new RegExp(words.join("|"), "gi");
    const tokens = [];
    while (true) {
      const match = regexp.exec(text);
      if (!match) {
        break;
      }
      const length = match[0].length;
      const before = text.slice(lastIndex, regexp.lastIndex - length);
      if (before.length > 0) {
        tokens.push(before);
      }
      lastIndex = regexp.lastIndex;
      tokens.push(<strong key={lastIndex}>{match[0]}</strong>);
    }
    const rest = text.slice(lastIndex);
    if (rest.length > 0) {
      tokens.push(rest);
    }
    return tokens;
  }

  const renderMenu = ({ items, itemsParentRef, query, renderItem }) => {
    const renderedItems = items.map(renderItem).filter((item) => item != null);
    return (
      <Menu ulRef={itemsParentRef}>
        <MenuItem
          disabled={true}
          text={`Found ${renderedItems.length} items matching "${query}"`}
        />
        {renderedItems}
      </Menu>
    );
  };

  return (
    <Select
      className="g-miniselect"
      // itemListRenderer={renderMenu}
      popoverProps={{
        minimal: false,
        className: "my-popover-mini",
      }}
      items={list || []}
      itemPredicate={(querystr, item, index) => {
        if (_.isNil(item.label) || !_.isString(item.label)) {
          return false;
        }
        return (
          item.label.toLowerCase().indexOf(querystr.toLowerCase()) != -1 ||
          item.label
            .toLowerCase()
            .replaceAll(/\W/g, "")
            .indexOf(querystr.toLowerCase()) != -1
        );
      }}
      itemRenderer={(x, { handleClick, modifiers, query }) => {
        const item = x;
        return (
          <MenuItem
            active={x.value == value}
            disabled={false}
            label={item.label}
            key={item.value}
            onClick={handleClick}
            text={highlightText(item.label, query)}
          />
        );
      }}
      noResults={<MenuItem disabled={true} text="No results." />}
      onItemSelect={(item) => {
        console.log("item select");
        chgFunc(item.value);
      }}
    >
      <Button
        loading={props.loading}
        style={{
          maxWidth: "319px",
          overflow: "hidden",
          textOverflow: "ellipsis",
        }}
        icon={props.icon}
        text={crtViewLabel || "Click here to select"}
        rightIcon="double-caret-vertical"
      />
    </Select>
  );
});
