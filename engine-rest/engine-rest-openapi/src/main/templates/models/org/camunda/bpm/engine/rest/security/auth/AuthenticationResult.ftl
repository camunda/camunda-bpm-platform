<@lib.dto>

    <@lib.property
        name = "authenticatedUser"
        type = "string"
        desc = "An id of authenticated user."
    />

    <@lib.property
        name = "isAuthenticated"
        type = "boolean"
        desc = "A flag indicating if user is authenticated."
    />

    <#-- The tenants and groups properties are always retured, but always null in 7.14 -->
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