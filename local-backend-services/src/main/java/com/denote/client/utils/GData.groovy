package com.denote.client.utils

import com.alibaba.druid.pool.DruidDataSource
import com.alibaba.fastjson.JSONObject
import com.denote.client.constants.InfraKeys
import com.denote.client.core.InternalCallingLogicFunc
import com.denote.client.core.ModifyDataQuickLogicFunc
import com.denote.client.core.TemplateDataQuickLogicFunc
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.handler.GlobalController
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.PreparedStatementCallback
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import javax.xml.ws.ServiceMode
import java.sql.PreparedStatement
import java.sql.SQLException

@ServiceMode
class GData {
    private NamedParameterJdbcTemplate jdbcRef;
    private static GData sysData = new GData();
    private static DruidDataSource dbRef;

    private GData() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("org.h2.Driver")
        dataSource.setUrl('jdbc:h2:~/' + GUtils.globalAppName + '/meta' + (GUtils.isDevServerMode() ? ';DB_CLOSE_ON_EXIT=FALSE;AUTO_SERVER=TRUE' : ''));
        jdbcRef = new NamedParameterJdbcTemplate(dataSource);
        dbRef = dataSource;
    }

    public static GData g() {
        return sysData;
    }

    static def clone(def ok) {
        return JSONObject.parseObject(JSONObject.toJSONString(ok), ok.getClass())
    }

    static APIResponse runTemplateUpdate(APIRequest apiRequest, String urlPrefix, String tableName) {
        tableName = tableName.toUpperCase()
        APIRequest tempReq = GData.clone(apiRequest);
        tempReq.param["urlPrefix"] = urlPrefix;
        tempReq.param["tableName"] = tableName;
        tempReq.param["apiRequest"] = apiRequest;
        return GlobalController.call(TemplateDataQuickLogicFunc.class, tempReq);
    }

    static void closeConn() {
        dbRef.close()
    }

    public List<Map<String, Object>> query(String sql) {
        return query(sql, [:]);
    }

    public static String getCurrentLang() {
        return 'en_US';
    }

    public Map<String, Object> queryFirst(String sql, Map params) {
        def arr = query(sql, params);
        if (arr == null || arr.size() == 0) {
            return null;
        }
        return arr[0]
    }

    public List<Map<String, Object>> query(String sql, Map params) {
        InfraKeys.updateLatestActiveTimeWithDelay()

        GLogger.g().debug(sql, params)
        def query = jdbcRef.query(sql, params, new ColumnMapRowMapper());

        def request = new APIRequest()
        request.param['query'] = query;
        APIResponse response = GlobalController.call(InternalCallingLogicFunc.class, request)
        query = response.content;

        return query;
    }

    public long ctn(String sql) {
        return ctn(sql, [:])
    }

    public long ctn(String sql, Map params) {
        def finalSQL = """
select count(*) as ctn from (${sql}) a
""";
        def query = query(finalSQL, params);
        return query[0]["ctn"];
    }

    public GData exec(String sql) {
        exec(sql, [:]);
        return g()
    }

    public GData execSafe(String sql) {
        sql.split(";").each {
            try {
                exec(it, [:]);
            } catch (Throwable e) {
                // DO NOTHING, making it quiet
            }
        };
        return g()
    }

    public static Set<String> preCheckInSet = new HashSet<String>();
    public static Map<String, String> checkingTableInfo = new HashMap<>();

    public static void updateTableColumnsThroughSQL(String sql) {
        // TODO: if current env is prod env, run only once after updating its version
        sql = sql.toLowerCase()
        if (sql.contains("create table")) {
            sql = sql.trim()
            sql = sql.replaceAll(";\$", "").replaceAll("\\)\\s*\$", "")
            sql.eachMatch("create\\s+table\\s+if\\s+not\\s+exists\\s+(\\w+?)\\(", { matchResults ->
                println matchResults
                String remainSQLBody = sql.substring(matchResults[0].length())
                String tableName = matchResults[1];

                if (GUtils.isDevMode()) {
                    def tableOldSQL = checkingTableInfo.get(tableName);
                    if (tableOldSQL == sql) {
                        return;
                    } else {
                        checkingTableInfo.put(tableName, sql);
                    }
                }

                def tableExistsCtn = GData.g().ctn("""
SELECT * FROM INFORMATION_SCHEMA.columns WHERE TABLE_name=upper('${tableName}')
""")
                if (tableExistsCtn == 0) {
                    return;
                }

                remainSQLBody.split(",").each({ eachColumnLine ->
                    def crtColumnDefines = eachColumnLine.split("\\s+").collect({ it -> it.trim().toLowerCase() }).findAll({ it -> !it.isEmpty() })
                    def crtColumnName = crtColumnDefines[0]
                    String uniqueKey = tableName + crtColumnDefines;
                    if (GUtils.isDevMode()) {
                        if (preCheckInSet.contains(uniqueKey)) {
                            return;
                        }
                    }
                    if (GUtils.isDevMode()) {
                        preCheckInSet.add(uniqueKey)
                    }
                    if (crtColumnDefines[0] == 'id') {
                        return;
                    }
                    def columnExistsCtn = GData.g().ctn("""
SELECT * FROM INFORMATION_SCHEMA.columns WHERE TABLE_name=upper('${tableName}') AND upper(upper(column_name)=upper('${crtColumnName}'))
""")
                    if (columnExistsCtn == 0) {
                        def rsql = """
alter table ${tableName} add column ${eachColumnLine}
"""
                        GData.g().exec(rsql)
                    }
                })
            })
        }
    }

    public GData exec(String sql, Map params) {
        execWithResult(sql, params)
        return g();
    }

    public Object execWithResult(String sql, Map params) {
        updateTableColumnsThroughSQL(sql);
        GLogger.g().debug(sql, params)

        def callback = new PreparedStatementCallback() {
            @Override
            Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                def execute = preparedStatement.execute()
                return execute
//                def keys = preparedStatement.getGeneratedKeys()
//                if (sql.contains("insert")) {
//                    def ID = keys.getLong("ID")
//                    def keys_str = JSON.toJSONString(keys)
//                    return keys;
//                } else {
//                }
            }
        }
        def execute = jdbcRef.execute(sql, params, callback)
        return execute;

//        def connection = dbRef.getConnection();
//        try {
//            if (sql.trim().substring(0, 10).toLowerCase().contains("insert")) {
//                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//                def execobj = statement.execute()
//                def genKeys = statement.getGeneratedKeys()
//                def ID = genKeys.getLong("ID")
//                return genKeys
//            } else {
//                PreparedStatement statement = connection.prepareStatement(sql);
//                def execobj = statement.execute()
//                return execobj
//            }
//        } catch (Throwable throwable) {
//            throw throwable
//        } finally {
//            if (!connection.isClosed()) {
//                connection.close()
//            }
//        }
    }

    public static APIResponse modify(String subject, String modifyType, Map param) {
        return modify(subject, modifyType, param, false)
    }

    public static APIResponse modify(String subject, String modifyType, Map param, boolean needGenerateKeys) {
        APIRequest tempAPIRequest = new APIRequest();
        tempAPIRequest.param["subject"] = subject;
        tempAPIRequest.param["modifyType"] = modifyType;
        tempAPIRequest.param["param"] = param;
        tempAPIRequest.setNeedGenerateID(needGenerateKeys)
        APIResponse finResponse = GlobalController.call(ModifyDataQuickLogicFunc.class, tempAPIRequest);
        return finResponse;
    }

    public static APIResponse runBatchTemplateChecking(ArrayList<List<String>> loopArr, APIRequest apiRequest) {
        APIResponse finReturn = null;
        for (def i = 0; i < loopArr.size(); i++) {
            def item = loopArr.get(i);
            finReturn = GData.runTemplateUpdate(apiRequest, item[0], item[1]);
            if (finReturn != null) {
                break;
            }
        }
        finReturn
    }

    String listColumn(String tbname, String skipColumnConcatStr) {
        def finarr = ['ID', 'CREATE_TIME']
        if (skipColumnConcatStr.length() != 0) {
            skipColumnConcatStr.split(",").each {
                finarr.push(it.toUpperCase())
            }
        }
        finarr = finarr.collect({ it -> "'${it}'".toString() })
        def arr = query("""
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.columns 
WHERE upper(TABLE_NAME) = upper('${tbname}') AND column_name NOT IN (${finarr.join(',')})
""")
        def mstr = arr.collect({ it -> it['COLUMN_NAME'] }).join(",")
        return mstr;
    }

    boolean hasColumn(String tablename, String column) {
        def arr = query("""
SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.columns 
WHERE upper(TABLE_NAME) = upper('${tablename}') AND upper(column_name) = upper('${column}')
""")
        return arr.size() != 0
    }
}
