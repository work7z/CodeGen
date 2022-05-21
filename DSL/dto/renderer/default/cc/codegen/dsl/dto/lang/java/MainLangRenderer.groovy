package cc.codegen.dsl.dto.lang.java

import cc.codegen.dsl.dto.connotations.LangRenderer
import cc.codegen.dsl.dto.mapping.DataType
import cc.codegen.dsl.dto.spec.impl.AbstractLangRendererProxy
import cc.codegen.dsl.dto.utils.GenUtils
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs
import cc.codegen.dsl.dto.vm.output.BaseOutputFile
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile
import com.alibaba.fastjson.JSON

@LangRenderer
class MainLangRenderer extends AbstractLangRendererProxy {

    public void test() {
        println "hello,world"
    }

    @Override
    String convertDataTypeFromGeneralDataType(String generalDataType, String databaseOriginalType) {
        switch (generalDataType) {
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
        }
        return 'Object'
    }

    @Override
    OutputArgs handle(InputArgs inputArgs) {
        def fieldName = 'gen_config_package'
        Object pkgGenFolder = getSubFolderFromPkgInfoField(inputArgs, fieldName)
        def clzName = inputArgs.clzBody.clzName
        return new OutputArgs(inputArgs, [new RelativeOutputFile("${pkgGenFolder}${clzName}.java",
                "${getCurrentLangFolderName()}/dto.ftl", [:])])
    }

    public String getCurrentLangFolderName() {
        return 'java'
    }

    public Closure getSaveLoggingFn(Map extMap) {
        def saveLogging = extMap['saveLogging']
        if (saveLogging == null) {
            saveLogging = { String logType, String logContent, List<String> arglist = [] ->
                println "[${logType}], ${logContent}, ${arglist}"
            }
        }
        return saveLogging
    }

    public void renderByOutputArgs(OutputArgs outputArgs, File currentOutputFolder, Map<String, String> extensionMaps) {
        def fn_saveLogging = getSaveLoggingFn(extensionMaps)
        def inputArgs = outputArgs.inputArgs
        def clzBody = outputArgs.getInputArgs().clzBody
        def clzFields = clzBody.fields
        clzFields.each {
            it.setGenerateGetter(inputArgs.options['gen_do_getter'] == 'true')
            it.setGenerateSetter(inputArgs.options['gen_do_setter'] == 'true')
        }
        outputArgs.getOutputFiles().eachWithIndex { BaseOutputFile baseOutputFile, int i ->
            RelativeOutputFile relativeOutputFile = (RelativeOutputFile) baseOutputFile;
            def templateName = relativeOutputFile.getTemplateName()
            def subFileName = relativeOutputFile.getSubFileName()
            def outputFile = new File(currentOutputFolder, subFileName)
            def dslFolder = new File(extensionMaps['val_DSLFolder'])
            fn_saveLogging("info", "writing to file ${outputFile}", [])
            def strCtn = extensionMaps.fn_callFreemarkerRender([
                    model       : [
                            ipt    : outputArgs.getInputArgs(),
                            options: outputArgs.getInputArgs().getOptions(),
                    ],
                    templateBase: new File(dslFolder,
                            "dto/templates/base_version"),
                    templateName: templateName,
                    outputFile  : outputFile
            ])
            fn_saveLogging("info", "[CG_975] Generated Result: \n${strCtn}", [])
        }
    }

    @Override
    public String getDataTypeStrWhenArrayType(String dataType) {
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
