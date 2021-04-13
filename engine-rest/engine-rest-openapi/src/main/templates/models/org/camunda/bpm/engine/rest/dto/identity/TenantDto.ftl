<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the tenant."
    />

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the tenant."
        last = true
    />

</@lib.dto>
</#macro>