package cc.codegen.dsl.dto.lang.coffeescript

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
        return noExplicitDataType(generalDataType,databaseOriginalType)
    }

    String getCurrentFileExtensionName() {
        return ".coffee"
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
        return 'coffeescript'
    }

    @Override
    public String getDataTypeStrWhenArrayType(String dataType) {
        return "List <${dataType}>"
    }

}
