package cc.codegen.dsl.dto.spec.lang

import cc.codegen.dsl.dto.spec.motion.InputArgs
import cc.codegen.dsl.dto.spec.motion.OutputFile

class AbstractLangRenderer {
    /**
     * please be noted that program will not generate any kinda file factually while is executing the method preHandle.
     * @param inputArgs
     * @return
     */
    public List<OutputFile> preHandle(InputArgs inputArgs) {
        return []
    }
}
