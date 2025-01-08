<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/job-log/get-job-log/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricJobLog"
      tag = "Historic Job Log"
      summary = "Get Job Log"
      desc = "Retrieves a historic job log by id."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the log entry."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricJobLogDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/history/job-log/someId`",
                       "value": {
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
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Historic job log with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>