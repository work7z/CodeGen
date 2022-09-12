package com.denote.client.core

import com.denote.client.concurrent.WebHandleServletHolder
import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.extra.CommonMissile
import com.denote.client.handler.extra.DefaultServletFromTomcat
import com.denote.client.utils.GData
import com.denote.client.utils.GUtils

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class StaticServerServiceImplLogicFunc extends CommonServiceImplLogicFunc {

    @Override
    APIResponse handle(APIRequest apiRequest) {
        super.beforeHandle(apiRequest)

        def req = apiRequest.param["req"] as HttpServletRequest
        def res = apiRequest.param['res'] as HttpServletResponse
        def ref = apiRequest.param['ref'] as CommonMissile

        WebHandleServletHolder.SYS_REF.set(ref)

        def crtDataObj = ref.getCrtDataObj()

        def rawReqURL = req.getRequestURI().toString();


        def listDirectory = crtDataObj['LIST_DIRECTORY'] + ''
        String method = req.getMethod();
        if (!(method in ['GET', 'OPTIONS', 'HEAD'])) {
            flushText(res, 404, 'Method ' + method + ' not allowed in static server, please use methods GET/HEAD/OPTIONS instead of.')
            return;
        }

        // handling service
        String completeCrtReqURL = URLDecoder.decode(rawReqURL);

        def requestURL = noBeginAndEndPath(completeCrtReqURL);
        if (requestURL.startsWith(contextPath)) {
            String folderValueInRequestURL = noBeginAndEndPath(requestURL.replaceFirst(contextPath, ""));

            File specifyFile = new File(directory, folderValueInRequestURL);
            if (!specifyFile.exists()) {
                flushText(res, 404, 'File ' + folderValueInRequestURL + ' Not Found')
                return;
            } else {
                if (specifyFile.isDirectory()) {
                    if (listDirectory != '1') {
                        flushText(res, 404, 'The directory cannot list its sub-files since its config rules.')
                        return;
                    }
                    def esc = { it -> com.google.common.html.HtmlEscapers.htmlEscaper().escape(it) }
                    def parentFolderName = noBeginAndEndPath(directory.getName());
                    def viewParentPathWithRootDirName = joinPath([parentFolderName, folderValueInRequestURL])
                    def subTotalFiles = specifyFile.listFiles();
                    def dirLists = subTotalFiles.collect({ subFile ->
                        def isSubFileDirectory = subFile.isDirectory();
                        def fullSubFileName = joinPath([folderValueInRequestURL,
                                                        noBeginAndEndPath(subFile.getName())]) + (isSubFileDirectory ? '/' : '');
                        def aText = fullSubFileName
                        def hrefval = joinPath([completeCrtReqURL, subFile.getName()])
                        return """
<tr>
<td align="left"><a href='${refineExistPath(hrefval)}'>${esc(aText)}</a></td>
<td align="right">${formatLength(subFile.length())}</td>
<td align="right">${GUtils.getDateStr(new Date(subFile.lastModified()))}</td>
</tr>
"""
                    }).join('');
                    def parentParentPath = '';
                    if (folderValueInRequestURL) {
                        def finArr = folderValueInRequestURL.split('/');
                        def tmparr = [];
                        finArr.eachWithIndex { String entry, int i ->
                            if (i != finArr.length - 1) {
                                tmparr.push(entry);
                            }
                        }
                        parentParentPath = '/' + noBeginAndEndPath(joinPath([contextPath, joinPath(tmparr)]))
                    }
                    def completeHTMLContent = """<!DOCTYPE html>
<html lang="${GData.getCurrentLang()}">
<head>
<title>Files within ${viewParentPathWithRootDirName}</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<style data-merge-styles="true">
table{width:100%; border-collapse: collapse;}
tbody tr:nth-child(even){background:#eeeeee;}
body{font-family: Tahoma,Arial,sans-serif;margin-bottom:0;}
.title{    background: #525D76;
    color: white;
    padding: 8px 6px;
    margin: 0;
    margin-bottom: 11px;}
    .control-bar{
    overflow: hidden;
    background: #525D76;
    margin-bottom: 9px;
    color: white;
    padding: 8px 6px;
    font-size: 15px;
    }
    .control-bar a{
    color:white;
    }.table-wrapper{
    min-height:calc(100vh - 154px);
    }
</style>
</head>
<body>
<h2 class="title">Index of ${viewParentPathWithRootDirName}</h2>
<div class='control-bar'>
<div style='float:left;'>${folderValueInRequestURL == '' ? 'Root Directory' : "<a href='${(refineExistPath(parentParentPath))}'>&lt; Parent Directory</a>"}</div>
<div style='float:right;'>${subTotalFiles.length} files</div>
</div>
<div class='table-wrapper'>
<table  width="100%" cellspacing="0" cellpadding="5" align="center">
<thead>
<th align="left">Filename</th>
<th align="right">Size</th>
<th align="right">Last Modified</th>
</thead>
<tbody>
${dirLists}
</tbody>
</table>
</div>
<hr/>
<div style='color: white;background-color: #525D76 ;   overflow: hidden;padding: 5px 6px;'>
<div style='float:left;'><a href='#' style='color:white'>Back to the Top</a></div>
<div style='float:right;'>Powered by CodeGen</div>
</div>
<!-- sorry for making such anti-paradigm code since i do not have enough time to review it -->
</body>
</html>
"""
                    flushHTML(res, completeHTMLContent, "served a request of reading the directory.")
                    return;
                } else {
                    doLogInternal(
                            200,
                            InfraKeys.LOG_TYPE_SUCCESS_1,
                            "served a request of retrieving the file."
                    )
                    DefaultServletFromTomcat defaultServletFromTomcat = new DefaultServletFromTomcat(specifyFile);
//                    defaultServletFromTomcat.setStaticServerServiceImplLogicFunc(this)
                    defaultServletFromTomcat.init()
                    defaultServletFromTomcat.service(req, res);
                    return;
                }
            }
        }

        sendErrorNotFound(res);
        return null;
    }

}