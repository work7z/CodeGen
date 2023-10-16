package cc.codegen.dsl.dto.lang.kotlin

import cc.codegen.dsl.dto.connotations.LangRenderer
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile

@LangRenderer
class MainLangRenderer extends cc.codegen.dsl.dto.lang.java.MainLangRenderer {

    @Override
    OutputArgs handle(InputArgs inputArgs) {
        super.initBeforeHandling(inputArgs)
        def fieldName = 'gen_config_package'
        Object pkgGenFolder = getSubFolderFromPkgInfoField(inputArgs, fieldName)
        def clzName = inputArgs.clzBody.clzName
        return new OutputArgs(inputArgs, [new RelativeOutputFile("${pkgGenFolder}${clzName}${getCurrentFileExtensionName()}",
                "${getCurrentLangFolderName()}/dto.ftl", [:])])
    }

    public String getCurrentFileExtensionName() {
        return '.kt'
    }

    public String getCurrentLangFolderName() {
        return 'kotlin'
    }

    @Override
    public String getDataTypeStrWhenArrayType(String dataType) {
        return "${dataType}[]"
    }

}

