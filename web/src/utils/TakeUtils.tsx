import ALL_NOCYCLE, { RootState2 } from "../nocycle";

const TakeUtils = {
  take<T>(callBack: (val: RootState2) => T): T | null {
    let st = ALL_NOCYCLE.store?.getState();
    if (st == null) {
      return null;
    }
    return callBack(st);
  },
};
export default TakeUtils;
