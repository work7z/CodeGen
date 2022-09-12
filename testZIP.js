const fs = require("fs");
const path = require("path");
// const unzip = require("unzip");
const decompress = require("decompress");
const decompressTargz = require("decompress-targz");
const decompressUnzip = require("decompress-unzip");
var cmd = require("node-cmd");

let myrunjar =
  "/Users/jerrylai/.codegen_repository/runtime/jdk8u322-b06-jre/Contents/Home/bin/java -cp /Users/jerrylai/Documents/PersonalProjects/denote-fe/infra/run-client -Djava.ext.dirs=/Users/jerrylai/.codegen_repository/core com.denote.client.Application";

const myref = cmd.run(myrunjar);
myref.stdout.on("data", function (data) {
  console.log(data);
  // let data_line = "";
  // data_line += data;
  // if (data_line[data_line.length - 1] == "\n") {
  //   console.log(data_line);
  // }
});

// java -cp /Users/jerrylai/Documents/PersonalProjects/denote-fe/infra/run-client -Djava.ext.dirs=/Users/jerrylai/.codegen_repository/core com.denote.client.Application

// decompress(
//   "/Users/jerrylai/testDIR/OpenJDK8U-jre_x64_mac_hotspot_8u322b06.tar.gz",
//   "/Users/jerrylai/testDIR",
//   {
//     plugins: [decompressTargz()],
//   }
// ).then(() => {
//   console.log("Files decompressed");
// });

// decompress(
//   "/Users/jerrylai/testDIR/OpenJDK8U-jre_x64_windows_hotspot_8u322b06.zip",
//   //   "/Users/jerrylai/testDIR/newKeyStoreFileName",
//   "/Users/jerrylai/testDIR"
// )
//   .then((err) => {
//     console.log("Files decompressed", err);
//   })
//   .catch((err) => {
//     console.log("got err");
//   });

// fs.createReadStream(
//   "/Users/jerrylai/testDIR/OpenJDK8U-jre_x64_windows_hotspot_8u322b06.zip"
// ).pipe(unzip.Extract({ path: "/Users/jerrylai/testDIR" }));

// decompress(
//   "/Users/jerrylai/Documents/PersonalProjects/denote-fe/infra/client-1.0-pg.jar",
//   "/Users/jerrylai/testDIR/client",
//   {
//     plugins: [decompressUnzip()],
//   }
// )
//   .then((err) => {
//     console.log("Files decompressed", err);
//   })
//   .catch((err) => {
//     console.log(err);
//     console.log("got err");
//   });
