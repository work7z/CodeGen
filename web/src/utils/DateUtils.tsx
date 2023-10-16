import { logutils } from "./LogUtils";

import _ from "lodash";
import { Dot } from "./TranslationUtils";
import moment from "moment";
import { AxiosError } from "axios";

const DateUtils = {
  formatDateTime(time: any): string {
    return moment().format("YYYY-MM-DD HH:mm:ss");
  },
};
export default DateUtils;
