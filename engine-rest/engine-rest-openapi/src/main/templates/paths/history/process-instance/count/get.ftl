<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricProcessInstancesCount"
      tag = "Historic Process Instance"
      summary = "Get List Count"
      desc = "Queries for the number of historic process instances that fulfill the given parameters.
              Takes the same parameters as the [Get Process Instances](${docsUrl}/reference/rest/history/process-instance/get-process-instance-query/) method." />

  "parameters": [
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/history-process-instance.ftl" >
    <@lib.parameters
        object = params
        skip = ["orQueries"]  <#-- OR Queries not avaialble in GET -->
        last = true/>
  ],
  "responses": {
    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for GET `/history/process-instance/count?variables=myVariable_eq_camunda`",
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