<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/task/get-task-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricTaskInstancesCount"
      tag = "Historic Task Instance"
      summary = "Get Task Count"
      desc = "Queries for the number of historic tasks that fulfill the given parameters.
              Takes the same parameters as the
              [Get Tasks (Historic)](${docsUrl}/reference/rest/history/task/get-task-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#assign requestMethod="GET"/>
    <#include "/lib/commons/history-task-instance-query-params.ftl" >
    <@lib.parameters
        object = params
        skip = ["orQueries"]  <#-- OR Queries not avaialble in GET -->
        last = last
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/task/count?taskAssginee=anAssignee&taskPriority=50`",
                       "description": "GET `/history/task/count?taskAssginee=anAssignee&taskPriority=50`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>