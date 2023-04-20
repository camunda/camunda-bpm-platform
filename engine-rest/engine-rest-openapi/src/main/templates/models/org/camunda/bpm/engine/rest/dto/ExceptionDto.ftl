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
        desc = "A detailed message of the error." />

    <@lib.property
        name = "code"
        type = "number"
        last = true
        desc = "The code allows your client application to identify the error in an automated fashion.
                You can look up the meaning of all built-in codes and learn how to add custom codes
                in the [User Guide](${docsUrl}/user-guide/process-engine/error-handling/#exception-codes)." />

</@lib.dto>
</#macro>