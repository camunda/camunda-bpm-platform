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
        enumValues = ['"assignee"', '"candidate"', '"owner"']
        last = true
        desc = "The type of the identity link. Can be any defined type. `assignee` and `owner`
                are reserved types for the task assignee and owner." />

</@lib.dto>