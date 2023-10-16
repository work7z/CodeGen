<#include "/common/snippet.ftl"/>
<#assign java_package=ipt.clzBody.packageName/>
<#if java_package?? && java_package!='' && !all_in_one>package ${java_package!''};

</#if>
class ${ipt.clzBody.clzName} {
<#list ipt.clzBody.fields as eachField>
<@comment_wrapper eachField "Field"></@comment_wrapper>
    var ${eachField.name}: ${eachField.dataType}<#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
</#list>
<#list ipt.clzBody.fields as eachField>
<#if eachField.generateGetter>
<@comment_wrapper eachField "Getter Method"></@comment_wrapper>
    public ${eachField.dataType} get${eachField.name?cap_first}(){
        return this.${eachField.name};
    }
</#if>
<#if eachField.generateSetter>
<@comment_wrapper eachField "Setter Method"></@comment_wrapper>
    public ${eachField.dataType} set${eachField.name?cap_first}(${eachField.dataType} ${eachField.name}){
        this.${eachField.name} = ${eachField.name};
    }
</#if>
</#list>
}