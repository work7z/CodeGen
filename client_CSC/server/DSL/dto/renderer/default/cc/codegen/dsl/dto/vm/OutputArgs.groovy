package cc.codegen.dsl.dto.vm

import cc.codegen.dsl.dto.vm.output.BaseOutputFile
import cc.codegen.dsl.dto.vm.output.impl.RelativeOutputFile

class OutputArgs {
    List<BaseOutputFile> outputFiles;
    InputArgs inputArgs;

    OutputArgs(InputArgs inputArgs, List<BaseOutputFile> outputFiles) {
        this.inputArgs = inputArgs;
        this.outputFiles = outputFiles
    }
}
