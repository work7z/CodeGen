package com.denote.client.utils.http

import com.denote.client.exceptions.CannotDownloadException
import com.denote.client.utils.Countable
import com.denote.client.utils.GHttpUtils
import org.apache.http.HttpHeaders
import org.apache.http.client.methods.HttpHead

import java.util.concurrent.Executors
import java.util.concurrent.Future

class HttpShardingFileDownloadUtils {
    public static boolean FORCE_USE_TRANDITION_DOWNLOAD = false;

    public static DownloadRef downloadFileQuickly(String downloadType, String url, File saveFile) {
        return downloadFileQuickly(downloadType, url, saveFile, null)
    }

    public static DownloadRef downloadFileQuickly(String downloadType, String url, File saveFile, Closure cbk) {
        if (cbk == null) {
            cbk = { int currentSize, int totalSize ->

            }
        }
        DownloadRef downloadRef = new DownloadRef();
        downloadRef.setCbk(cbk)
        // firstly, get all content-length by head
        def httpClients = GHttpUtils.getHttpClient()
        HttpHead head = new HttpHead(url)
        head.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes")
        def head_resp = httpClients.execute(head)
        def head_statusCode = head_resp.getStatusLine().getStatusCode()
        if (FORCE_USE_TRANDITION_DOWNLOAD || (head_statusCode != 206 && (head_resp.getFirstHeader(HttpHeaders.ACCEPT_RANGES) == null || head_resp.getFirstHeader(HttpHeaders.ACCEPT_RANGES).getValue() != 'bytes'))) {
            println "Do not support ranges request, code: ${head_statusCode}"
            GHttpUtils.old_slowness_downloadFile(downloadType, url, saveFile)
            downloadRef.lock.countDown()
            return downloadRef;
        } else {
            def totalSize = head_resp.getFirstHeader(HttpHeaders.CONTENT_LENGTH).getValue().toInteger()
            int chunkSizeForM = 1024 * 1024 * 5;
            def maxThreads = 5;
            downloadRef.chunkSizeForM = chunkSizeForM;
            println "total-size: ${totalSize}, maxThreads: ${maxThreads}"
            cbk(-1, totalSize)
            downloadRef.setTotalSize(totalSize)
            Countable countable = new Countable();
            def pool = Executors.newFixedThreadPool(maxThreads)
            List<Future> futureList = new ArrayList<>()
            for (int i = 0; i < maxThreads; i++) {
                def future = pool.submit(new SmallPartDownloadThread(downloadRef,
                        url,
                        saveFile,
                        i,
                        httpClients,
                        cbk , downloadType))
                futureList.add(future)
            }
            futureList.each {
                it.get()
            }
            downloadRef.closeRs()
            if (downloadRef.getErrList().size() != 0) {
                throw new CannotDownloadException(downloadRef.getErrList().join("\n"))
            } else {
                String finalStr = countable.countMiles()
                downloadRef.getLock().countDown()
                println "Spent time: ${finalStr}, Thread: ${maxThreads}, chunkSizeForM: ${chunkSizeForM}"
                cbk(totalSize, totalSize)
            }
        }
        return downloadRef
    }

    public static void main(String[] args) {
//        System.setProperty("jdk.httpclient.bufsize","50000")
//        System.setProperty("jdk.httpclient.hpack.maxheadertablesize","50000")
//        System.setProperty("jdk.httpclient.maxframesize","50000")
//        System.setProperty("jdk.httpclient.websocket.writeBufferSize","50000")
        String url = "https://mirrors.tuna.tsinghua.edu.cn/AdoptOpenJDK/8/jre/x64/linux/OpenJDK8U-jre_x64_linux_hotspot_8u322b06.tar.gz";
//        url = "http://localhost:3000/PS2019%20%E4%B8%AD%E6%96%87%E7%89%88.dmg";
        File toFile = new File("/Users/jerrylai/test/jdk_multiple_thread.tar.gz");
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        if (toFile.exists()) {
            toFile.delete();
        }
        DownloadRef downloadRef = downloadFileQuickly(url, toFile)
        downloadRef.getLock().await()

        Countable countable = new Countable()
        GHttpUtils.old_slowness_downloadFile(url, new File("/Users/jerrylai/test/jdk_multiple_thread_temp.tar.gz"))
        println "second count: ${countable.countMiles()}"
    }
}
