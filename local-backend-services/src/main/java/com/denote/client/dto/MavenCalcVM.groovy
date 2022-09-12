package com.denote.client.dto

import org.apache.commons.lang3.StringUtils

public class MavenCalcVM {
    public String getJarURL() {
        return getFullURL(".jar", true);
    }
    public String getJarAscURL() {
        return getFullURL(".jar.asc", true);
    }

    public String getWarURL() {
        return getFullURL(".war", true);
    }

    public String getDocURL() {
        return getFullURL("-javadoc.jar", true);
    }

    public String getSourcesURL() {
        return getFullURL("-sources.jar", true);
    }

    String mybaseurl = "";

    public String getFullURL(String aftfix, boolean isNeedFileName) {
//        String mybaseurl = "https://mirrors.huaweicloud.com/repository/maven/";
//        String mybaseurl = "https://maven.aliyun.com/nexus/content/groups/public/";
//        String mybaseurl = "https://mirrors.cloud.tencent.com/maven/";
        String aversion = this.aversion;
        String str_groupidval = this.groupId;
        String str_artifactidval = this.artifactId;
        if (StringUtils.isBlank(aversion)) {
            aversion = "";
        }
        String url_groupidval = str_groupidval.replaceAll("\\.", "/");
        String syslistdescstr = url_groupidval + "/" +
                str_artifactidval + "/" +
                aversion + "/";
        String fullpath = mybaseurl +
                syslistdescstr;
        String filename = str_artifactidval + "-" + aversion;
        if (!isNeedFileName) {i
            filename = "";
        }
        String finallyURL = fullpath + filename + aftfix;
        return finallyURL;
    }
    String aversion;
    String groupId;
    String artifactId;

    MavenCalcVM(String aversion, String groupId, String artifactId) {
        this.aversion = aversion
        this.groupId = groupId
        this.artifactId = artifactId
    }
}
