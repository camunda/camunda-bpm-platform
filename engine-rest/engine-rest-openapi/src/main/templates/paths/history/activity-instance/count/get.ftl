<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricActivityInstancesCount"
      tag = "Historic Activity Instance"
      summary = "Get List Count"
      desc = "Queries for the number of historic activity instances that fulfill the given parameters.
              Takes the same parameters as the [Get Historic Activity Instance](${docsUrl}/reference/rest/history/activity-instance/get-activity-instance-query/)  method." />

  "parameters": [
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/history-activity-instance.ftl" >

    <@lib.parameters
        object = params
        last = true/>
  ],
  "responses": {
    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for GET `/history/activity-instance/count?activityType=userTask`",
                       "value": {
                         "count": 1
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>
  }
}
</#macro>