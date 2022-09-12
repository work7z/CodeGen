package com.denote.client.core

import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.exceptions.UnqualifiedParamException
import com.denote.client.utils.GHttpUtils
import com.denote.client.utils.GLogger
import com.denote.client.utils.GUtils
import org.codehaus.plexus.util.StringUtils

class DownloadLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def type = [
                "basis"  : [],
                "drivers": [],
                "user"   : [],
                "other"  : []
        ]
        def param = apiRequest.param
        def crtDownloadType = param['type'] as String
        def crtDownloadHref = param['href'] as String
        if (type[crtDownloadType] == null) {
            throw new UnqualifiedParamException(crtDownloadType)
        }
        if (StringUtils.isBlank(crtDownloadHref)) {
            throw new UnqualifiedParamException(crtDownloadHref)
        }
        def logger = GLogger.g(DownloadLogicFunc.class)
        logger.info("receiving parameter: ${param.toMapString()}")
        def fileName = crtDownloadHref.trim().substring(crtDownloadHref.trim().lastIndexOf("/") + 1)
        logger.info("preparing for the fileName ${fileName}")
        File targetFile = new File(
                new File(GUtils.getAppDownloadDir(), crtDownloadType),
                fileName
        )
//        GHttpUtils.old_slowness_downloadFile(crtDownloadHref, targetFile)
        GHttpUtils.downloadFileQuickly(crtDownloadHref, targetFile)
        logger.info("downloaded the file")
        return APIResponse.ok(targetFile.getAbsolutePath())
    }

}
