<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setRetriesByProcess"
      tag = "Process Instance"
      summary = "Set Job Retries Async (POST)"
      desc = "Create a batch to set retries of jobs associated with given processes asynchronously." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetJobRetriesByProcessDto"
      requestDesc = "Please note that if both processInstances and processInstanceQuery are provided,
                     then the resulting execution will be performed on the union of these sets.
                     **Unallowed property**: `historicProcessInstanceQuery`"
      examples = ['"example-1": {
                     "summary": "POST `/process-instance/job-retries` Request Body 1",
                     "value": {
                         "retries": 5,
                         "dueDate": "2017-04-06T13:57:45.000+0200",
                         "processInstances": ["aProcess", "secondProcess"],
                         "processInstanceQuery": {
                           "processDefinitionId": "aProcessDefinitionId"
                         }
                       }
                    }'] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "value": {
                         "id": "aBatchId",
                         "type": "aBatchType",
                         "totalJobs": 10,
                         "batchJobsPerSeed": 100,
                         "jobsCreated": 10,
                         "invocationsPerBatchJob": 1,
                         "seedJobDefinitionId": "aSeedJobDefinitionId",
                         "monitorJobDefinitionId": "aMonitorJobDefinitionId",
                         "batchJobDefinitionId": "aBatchJobDefinitionId",
                         "suspended": false,
                         "tenantId": "aTenantId",
                         "createUserId": "demo"
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, for example if neither processInstanceIds, nor processInstanceQuery is present.
                Or if the retry count is not specified."/>

  }
}
</#macro>