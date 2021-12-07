<#macro dto_macro docsUrl="">
<@lib.dto>

        <@lib.property
        name = "count"
        type = "integer"
        format = "int64"
        last = true
        desc = "An integer value representing the count for this metric."/>

</@lib.dto>

</#macro>