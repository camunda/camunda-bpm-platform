<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/process-definition/get-cleanable-process-instance-report-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getCleanableHistoricProcessInstanceReport"
      tag = "Historic Process Definition"
      summary = "Get Cleanable Process Instance Report"
      desc = "Retrieves a report about a process definition and finished process instances
              relevant to history cleanup (see
              [History cleanup](${docsUrl}/user-guide/process-engine/history/#history-cleanup)) 
              so that you can tune the history time to live.
              These reports include the count of the finished historic process
              instances, cleanable process instances and basic process definition
              data - id, key, name and version.
              The size of the result set can be retrieved by using the
              [Get Cleanable Process Instance Report Count](${docsUrl}/reference/rest/history/process-definition/get-cleanable-process-instance-report-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/history-process-definition-process-instance-report.ftl" >
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
        dto = "CleanableHistoricProcessInstanceReportResultDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/process-definition/cleanable-process-instance-report`",
                       "description": "GET `/history/process-definition/cleanable-process-instance-report`",
                       "value": [
                         {
                           "processDefinitionId": "invoice:1:7bf79f13-ef95-11e6-b6e6-34f39ab71d4e",
                           "processDefinitionKey": "invoice",
                           "processDefinitionName": "Invoice Receipt",
                           "processDefinitionVersion": 1,
                           "historyTimeToLive": 5,
                           "finishedProcessInstanceCount": 100,
                           "cleanableProcessInstanceCount": 53,
                           "tenantId": "aTenantId"
                         },
                         {
                           "processDefinitionId": "invoice:2:7bf79f13-ef95-11e6-b6e6-34f39ab71d4e",
                           "processDefinitionKey": "invoice",
                           "processDefinitionName": "Invoice Receipt v2.0",
                           "processDefinitionVersion": 2,
                           "historyTimeToLive": 5,
                           "finishedProcessInstanceCount": 1000,
                           "cleanableProcessInstanceCount": 13,
                           "tenantId": "aTenantId"
                         }
                       ]
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