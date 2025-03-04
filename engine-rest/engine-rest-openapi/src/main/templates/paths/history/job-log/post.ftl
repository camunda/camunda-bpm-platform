<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/post-job-log-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "queryHistoricJobLogs"
      tag = "Historic Job Log"
      summary = "Get Job Logs (POST)"
      desc = "Queries for historic job logs that fulfill the given parameters.
              This method is slightly more powerful than the
              [Get Job Logs](${docsUrl}/reference/rest/history/job-log/get-job-log-query/)
              method because it allows filtering by historic job logs values of the
              different types `String`, `Number` or `Boolean`."
  />

  "parameters" : [

    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "HistoricJobLogQueryDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/job-log`",
                     "value": {
                       "jobId": "aJobId"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricJobLogDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/job-log`",
                       "description": "POST `/history/job-log`",
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
</#macro>