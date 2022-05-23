<#include "/common/snippet.ftl"/>
class ${ipt.clzBody.clzName}:
    def __init__(self, <@compress single_line=true>
<#list ipt.clzBody.fields as eachField>
${eachField.name}<#if eachField_index != ipt.clzBody.fields?size - 1>,</#if>
</#list>
    </@compress>):
<#list ipt.clzBody.fields as eachField>
        <@comment_wrapper eachField "Field, dataType -> ${eachField.dataType}"></@comment_wrapper>
        self.${eachField.name}<#if eachField.defaultValue??> = ${eachField.defaultValue}<#else> = ${eachField.name}</#if>;
</#list>
