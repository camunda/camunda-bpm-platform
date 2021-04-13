<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the user." />

    <@lib.property
        name = "firstName"
        type = "string"
        desc = "The first name of the user." />

    <@lib.property
        name = "lastName"
        type = "string"
        desc = "The first name of the user." />

    <@lib.property
         name = "email"
         type = "string"
         last = true
         desc = "The email of the user." />

</@lib.dto>
</#macro>