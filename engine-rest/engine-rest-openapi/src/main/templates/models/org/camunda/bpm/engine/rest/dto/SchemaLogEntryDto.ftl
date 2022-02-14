<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the schema log entry." />

    <@lib.property
        name = "timestamp"
        type = "string"
        format = "date-time"
        desc = "The date and time of the schema update." />

    <@lib.property
        name = "version"
        type = "string"
        last = true
        desc = "The version of the schema." />

</@lib.dto>

</#macro>