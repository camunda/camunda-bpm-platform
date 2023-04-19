<#macro dto_macro docsUrl="">
<@lib.dto >
    
    <@lib.property
        name = "permissionName"
        type = "string"
        desc = "Name of the permission which was checked."
    />
    
    <@lib.property
        name = "resourceName"
        type = "string"
        desc = "The name of the resource for which the permission check was performed."
    />
    
    <@lib.property
        name = "resourceId"
        type = "string"
        desc = "The id of the resource for which the permission check was performed."
    />
    
    <@lib.property
        name = "authorized"
        type = "boolean"
        desc = "Returns true or false depending on whether the user is authorized or not."
        last = true
    />

</@lib.dto>
</#macro>
