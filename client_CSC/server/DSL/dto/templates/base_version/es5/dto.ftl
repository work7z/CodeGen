<#include "/common/snippet.ftl"/>
function ${ipt.clzBody.clzName}(){
<#list ipt.clzBody.fields as eachField>
    <@comment_wrapper eachField "Field, dataType -> ${eachField.dataType}"></@comment_wrapper>
    this.${eachField.name}<#if eachField.defaultValue??> = ${eachField.defaultValue}<#else> = null</#if>;
</#list>
}