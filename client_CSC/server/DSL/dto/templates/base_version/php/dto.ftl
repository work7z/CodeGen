<#include "/common/snippet.ftl"/>
<?php
class ${ipt.clzBody.clzName} {
    <#list ipt.clzBody.fields as eachField>
        <@comment_wrapper eachField "Field"></@comment_wrapper>
        ${eachField.dataType} ${eachField.name}<#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
    </#list>
}
?>