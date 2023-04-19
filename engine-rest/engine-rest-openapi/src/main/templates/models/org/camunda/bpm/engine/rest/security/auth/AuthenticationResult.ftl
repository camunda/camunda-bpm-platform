<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "authenticatedUser"
        type = "string"
        desc = "An id of authenticated user."
    />

    <@lib.property
        name = "authenticated"
        type = "boolean"
        desc = "A flag indicating if user is authenticated."
    />

    <#-- The tenants and groups properties are always returned as null from the /identity/verify endpoint.
    Consider adjusting the description if this dto is used by another endpoint.-->
    <@lib.property
        name = "tenants"
        type = "array"
        itemType = "string"
        desc = "Will be null."
    />

    <@lib.property
        name = "groups"
        type = "array"
        itemType = "string"
        desc = "Will be null."
        last = true
    />

</@lib.dto>
</#macro>
