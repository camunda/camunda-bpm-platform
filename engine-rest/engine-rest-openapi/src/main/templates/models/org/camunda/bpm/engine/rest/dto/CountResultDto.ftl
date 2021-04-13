<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "count"
        type = "integer"
        format = "int64"
        nullable = false
        last = true
        desc = "The number of matching instances."/>

</@lib.dto>
</#macro>