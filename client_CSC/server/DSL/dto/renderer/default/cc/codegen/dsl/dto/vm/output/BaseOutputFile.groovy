package cc.codegen.dsl.dto.vm.output

abstract class BaseOutputFile {
    String templateName = "dto.ftl";
    Map<String, String> modelMap;
}
