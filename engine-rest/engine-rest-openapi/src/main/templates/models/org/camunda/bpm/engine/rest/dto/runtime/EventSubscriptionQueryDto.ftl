<@lib.dto desc = "A event subscription query which retrieves a list of event subscriptions">

    <@lib.property
        name = "eventSubscriptionId"
        type = "string"
        desc = "The id of the event subscription." />

    <@lib.property
        name = "eventName"
        type = "string"
        desc = "The name of the event this subscription belongs to as defined in the process model." />

    <@lib.property
        name = "eventType"
        type = "string"
        desc = "The type of the event subscription." />

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
        desc = "The identifier of the activity that this event subscription belongs to. This could for example be the id of a receive task." />

    <@lib.property
        name = "tenantIdIn"
        type = "array"
        desc = "Filter by a comma-separated list of tenant ids. Only select subscriptions that belong to one of the given tenant ids." />

    <@lib.property
        name = "withoutTenantId"
        type = "boolean"
        desc = "Only select subscriptions which have no tenant id. Value values are true and false." />

    <@lib.property
        name = "includeEventSubscriptionsWithoutTenantId"
        type = "boolean"
        desc = "Select event subscriptions which have no tenant id. Can be used in combination with tenantIdIn parameter. Value values are true and false." />
      
      "sorting": {
      "type": "array",
      "description": "Apply sorting of the result",
      "items":

        <#assign last = true >
        <#assign sortByValues = ['"created"', '"tenantId"']>
        <#include "/lib/commons/sort-props.ftl" >

       }
      
</@lib.dto>