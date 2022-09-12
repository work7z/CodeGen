package com.denote.client.core

import com.denote.client.constants.InfraKeys
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.GData

class TemplateDataQuickLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest tempAPIRequest) {
        def urlPrefix = tempAPIRequest.param["urlPrefix"];
        def tableName = tempAPIRequest.param["tableName"];
        APIRequest apiRequest = tempAPIRequest.param["apiRequest"];
        def matchMethods = ["add"                  : "insert",
                            "upset"                : "insert",
                            "delete"               : "delete",
                            "update"               : "update",
                            "query-by-id"          : "select",
                            "query-by-cust-id"     : "select",
                            "query-by-page"        : "select",
                            "query-by-page-id-desc": "select",
                            "query-all"            : "select"
        ]
        def actionType = apiRequest.getActionType();
        if (!actionType.contains(urlPrefix)) {
            return APIResponse.err(-1, "no related url prefix information", null);
        }
        def actionName = actionType.replace(urlPrefix, '');
        def matchCorrespondingMethods = matchMethods[actionName];
        if (actionName == 'upset') {
            if (apiRequest.param["ID"]) {
                matchCorrespondingMethods = 'update';
            } else {
                matchCorrespondingMethods = 'insert'
            }
        }
        if (matchCorrespondingMethods) {
            switch (actionName) {
                case 'query-by-cust-id':
                    def mykey = apiRequest.param['_KEY'];
                    if (GData.g().hasColumn(tableName as String, mykey as String)) {
                        return APIResponse.ok(GData.g().query("select * from ${tableName} where ${mykey} = :id", [
                                "id": apiRequest.param["_VALUE"]
                        ]))
                    }
                    break;
                case 'query-by-id':
                    return APIResponse.ok(GData.g().queryFirst("select * from ${tableName} where id = :id", ["id": apiRequest.param["ID"]]))
                    break;
                case 'query-by-page':
                    def callSQL = """ select * from ${tableName} """;

                    def pageInfo = apiRequest.param["pageInfo"];

                    if (pageInfo["pageIndex"] + '' == '0') {
                        pageInfo["pageIndex"] = 1
                    }

                    int pageSize = pageInfo["pageSize"] as int;
                    int pageIndex = pageInfo["pageIndex"] as int
                    def mymap = [
                            "o": (pageSize * (pageIndex - 1)),
                            "l": pageInfo["pageSize"]
                    ]

                    String callType = apiRequest.param["callType"];
                    switch (callType) {
                        case "logging-query":
                            def logTypes = apiRequest.param['loggingTypes']
                            callSQL = """
    select * from (${callSQL}) a where LOG_TYPE not in (${InfraKeys.LOG_TYPE_INTERNAL_5}) 
${apiRequest.param['MSG_SOURCE'] != null ? ' and MSG_SOURCE=:MSG_SOURCE ' : ''}
${logTypes != null && logTypes != '@all' ?
                                    (logTypes.toString().trim() == '' ? 'and 1!=1' :
                                            logTypes.toString()
                                                    .split(",")
                                                    .collect({ x -> x.trim().toInteger() })
                                                    .join(",")
                                                    .with({ a -> " and LOG_TYPE in (${a})" })
                                    ) : ''
                            }
"""

                            break;
                        default:
                            if (callType in ["static-list-with-folder", "proxy-list-with-folder"]) {
                                def internal_tableName = callType.startsWith("proxy") ? "proxy" : "static"
                                callSQL = """
select a.*,b.name as folder_name,b.brief as folder_brief
 from g_${internal_tableName}_config a join g_${internal_tableName}_folder b on a.folder_id = b.id
 order by a.id desc 
"""
                            }
                            break;
                    }
                    if (apiRequest.param["idDesc"]) {
                        callSQL = """
select * from (${callSQL}) ia order by id desc
"""
                    }
                    ['ID', 'MSG_SOURCE'].each { ok ->
                        if (apiRequest.param[ok]) {
                            mymap[ok] = apiRequest.param[ok]
                            callSQL = """
select * from (${callSQL}) g1 where ${ok}=:${ok}
"""
                        }
                    }

                    def pageData = GData.g().query("select * from (${callSQL}) g limit :l offset :o", mymap);
                    // TODO: here can add cache for count
                    def ctn = GData.g().ctn("select * from (${callSQL}) g", mymap)

                    // if it's proxy mode, then query all of these extra data to be returned
                    if (callType == 'proxy-list-with-folder') {
                        pageData.each { eachServer ->
                            def CONFIG_ID = eachServer['ID']
                            def rules = GData.g().query("select * from G_PROXY_CONFIG_RULE where config_id=${CONFIG_ID}")
                            eachServer['EXTRA_DATA_PROXY_RULES'] = rules;
                            if (rules) {
                                rules.each { eachRule ->
                                    def RULE_ID = eachRule['ID']
                                    def pathRewrites = GData.g().query("select * from G_PROXY_CONFIG_RULES_PATH_REWRITE where config_rule_id=${RULE_ID}")
                                    eachRule['EXTRA_DATA_PROXY_RULES_PATH_REWRITE'] = pathRewrites
                                }
                            }
                        }
                    }
                    return APIResponse.ok(["pageData" : pageData,
                                           "pageCount": ctn])
                    break;
                case 'query-all':
                    return APIResponse.ok(GData.g().query("select * from ${tableName}"))
                    break;
                default:
                    GData.modify(tableName, matchCorrespondingMethods, apiRequest.param)
                    return APIResponse.ok()
                    break;
            }
        } else {
            return APIResponse.err(-1, "no suitable solution to be used", null);
        }
    }
}
