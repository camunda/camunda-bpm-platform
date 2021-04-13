<#macro dto_macro docsUrl="">
<@lib.dto extends="ConditionQueryParameterDto">

    <@lib.property
        name = "name"
        type = "string"
        desc = "Variable name"
        last = true
    />

</@lib.dto>

</#macro>