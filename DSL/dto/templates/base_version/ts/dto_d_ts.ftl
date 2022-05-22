<#include "/common/snippet.ftl"/>
declare class ${ipt.clzBody.clzName} {
<#list ipt.clzBody.fields as eachField>
    <@comment_wrapper eachField "Field"></@comment_wrapper>
    ${eachField.name}: ${eachField.dataType};
</#list>
}
