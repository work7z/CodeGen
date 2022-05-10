package cc.codegen.dsl.dto

import cc.codegen.dsl.dto.spec.lang.AbstractLangRenderer
import cc.codegen.dsl.dto.spec.MainSpecification
import cc.codegen.dsl.dto.utils.AccessingAllClassesInPackage

class Main implements MainSpecification {
    public static Map<String, AbstractLangRenderer> getAllLangRendererMappings() {
        AccessingAllClassesInPackage accessingAllClassesInPackage = new AccessingAllClassesInPackage();
        def clzName = "cc.codegen.dsl.dto";
        def allRendererMappings = new HashSet<>()
        accessingAllClassesInPackage.findAllClassesUsingClassLoader(clzName).each {
            def packageName = it.getCanonicalName().replaceFirst(clzName + "\\.", "").split("\\.")[0]
            def inst = it.newInstance()
            allRendererMappings[packageName] = inst as AbstractLangRenderer;
        }
        return allRendererMappings
    }

    static void main(String[] args) {
        def instMappings = getAllLangRendererMappings()
        println instMappings
    }
}
