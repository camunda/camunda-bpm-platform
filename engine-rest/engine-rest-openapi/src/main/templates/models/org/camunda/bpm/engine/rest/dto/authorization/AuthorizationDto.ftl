<#macro dto_macro docsUrl="">
<@lib.dto >

    <#-- NOTE: Any changes made to this Dto should be mirrored to the
        AuthorizationUpdateDto (if the new/adjusted property can be updated) -->

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the authorization."
    />
    
    <@lib.property
        name = "type"
        type = "integer"
        format = "int32"
        desc = "The type of the authorization (0=global, 1=grant, 2=revoke). See the
                [User Guide](${docsUrl}/user-guide/process-engine/authorization-service.md#authorization-type)
                for more information about authorization types."
    />
    
    <@lib.property
        name = "permissions"
        type = "array"
        itemType = "string"
        desc = "An array of Strings holding the permissions provided by this authorization."
    />
    
    <@lib.property
        name = "userId"
        type = "string"
        desc = "The id of the user this authorization has been created for. The value `*`
                represents a global authorization ranging over all users."
    />
    
    <@lib.property
        name = "groupId"
        type = "string"
        desc = "The id of the group this authorization has been created for."
    />
    
    <@lib.property
        name = "resourceType"
        type = "integer"
        format = "int32"
        desc = "An integer representing the resource type. See the
                [User Guide](${docsUrl}/user-guide/process-engine/authorization-service/#resources)
                for a list of integer representations of resource types."
    />
    
    <@lib.property
        name = "resourceId"
        type = "string"
        desc = "The resource Id. The value `*` represents an authorization ranging over all
                instances of a resource."
    />
    
    <@lib.property
        name = "removalTime"
        type = "string"
        format = "date-time"
        desc = "The removal time indicates the date a historic instance
                authorization is cleaned up. A removal time can only be assigned to a historic
                instance authorization. Can be `null` when not related to a historic instance
                resource or when the removal time strategy is end and the root process instance
                is not finished. Default format `yyyy-MM-dd'T'HH:mm:ss.SSSZ`."
    />
    
    <@lib.property
        name = "rootProcessInstanceId"
        type = "string"
        desc = "The process instance id of the root process instance the historic
                instance authorization is related to. Can be `null` if not related to a historic instance
                resource."
        last = true
    />

</@lib.dto>
</#macro>