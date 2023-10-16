package com.denote.client.dto.dblink

import com.denote.client.dto.dblink.impl.DB2_LinkType
import com.denote.client.dto.dblink.impl.Mariadb_LinkType
import com.denote.client.dto.dblink.impl.MySQL_LinkType
import com.denote.client.dto.dblink.impl.Oracle_LinkType
import com.denote.client.dto.dblink.impl.Postgresql_LinkType
import com.denote.client.dto.dblink.impl.Sybase_LinkType

class DBTypeRef {
    public static MySQL_LinkType mysql = new MySQL_LinkType();
    public static DB2_LinkType db2 = new DB2_LinkType();
    public static Mariadb_LinkType mariadb = new Mariadb_LinkType();
    public static Postgresql_LinkType postgresql = new Postgresql_LinkType();
    public static Oracle_LinkType oracle = new Oracle_LinkType();
    public static Sybase_LinkType sybase = new Sybase_LinkType();
    public final static Map<String, AbstractDBLinkType> ALL_DB_TYPE = [
            mysql     : mysql,
            db2       : db2,
            mariadb   : mariadb,
            postgresql: postgresql,
            oracle    : oracle,
            sybase    : sybase
    ] as Map<String, AbstractDBLinkType>
}
