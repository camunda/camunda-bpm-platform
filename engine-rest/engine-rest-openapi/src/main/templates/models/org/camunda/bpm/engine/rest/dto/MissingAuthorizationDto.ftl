<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "permissionName"
        type = "string"
        desc = "The permission name that the user is missing." />

    <@lib.property
        name = "resourceName"
        type = "string"
        desc = "The name of the resource that the user is missing permission for." />

    <@lib.property
        name = "resourceId"
        type = "string"
        last = true
        desc = "The id of the resource that the user is missing permission for." />

</@lib.dto>
</#macro>