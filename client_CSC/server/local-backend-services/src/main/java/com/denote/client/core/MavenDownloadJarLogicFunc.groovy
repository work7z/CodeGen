package com.denote.client.core

import com.alibaba.fastjson.JSON
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.MavenCalcVM
import com.denote.client.utils.GData
import com.denote.client.utils.GHttpUtils
import com.denote.client.utils.GLogger
import com.denote.client.utils.GSysConfigUtils
import com.denote.client.utils.GUtils
import com.denote.client.utils.http.HttpShardingFileDownloadUtils

class MavenDownloadJarLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def downloadType = apiRequest.param['downloadType'] as String
        def groupId = apiRequest.param['groupId'] as String
        def artifactId = apiRequest.param['artifactId'] as String
        def version = apiRequest.param['version'] as String
        def onProgress = apiRequest.param['onProgress'] as Closure
        int currentSize = 0;
        int totalSize = 0;
        def status = 'init'
        def g = GLogger.g("downloadJar")

        try {
            // init
            onProgress(currentSize, totalSize, status, null)
            g.info("initializing the files")

            // start handling
            MavenCalcVM mavenCalcVM = new MavenCalcVM(version, groupId, artifactId);
            mavenCalcVM.setMybaseurl(GSysConfigUtils.getBaseMavenLink())
            def jarURL = mavenCalcVM.getJarURL();
            def jarAscURL = mavenCalcVM.getJarAscURL()

            jarURL = "https://mirrors.tuna.tsinghua.edu.cn/AdoptOpenJDK/8/jre/x64/windows/OpenJDK8U-jre_x64_windows_hotspot_8u322b06.zip";

            g.info("initialized the files", jarURL, jarAscURL, mavenCalcVM)

            File saveFolder = new File(GUtils.getDriversDir(), "${groupId}/${artifactId}");
            File saveFile = new File(saveFolder, jarURL.substring(jarURL.lastIndexOf("/")).trim());
            status = "started"
            onProgress(0, totalSize, status, null)
            // downloading
            GHttpUtils.downloadFileQuickly(downloadType,jarURL, saveFile, { int p_currentSize, int p_totalSize ->
                currentSize = p_currentSize;
                totalSize = p_totalSize;
                onProgress(currentSize, totalSize, status, null)
            })
            // verifying
            def isVerified = true;
            if (!isVerified) {
                HttpShardingFileDownloadUtils.FORCE_USE_TRANDITION_DOWNLOAD = true;
                throw new RuntimeException("File broken after having verified the checksum.")
            }
            status = 'done'
            onProgress(totalSize, totalSize, status, null)
        } catch (Throwable throwable) {
            g.error("got an error, " + throwable.getMessage(), throwable)
            onProgress(currentSize, totalSize, 'error', throwable.getMessage())
        }
        return APIResponse.ok();
    }
}
