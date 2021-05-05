<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/external-task-log/get-external-task-log-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricExternalTaskLogsCount"
      tag = "Historic External Task Log"
      summary = "Get External Task Log Count"
      desc = "Queries for the number of historic external task logs that fulfill the given
              parameters.
              Takes the same parameters as the
              [Get External Task Logs](${docsUrl}/reference/rest/history/external-task-log/get-external-task-log-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/historic-external-task-log-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CountResultDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/external-task-log/count?externalTaskId=anExternalTaskId`",
                       "description": "GET `/history/external-task-log/count?externalTaskId=anExternalTaskId`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid."
        last = true
    />

  }

}
</#macro>