<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "groups"
        type = "array"
        dto = "IdentityServiceGroupDto"
        desc = "An array of group objects."
    />

    <@lib.property
        name = "groupUsers"
        type = "array"
        dto = "IdentityServiceUserDto"
        last = true
        desc = "An array that contains all users that are member in one of the groups."
    />

</@lib.dto>
</#macro>