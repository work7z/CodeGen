<#include "/common/snippet.ftl"/>
type ${ipt.clzBody.clzName} struct {
<#list ipt.clzBody.fields as eachField>
    <@comment_wrapper eachField "Field"></@comment_wrapper>
    ${eachField.name} ${eachField.dataType}<#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
</#list>
}