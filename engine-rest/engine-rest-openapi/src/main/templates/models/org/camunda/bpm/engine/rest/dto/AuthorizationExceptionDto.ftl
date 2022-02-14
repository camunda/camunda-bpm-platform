<#macro dto_macro docsUrl="">
<@lib.dto
    extends = "ExceptionDto" >

    <@lib.property
        name = "userId"
        type = "string"
        desc = "The id of the user that does not have expected permissions" />

    <@lib.property
        name = "missingAuthorizations"
        type = "array"
        dto = "MissingAuthorizationDto"
        last = true />

</@lib.dto>
</#macro>