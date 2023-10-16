package com.denote.client.quick

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.denote.client.constants.InfraKeys
import com.denote.client.core.AuthLogicFunc
import com.denote.client.handler.WebListener
import com.denote.client.quick.check.CheckFileStatusThread
import com.denote.client.quick.servlet.ClientInternalServlet
import com.denote.client.utils.Countable
import com.denote.client.utils.GHttpUtils
import com.denote.client.utils.GLogger
import com.denote.client.utils.GUtils
import org.apache.catalina.Context
import org.apache.catalina.connector.Connector
import org.apache.catalina.startup.Tomcat
import org.apache.http.client.methods.HttpPost
import org.apache.tomcat.util.http.Rfc6265CookieProcessor
import org.apache.tomcat.util.net.SSLHostConfig
import org.apache.tomcat.util.net.SSLHostConfigCertificate

import javax.servlet.http.Cookie

class QuickBootDetailLogic {
    private static void cleanOtherInstance(int myport) {
        def i = 0;
        while (true) {
            i++;
            def infra_p_uid = InfraKeys.INFRA_P_UID
            def bootJSON = new File(GUtils.getAppHomeDir(), "boot_local_service.json")
            try {
                if (!bootJSON.exists()) {
                    bootJSON.createNewFile()
                    bootJSON.write("[]")
                }
                def my_arr = JSONArray.parseArray(bootJSON.readLines().join(""), Map.class)
                def client = GHttpUtils.getHttpClient()
                my_arr.each {
                    int his_port = it['port'] as int;
                    GLogger.g().info("closing the previous process", it)
                    HttpPost httpPost = new HttpPost("http://127.0.0.1:${his_port}/infra/close");
                    httpPost.setHeader("X-FE-RUID", AuthLogicFunc.getTheKeyToken());
                    def execute = client.execute(httpPost)
                    GLogger.g().info("closed the previous process")
                }
                def tmplist = []
                for (def k = 0; k < Math.min(10, my_arr.size()); k++) {
                    tmplist.add(my_arr.get(k));
                }
                my_arr = [[port: myport,
                           puid: infra_p_uid]]
                my_arr.addAll(tmplist)
                bootJSON.write(JSON.toJSONString(my_arr))
                break;
            } catch (Throwable throwable) {
                if (throwable instanceof ConnectException) {
                    break;
                }
                if (bootJSON.exists()) {
                    bootJSON.delete()
                }
                GLogger.g().error("cannot handle clean task", throwable)
                throwable.printStackTrace()
                if (i <= 2) {
                    continue
                }
            }
            break;
        }

    }

    public static void run(String[] args) {
        Countable countable = new Countable();

        def tomcat = new Tomcat();
        File base = new File(GUtils.get_USER_HOME_DIR(), ".codegen/sys/" + UUID.randomUUID().toString());
        if (!base.exists()) {
            base.mkdirs();
        }

        if (args == null || args.length == 0) {
            args = ["18080", "auto"]
        }
        if (args[1].equalsIgnoreCase("prod")) {
            GUtils.setDevFlag(false)
        }
        int finport = args[0].toInteger()

        // write current field Puid and mark it is the latest one
        File finalPUIDFile = new File(GUtils.getAppHomeDir(), "boot.pid");
        finalPUIDFile.write(InfraKeys.INFRA_P_UID)

        cleanOtherInstance(finport);

        tomcat.setPort(finport)

        Context rootContext = tomcat.addContext("", base.getAbsolutePath());
        rootContext.setCookieProcessor(new Rfc6265CookieProcessor() {
            @Override
            String generateHeader(Cookie cookie) {
                def header = super.generateHeader(cookie)
                return header + '; SameSite=None; Secure'
            }
        })

        Tomcat.addServlet(rootContext, "client-internal-api", new ClientInternalServlet()).setAsyncSupported(true);
        rootContext.addServletMapping("/*", "client-internal-api", false)

//        int finPort = 11443;
//        SysConnector sysConnector = new SysConnector("HTTP/1.1")
//        sysConnector.setPort(finPort);
//        sysConnector.setRedirectPort(finPort + 1)
//        tomcat.setPort(finPort)
//        TomcatServerLogicFunc.setTheSSLFunc(sysConnector)

        // check run status before running
        CheckFileStatusThread checkFileStatusThread = new CheckFileStatusThread(finalPUIDFile)
        checkFileStatusThread.checkOnce()
        Thread thread = new Thread(checkFileStatusThread);
        thread.start()

        // run WebListener logic
        WebListener webListener = new WebListener();
        webListener.onApplicationEvent(null)

        // starting after all tasks is done
        tomcat.start()

        println "started, spent ${countable.countMiles()}"


        // CANNOT DELETE THIS FLAG
        println "${DONE_FLAG}";

        tomcat.getServer().await();
    }
    public static final String DONE_FLAG = "APP_BOOT_DONE"

    private static void temp(Tomcat tomcat) {
// setting SSL connect
        Connector httpsConnector = new Connector();
        httpsConnector.setPort(11443);
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        httpsConnector.setAttribute("SSLEnabled", "true");

        SSLHostConfig sslConfig = new SSLHostConfig();


        SSLHostConfigCertificate certConfig = new SSLHostConfigCertificate(sslConfig, SSLHostConfigCertificate.Type.RSA);
        certConfig.setCertificateKeystoreFile("/Users/jerrylai/temp/my-release-key.keystore");
        certConfig.setCertificateKeystorePassword("test123");
        certConfig.setCertificateKeyAlias("alias_name");
        sslConfig.addCertificate(certConfig);


        httpsConnector.addSslHostConfig(sslConfig);
        tomcat.setConnector(httpsConnector)
    }
}
