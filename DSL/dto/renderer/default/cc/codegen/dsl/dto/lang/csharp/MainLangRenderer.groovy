package cc.codegen.dsl.dto.lang.csharp

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
                return 'List'
            case DataType.GeneralDataType.STRING:
                return 'string';
            case DataType.GeneralDataType.TIMESTAMP:
                return 'DateTime';
            case DataType.GeneralDataType.BOOLEAN:
                return 'bool';
            case DataType.GeneralDataType.BYTE_ARR:
                return 'List <byte>';
            case DataType.GeneralDataType.BIG_DECIMAL:
                return 'decimal';
            case DataType.GeneralDataType.DATE:
                return 'Date';
            case DataType.GeneralDataType.OTHER:
                return 'Object';
            case DataType.GeneralDataType.LONG:
                return 'long';
            case DataType.GeneralDataType.INTEGER:
                return 'int';
        }
        return 'object'
    }


    String getCurrentFileExtensionName() {
        return ".cs"
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
        return 'csharp'
    }

    @Override
    public String getDataTypeStrWhenArrayType(String dataType) {
        return "List <${dataType}>"
    }

}
