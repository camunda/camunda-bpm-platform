<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "variables"
        type = "object"
        additionalProperties = true
        last = true
        dto = "VariableValueDto" />

</@lib.dto>
</#macro>