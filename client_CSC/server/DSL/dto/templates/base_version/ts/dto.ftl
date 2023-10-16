<#include "/common/snippet.ftl"/>
class ${ipt.clzBody.clzName} {
<#list ipt.clzBody.fields as eachField>
    <@comment_wrapper eachField "Field, dataType -> ${eachField.dataType}"></@comment_wrapper>
    ${eachField.name}: ${eachField.dataType} <#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
</#list>
}

<#if !all_in_one>
export default ${ipt.clzBody.clzName};
</#if>