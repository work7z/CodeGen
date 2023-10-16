package cc.codegen.dsl.dto.lang.go

import cc.codegen.dsl.dto.connotations.LangRenderer
import cc.codegen.dsl.dto.mapping.DataType
import cc.codegen.dsl.dto.spec.impl.AbstractLangRendererProxy
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile

@LangRenderer
class MainLangRenderer extends AbstractLangRendererProxy {

    @Override
    String convertDataTypeFromGeneralDataType(String generalDataType, String databaseOriginalType, Map extMap) {
        def inputArgs = extMap['inputArgs'] as InputArgs
        switch (generalDataType) {
            case DataType.GeneralDataType.CG_TYPE_ARRAY:
                return '[]'
            case DataType.GeneralDataType.STRING:
                return 'string';
            case DataType.GeneralDataType.TIMESTAMP:
                return 'int';
            case DataType.GeneralDataType.BOOLEAN:
                return 'bool';
            case DataType.GeneralDataType.BYTE_ARR:
                return inputArgs.options['gen_golang_byte_array_definition'];
            case DataType.GeneralDataType.BIG_DECIMAL:
                return 'float64';
            case DataType.GeneralDataType.DATE:
                return 'int';
            case DataType.GeneralDataType.OTHER:
                return 'struct';
            case DataType.GeneralDataType.LONG:
                return 'int';
            case DataType.GeneralDataType.INTEGER:
                return 'int';
        }
        return 'struct'
    }

    String getCurrentFileExtensionName() {
        return ".go"
    }

    @Override
    OutputArgs handle(InputArgs inputArgs) {
        super.initBeforeHandling(inputArgs)
        def fieldName = 'gen_config_package'
        Object pkgGenFolder = getSubFolderFromPkgInfoField(inputArgs, fieldName)
        pkgGenFolder = ""
        def clzName = inputArgs.clzBody.clzName
        return new OutputArgs(inputArgs, [new RelativeOutputFile("${pkgGenFolder}${clzName}${getCurrentFileExtensionName()}",
                "${getCurrentLangFolderName()}/dto.ftl", [:])])
    }

    public String getCurrentLangFolderName() {
        return 'go'
    }

    @Override
    public String getDataTypeStrWhenArrayType(String dataType) {
        return "[]${dataType}"
    }

}
