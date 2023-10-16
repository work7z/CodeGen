import { logutils } from "./LogUtils";
import {
  createApi,
  fetchBaseQuery,
  TypedUseQueryHookResult,
  TypedUseQueryStateResult,
  TypedUseQuerySubscriptionResult,
  TypedUseMutationResult,
} from "@reduxjs/toolkit/query/react";
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
  MenuItem,
  Radio,
  ButtonGroup,
  TextArea,
  HotkeysProvider,
  Intent,
  Position,
  Toaster,
  Checkbox,
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
} from "@blueprintjs/core";

import _ from "lodash";
import { Dot } from "./TranslationUtils";
import gutils from "./GlobalUtils";
import AlertUtils from "./AlertUtils";

const QueryUtils = {
  validateResult: (
    res_toolCategory: TypedUseQueryHookResult<any, any, any>,
    options: { label: string }
  ): React.JSX.Element | undefined => {
    let errObj = _.get(res_toolCategory, "data.errors");
    if (res_toolCategory.isFetching) {
      return (
        <NonIdealState
          title={Dot("aHAfR", "Fetching data for {0}...", options.label)}
        ></NonIdealState>
      );
    } else if (res_toolCategory.isError) {
      return (
        <NonIdealState
          title={Dot(
            "YQN9u",
            "An Error occurred while loading {0}, please check below detail.",
            options.label
          )}
          description={
            `[${res_toolCategory.status}] ` +
            gutils.getWebErrMsg(res_toolCategory.error)
          }
          action={
            <Button
              onClick={() => {
                res_toolCategory.refetch();
                AlertUtils.alert("success", {
                  message: Dot("jPNCb", "Retried."),
                });
              }}
            >
              {Dot("ySVf-", "Re-try this Request")}
            </Button>
          }
        ></NonIdealState>
      );
    }
    return undefined;
  },
};
export default QueryUtils;
