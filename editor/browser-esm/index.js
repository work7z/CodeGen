import * as monaco from "monaco-editor";

self.MonacoEnvironment = {
  getWorkerUrl: function (moduleId, label) {
    console.log("loading work url", moduleId, label);
    let prefix = "../e/";
    if (false && window.dev_mode) {
      prefix = "http://127.0.0.1:15000/e/";
    }
    if (label === "sql") {
      return prefix + "sql.worker.bundle.js";
    }
    if (label === "json") {
      return prefix + "json.worker.bundle.js";
    }
    if (label === "css" || label === "scss" || label === "less") {
      return prefix + "css.worker.bundle.js";
    }
    if (label === "html" || label === "handlebars" || label === "razor") {
      return prefix + "html.worker.bundle.js";
    }
    if (label === "typescript" || label === "javascript") {
      return prefix + "ts.worker.bundle.js";
    }
    return prefix + "editor.worker.bundle.js";
  },
};

window.CodeGenPluginED = () => {
  return {
    monaco,
  };
};

// monaco.editor.create(document.getElementById("container"), {
//   value: ["function x() {", '\tconsole.log("Hello world!");', "}"].join("\n"),
//   language: "javascript",
// });
