const path = require("path");
const utils = {
  getCrtPath: (relativePath) => {
    return path.join(__dirname, relativePath);
  },
};
module.exports = {
  mode: "development",
  entry: {
    app: "./index.js",
    "editor.worker": "monaco-editor/esm/vs/editor/editor.worker.js",
    "json.worker": "monaco-editor/esm/vs/language/json/json.worker",
    "css.worker": "monaco-editor/esm/vs/language/css/css.worker",
    // "sql.worker": "monaco-editor/esm/vs/language/sql/sql.worker",
    "html.worker": "monaco-editor/esm/vs/language/html/html.worker",
    "ts.worker": "monaco-editor/esm/vs/language/typescript/ts.worker",
  },
  output: {
    globalObject: "self",
    filename: "[name].bundle.js",
    publicPath: "../e/",
    // path: utils.getCrtPath("dist"),
    // path: path.resolve(__dirname, "..", "..", "web", "dist", "e"),
    path: path.resolve(__dirname, "..", "..", "editor_dist", "e"),
  },
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
      },
      {
        test: /\.ttf$/,
        use: ["file-loader"],
      },
    ],
  },
};
