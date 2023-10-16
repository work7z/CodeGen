package cc.codegen.dsl.dto.vm.output.impl

import cc.codegen.dsl.dto.vm.output.BaseOutputFile

class RelativeOutputFile extends BaseOutputFile {
    // The field below will determine in which the file will be putted.
    // by default it will be putted in the root folder
    String subFileName;

    RelativeOutputFile(String subFileName, String templateName, Map<String, String> modelMap) {
        this.subFileName = subFileName
        this.templateName = templateName
        this.modelMap = modelMap
    }

}
