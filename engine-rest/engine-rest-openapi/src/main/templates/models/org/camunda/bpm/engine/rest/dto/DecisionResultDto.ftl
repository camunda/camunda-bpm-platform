<#macro dto_macro docsUrl="">
    <@lib.dto>
        <@lib.property
            name = "result"
            type = "object"
            additionalProperties = true
            last = true
            dto = "VariableValueDto" />
    </@lib.dto>
</#macro>
