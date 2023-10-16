import { useSelector, useDispatch } from "react-redux";
import { RootState } from "../store";
// import ALL_NOCYCLE from "../nocycle";

const exportUtils = {
  refresh_v1: () => {
    return {
      v: exportUtils.useSelector((val) => val.system.RefreshID),
    };
  },
  refresh_v2: () => {
    return { refetchOnMountOrArgChange: true };
  },
  dispatch: useDispatch,
  useDispatch: useDispatch,
  useSelector<T>(callBack: (val: RootState) => T): T {
    return useSelector((val2: RootState) => {
      return callBack(val2);
    });
  },
};

export default exportUtils;
