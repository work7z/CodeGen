package com.denote.client.core

import com.alibaba.druid.pool.DruidDataSource
import com.denote.client.dto.connection.ConnectionManager
import com.denote.client.dto.APIRequest
import com.denote.client.dto.APIResponse
import com.denote.client.dto.connection.ConnectionLiveContext
import com.denote.client.dto.connection.ConnectionMemContext
import com.denote.client.dto.dblink.DBTypeRef
import com.denote.client.exceptions.CannotConnectionException
import com.denote.client.utils.GData
import com.denote.client.utils.GUtils

class DbLinkGetConnectionLogicFunc extends BasicLogicFunc {
    @Override
    APIResponse handle(APIRequest apiRequest) {
        def conn = apiRequest.param['conn']
        def isTest = apiRequest.param['test'] as boolean
        if (conn['ID'] == null && isTest) {
            conn['ID'] = GUtils.uuid()
        }
        def g = GData.g();
        def crtSaveId = Objects.toString(conn['ID'])
        // searching more detail about this connection
        def crtDbDriver = g.queryFirst("""
SELECT b.DATABASE_ICON,b.DATABASE_NAME,B.DATABASE_PROP,a.* FROM G_DBLINK_DRIVER a JOIN G_DBLINK_DBTYPE  b ON a.dbtype_id=b.id
where a.id=:DRIVER_ID
""", [DRIVER_ID: conn['DRIVER_ID']])
        if (crtDbDriver == null) {
            throw new CannotConnectionException("no qualified driver for the connection")
        }
        def crtConn = conn;
        // init class loader
        // init datasource
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        def localJarFilePath = crtDbDriver['LOCAL_JAR_FILE'] as String
        if (localJarFilePath == null) {
            localJarFilePath = new File(GUtils.getDriversDir(),
                    (crtDbDriver['MVN_GROUP_ID'] as String) + '/' + (crtDbDriver['MVN_ARTIFACT_ID'] as String) + '/' + crtDbDriver['MVN_ARTIFACT_ID'] + '-' + (crtDbDriver['MVN_VERSION'] as String) + '.jar')
        }
        def localJarFile = new File(localJarFilePath as String);
        if (!localJarFile.exists()) {
            throw new CannotConnectionException("driver file doesn't exists, path is ${localJarFile.getAbsolutePath()}")
        }
        groovyClassLoader.addClasspath(localJarFile.getAbsolutePath())
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassLoader(groovyClassLoader)
        def IDX_driver = 0;
        def IDX_protocol = 1;
        def driverClzNameMappings = DBTypeRef.ALL_DB_TYPE
        def DATABASE_PROP = crtDbDriver['DATABASE_PROP'] as String
        def crtConnID = crtConn['ID'] as int
        def jdbcURL = crtConn['JDBC_URL'] as String
        def port = crtConn['PORT'] as String
        def host = crtConn['HOST'] as String
        def username = crtConn['USERNAME'] as String
        String password = crtConn['PASSWORD'] as String
        def defaultDatabase = crtConn['DEFAULT_DATABASE'] as String
        def savePasswordLocally = crtConn['SAVE_PASSWORD_LOCALLY'] == 1;
        if (password == null || password.length() == 0) {
            password = null;
        }
        if (!isTest) {
            if (!savePasswordLocally) {
                if (password == null || password.length() == 0) {
                    def obj = ConnectionManager.MEM_CONTEXT.get(crtConnID as int)
                    if (obj != null) {
                        password = obj.getPassword()
                    }
                }
            }
        }
        def matchDriverObj = driverClzNameMappings[DATABASE_PROP];
        switch (DATABASE_PROP) {
            case { it -> ['mysql', 'mariadb', 'postgresql'] }:
                dataSource.setUsername(username)
                dataSource.setPassword(password)
                def protocolStr = matchDriverObj.getProtocolType();
                dataSource.setUrl("${protocolStr}://${host}:${port}/${defaultDatabase}")
                break;
        }
        dataSource.setDriverClassName(matchDriverObj.getDriverClassName())
        // put the keeper into manager
        ConnectionLiveContext connectionKeeper = new ConnectionLiveContext(crtConnID, DATABASE_PROP, isTest, dataSource)
        connectionKeeper.setCrtConn(crtConn as Map<String, Object>)
        connectionKeeper.setCrtDriver(crtDbDriver)
        connectionKeeper.setCrtConnID(crtConnID as int)
        if (!isTest) {
            ConnectionManager.setLiveObject(crtSaveId, connectionKeeper)
        }
        // TODO: to be verified about handling save password locally
        if (!isTest) {
            if (!savePasswordLocally) {
                ConnectionMemContext connectionMemStore = ConnectionManager.MEM_CONTEXT.getOrDefault(crtConnID, new ConnectionMemContext());
                connectionMemStore.setPassword(password)
                ConnectionManager.MEM_CONTEXT.put(crtConnID as int, connectionMemStore);
                g.exec("update g_dblink_connections set password=null where id=:ID", [ID: crtConnID])
            }
        }
        return APIResponse.ok(connectionKeeper)
    }
}
