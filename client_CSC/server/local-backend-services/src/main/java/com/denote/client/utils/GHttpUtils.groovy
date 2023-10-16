package com.denote.client.utils

import ch.qos.logback.core.util.CloseUtil
import com.denote.client.constants.InfraKeys
import com.denote.client.exceptions.CannotDownloadException
import com.denote.client.utils.http.DownloadRef
import com.denote.client.utils.http.HttpShardingFileDownloadUtils
import org.apache.commons.io.IOUtils
import org.apache.http.HeaderElement
import org.apache.http.HeaderElementIterator
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.config.ConnectionConfig
import org.apache.http.conn.ConnectionKeepAliveStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.message.BasicHeaderElementIterator
import org.apache.http.protocol.HTTP
import org.apache.http.protocol.HttpContext

import java.util.concurrent.atomic.AtomicInteger

class GHttpUtils {
    private static HttpClientBuilder setRouteLimitForHttpClient(HttpClientBuilder httpClientBuilder) {
        ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        return Long.parseLong(value) * 1000;
                    }
                }
                return 300 * 1000;//如果没有约定，则默认定义时长为60s
            }
        };

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(3000);
        connectionManager.setDefaultMaxPerRoute(1500);//例如默认每路由最高50并发，具体依据业务来定

        ConnectionConfig config = ConnectionConfig.custom().setBufferSize(1024 * 1024 * 5).build();

        httpClientBuilder = httpClientBuilder
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(myStrategy)
                .setDefaultConnectionConfig(config)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setStaleConnectionCheckEnabled(true).build());
        return httpClientBuilder;
    }

    public static CloseableHttpClient getHttpClient() {
        HttpClientBuilder custom = HttpClients.custom().useSystemProperties();
        custom = setRouteLimitForHttpClient(custom);

        CloseableHttpClient build = custom
                .build();

        return build;
    }

    static AtomicInteger RAND_CLIENT_REF = new AtomicInteger(1);
    static Map<String, CloseableHttpResponse> RESP_MAP = new HashMap<>()

    static String putHttpResponse(String mykey, CloseableHttpResponse httpResponse) {
        RESP_MAP.put(mykey, httpResponse);
        return mykey
    }

    static void removeHttpResponse(String key) {
        def obj = RESP_MAP.get(key);
        if(obj != null){
            def entity = obj.getEntity()
            if(entity && entity.getContent() != null){
                IOUtils.closeQuietly(entity.getContent())
            }
            CloseUtil.closeQuietly(obj.close())
        }
        RESP_MAP.remove(key)
    }

    public static void closeAllByType(String type) {
        RESP_MAP.eachWithIndex { Map.Entry<String, CloseableHttpResponse> entry, int i ->
            def isMatch = entry.getKey().toLowerCase().startsWith(type.toLowerCase() + "_")
            removeHttpResponse(entry.getKey())
        }
    }


    static boolean old_slowness_downloadFile(String crtDownloadHref, File targetFile) {
        return old_slowness_downloadFile("system", crtDownloadHref, targetFile)
    }

    static boolean old_slowness_downloadFile(String ___key, String crtDownloadHref, File targetFile) {
        def crtID = RAND_CLIENT_REF.incrementAndGet();
        def mykey = ___key + "_" + crtID
        if (!targetFile.getParentFile().exists()) {
            targetFile.getParentFile().mkdirs()
        }
        if (targetFile.exists()) {
            targetFile.delete()
        }
        try {
            def httpClient = getHttpClient()
            HttpGet httpGet = new HttpGet(crtDownloadHref)
            def httpResponse = httpClient.execute(httpGet)
            putHttpResponse(mykey, httpResponse)
            def entity = httpResponse.getEntity()
            IOUtils.copy(entity.getContent(), new FileOutputStream(targetFile))
            httpClient.close()
            removeHttpResponse(mykey)
            return true;
        } catch (Throwable ignored) {
            removeHttpResponse(mykey)
            GLogger.g().error("download ${crtDownloadHref} to ${targetFile} failed", ignored)
            throw ignored
        }
    }

    public static boolean downloadFileQuickly(String url, File toFile, Closure cbk) {
        return downloadFileQuickly("system", url, toFile, cbk)
    }

    public static boolean downloadFileQuickly(String downloadType, String url, File toFile, Closure cbk) {
        if (!toFile.getParentFile().exists()) {
            toFile.getParentFile().mkdirs();
        }
        if (toFile.exists()) {
            toFile.delete();
        }
        DownloadRef downloadRef = HttpShardingFileDownloadUtils.downloadFileQuickly(downloadType, url, toFile, cbk)
        downloadRef.getLock().await()
    }

    static void isCanDownload(String url) {
        def clients = getHttpClient()
        def resp = clients.execute(new HttpGet(url))
        if (resp.getStatusLine().getStatusCode() != 200) {
            throw new CannotDownloadException("cannot execute this url: ${url}")
        }
        clients.close()
    }

    static {
        GUtils.highrun(new Runnable() {
            @Override
            void run() {
                def crtDownloadTasks = InfraKeys.CRT_LIVE_ID + InfraKeys.CRT_DOWNLOAD_ID;
            }
        })
    }
}
