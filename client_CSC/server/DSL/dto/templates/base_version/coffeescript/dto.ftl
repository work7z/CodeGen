<#include "/common/snippet.ftl"/>
class ${ipt.clzBody.clzName}:
<#list ipt.clzBody.fields as eachField>
    <@comment_wrapper eachField "Field, dataType -> ${eachField.dataType}"></@comment_wrapper>
    ${eachField.name}<#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
</#list>
