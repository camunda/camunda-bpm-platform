<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/get-job-log-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricJobLogs"
      tag = "Historic Job Log"
      summary = "Get Job Logs"
      desc = "Queries for historic job logs that fulfill the given parameters.
              The size of the result set can be retrieved by using the
              [Get Job Log Count](${docsUrl}/reference/rest/history/job-log/get-job-log-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/history-job-log-params.ftl">
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
        dto = "HistoricJobLogDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET `/history/job-log?jobId=aJobId`",
                       "description": "GET `/history/job-log?jobId=aJobId`",
                       "value": [
                         {
                           "id": "someId",
                           "timestamp": "2015-01-15T15:22:20.000+0200",
                           "removalTime": "2018-02-10T14:33:19.000+0200",
                           "jobId": "aJobId",
                           "jobDefinitionId": "aJobDefinitionId",
                           "activityId": "serviceTask",
                           "jobType": "message",
                           "jobHandlerType": "async-continuation",
                           "jobDueDate": null,
                           "jobRetries": 3,
                           "jobPriority": 15,
                           "jobExceptionMessage": null,
                           "failedActivityId": null,
                           "executionId": "anExecutionId",
                           "processInstanceId": "aProcessInstanceId",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processDefinitionKey": "aProcessDefinitionKey",
                           "deploymentId": "aDeploymentId",
                           "rootProcessInstanceId": "aRootProcessInstanceId",
                           "tenantId": null,
                           "hostname": "aHostname",
                           "batchId": "aBatchId",
                           "creationLog": true,
                           "failureLog": false,
                           "successLog": false,
                           "deletionLog": false
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, for example if a `sortOrder`
                parameter is supplied, but no `sortBy`. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>d