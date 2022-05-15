<#if java_package?? && java_package!=''>package ${java_package!''};
</#if>
class ${ipt.clzBody.clzName} {
<#list ipt.clzBody.fields as eachField>
<#compress>
<#if eachField.comment?? || eachField.example>
    /**
        <#if eachField.comment??>
        <b>Comment:</b>
            ${eachField.comment}
        </#if>
        <#if eachField.example??>
        <b>Example:</b>
            ${eachField.example!''}
        </#if>
    */
</#if>
<@compress single_line=true>
${eachField.dataType} ${eachField.name}<#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
</@compress>
<#/compress>
</#list>

<#list ipt.clzBody.fields as eachField>
<#compress>
    <#if eachField.generateGetter>
        public ${eachField.dataType} get${eachField?cap_first}(){
            return this.${eachField};
        }
    </#if>
    <#if eachField.generateSetter>
        public ${eachField.dataType} set${eachField?cap_first}(${eachField.dataType} ${eachField.name}){
            this.${eachField.name} = ${eachField.name};
        }
    </#if>
</#compress>

</#if>

}