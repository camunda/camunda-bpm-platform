<#macro dto_macro docsUrl="">
<@lib.dto>
    <#-- In the java code this inherits from UserCredentialsDto, but that brings the unnecessary
    attribute authenticatedUserPassword -->

    <@lib.property
        name = "username"
        type = "string"
        desc = "The username of a user."
    />

    <@lib.property
        name = "password"
        type = "string"
        desc = "A password of a user."
        last = true
    />

</@lib.dto>
</#macro>