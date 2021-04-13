<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "historyTimeToLive"
        type = "integer"
        format = "int32"
        minimum = 0
        last = true
        desc = "New value for historyTimeToLive field of the definition.
                Can be `null`. Can not be negative."/>

</@lib.dto>
</#macro>