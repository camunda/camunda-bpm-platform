<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getEventSubscriptions"
      tag = "Event Subscription"
      summary = "Get List"
      desc = "Queries for event subscriptions that fulfill given parameters.
              The size of the result set can be retrieved by using the
              [Get Event Subscriptions count](${docsUrl}/reference/rest/event-subscription/get-query-count/) method." />

 "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/event-subscription-query-params.ftl" >

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
                for example if a `sortOrder` parameter is supplied, but no `sortBy`.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."/>
  }
}

</#macro>