// utils and node.js api
const fs = require("fs");
const sh = require("shelljs");
const _ = require("lodash");
const os = require("os");
const path = require("path");
// webpack import
const webpack = require("webpack");
const compiler = webpack.compiler;
const HappyPack = require("happypack");
const MiniCssExtractPlugin = require("mini-css-extract-plugin");
const VueLoaderPlugin = require("vue-loader/lib/plugin");
const AddAssetHtmlPlugin = require("add-asset-html-webpack-plugin");
const CleanWebpackPlugin = require("clean-webpack-plugin");
const HtmlWebpackPlugin = require("html-webpack-plugin");
const CopyWebpackPlugin = require("copy-webpack-plugin");
// webpack init
const happyThreadPool = HappyPack.ThreadPool({
  size: os.cpus().length,
});
// function declare
const utils = {
  getCrtPath: (relativePath) => {
    return path.join(__dirname, relativePath);
  },
};
function createStyleUseObject(isModule = true) {
  return [
    { loader: MiniCssExtractPlugin.loader },
    {
      loader: "css-loader",
      query:
        isModule && false
          ? {
              modules: true,
              localIdentName: "[name]__[local]___[hash:base64:5]",
            }
          : { modules: false },
    },
    { loader: "less-loader" },
  ];
}
// variables declare
var babelrc = JSON.parse(
  fs.readFileSync(utils.getCrtPath(".babelrc"), "UTF-8")
);
var entryobj = {};
var htmlPlugins = [];
var distdir = utils.getCrtPath("dist");
// add page by read dir
var pagesPath = utils.getCrtPath("./pages");
var pagesArr = fs.readdirSync(pagesPath);
_.forEach(pagesArr, (eachPage) => {
  var chunkName = eachPage;
  var indexHtmlPath = utils.getCrtPath(`./pages/${chunkName}/index.html`);
  var indexPageArg = {
    template: indexHtmlPath,
    filename: utils.getCrtPath(`./dist/${chunkName}/index.html`),
    chunks: [chunkName],
  };
  var indexPagePlugin = new HtmlWebpackPlugin(indexPageArg);
  htmlPlugins.push(indexPagePlugin);
  entryobj[chunkName] = utils.getCrtPath(`./pages/${chunkName}/index.js`);
});
if (entryobj["index"]) {
  htmlPlugins.push(
    new HtmlWebpackPlugin({
      template: utils.getCrtPath(`./pages/index/index.html`),
      filename: utils.getCrtPath(`./dist/index.html`),
      chunks: ["index"],
    })
  );
}

var configureWebpack = require("./config/configureWebpack");

module.exports = (mode) => {
  var isDev = mode === "dev";
  // environment
  process.env.NODE_ENV = isDev ? "development" : "production";
  var contentHashValue = isDev ? "hash" : "contenthash";
  var webpackConfig = {
    devServer: {
      writeToDisk: true,
      inline: true,
      hot: true,
      inline: true,
      progress: true,
      publicPath: "/",
      contentBase: utils.getCrtPath("dist"),
      compress: true,
      port: 1234,
    },
    entry: entryobj,
    resolve: {
      extensions: [".js", ".vue", ".json", ".ts", ".tsx"],
      alias: {},
    },
    plugins: _.filter(
      [
        new VueLoaderPlugin(),
        new CleanWebpackPlugin([distdir], {
          allowExternal: true,
        }),
        ...htmlPlugins,
        new webpack.ProvidePlugin({
          _: "lodash",
          moment: "moment",
          axios: "axios",
          vue: ["vue", "default"],
          Vue: ["vue", "default"],
          vuex: "vuex",
          Vuex: "vuex",
          VueRouter: "vue-router",
          ELEMENT: "element-ui",
          // gutils: path.join(__dirname, 'gutils.js'),
          // gapi: [path.join(__dirname, 'gapi.js'),'default'],
        }),
        new MiniCssExtractPlugin({
          filename: "[name].[" + contentHashValue + "].css",
        }),
        new HappyPack({
          id: "happybabel",
          loaders: ["babel-loader", "xml-loader"],
          threadPool: happyThreadPool,
          verbose: true,
        }),
        new CopyWebpackPlugin([
          {
            from: "/Users/jerrylai/Documents/PersonalProjects/denote-fe/editor_dist/e",
            to: "/Users/jerrylai/Documents/PersonalProjects/denote-fe/web/dist/e",
          },
        ]),
        new CopyWebpackPlugin([
          {
            from: "/Users/jerrylai/Documents/PersonalProjects/denote-fe/web/node_modules/@vscode/codicons/dist",
            to: "/Users/jerrylai/Documents/PersonalProjects/denote-fe/web/dist",
          },
        ]),
      ],
      (x, d, n) => {
        return !_.isNil(x);
      }
    ),
    output: {
      filename: "[name].[" + contentHashValue + "].js",
      publicPath: "../",
      // publicPath: './',
      path: distdir,
    },
    optimization: {},
    module: {
      rules: [
        {
          test: /\.vue$/,
          loader: "vue-loader",
        },
        {
          test: /\.(ts|tsx)$/,
          use: [
            {
              loader: "babel-loader",
              options: babelrc,
            },
            {
              loader: "ts-loader",
            },
          ],
        },
        {
          test: /node_modules\.(ts|tsx)$/,
          use: [
            {
              loader: "babel-loader",
              options: babelrc,
            },
            {
              loader: "ts-loader",
            },
          ],
        },
        {
          test: /\.css$/,
          use: [
            {
              loader: MiniCssExtractPlugin.loader,
            },
            {
              loader: "css-loader",
            },
          ],
        },
        {
          test: /\.less$/,
          exclude: /antd/,
          use: createStyleUseObject(),
        },
        {
          test: /\.worker\.js$/,
          use: { loader: "worker-loader" },
        },
        {
          test: /\.(js|jsx)$/,
          exclude: /(node_modules|bower_components|link_react)/,
          use: [
            {
              loader: "babel-loader",
              options: babelrc,
            },
          ],
        },
        // {
        //   test: /node_modules\/monaco-editor\/.*js$/,
        //   exclude: /(node_modules|bower_components|link_react)/,
        //   use: [
        //     {
        //       loader: "babel-loader",
        //       options: babelrc,
        //     },
        //   ],
        // },
        // {
        //   test: /\.(js|jsx)$/,
        //   use: [
        //     {
        //       loader: "babel-loader",
        //       options: babelrc,
        //     },
        //   ],
        // },
        {
          test: /\.(png|svg|jpg|gif|jpeg)$/,
          use: ["file-loader"],
        },
        {
          test: /\.(woff|woff2|eot|ttf|otf)$/,
          use: [
            {
              loader: "file-loader",
              options: {
                name: "./[name].[ext]",
              },
            },
          ],
        },
        {
          test: /codicon\.(ttf)$/,
          use: [
            {
              loader: "file-loader",
              options: {
                name: "./codicon.[ext]",
              },
            },
          ],
        },
        {
          test: /\.(woff|woff2|eot|ttf|otf)$/,
          use: [
            {
              loader: "file-loader",
              options: {
                name: "./[name].[ext]",
              },
            },
          ],
        },
      ],
    },
  };

  return configureWebpack(webpackConfig, mode);
};
