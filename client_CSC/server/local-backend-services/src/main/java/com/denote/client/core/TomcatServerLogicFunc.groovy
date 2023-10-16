package com.denote.client.core

import com.denote.client.concurrent.SysConnector
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.TomcatInstanceWrapper
import com.denote.client.handler.extra.CommonMissile
import com.denote.client.utils.GUtils
import org.apache.catalina.Context
import org.apache.catalina.startup.Tomcat
import org.apache.coyote.http11.AbstractHttp11Protocol

import javax.servlet.http.HttpServlet

class TomcatServerLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        TomcatInstanceWrapper tomcatInstanceWrapper = bootTomcat(apiRequest.param);
        return APIResponse.ok(tomcatInstanceWrapper);
    }

    static void main(String[] args) {
    }

    private static TomcatInstanceWrapper bootTomcat(def param) {
        CommonMissile ref = param['ref'] as CommonMissile;
        String webPort = param['webPort']
        HttpServlet httpServlet = param["httpServlet"]
        String host = param['host']
        boolean isLocalSSL = (param['isLocalSSL'] + '') as String == '1'

        // create tomcat instance
        // initializing servlet
        Tomcat tomcat = new Tomcat();

        Integer finPort = Integer.valueOf(webPort);

        SysConnector sysConnector = new SysConnector("HTTP/1.1")
        sysConnector.setPort(finPort);
        sysConnector.setRedirectPort(finPort + 1)
        tomcat.setPort(finPort)

        boolean isSSLMode = isLocalSSL;
        if (isSSLMode) {
//            sysConnector.setAttribute("maxThreads", "150")
//            sysConnector.setAttribute("SSLEnabled", true)

            setTheSSLFunc(sysConnector)
        }

        tomcat.setConnector(sysConnector)

//        File base = new File(System.getProperty("java.io.tmpdir"));
//        File base = File.createTempFile("tomcat","")
        File base = new File(GUtils.getAppHomeDir(), "servers/" + UUID.randomUUID().toString());
        if (!base.exists()) {
            base.mkdirs()
        }
        println "base dir: ${base}"
        Context rootContext = tomcat.addContext("", base.getAbsolutePath());
        Tomcat.addServlet(rootContext, "system-internal-api", httpServlet).setAsyncSupported(true);
        rootContext.addServletMapping("/*", "system-internal-api", false)

        // set address
        AbstractHttp11Protocol abstractHttp11Protocol = (AbstractHttp11Protocol) sysConnector.getProtocolHandler()
        def endpoint = abstractHttp11Protocol['endpoint']
        def address = InetAddress.getByName(host)
        endpoint['address'] = address;

        tomcat.getEngine().setDefaultHost(host)
        tomcat.getServer().setAddress(host)
//        tomcat.getServer().setPort(finPort)
        tomcat.getEngine().setDefaultHost(host)
        tomcat.getEngine().findChildren().each {
            it.setName(host)
        }

        TomcatInstanceWrapper tomcatInstanceWrapper = new TomcatInstanceWrapper(tomcat);
        return tomcatInstanceWrapper;
    }

    public static void setTheSSLFunc(SysConnector sysConnector) {
// Other
        SysConnector connector = sysConnector;
        connector.setSecure(true);
        connector.setScheme("https");
        connector.setAttribute("keyAlias", "tomcat");
        connector.setAttribute("keystorePass", "password");
        connector.setAttribute("keystoreType", "JKS");
//            def file_keystore =  new File("/Users/jerrylai/Documents/PersonalProjects/denote-be/src/main/resources/keystore.jks");
        def file_keystore = GUtils.getClzFile("keystore.jks")
        if (!file_keystore.exists() || file_keystore.length() == 0) {
            throw new RuntimeException("no valid keystore")
        }
        connector.setAttribute("keystoreFile", file_keystore.getAbsolutePath());
        connector.setAttribute("clientAuth", "false");
        connector.setAttribute("protocol", "HTTP/1.1");
        connector.setAttribute("sslProtocol", "TLS");
//            TLS_ECDHE_ECDSA_WITH_AES_256_CCM_8
        connector.setAttribute("maxThreads", "200");
        connector.setAttribute("protocol", "org.apache.coyote.http11.Http11AprProtocol");
        connector.setAttribute("SSLEnabled", true);
    }

}
