<#include "/common/snippet.ftl"/>
class ${ipt.clzBody.clzName}
{
    public:
    <#list ipt.clzBody.fields as eachField>
        <@comment_wrapper eachField "Field"></@comment_wrapper>
        var ${eachField.name}<#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
    </#list>
}