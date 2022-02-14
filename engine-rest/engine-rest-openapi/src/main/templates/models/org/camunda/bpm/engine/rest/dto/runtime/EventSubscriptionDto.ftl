<#macro dto_macro docsUrl="">
<@lib.dto>

    <@lib.property
        name = "id"
        type = "string"
        desc = "The id of the event subscription." />

    <@lib.property
        name = "eventType"
        type = "string"
        desc = "The type of the event subscription." />

    <@lib.property
        name = "eventName"
        type = "string"
        desc = "The name of the event this subscription belongs to as defined in the process model." />

    <@lib.property
        name = "executionId"
        type = "string"
        desc = "The execution that is subscribed on the referenced event." />

    <@lib.property
        name = "processInstanceId"
        type = "string"
        desc = "The process instance this subscription belongs to." />

    <@lib.property
        name = "activityId"
        type = "string"
        desc = "The identifier of the activity that this event subscription belongs to.
                This could for example be the id of a receive task." />

    <@lib.property
        name = "createdDate"
        type = "string"
        format = "date-time"
        desc = "The time this event subscription was created." />

    <@lib.property
        name = "tenantId"
        type = "string"
        last = true
        desc = "The id of the tenant this event subscription belongs to.
                Can be `null` if the subscription belongs to no single tenant." />
</@lib.dto>
</#macro>