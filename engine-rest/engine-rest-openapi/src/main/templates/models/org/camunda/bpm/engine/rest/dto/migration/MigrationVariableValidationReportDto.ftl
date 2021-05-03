<#macro dto_macro docsUrl="">
<@lib.dto extends="VariableValueDto">

    <@lib.property
        name = "failures"
        type = "array"
        itemType = "string"
        desc = "A list of variable validation report messages."
        last = true
    />

</@lib.dto>
</#macro>