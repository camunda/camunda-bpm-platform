<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-definition/get-cleanable-decision-instance-report-count/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getCleanableHistoricDecisionInstanceReportCount"
      tag = "Historic Decision Definition"
      summary = "Get Cleanable Decision Instance Report Count"
      desc = "Queries for the number of report results about a decision definition and finished
              decision instances relevant to history cleanup (see
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)).
              Takes the same parameters as the [Get Cleanable Decision Instance Report](${docsUrl}/reference/rest/history/decision-definition/get-cleanable-decision-instance-report/) 
              method."
  />

  "parameters" : [
  
    <#assign last = true >
    <#include "/lib/commons/history-cleanable-decision-definition-query-params.ftl" >
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
                       "summary": "request",
                       "description": "GET `/history/decision-definition/cleanable-decision-instance-report/count`",
                       "value": {
                         "count": 1
                       }
                     }']
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the
                error response format."
        last = true
    />

  }

}
</#macro>
