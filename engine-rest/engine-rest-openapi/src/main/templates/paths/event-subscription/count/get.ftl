<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getEventSubscriptionsCount"
      tag = "Event Subscription"
      summary = "Get List Count"
      desc = "Queries for the number of event subscriptions that fulfill given parameters.
              Takes the same parameters as the
              [Get Event Subscriptions](${docsUrl}/reference/rest/event-subscription/get-query/) method." />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/event-subscription-query-params.ftl" >

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
        desc = "Returned if some of the query parameters are invalid.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
  }
}

</#macro>