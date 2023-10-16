const proxy = require("http-proxy-middleware");

module.exports = function (app) {
  app.use(
    proxy.createProxyMiddleware("/api/", {
      target: "http://localhost:8080/",
      headers: {
        //
      },
      changeOrigin: true, // 设置跨域请求
      pathRewrite: {
        // "^/api/": "", // 将/api/ 变为 ''
      },
    })
  );
};
