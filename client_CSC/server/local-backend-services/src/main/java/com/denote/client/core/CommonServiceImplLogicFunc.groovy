package com.denote.client.core

import com.denote.client.concurrent.WebHandleServletHolder
import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.handler.extra.CommonMissile

import javax.servlet.http.HttpServletResponse
import java.text.DecimalFormat

abstract class CommonServiceImplLogicFunc extends BasicLogicFunc {
    String contextPath;
    File directory;

    def doLogInternal(int httpCode, int logType, String logContent) {
        def ref = WebHandleServletHolder.SYS_REF.get()
        def req = WebHandleServletHolder.SYS_REQ.get()
        def res = WebHandleServletHolder.SYS_RES.get()
        def rawReqURL = req.getRequestURI().toString();
        ref.doLog(ref, logType, "${req.getMethod()} ${rawReqURL} ${httpCode} - ${logContent}")
    }

    def doLogInternalWithNoCode(int logType, String logContent) {
        def ref = WebHandleServletHolder.SYS_REF.get()
        def req = WebHandleServletHolder.SYS_REQ.get()
        def res = WebHandleServletHolder.SYS_RES.get()
        def rawReqURL = req.getRequestURI().toString();
        ref.doLog(ref, logType, "${req.getMethod()} ${rawReqURL} - ${logContent}")
    }


    static String joinPath(def arr) {
        List<String> newarr = [];
        arr.each {
            if (it != '' && it != '/') {
                newarr.push(it)
            }
        }
        return newarr.join('/')
    }

    static String noBeginAndEndPath(String path) {
        if (path == null) {
            path = ''
        }
        path = path.trim()
        if (path == '/') {
            path = '';
        } else if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        path = path.replaceAll("^/", "").replaceAll("/\$", "");
        return path.trim();
    }

    static String refineExistPath(String rawstr) {
        def arr = rawstr.split("/");
        return (rawstr.startsWith("/") ? '/' : '') + noBeginAndEndPath(joinPath(arr.collect({ it -> URLEncoder.encode(it) }))) + (rawstr.endsWith("/") ? '/' : '')
    }


    def limitValue(double value) {
        DecimalFormat df1 = new DecimalFormat("0.0");
        String str = df1.format(value);
        return str;
//        def of = BigDecimal.valueOf(value)
//        of.setScale(1,RoundingMode.HALF_UP)
//        return of.toString();
    }

    def formatLength(long length) {
        if (length > GB) {
            return limitValue(length / GB) + 'GB'
        } else if (length > MB) {
            return limitValue(length / MB) + 'MB'
        } else if (length > KB) {
            return limitValue(length / KB) + "KB"
        } else {
            return length + 'B'
        }
    }

    def flushText = { HttpServletResponse res, code, arg ->
        res.setStatus(code)
        res.getWriter().write(arg)
        res.getWriter().flush()
        res.getWriter().close()
        doLogInternal(
                code,
                code == 200 ?
                        InfraKeys.LOG_TYPE_SUCCESS_1
                        : ("" + code).startsWith("4") || ("" + code).startsWith("5") ?
                        InfraKeys.LOG_TYPE_DANGER_2
                        : InfraKeys.LOG_TYPE_WARNING_3,
                Objects.toString(arg)
        )
    }


    def flushHTML = { HttpServletResponse res, String arg, String msg ->
        res.setContentType("text/html;charset=utf-8")
        res.setStatus(200)
//        res.setContentLength(arg.length())
        res.getWriter().write(arg)
        res.getWriter().flush()
        res.getWriter().close()
        doLogInternal(
                200,
                InfraKeys.LOG_TYPE_SUCCESS_1,
                msg
        )
    }

    def sendErrorNotFound = { HttpServletResponse res ->
        flushText(res, 404, '404 Not Found')
    }

    public static final int KB = 1024;
    public static final int MB = 1024 * 1024;
    public static final int GB = 1024 * 1024 * 1024

    void beforeHandle(APIRequest apiRequest) {
        def crtDataObj = (apiRequest.param['ref'] as CommonMissile).getCrtDataObj()
        def res = apiRequest.param['res'] as HttpServletResponse
        res.setCharacterEncoding("utf-8");
        contextPath = crtDataObj['CONTEXT_PATH'] as String
        if (contextPath == '') {
            contextPath = ''
        }
        contextPath = noBeginAndEndPath(contextPath)
        def myfilepath = crtDataObj['FILE_PATH'] as String
        if (myfilepath != null) {
            directory = new File(myfilepath) as File
        }
    }
}
