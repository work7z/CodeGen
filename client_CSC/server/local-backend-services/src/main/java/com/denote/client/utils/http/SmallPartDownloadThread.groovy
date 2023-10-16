package com.denote.client.utils.http

import com.denote.client.utils.GHttpUtils
import com.denote.client.utils.GLogger
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient

class SmallPartDownloadThread implements Runnable {
    private DownloadRef downloadRef;
    private String url;
    private File saveFile;
    private int order;
    private CloseableHttpClient httpClient;
    private Closure cbk;
    private String downloadType;

    SmallPartDownloadThread(DownloadRef downloadRef, String url, File saveFile, int order, CloseableHttpClient httpClient, Closure cbk, String downloadType) {
        this.downloadRef = downloadRef
        this.url = url
        this.saveFile = saveFile
        this.order = order
        this.httpClient = httpClient
        this.cbk = cbk;
        this.downloadType = downloadType;
    }

    @Override
    public void run() {
//        httpClient = HttpClients.createDefault()
        def logger = GLogger.g("download-thread-${order}")
        def crtID = GHttpUtils.RAND_CLIENT_REF.incrementAndGet();
        while (true) {
            def crtTask = downloadRef.getNextBeginAndEnd();
            if (crtTask == null) {
                logger.info("no more begin and end, break the loop")
                break;
            }
            def begin = crtTask['begin']
            def end = crtTask['end']
            logger.info("begin downloading, range: ${begin}-${end}")
            HttpGet httpGet = new HttpGet(url);
//            httpGet.setHeader("Accept-Encoding","gzip")
            httpGet.setHeader(HttpHeaders.RANGE, "bytes=${begin}-${end}")
            def resp = httpClient.execute(httpGet)
            def mykey = downloadType + "_" + crtID
            GHttpUtils.putHttpResponse(mykey, resp)
            try {
                if (resp.getStatusLine().getStatusCode() != 206) {
                    logger.info("got an err while the request, range: ${begin}-${end}")
                    downloadRef.getErrList().add("failed to download for ${begin}-${end} by judging the code ${resp.getStatusLine().getStatusCode()}")
                    GHttpUtils.removeHttpResponse(mykey)
                    break;
                } else {
                    def iptStream = resp.getEntity().getContent();
                    downloadRef.writeData(saveFile, iptStream, begin, end)
                    logger.info("downloaded, range: ${begin}-${end}")
                    GHttpUtils.removeHttpResponse(mykey)
                }
            } catch (Throwable throwable) {
                GLogger.g().error("got an error while handling quickly request", throwable)
                GHttpUtils.removeHttpResponse(mykey)
                throw throwable;
            }
        }
    }
}
