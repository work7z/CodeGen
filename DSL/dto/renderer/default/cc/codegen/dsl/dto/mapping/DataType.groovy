package cc.codegen.dsl.dto.mapping

class DataType {
    public static class GeneralDataType {
        public static final String DATE = "DATE"
        public static final String TIMESTAMP = "TIMESTAMP"
        public static final String BIG_DECIMAL = "BIG_DECIMAL"
        public static final String LONG = "LONG"
        public static final String INTEGER = "INTEGER"
        public static final String BOOLEAN = "BOOLEAN"
        public static final String BYTE_ARR = "BYTE_ARR"
        public static final String STRING = "STRING"
        public static final String OTHER = "OTHER"
        public static final String BLOB = "BLOB"
        public static final String CG_TYPE_ARRAY = "CG_TYPE_ARRAY"
    }

    public static class DatabaseOriginalType {
        // e.g. array type
        public static final String CG_TYPE_ARRAY = "CG_TYPE_ARRAY"
// e.g. null
        public static final String CHARACTER_VARYING = "CHARACTER_VARYING"
// e.g. String
        public static final String CHARACTER = "CHARACTER"
// e.g. null
        public static final String CHARACTER_LARGE_OBJECT = "CHARACTER_LARGE_OBJECT"
// e.g. String
        public static final String VARCHAR_IGNORECASE = "VARCHAR_IGNORECASE"
// e.g. String
        public static final String CHAR = "CHAR"
// e.g. String
        public static final String VARCHAR = "VARCHAR"
// e.g. Object
        public static final String TINYBLOB = "TINYBLOB"
// e.g. String
        public static final String TINYTEXT = "TINYTEXT"
// e.g. String
        public static final String BLOB = "BLOB"
// e.g. String
        public static final String TEXT = "TEXT"
// e.g. Object
        public static final String MEDIUMBLOB = "MEDIUMBLOB"
// e.g. String
        public static final String MEDIUMTEXT = "MEDIUMTEXT"
// e.g. Object
        public static final String LONGBLOB = "LONGBLOB"
// e.g. String
        public static final String LONGTEXT = "LONGTEXT"
// e.g. String
        public static final String VHARCHAR2 = "VHARCHAR2"
// e.g. String
        public static final String NVARCHAR = "NVARCHAR"
// e.g. String
        public static final String NVARCHAR2 = "NVARCHAR2"
// e.g. String
        public static final String LOB = "LOB"
// e.g. Byte[]
        public static final String BINARY = "BINARY"
// e.g. Byte[]
        public static final String BINARY_VARYING = "BINARY_VARYING"
// e.g. Object
        public static final String BINARY_LARGE_OBJECT = "BINARY_LARGE_OBJECT"
// e.g. Boolean
        public static final String BOOLEAN = "BOOLEAN"
// e.g. Integer
        public static final String TINYINT = "TINYINT"
// e.g. Integer
        public static final String SMALLINT = "SMALLINT"
// e.g. Integer
        public static final String INTEGER = "INTEGER"
// e.g. Long
        public static final String BIGINT = "BIGINT"
// e.g. Long
        public static final String LONG = "LONG"
// e.g. java.math.BigDecimal
        public static final String real = "real"
// e.g. java.math.BigDecimal
        public static final String double_precision = "double_precision"
// e.g. java.math.BigDecimal
        public static final String smallserial = "smallserial"
// e.g. java.math.BigDecimal
        public static final String serial = "serial"
// e.g. java.math.BigDecimal
        public static final String bigserial = "bigserial"
// e.g. java.math.BigDecimal
        public static final String money = "money"
// e.g. java.math.BigDecimal
        public static final String NUMBER = "NUMBER"
// e.g. java.math.BigDecimal
        public static final String NUMERIC = "NUMERIC"
// e.g. java.math.BigDecimal
        public static final String BINARY_FLOAT = "BINARY_FLOAT"
// e.g. java.math.BigDecimal
        public static final String BINARY_DOUBLE = "BINARY_DOUBLE"
// e.g. java.math.BigDecimal
        public static final String REAL = "REAL"
// e.g. java.math.BigDecimal
        public static final String DOUBLE_PRECISION = "DOUBLE_PRECISION"
// e.g. java.math.BigDecimal
        public static final String DECFLOAT = "DECFLOAT"
// e.g. java.util.Date
        public static final String DATE = "DATE"
// e.g. java.util.Date
        public static final String YEAR = "YEAR"
// e.g. java.util.Date
        public static final String DATETIME = "DATETIME"
// e.g. java.sql.Timestamp
        public static final String TIME = "TIME"
// e.g. null
        public static final String TIMESTAMP_WITH_LOCAL_TIME_ZONE = "TIMESTAMP_WITH_LOCAL_TIME_ZONE"
// e.g. null
        public static final String INTERVAL_YEAR_TO_MONTH = "INTERVAL_YEAR_TO_MONTH"
// e.g. Object
        public static final String CLOB = "CLOB"
// e.g. Object
        public static final String NCLOB = "NCLOB"
// e.g. Object
        public static final String BFILE = "BFILE"
// e.g. Integer
        public static final String MEDIUMINT = "MEDIUMINT"
// e.g. Integer
        public static final String INT = "INT"
// e.g. java.math.BigDecimal
        public static final String FLOAT = "FLOAT"
// e.g. java.math.BigDecimal
        public static final String DOUBLE = "DOUBLE"
// e.g. java.math.BigDecimal
        public static final String DECIMAL = "DECIMAL"
// e.g. null
        public static final String INTERVAL_DAY_TO_SECOND = "INTERVAL_DAY_TO_SECOND"
// e.g. null
        public static final String TIME_WITH_TIME_ZONE = "TIME_WITH_TIME_ZONE"
// e.g. java.sql.Timestamp
        public static final String TIMESTAMP = "TIMESTAMP"
// e.g. null
        public static final String TIMESTAMP_WITH_TIME_ZONE = "TIMESTAMP_WITH_TIME_ZONE"
// e.g. Object
        public static final String OTHER = "OTHER"
    }

}
