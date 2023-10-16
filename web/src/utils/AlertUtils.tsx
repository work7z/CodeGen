import { logutils } from "./LogUtils";

import _ from "lodash";
import { Dot } from "./TranslationUtils";
import { Position, Toaster, ToastProps, Intent } from "@blueprintjs/core";
import gutils from "./GlobalUtils";

let rootInst = Toaster.create({
  className: " m_all_recipe",
  position: Position.TOP,
});
const AlertUtils = {
  alertError(intent: Intent, e: Error, additionalMsgLabel?: string) {
    let msg = gutils.getErrMsg(e);
    AlertUtils.alert(intent, {
      message: additionalMsgLabel ? `[${additionalMsgLabel}]` : "" + msg,
    });
  },
  alert(intent: Intent, config: ToastProps) {
    config.intent = intent;
    rootInst.show(config);
  },
};
export default AlertUtils;
