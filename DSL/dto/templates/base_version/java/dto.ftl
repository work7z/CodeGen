<#assign java_package=ipt.clzBody.packageName/>
<#if java_package?? && java_package!=''>package ${java_package!''};

</#if>
class ${ipt.clzBody.clzName} {
<#list ipt.clzBody.fields as eachField>
<#if eachField.comment?? || eachField.example??>
    /**
        <#if eachField.comment??>
        <b>Comment:</b> ${eachField.comment}
        </#if>
        <#if eachField.example??>
        <b>Example:</b> ${eachField.example!''}
        </#if>
    */
</#if>
    ${eachField.dataType} ${eachField.name}<#if eachField.defaultValue??> = ${eachField.defaultValue}</#if>;
</#list>
<#list ipt.clzBody.fields as eachField>
<#if eachField.generateGetter>
    public ${eachField.dataType} get${eachField.name?cap_first}(){
        return this.${eachField.name};
    }
</#if>
<#if eachField.generateSetter>
    public ${eachField.dataType} set${eachField.name?cap_first}(${eachField.dataType} ${eachField.name}){
        this.${eachField.name} = ${eachField.name};
    }
</#if>
</#list>
}
