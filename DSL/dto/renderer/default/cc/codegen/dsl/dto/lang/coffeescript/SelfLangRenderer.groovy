package cc.codegen.dsl.dto.lang.coffeescript

import cc.codegen.dsl.dto.spec.impl.AbstractLangRendererProxy
import cc.codegen.dsl.dto.vm.InputArgs
import cc.codegen.dsl.dto.vm.OutputArgs

class SelfLangRenderer extends AbstractLangRendererProxy {

    @Override
    String convertDataTypeFromGeneralDataType(String generalDataType, String databaseOriginalType) {
        return null
    }

    @Override
    OutputArgs handle(InputArgs inputArgs) {
        return null
    }

    @Override
    String getDataTypeStrWhenArrayType(String dataType) {
        return null
    }
}
