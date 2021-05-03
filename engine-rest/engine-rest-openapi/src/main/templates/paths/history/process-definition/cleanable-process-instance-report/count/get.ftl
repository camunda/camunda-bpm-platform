<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/process-definition/get-cleanable-process-instance-report-query-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getCleanableHistoricProcessInstanceReportCount"
      tag = "Historic Process Definition"
      summary = "Get Cleanable Process Instance Report Count"
      desc = "Queries for the number of report results about a process definition and finished
              process instances relevant to history cleanup (see
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)).
              Takes the same parameters as the
              [Get Cleanable Process Instance Report](${docsUrl}/reference/rest/history/process-definition/get-cleanable-process-instance-report/)
              method."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/history-process-definition-process-instance-report.ftl" >
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
                       "summary": "GET `/history/process-definition/cleanable-process-instance-report/count`",
                       "description": "GET `/history/process-definition/cleanable-process-instance-report/count`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>