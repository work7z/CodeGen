package cc.codegen.dsl.dto.spec

import cc.codegen.dsl.dto.mapping.DataType

interface DatabaseLangRenderer {
    String convertDataTypeFromGeneralDataType(String GENERAL_DATA_TYPE, String DATABASE_ORIGIN_DATATYPE);

    String getGeneralDataTypeFromDatabaseOriginType(String DATABASE_ORIGIN_DATATYPE);
}

