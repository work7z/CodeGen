<#assign all_in_one=ipt.options.gen_config_all_in_one == 'yes' />
<#macro comment_wrapper eachField titleText>
<#if eachField.showingComment?? || eachField.showingExample??>
    /**
        <h1>${titleText!''}</h1>
        -----------
        <#if eachField.comment??>
        <p>
            Description: <br/>
            ${eachField.comment}
        </p>
        </#if>
        -----------
        <#if eachField.example??>
        <p>
            Example Value: <br/>
                ${eachField.example}
        </p>
        </#if>
     */
</#if>
</#macro>