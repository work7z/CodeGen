import TokenUtils from "./TokenUtils";

const SystemUtils = {
  logout() {
    TokenUtils.clearSystemToken();
    SystemUtils.reloadPage();
  },
  reloadPage() {
    location.reload();
  },
};

export default SystemUtils;
