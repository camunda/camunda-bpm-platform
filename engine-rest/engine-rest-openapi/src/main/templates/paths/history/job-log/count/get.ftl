<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/get-job-log-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricJobLogsCount"
      tag = "Historic Job Log"
      summary = "Get Job Log Count"
      desc = "Queries for the number of historic job logs that fulfill the given parameters.
              Takes the same parameters as the
              [Get Job Logs](${docsUrl}/reference/rest/history/job-log/get-job-log-query/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/history-job-log-params.ftl">
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
                       "summary": "GET `/history/job-log/count?jobId=aJobId`",
                       "description": "GET `/history/job-log/count?jobId=aJobId`",
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