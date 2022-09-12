const COMMON_SIZE = 25;

const constants = {
  getPageData(obj) {
    return {
      loading: false,
      formModel: {
        FOLDER_ID: null,
      },
      afterConfirmFunc: null,
      toggle_status_loading: false,
      alertType: "create",
      addModelFailures: {},
      isAddModelPass: false,
      initModel: constants.initModel(),
      addModel: constants.initModel(),
      formNeeds: {
        groups: [],
        netcards: [],
      },
      pageData: [],
      pageCount: 0,
      pageInfo: {
        pageIndex: 1,
        pageSize: constants.COMMON_SIZE,
      },
    };
    // return _.merge(
    //   ,
    //   obj
    // );
  },
  COMMON_SIZE,
  commonPropsForDialog: () => {
    return {
      loading: false,
      open: false,
      confirm: null,
      title: null,
      icon: null,
      cancelText: "Cancel",
      confirmIntent: null,
      confirmText: "Confirm",
    };
  },
  extraModelProps: () => {
    return {
      addModelFailures: {},
      isAddModelPass: false,
    };
  },
  initModel: () => {
    return {
      FOLDER_ID: 1,
      IS_LOCAL_SSL: 0,
      FILE_PATH: null,
      CONTEXT_PATH: null,
      LOCAL_LISTEN_PORT: null,
      LOCAL_LISTEN_SSL_PORT: null,
      LIST_DIRECTORY: 1,
      PLAIN_VIEW_MODE: 1,
      BRIEF: null,
      NAME: null,
      BOOT_FLAG: 1,
    };
  },
};

export default constants;
