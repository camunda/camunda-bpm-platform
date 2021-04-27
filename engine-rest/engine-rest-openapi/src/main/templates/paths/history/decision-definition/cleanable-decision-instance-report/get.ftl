<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-definition/get-cleanable-decision-instance-report/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getCleanableHistoricDecisionInstanceReport"
      tag = "Historic Decision Definition"
      summary = "Get Cleanable Decision Instance Report"
      desc = "Retrieves a report about a decision definition and finished decision instances
              relevant to history cleanup (see
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)), 
              so that you can tune the history time to live.
              These reports include the count of the finished historic decision
              instances, cleanable decision instances and basic decision definition
              data - id, key, name and version.
              The size of the result set can be retrieved by using the 
              [Get Cleanable Decision Instance Report Count](${docsUrl}/reference/rest/history/decision-definition/get-cleanable-decision-instance-report-count/) method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/history-cleanable-decision-definition-query-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "CleanableHistoricDecisionInstanceReportResultDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "request",
                       "description": "GET `/history/decision-definition/cleanable-decision-instance-report`",
                       "value": [
                         {
                           "decisionDefinitionId": "invoice:1:7bf79f13-ef95-11e6-b6e6-34f39ab71d4e",
                           "decisionDefinitionKey": "invoice",
                           "decisionDefinitionName": "Invoice Receipt",
                           "decisionDefinitionVersion": 1,
                           "historyTimeToLive": 5,
                           "finishedDecisionInstanceCount": 100,
                           "cleanableDecisionInstanceCount": 53,
                           "tenantId": "aTenantId"
                         },
                         {
                           "decisionDefinitionId": "invoice:2:7bf79f13-ef95-11e6-b6e6-34f39ab71d4e",
                           "decisionDefinitionKey": "invoice",
                           "decisionDefinitionName": "Invoice Receipt v2.0",
                           "decisionDefinitionVersion": 2,
                           "historyTimeToLive": 5,
                           "finishedDecisionInstanceCount": 1000,
                           "cleanableDecisionInstanceCount": 13,
                           "tenantId": "aTenantId"
                         }
                       ]
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
