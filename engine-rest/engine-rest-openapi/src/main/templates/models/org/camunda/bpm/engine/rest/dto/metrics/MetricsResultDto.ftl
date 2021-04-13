<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "result"
        type = "integer"
        format = "int64"
        last = true
        desc = "The current sum (count) for the selected metric."/>

</@lib.dto>

</#macro>