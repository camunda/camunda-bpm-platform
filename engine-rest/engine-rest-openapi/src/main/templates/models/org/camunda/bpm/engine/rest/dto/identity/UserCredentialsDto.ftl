<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "password"
        type = "string"
        desc = "The users new password." />

    <@lib.property
        name = "authenticatedUserPassword"
        type = "string"
        last = true
        desc = "The password of the authenticated user who changes the password of the user
                          (i.e., the user with passed id as path parameter)." />

</@lib.dto>
</#macro>