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
  TableLoadingOption,
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
import GVisualTable from "../GVisualTable";

export default observer((props) => {
  if (props.type == "epoch") {
    return <GVisualTable {...props} />;
  }
  const [mycolw, onMyColW] = useState(props.fixedColWidthArr);
  return (
    <div className="control-table-wrapper">
      <Example options={false} showOptionsBelowExample={true}>
        <Table
          columnWidths={mycolw ? mycolw : null}
          onColumnWidthChanged={
            !mycolw
              ? null
              : (...x) => {
                  console.log("chg column width", x);
                  const newarr = [...mycolw];
                  newarr[x[0]] = x[1];
                  onMyColW(newarr);
                }
          }
          numFrozenColumns={_.get(props, "leftfix", 2)}
          numRows={_.get(props, "tableInfo.pageInfo.pageSize", props.size)}
          loadingOptions={
            gstore.staticServerPageData.loading
              ? [
                  TableLoadingOption.CELLS,
                  TableLoadingOption.COLUMN_HEADERS,
                  TableLoadingOption.ROW_HEADERS,
                ]
              : null
          }
        >
          {_.map(props.cols, (x, d, n) => {
            return (
              <Column key={x.name} name={x.name} cellRenderer={x.render} />
            );
          })}
        </Table>
      </Example>
    </div>
  );
});
