<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        minimum = 0
        last = true
        desc = "New value for historyTimeToLive field of the definition.
                Cannot be negative."/>

</@lib.dto>
</#macro>
