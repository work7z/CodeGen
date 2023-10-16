package cc.codegen.dsl.dto.test

import cc.codegen.dsl.dto.spec.impl.AbstractLangRendererProxy
import cc.codegen.dsl.dto.utils.AccessingAllClassesInPackage

class CodeGenLogicEntry {
    public static Map<String, AbstractLangRendererProxy> getAllLangRendererMappings() {
        AccessingAllClassesInPackage accessingAllClassesInPackage = new AccessingAllClassesInPackage();
        def clzName = "cc.codegen.dsl.dto";
        def allRendererMappings = new HashSet<>()
        accessingAllClassesInPackage.findAllClassesUsingClassLoader(clzName).each {
            def packageName = it.getCanonicalName().replaceFirst(clzName + "\\.", "").split("\\.")[0]
            def inst = it.newInstance()
            allRendererMappings[packageName] = inst as AbstractLangRendererProxy;
        }
        return allRendererMappings
    }

    static void main(String[] args) {
        def instMappings = getAllLangRendererMappings()
        println instMappings
    }
}
