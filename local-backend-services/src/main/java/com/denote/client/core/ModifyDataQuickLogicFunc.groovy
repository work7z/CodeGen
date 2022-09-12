package com.denote.client.core

import com.denote.client.concurrent.GenerateAssignCentre
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.utils.GData

import java.util.concurrent.atomic.AtomicInteger

class ModifyDataQuickLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest tempAPIRequest) {
        def subject = tempAPIRequest.param["subject"] as String;
        def modifyType = tempAPIRequest.param["modifyType"];
        def param = tempAPIRequest.param["param"];
        def runSQL = null;
        def runMap = [:];
        runMap.putAll(param)
        if (modifyType == 'upset') {
            if (param['ID'] == null) {
                modifyType = 'insert'
            } else {
                modifyType = 'update'
            }
        }
        switch (modifyType) {
            case "insert":
                if (param['ID'] == null) {
                    def needGenerateID = tempAPIRequest.needGenerateID
                    if (needGenerateID && modifyType == 'insert') {
                        AtomicInteger atomicInteger = GenerateAssignCentre.TABLE_UNIQUE_ASSIGN.get(subject)
                        if (atomicInteger == null) {
                            synchronized (GenerateAssignCentre.class) {
                                def tmpObj = GData.g().queryFirst("select ifnull(max(ID) + 1,1) as ID from ${subject}", [:])
                                def tmpID = tmpObj['ID'] as Integer
                                if (tmpID == null) {
                                    tmpID = 1;
                                }
                                atomicInteger = new AtomicInteger(tmpID)
                                GenerateAssignCentre.TABLE_UNIQUE_ASSIGN.put(subject, atomicInteger)
                            }
                        }
                        def latestID = atomicInteger.incrementAndGet()
                        param['ID'] = latestID
                        runMap['ID'] = latestID
                    }
                }
                runSQL = " insert into ${subject} "
                def waitAddedArr = [];
                def waitValueArr = [];
                param.keySet().each {
                    def ctn = GData.g().ctn("""
SELECT * FROM INFORMATION_SCHEMA.columns WHERE TABLE_name=upper('${subject}') AND upper(upper(column_name)=upper('${it}'))
""")

                    if (ctn != 0 && !(it in ['CREATE_TIME'] || it.toString().endsWith("SOLID_TIME"))) {
                        waitAddedArr.add(it);
                        waitValueArr.add(":${it}")
                    }
                }
                runSQL += " (${waitAddedArr.join(",")}) "
                runSQL += " values (${waitValueArr.join(",")}) "
                break;
            case "update":
                def newarr = [];
                param.keySet().toArray().each {
                    it
                    def ctn = GData.g().ctn("""
SELECT * FROM INFORMATION_SCHEMA.columns WHERE TABLE_name=upper('${subject}') AND upper(column_name=upper('${it}'))
""")
                    if (ctn != 0 && !(it in ['CREATE_TIME'] || it.toString().endsWith("SOLID_TIME"))) {
                        newarr.push(" ${it} = :${it} ")
                    }
                }
                runSQL = """ 
update ${subject} set ${newarr.join(",")}  where id = :ID
"""
                break;
            case "delete":
                runSQL = """
delete ${subject} where id = ${param["ID"]}
"""
                break;
            default:
                return APIResponse.err(-1, "request invalid", null);
        }
        GData.g().execWithResult(runSQL, runMap);
        return APIResponse.ok(param);
    }
}
