{
  <@lib.endpointInfo
      id = "getEventSubscriptions"
      tag = "Event Subscription"
      desc = "Queries for event subscriptions that fulfill given parameters.
              The size of the result set can be retrieved by using the [Get Event Subscriptions count](${docsUrl}/reference/rest/event-subscription/get-query-count/) method." />

 "parameters" : [

    <@lib.parameter
        name = "eventSubscriptionId"
        location = "query"
        type = "string"
        desc = "Only select subscription with the given id." />

    <@lib.parameter
        name = "eventName"
        location = "query"
        type = "string"
        desc = "Only select subscriptions for events with the given name." />

    <@lib.parameter
        name = "eventType"
        location = "query"
        type = "string"
        desc = "Only select subscriptions for events with the given type. Valid values: `message`, `signal`, `compensate` and `conditional`." />

    <@lib.parameter
        name = "executionId"
        location = "query"
        type = "string"
        desc = "Only select subscriptions that belong to an execution with the given id." />

    <@lib.parameter
        name = "processInstanceId"
        location = "query"
        type = "string"
        desc = "Only select subscriptions that belong to a process instance with the given id." />

    <@lib.parameter
        name = "activityId"
        location = "query"
        type = "string"
        desc = "Only select subscriptions that belong to an activity with the given id." />

    <@lib.parameter
        name = "tenantIdIn"
        location = "query"
        type = "string"
        desc = "Filter by a comma-separated list of tenant ids. Only select subscriptions that belong to one of the given tenant ids." />

    <@lib.parameter
        name = "withoutTenantId"
        location = "query"
        type = "boolean"
        desc = "Only select subscriptions which have no tenant id. Value may only be `true`, as `false` is the default behavior." />

    <@lib.parameter
        name = "includeEventSubscriptionsWithoutTenantId"
        location = "query"
        type = "boolean"
        desc = "Select event subscriptions which have no tenant id. Can be used in combination with tenantIdIn parameter. Value may only be `true`, as `false` is the default behavior." />

	<#assign last = false >
    <#assign sortByValues = ['"created"', '"tenantId"']>
    <#include "/lib/commons/sort-params.ftl" >
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl" >
   ],
  
  "responses" : {
    <@lib.response
        code = "200"
        dto = "EventSubscriptionDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                      "summary": "GET `/event-subscription?eventType=message&sortBy=created&sortOrder=desc`",
                      "value": [
                        {
 						"id":"anId",
 						"eventType":"message",
 						"eventName":"anEventName",
 						"executionId":"anExecutionId",
 						"processInstanceId":"aProcessInstanceId",
 						"activityId":"anActivityId",
 						"createdDate":"2020-04-20T15:23:12.229+0200",
 						"tenantId":null
 						},
 						{
 						"id":"anotherId",
 						"eventType":"message",
 						"eventName":"anotherEventName",
 						"executionId":"anotherExecutionId",
 						"processInstanceId":"anotherProcessInstanceId",
 						"activityId":"anotherActivityId",
 						"createdDate":"2020-04-20T15:20:12.229+0200",
 						"tenantId":null
 					    }
 					   ]
                     }'] />
   <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid,
                for example if a sortOrder parameter is supplied, but no sortBy."/>
  }
}
