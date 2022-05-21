package cc.codegen.dsl.dto.test

class TestMapping {
    public static Map<String, String> GENERAL_DATA_TYPE_MAPPING = [
            'CHARACTER VARYING'             : 'String',
            'CHARACTER'                     : 'String',
            'CHARACTER LARGE OBJECT'        : 'String',
            'VARCHAR_IGNORECASE'            : 'String',
            'CHAR'                          : 'String',
            'VARCHAR'                       : 'String',
            'TINYBLOB'                      : 'String',
            'TINYTEXT'                      : 'String',
            'BLOB'                          : 'String',
            'TEXT'                          : 'String',
            'MEDIUMBLOB'                    : 'String',
            'MEDIUMTEXT'                    : 'String',
            'LONGBLOB'                      : 'String',
            'LONGTEXT'                      : 'String',
            'VHARCHAR2'                     : 'String',
            'NVARCHAR'                      : 'String',
            'NVARCHAR2'                     : 'String',
            'LOB'                           : 'String',
            'BINARY'                        : 'Byte[]',
            'BINARY VARYING'                : 'Byte[]',
            'BINARY LARGE OBJECT'           : 'Byte[]',
            'BOOLEAN'                       : 'Boolean',
            'TINYINT'                       : 'Integer',
            'SMALLINT'                      : 'Integer',
            'INTEGER'                       : 'Integer',
            'BIGINT'                        : 'Long',
            'LONG'                          : 'String',
            'real'                          : 'java.math.BigDecimal',
            'double precision'              : 'java.math.BigDecimal',
            'smallserial'                   : 'java.math.BigDecimal',
            'serial'                        : 'java.math.BigDecimal',
            'bigserial'                     : 'java.math.BigDecimal',
            'money'                         : 'java.math.BigDecimal',
            'NUMBER'                        : 'java.math.BigDecimal',
            'NUMERIC'                       : 'java.math.BigDecimal',
            'BINARY_FLOAT'                  : 'java.math.BigDecimal',
            'BINARY_DOUBLE'                 : 'java.math.BigDecimal',
            'REAL'                          : 'java.math.BigDecimal',
            'DOUBLE PRECISION'              : 'java.math.BigDecimal',
            'DECFLOAT'                      : 'java.math.BigDecimal',
            'DATE'                          : 'java.util.Date',
            'YEAR'                          : 'java.util.Date',
            'DATETIME'                      : 'java.util.Date',
            'TIME'                          : 'java.sql.Timestamp',
            'TIMESTAMP WITH LOCAL TIME ZONE': 'java.sql.Timestamp',
            'INTERVAL YEAR TO MONTH'        : 'Long',
            'CLOB'                          : 'Object',
            'NCLOB'                         : 'Object',
            'java.util.ArrayList'           : 'java.util.ArrayList',
            'BFILE'                         : 'Object',

            'MEDIUMINT'                     : 'Integer',
            'INT'                           : 'Integer',
            'FLOAT'                         : 'java.math.BigDecimal',
            'DOUBLE'                        : 'java.math.BigDecimal',
            'DECIMAL'                       : 'java.math.BigDecimal',

            'INTERVAL DAY TO SECOND'        : 'Long',
            'TIME WITH TIME ZONE'           : 'java.sql.Timestamp',
            'TIMESTAMP'                     : 'java.sql.Timestamp',
            'TIMESTAMP WITH TIME ZONE'      : 'java.sql.Timestamp',
            '@@OTHER'                       : 'Object'

    ]

    static void main(String[] args) {
        GENERAL_DATA_TYPE_MAPPING.keySet().each {
            it = it.replaceAll("\\s+", "_")
            def myvalue = GENERAL_DATA_TYPE_MAPPING[it]
            def str = """// e.g. ${myvalue}\n""" + it + ","
            println str
        }
    }
}
