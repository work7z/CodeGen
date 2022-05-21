<#include "/common/snippet.ftl"/>
<#assign java_package=ipt.clzBody.packageName/>
<#if java_package?? && java_package!='' && !all_in_one>namespace ${java_package!''};

</#if>
using System;

public class ${ipt.clzBody.clzName} {
<#list ipt.clzBody.fields as eachField>
<@comment_wrapper eachField "Field"></@comment_wrapper>
    ${eachField.dataType} ${eachField.name}<#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
</#list>
<#list ipt.clzBody.fields as eachField>
<#if eachField.generateGetter || eachField.generateSetter>
<@comment_wrapper eachField "Getter/Setter Method"></@comment_wrapper>
    public ${eachField.dataType} ${eachField.name}{
        <#if eachField.generateGetter>get;</#if>
        <#if eachField.generateSetter>set;</#if>
    }
</#if>
</#list>
}