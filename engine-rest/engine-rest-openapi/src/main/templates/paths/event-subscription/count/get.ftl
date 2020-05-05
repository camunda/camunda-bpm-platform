{

  <@lib.endpointInfo
      id = "getEventSubscriptionsCount"
      tag = "Event Subscription"
      desc = "Queries for the number of event subscriptions that fulfill given parameters. Takes the same parameters as the
              [Get Event Subscriptions](${docsUrl}/reference/rest/event-subscription/get-query/) method." />

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
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/event-subscription/count`",
                       "value": {
                         "count": 1
                       }
                     }'] />
                     
     <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />   
  }
}
