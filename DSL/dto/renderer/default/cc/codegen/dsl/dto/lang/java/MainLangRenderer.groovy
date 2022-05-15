package cc.codegen.dsl.dto.lang.java

import cc.codegen.dsl.dto.connotations.LangRenderer
import cc.codegen.dsl.dto.mapping.DatabaseOriginalType
import cc.codegen.dsl.dto.mapping.GeneralDataType
import cc.codegen.dsl.dto.spec.impl.AbstractLangRendererProxy
import cc.codegen.dsl.dto.utils.GenUtils
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile

@LangRenderer
class MainLangRenderer extends AbstractLangRendererProxy {

    @Override
    String convertDataTypeFromGeneralDataType(GeneralDataType generalDataType, DatabaseOriginalType databaseOriginalType) {
        switch (generalDataType) {
            case GeneralDataType.STRING:
                return 'String';
            case GeneralDataType.TIMESTAMP:
                return 'java.sql.Timestamp';
            case GeneralDataType.BOOLEAN:
                return 'Boolean';
            case GeneralDataType.BYTE_ARR:
                return 'Byte[]';
            case GeneralDataType.BIG_DECIMAL:
                return 'java.math.BigDecimal';
            case GeneralDataType.DATE:
                return 'java.util.Date';
            case GeneralDataType.OTHER:
                return 'Object';
            case GeneralDataType.LONG:
                return 'Long';
        }
        return 'Object'
    }

    @Override
    OutputArgs handle(InputArgs inputArgs) {
        def fieldName = 'java_package'
        Object pkgGenFolder = getSubFolderFromPkgInfoField(inputArgs, fieldName)
        def clzName = inputArgs.clzBody.clzName
        return new OutputArgs(inputArgs, [new RelativeOutputFile("${pkgGenFolder}${clzName}.java",
                "java/dto.ftl", [:])])
    }

    public static String getDataTypeStrWhenArrayType(String dataType) {
        return "${dataType}[]"
    }

    private static String getSubFolderFromPkgInfoField(InputArgs inputArgs, String fieldName) {
        def java_package = inputArgs.getOptions().getOrDefault(fieldName, "").trim()
        def pkgGenFolder = java_package.replaceAll("\\.", GenUtils.getPathJoinChar())
        if (pkgGenFolder != '') {
            pkgGenFolder += '/'
        }
        if (inputArgs.clzBody.packageName == null) {
            inputArgs.clzBody.packageName = java_package
        }
        return pkgGenFolder
    }

    static void main(String[] args) {
        println "hello world for java generator"
    }

}
