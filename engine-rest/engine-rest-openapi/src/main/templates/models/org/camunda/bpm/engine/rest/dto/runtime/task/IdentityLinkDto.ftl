<#macro dto_macro docsUrl="">
<@lib.dto required = ["type"] >

    <@lib.property
        name = "userId"
        type = "string"
        desc = "The id of the user participating in this link. Either `userId` or `groupId` is set." />

    <@lib.property
        name = "groupId"
        type = "string"
        desc = "The id of the group participating in this link. Either `groupId` or `userId` is set." />

    <@lib.property
        name = "type"
        type = "string"
        last = true
        desc = "The type of the identity link. The value of the this property can be user-defined. The Process Engine
                provides three pre-defined Identity Link `type`s:

                * `candidate`
                * `assignee` - reserved for the task assignee
                * `owner` - reserved for the task owner

                **Note**: When adding or removing an Identity Link, the `type` property must be defined." />

</@lib.dto>
</#macro>