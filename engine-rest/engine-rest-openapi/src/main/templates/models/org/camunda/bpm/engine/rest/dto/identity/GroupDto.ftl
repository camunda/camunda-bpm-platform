<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the group." />

    <@lib.property
        name = "name"
        type = "string"
        desc = "The name of the group." />

    <@lib.property
        name = "type"
        type = "string"
        last = true
        desc = "The type of the group." />

</@lib.dto>
</#macro>