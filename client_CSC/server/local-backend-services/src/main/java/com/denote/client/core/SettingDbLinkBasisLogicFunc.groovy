package com.denote.client.core

import com.alibaba.fastjson.JSON
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.CryptoUtil
import com.denote.client.utils.GData
import com.denote.client.utils.GUtils

class SettingDbLinkBasisLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        // init DBType
        [
                [
                        "name": "MySQL",
                        "icon": "mysql",
                        'prop': 'mysql'
                ],
                [
                        "name": "MariaDB",
                        icon  : "mariadb",
                        prop  : 'mariadb'
                ],
                [
                        "name": "Oracle",
                        icon  : "oracle",
                        prop  : 'oracle'
                ],
                [
                        name: "PostgreSQL",
                        icon: "postgresql",
                        prop: 'postgresql'
                ],
                [
                        name: "SQLLite",
                        icon: "sqllite",
                        prop: 'sqllite'
                ],
                [
                        name: 'H2 Embedded',
                        icon: 'h2embedded',
                        prop: 'h2embedded'
                ]
        ].each {
            def name = it['name']
            def icon = it['icon']
            def prop = it['prop']
            def ctn = GData.g().ctn("SELECT * FROM G_DBLINK_DBTYPE WHERE upper(DATABASE_NAME)=upper(:NAME)", [NAME: name])
            if (ctn == 0) {
                GData.modify("G_DBLINK_DBTYPE", "insert", [
                        'database_name': name,
                        database_icon  : icon,
                        database_prop  : prop
                ])
            }
        }

        // init DBDriver
        def sysValue = CryptoUtil.d(((GUtils.getClzFile("_" + "a" + "ut" + "h").readLines().join("\n"))))
        def finalValue = JSON.parseObject(sysValue)
        finalValue['mvnDepInfo'].each {
            def prop = it['prop']
            def meta = it['meta']
            def groupId = meta['groupId']
            def artifactId = meta['artifactId']
            def versions = it['versions'] as List<String>
            def crtDbTypeObj = GData.g().queryFirst("SELECT * FROM G_DBLINK_DBTYPE where database_prop=:PROP", [PROP: prop])
            versions.each { eachVersion ->
                def finalPushAndCheckObj = [
                        DBTYPE_ID      : crtDbTypeObj['ID'],
                        DRIVER_NAME    : "${groupId}/${artifactId}:${eachVersion}",
                        MVN_GROUP_ID   : groupId,
                        MVN_ARTIFACT_ID: artifactId,
                        MVN_VERSION    : eachVersion,
                ]
                def myctn = GData.g().ctn("""
SELECT * FROM G_DBLINK_DRIVER 
WHERE DBTYPE_ID=:DBTYPE_ID
AND MVN_GROUP_ID=:MVN_GROUP_ID
AND MVN_ARTIFACT_ID=:MVN_ARTIFACT_ID
AND MVN_VERSION=:MVN_VERSION
""", finalPushAndCheckObj)
                if (myctn == 0) {
                    GData.modify("G_DBLINK_DRIVER", "insert", finalPushAndCheckObj)
                }
            }
        }

        return APIResponse.ok();
    }
}
