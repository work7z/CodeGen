package cc.codegen.dsl.dto.spec.impl


import cc.codegen.dsl.dto.mapping.DataType
import cc.codegen.dsl.dto.spec.DatabaseLangRenderer
import cc.codegen.dsl.dto.utils.GenUtils
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs
import cc.codegen.dsl.dto.vm.clz.ClzBody
import cc.codegen.dsl.dto.vm.clz.ClzField
import cc.codegen.dsl.dto.vm.output.BaseOutputFile
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile
import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSONObject

import static cn.hutool.core.util.StrUtil.lowerFirst
import static cn.hutool.core.util.StrUtil.upperFirst
import static cn.hutool.core.util.StrUtil.upperFirst
import static cn.hutool.core.util.StrUtil.upperFirst

//import static cc.codegen.dsl.dto.mapping.DataType.DataType.DatabaseOriginalType

abstract class AbstractLangRendererProxy implements DatabaseLangRenderer {

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


    public String formattingClzNameByRule(String clzName, InputArgs inputArgs, Map extMaps) {
        def fn_auto_name = { String n ->
            if (n.contains("_")) {
                return upperFirst(StrUtil.toCamelCase(n.toLowerCase()))
            } else {
                return upperFirst(n)
            }
        }
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
        def first = StrUtil.lowerFirst(fieldVariableName)
        return first;
    }

    public void initBeforeHandling(InputArgs inputArgs) {
        formatCurrentInputElement(inputArgs)
    }

    public void renderByOutputArgs(OutputArgs outputArgs, File currentOutputFolder, Map<String, String> extensionMaps) {
        // declaring items
        def fn_saveLogging = getSaveLoggingFn(extensionMaps)
        def inputArgs = outputArgs.inputArgs
        List<ClzField> clzFields = inputArgs.clzBody.fields
        // start handling
        clzFields.each {
            it.setGenerateGetter(inputArgs.options['gen_do_getter'] == 'true')
            it.setGenerateSetter(inputArgs.options['gen_do_setter'] == 'true')
        }
        def gen_generate_source_definition = inputArgs.options['gen_generate_source_definition']
        def isDatabaseType = gen_generate_source_definition == 'database';
        def isJsonType = gen_generate_source_definition == 'json';
        clzFields.each {
            def databaseDataType = it.getDataType()
            if (it.isUsingClzType()) {
                // do nothing here
            } else {
                def generalDataType = getGeneralDataTypeFromDatabaseOriginType(databaseDataType)
                def factualDataType = convertDataTypeFromGeneralDataType(generalDataType, databaseDataType)
                it.setDatabaseDataType(databaseDataType)
                it.setDataType(factualDataType)
            }
            if (!(it.generalType in [null, ''])) {
                println "here"
                def generalDataType = getGeneralDataTypeFromDatabaseOriginType(it.generalType)
                def factualDataType = convertDataTypeFromGeneralDataType(generalDataType, it.generalType)
                def factualDataTypeForGeneralType = factualDataType;
                it.setHasArrayCollectionType(true)
                println "factualDataTypeForGeneralType, ${factualDataTypeForGeneralType}"
                it.setDataType(getDataTypeStrWhenArrayType(factualDataTypeForGeneralType))
            }
        }
        outputArgs.getOutputFiles().eachWithIndex { BaseOutputFile baseOutputFile, int i ->
            RelativeOutputFile relativeOutputFile = (RelativeOutputFile) baseOutputFile;
            def templateName = relativeOutputFile.getTemplateName()
            def subFileName = relativeOutputFile.getSubFileName()
            def outputFile = new File(currentOutputFolder, subFileName)
            def dslFolder = new File(extensionMaps['val_DSLFolder'])
            fn_saveLogging("info", "writing to file ${outputFile}", [])
            def strCtn = extensionMaps.fn_callFreemarkerRender([model       : [ipt    : outputArgs.getInputArgs(),
                                                                               options: outputArgs.getInputArgs().getOptions(),],
                                                                templateBase: new File(dslFolder,
                                                                        "dto/templates/base_version"),
                                                                templateName: templateName,
                                                                outputFile  : outputFile])
            fn_saveLogging("info", "[CG_975] Generated Result: \n${strCtn}", [])
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
    }


    @Override
    String getGeneralDataTypeFromDatabaseOriginType(String DATABASE_ORIGIN_DATATYPE) {
        switch (DATABASE_ORIGIN_DATATYPE) {
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
