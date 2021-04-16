<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the user."
    />

    <@lib.property
        name = "firstName"
        type = "string"
        desc = "The firstname of the user."
    />

    <@lib.property
        name = "lastName"
        type = "string"
        desc = "The lastname of the user."
    />

    <@lib.property
        name = "displayName"
        type = "string"
        desc = "The displayName is generated from the id or firstName and lastName if available."
        last = true
    />

</@lib.dto>
</#macro>