package cc.codegen.dsl.dto.vm.clz

class ClzField {
    String name;
    String databaseDataType;
    String dataType;
    String generalType;
    String comment;
    String example;
    String defaultValue;
    boolean usingClzType=false;
    boolean hasArrayCollectionType=false;
    boolean generateGetter=false;
    boolean generateSetter=false;
    // TODO: new features yet not supported
//    List<String> enumValue;
}
