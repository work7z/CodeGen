// This file is required by the index.html file and will
// be executed in the renderer process for that window.
// No Node.js APIs are available in this process because
// `nodeIntegration` is turned off. Use `preload.js` to
// selectively enable features needed in the rendering
// process.

// 
// document.addEventListener('load',()=>{
// })
var ele = document.createElement('webview')
ele.nodeintegration = 'true'
ele.src = 'http://127.0.0.1:1234/#/mp/index'
ele.id = 'webview'
var webview = ele;
webview.addEventListener("dom-ready", function() {
  webview.openDevTools();
});
console.log('fixcurrent')
document.body.appendChild(ele)