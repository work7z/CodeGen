const _ = require("lodash");
const log4js = require("log4js");

let logger = log4js.getLogger("system");
logger.level = "debug";

const common_utils = {
  log: logger,
};

module.exports = common_utils;
