package cc.codegen.dsl.dto.lang.java

import cc.codegen.dsl.dto.connotations.LangRenderer
import cc.codegen.dsl.dto.mapping.DataType
import cc.codegen.dsl.dto.spec.impl.AbstractLangRendererProxy
import cc.codegen.dsl.dto.utils.GenUtils
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs
import cc.codegen.dsl.dto.vm.output.BaseOutputFile
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile

@LangRenderer
class MainLangRenderer extends AbstractLangRendererProxy {

    @Override
    String convertDataTypeFromGeneralDataType(String generalDataType, String databaseOriginalType, Map extMap) {
        switch (generalDataType) {
            case DataType.GeneralDataType.CG_TYPE_ARRAY:
                return 'java.util.ArrayList'
            case DataType.GeneralDataType.STRING:
                return 'String';
            case DataType.GeneralDataType.TIMESTAMP:
                return 'java.sql.Timestamp';
            case DataType.GeneralDataType.BOOLEAN:
                return 'Boolean';
            case DataType.GeneralDataType.BYTE_ARR:
                return 'Byte[]';
            case DataType.GeneralDataType.BIG_DECIMAL:
                return 'java.math.BigDecimal';
            case DataType.GeneralDataType.DATE:
                return 'java.util.Date';
            case DataType.GeneralDataType.OTHER:
                return 'Object';
            case DataType.GeneralDataType.LONG:
                return 'Long';
            case DataType.GeneralDataType.INTEGER:
                return 'Integer';
        }
        return 'Object'
    }

    String getCurrentFileExtensionName() {
        return ".java"
    }

    @Override
    OutputArgs handle(InputArgs inputArgs) {
        super.initBeforeHandling(inputArgs)
        def fieldName = 'gen_config_package'
        Object pkgGenFolder = getSubFolderFromPkgInfoField(inputArgs, fieldName)
        def clzName = inputArgs.clzBody.clzName
        return new OutputArgs(inputArgs, [new RelativeOutputFile("${pkgGenFolder}${clzName}${getCurrentFileExtensionName()}",
                "${getCurrentLangFolderName()}/dto.ftl", [:])])
    }

    public String getCurrentLangFolderName() {
        return 'java'
    }

    @Override
    public String getDataTypeStrWhenArrayType(String dataType) {
        return "${dataType}[]"
    }

}
