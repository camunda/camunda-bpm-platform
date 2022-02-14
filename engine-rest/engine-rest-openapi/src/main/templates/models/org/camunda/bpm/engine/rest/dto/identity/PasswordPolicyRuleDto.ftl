<#macro dto_macro docsUrl="">
<@lib.dto desc = "Describes a rule of a password policy.">

    <@lib.property
        name = "placeholder"
        type = "string"
        desc = "A placeholder string that contains the name of a password policy rule."
    />

    <@lib.property
        name = "parameter"
        type = "object"
        last = true
        addProperty = "\"additionalProperties\": { \"type\": \"string\"}"
        desc = "A map that describes the characteristics of a password policy rule, such as the minimum number of digits."
    />

</@lib.dto>
</#macro>