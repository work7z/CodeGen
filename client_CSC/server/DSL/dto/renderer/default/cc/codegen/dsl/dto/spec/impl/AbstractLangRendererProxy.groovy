package cc.codegen.dsl.dto.spec.impl


import cc.codegen.dsl.dto.mapping.DataType
import cc.codegen.dsl.dto.spec.DatabaseLangRenderer
import cc.codegen.dsl.dto.utils.GenUtils
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs
import cc.codegen.dsl.dto.vm.clz.ClzField
import cc.codegen.dsl.dto.vm.output.BaseOutputFile
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile
import cn.hutool.core.util.EscapeUtil
import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSONObject

import static cn.hutool.core.util.StrUtil.upperFirst

//import static cc.codegen.dsl.dto.mapping.DataType.DataType.DatabaseOriginalType

abstract class AbstractLangRendererProxy implements DatabaseLangRenderer {

    public String noExplicitDataType(String generalDataType, String databaseOriginalType) {
        return generalDataType + "(${databaseOriginalType})"
    }

    /**
     * preserved method
     * @param inputArgStr
     * @return
     */
    OutputArgs handle_by_str(String inputArgStr) {
        def inputArgs = JSONObject.parseObject(inputArgStr, InputArgs.class)
        def outputArgs = handle(inputArgs)
        return outputArgs;
    }

    private static boolean isAcronym(String word) {
        def arr = word.split("")
        for (int i = 0; i < arr.size(); i++) {
            def c = arr[i];
            if (Character.isLowerCase(c.charAt(0))) {
                return false;
            }
        }
        return true;
    }

    public abstract String getCurrentFileExtensionName();

    public boolean isAllUpper(String n) {
        return n.toString().replaceAll("[^\\w]", "").split("").find {
            return StrUtil.isLowerCase(it)
        } == null
    }

    def fn_auto_name = { String n ->
        def isAllUpper = isAllUpper(n)
        if (isAllUpper) {
            n = n.toLowerCase()
        }
        def finvalue = null
        if (n.contains("_")) {
            finvalue = upperFirst(StrUtil.toCamelCase(n))
        } else {
            finvalue = upperFirst(n)
        }
        if (n.toLowerCase() == 'glossentry') {
            println "handling"
        }
        return finvalue;
    }

    public String formattingClzNameByRule(String clzName, InputArgs inputArgs, Map extMaps) {
        def mystr = clzName;
        def gen_config_naming_rules = inputArgs.options['gen_config_naming_rules']
        switch (gen_config_naming_rules) {
            case 'auto':
                return (fn_auto_name(mystr))
            case 'hungarian':
                return StrUtil.toUnderlineCase(fn_auto_name(mystr))
            case 'camel':
                return upperFirst(StrUtil.toCamelCase(mystr.toLowerCase()))
            case 'upperAll':
                return (fn_auto_name(mystr)).toUpperCase()
            case 'lowerAll':
                return (fn_auto_name(mystr)).toLowerCase()
            case 'keepOriginalValue':
                return clzName;
            default:
                throw new RuntimeException("Unsupported naming rule definition")
        }
    }

    public String formattingFieldVariableNameByRule(String fieldVariableName, InputArgs inputArgs, Map extMaps) {
        def first = StrUtil.lowerFirst(formattingClzNameByRule(fieldVariableName, inputArgs, extMaps))
        return first;
    }

    public void initBeforeHandling(InputArgs inputArgs) {
        formatCurrentInputElement(inputArgs)
    }

    public void renderByOutputArgs(OutputArgs outputArgs, File currentOutputFolder, Map<String, String> extensionMaps) {
        def fn_saveLogging = getSaveLoggingFn(extensionMaps)
        try {
            // declaring items
            def inputArgs = outputArgs.inputArgs
            List<ClzField> clzFields = inputArgs.clzBody.fields
            // start handling
            clzFields.each {
                it.setGenerateGetter(inputArgs.options['gen_do_getter'] == 'true')
                it.setGenerateSetter(inputArgs.options['gen_do_setter'] == 'true')
            }
            def gen_generate_source_definition = inputArgs.options['gen_generate_source_definition']
            def gen_global_comment_with_raw_type = inputArgs.options['gen_global_comment_with_raw_type']
            def gen_global_comment_with_value_as_example = inputArgs.options['gen_global_comment_with_value_as_example']
            def isDatabaseType = gen_generate_source_definition == 'database';
            def isJsonType = gen_generate_source_definition == 'json';
            clzFields.each {
                if (gen_global_comment_with_raw_type == 'true') {
                    it.setComment("\t1. DataType: ${it.getDataType()}")
                    it.setShowingComment(true)
                }
                if (gen_global_comment_with_value_as_example == 'true') {
                    if (it.getExample() == null || it.getExample().trim() == '') {
                        it.setExample("[NULL]")
                    }
                    it.setShowingExample(true)
                }
                def databaseDataType = it.getDataType()
                if (it.isUsingClzType()) {
                    // do nothing here
                } else {
                    def generalDataType = getGeneralDataTypeFromDatabaseOriginType(databaseDataType)
                    def factualDataType = convertDataTypeFromGeneralDataType(generalDataType, databaseDataType, [
                            inputArgs: inputArgs
                    ])
                    it.setDatabaseDataType(databaseDataType)
                    it.setDataType(factualDataType)
                }
                if (!(it.generalType in [null, ''])) {
                    if (it.name == 'menuitem') {
                        println "here testing"
                    }
                    println "here"
                    def generalDataType = getGeneralDataTypeFromDatabaseOriginType(it.generalType)
                    def factualDataType = convertDataTypeFromGeneralDataType(generalDataType, it.generalType, [
                            inputArgs: inputArgs
                    ])
                    def factualDataTypeForGeneralType = factualDataType;
                    if (it.generalType.startsWith("@clz_")) {
                        it.generalType = it.generalType.replace("@clz_", "")
                        factualDataTypeForGeneralType = formattingClzNameByRule(it.generalType, inputArgs, [:])
                    }
                    it.setHasArrayCollectionType(true)
                    println "factualDataTypeForGeneralType, ${factualDataTypeForGeneralType}"
                    it.setDataType(getDataTypeStrWhenArrayType(factualDataTypeForGeneralType))
                }
            }
            outputArgs.getOutputFiles().eachWithIndex { BaseOutputFile baseOutputFile, int i ->
                RelativeOutputFile relativeOutputFile = (RelativeOutputFile) baseOutputFile;
                def templateName = relativeOutputFile.getTemplateName()
                def subFileName = relativeOutputFile.getSubFileName()
                def all_in_one = inputArgs.options['gen_config_all_in_one'] == 'yes'
                def outputFile = new File(currentOutputFolder, subFileName)
                if (all_in_one) {
                    def baseSplit = outputFile.getName().split("\\.")
                    def filterSplit = []
                    baseSplit.eachWithIndex { String entry, int ii ->
                        filterSplit.add(entry)
                    }
                    outputFile = new File(
                            outputFile.getParentFile(),
                            "AllInOne.${filterSplit.subList(1, filterSplit.size()).join(".")}"
                    )
                }
                def dslFolder = new File(extensionMaps['val_DSLFolder'])
                fn_saveLogging("info", "writing to file ${outputFile}", [])
                def strCtn = extensionMaps.fn_callFreemarkerRender([model       : [ipt    : outputArgs.getInputArgs(),
                                                                                   options: outputArgs.getInputArgs().getOptions(),],
                                                                    templateBase: new File(dslFolder,
                                                                            "dto/templates/base_version"),
                                                                    templateName: templateName,
                                                                    outputFile  : outputFile,
                                                                    isAppend    : all_in_one,
                ])
                fn_saveLogging("info", "[CG_975] Generated Result: \n${EscapeUtil.escapeHtml4(strCtn)}", [])
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace()
            def e_str = GenUtils.getErrToStr(throwable)
            fn_saveLogging("error", "[CG_975] ${e_str}", [])
            throw throwable
        }
    }

    public void formatCurrentInputElement(inputArgs) {
        def clzBody = inputArgs.clzBody
        def clzFields = clzBody.fields
        checkBeforeHandlingFieldsConversion()
        // convert for the name of clz and fields
        clzBody.clzName = formattingClzNameByRule(clzBody.clzName, inputArgs, [:])
        clzFields.each {
            if (it.usingClzType) {
                it.dataType = formattingClzNameByRule(it.dataType, inputArgs, [:])
            }
            it.name = formattingFieldVariableNameByRule(it.name, inputArgs, [:]);
        }
        clzBody.fields = clzFields
    }


    @Override
    String getGeneralDataTypeFromDatabaseOriginType(String DATABASE_ORIGIN_DATATYPE) {
        switch (DATABASE_ORIGIN_DATATYPE.replaceAll("\\s", "_")) {
            case {
                it in [DataType.DatabaseOriginalType.CG_TYPE_ARRAY]
            }:
                return DataType.DatabaseOriginalType.CG_TYPE_ARRAY
                break;
            case {
                it in [DataType.DatabaseOriginalType.VARCHAR,
                       DataType.DatabaseOriginalType.TEXT,
                       DataType.DatabaseOriginalType.LONGTEXT,
                       DataType.DatabaseOriginalType.LONGTEXT,
                       DataType.DatabaseOriginalType.CHARACTER_VARYING,
                       DataType.DatabaseOriginalType.CHARACTER,
                       DataType.DatabaseOriginalType.CHARACTER_LARGE_OBJECT,
                       DataType.DatabaseOriginalType.VARCHAR_IGNORECASE,
                       DataType.DatabaseOriginalType.CHAR,
                       DataType.DatabaseOriginalType.TINYTEXT,
                       DataType.DatabaseOriginalType.TEXT,
                       DataType.DatabaseOriginalType.MEDIUMTEXT,
                       DataType.DatabaseOriginalType.VHARCHAR2,
                       DataType.DatabaseOriginalType.NVARCHAR2,
                       DataType.DatabaseOriginalType.NVARCHAR,
                       DataType.DatabaseOriginalType.LOB,
                       DataType.DatabaseOriginalType.CHARACTER_VARYING,
                       DataType.DatabaseOriginalType.CHARACTER_VARYING,
                       DataType.DatabaseOriginalType.CHARACTER_VARYING,
                       DataType.DatabaseOriginalType.CHARACTER_VARYING,]
            }:
                return DataType.GeneralDataType.STRING
                break;
            case {
                it in [DataType.DatabaseOriginalType.TINYBLOB,
                       DataType.DatabaseOriginalType.BLOB,
                       DataType.DatabaseOriginalType.MEDIUMBLOB,
                       DataType.DatabaseOriginalType.LONGBLOB,
                       DataType.DatabaseOriginalType.LOB,
                       DataType.DatabaseOriginalType.LONGBLOB,
                       DataType.DatabaseOriginalType.BINARY,
                       DataType.DatabaseOriginalType.BINARY_VARYING,
                       DataType.DatabaseOriginalType.BINARY_LARGE_OBJECT,
                       DataType.DatabaseOriginalType.LONGBLOB,]
            }:
                return DataType.GeneralDataType.BYTE_ARR
                break;
            case {
                it in [DataType.DatabaseOriginalType.OTHER,]
            }:
                return DataType.GeneralDataType.BLOB
                break;
            case {
                it in [DataType.DatabaseOriginalType.BFILE,
                       DataType.DatabaseOriginalType.NCLOB,
                       DataType.DatabaseOriginalType.CLOB,]
            }:
                return DataType.GeneralDataType.BLOB
            case {
                it in [DataType.DatabaseOriginalType.BOOLEAN,]
            }:
                return DataType.GeneralDataType.BOOLEAN
            case {
                it in [DataType.DatabaseOriginalType.TINYINT,
                       DataType.DatabaseOriginalType.INTEGER,
                       DataType.DatabaseOriginalType.SMALLINT,
                       DataType.DatabaseOriginalType.MEDIUMINT,
                       DataType.DatabaseOriginalType.INT,]
            }:
                return DataType.GeneralDataType.INTEGER
            case {
                it in [DataType.DatabaseOriginalType.BIGINT,
                       DataType.DatabaseOriginalType.LONG,]
            }:
                return DataType.GeneralDataType.LONG
            case {
                it in [DataType.DatabaseOriginalType.real,
                       DataType.DatabaseOriginalType.smallserial,
                       DataType.DatabaseOriginalType.double_precision,
                       DataType.DatabaseOriginalType.serial,
                       DataType.DatabaseOriginalType.DECIMAL,
                       DataType.DatabaseOriginalType.bigserial,
                       DataType.DatabaseOriginalType.money,
                       DataType.DatabaseOriginalType.NUMBER,
                       DataType.DatabaseOriginalType.NUMERIC,
                       DataType.DatabaseOriginalType.BINARY_FLOAT,
                       DataType.DatabaseOriginalType.BINARY_DOUBLE,
                       DataType.DatabaseOriginalType.REAL,
                       DataType.DatabaseOriginalType.DOUBLE_PRECISION,
                       DataType.DatabaseOriginalType.DECFLOAT,
                       DataType.DatabaseOriginalType.FLOAT,
                       DataType.DatabaseOriginalType.DOUBLE,
                       DataType.DatabaseOriginalType.FLOAT,
                       DataType.DatabaseOriginalType.FLOAT,]
            }:
                return DataType.GeneralDataType.BIG_DECIMAL
            case {
                it in [DataType.DatabaseOriginalType.INTERVAL_DAY_TO_SECOND,
                       DataType.DatabaseOriginalType.DATE,
                       DataType.DatabaseOriginalType.YEAR,
                       DataType.DatabaseOriginalType.DATETIME,]
            }:
                return DataType.GeneralDataType.DATE
            case {
                it in [DataType.DatabaseOriginalType.DATETIME,
                       DataType.DatabaseOriginalType.TIME,
                       DataType.DatabaseOriginalType.TIMESTAMP_WITH_LOCAL_TIME_ZONE,
                       DataType.DatabaseOriginalType.INTERVAL_YEAR_TO_MONTH,
                       DataType.DatabaseOriginalType.INTERVAL_DAY_TO_SECOND,
                       DataType.DatabaseOriginalType.TIME_WITH_TIME_ZONE,
                       DataType.DatabaseOriginalType.TIMESTAMP,
                       DataType.DatabaseOriginalType.TIMESTAMP_WITH_TIME_ZONE,]
            }:
                return DataType.GeneralDataType.TIMESTAMP
            case {
                it in [DataType.DatabaseOriginalType.CLOB,
                       DataType.DatabaseOriginalType.NCLOB,
                       DataType.DatabaseOriginalType.BFILE,]
            }:
                return DataType.GeneralDataType.BLOB
        }
        return DataType.GeneralDataType.OTHER
    }


    public void checkBeforeHandlingFieldsConversion() {
        DataType.DatabaseOriginalType.fields.each {
            if (it.getName() == '__$stMC' || it.getName() == 'OTHER') {
                return;
            }
            def dbName = it.getName()
            def n = getGeneralDataTypeFromDatabaseOriginType(dbName)
            if (n == DataType.GeneralDataType.OTHER) {
                println it.getName()
                throw new RuntimeException("Please finish all of these type mappings for its conversion, no related fields for ${it.getName()} in DataType.DatabaseOriginalType.fields")
            }
        }
    }

    /**
     * Getting related output args
     * @param inputArgs
     * @return
     */
    public abstract OutputArgs handle(InputArgs inputArgs);

    public abstract String getDataTypeStrWhenArrayType(String dataType);

    public String convertFieldFromJSONSource(Object value) {
        def finalDatabaseType = DataType.DatabaseOriginalType.OTHER;
        if (value == null) {
            finalDatabaseType = DataType.DatabaseOriginalType.OTHER
        } else if (value instanceof String) {
            finalDatabaseType = DataType.DatabaseOriginalType.VARCHAR
        } else if (value instanceof BigDecimal) {
            finalDatabaseType = DataType.DatabaseOriginalType.DECIMAL
        } else if (value instanceof Double || value instanceof Float) {
            finalDatabaseType = DataType.DatabaseOriginalType.DOUBLE
        } else if (value instanceof Integer) {
            finalDatabaseType = DataType.DatabaseOriginalType.INTEGER
        }
        return getGeneralDataTypeFromDatabaseOriginType(finalDatabaseType);
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


    public Closure getSaveLoggingFn(Map extMap) {
        def saveLogging = extMap['saveLogging']
        if (saveLogging == null) {
            saveLogging = { String logType, String logContent, List<String> arglist = [] -> println "[${logType}], ${logContent}, ${arglist}"
            }
        }
        return saveLogging
    }


}
