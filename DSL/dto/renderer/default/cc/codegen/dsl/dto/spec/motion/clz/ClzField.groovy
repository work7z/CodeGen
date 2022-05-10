package cc.codegen.dsl.dto.spec.motion.clz

class ClzField {
    String name;
    String dataType;
    String comment;
    String example;
    boolean isArray=false;
    boolean isClz=false;
    boolean generateGetter=false;
    boolean generateSetter=false;
}
