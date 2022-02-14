<#macro dto_macro docsUrl="">
<@lib.dto
    title="ExceptionDto">

    <@lib.property
        name = "type"
        type = "string"
        desc = "An exception class indicating the occurred error." />

    <@lib.property
        name = "message"
        type = "string"
        last = true
        desc = "A detailed message of the error." />

</@lib.dto>
</#macro>