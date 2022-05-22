package cc.codegen.dsl.dto.lang.ts

import cc.codegen.dsl.dto.connotations.LangRenderer
import cc.codegen.dsl.dto.mapping.DataType
import cc.codegen.dsl.dto.spec.impl.AbstractLangRendererProxy
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile

@LangRenderer
class MainLangRenderer extends AbstractLangRendererProxy {

    @Override
    String convertDataTypeFromGeneralDataType(String generalDataType, String databaseOriginalType) {
        switch (generalDataType) {
            case DataType.GeneralDataType.CG_TYPE_ARRAY:
                return 'any[]'
            case DataType.GeneralDataType.STRING:
                return 'string';
            case DataType.GeneralDataType.TIMESTAMP:
                return 'Date';
            case DataType.GeneralDataType.BOOLEAN:
                return 'boolean';
            case DataType.GeneralDataType.BYTE_ARR:
                return 'number[]';
            case DataType.GeneralDataType.BIG_DECIMAL:
                return 'number';
            case DataType.GeneralDataType.DATE:
                return 'java.util.Date';
            case DataType.GeneralDataType.OTHER:
                return 'Object';
            case DataType.GeneralDataType.LONG:
                return 'number';
            case DataType.GeneralDataType.INTEGER:
                return 'number';
        }
        return 'any'
    }

    String getCurrentFileExtensionName() {
        return ".ts"
    }

    @Override
    OutputArgs handle(InputArgs inputArgs) {
        super.initBeforeHandling(inputArgs)
        def fieldName = 'gen_config_package'
        Object pkgGenFolder = getSubFolderFromPkgInfoField(inputArgs, fieldName)
        pkgGenFolder = ""
        def clzName = inputArgs.clzBody.clzName
        return new OutputArgs(inputArgs, [
                new RelativeOutputFile("${pkgGenFolder}${clzName}.ts",
                        "${getCurrentLangFolderName()}/dto.ftl", [:]),
                new RelativeOutputFile("${pkgGenFolder}@types/${clzName}.d.ts",
                        "${getCurrentLangFolderName()}/dto_d_ts.ftl", [:]),
        ]
        )
    }

    public String getCurrentLangFolderName() {
        return 'ts'
    }

    @Override
    public String getDataTypeStrWhenArrayType(String dataType) {
        return "${dataType}[]"
    }

}
