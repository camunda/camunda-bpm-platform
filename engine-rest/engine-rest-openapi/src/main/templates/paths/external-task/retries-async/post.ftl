<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "setExternalTaskRetriesAsyncOperation"
      tag = "External Task"
      summary = "Set Retries Async"
      desc = "Sets the number of retries left to execute external tasks by id asynchronously. If retries are set to 0,
              an incident is created." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetRetriesForExternalTasksDto"
      examples = ['"example-1": {
                       "summary": "POST /external-task/retries-async",
                       "value": {
                         "retries": 123,
                         "externalTaskIds": [
                           "anExternalTask",
                           "anotherExternalTask"
                         ]
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "value": {
                         "id": "aBatchId",
                         "type": "aBatchType",
                         "totalJobs": 10,
                         "jobsCreated": 10,
                         "batchJobsPerSeed": 100,
                         "invocationsPerBatchJob": 1,
                         "seedJobDefinitionId": "aSeedJobDefinitionId",
                         "monitorJobDefinitionId": "aMonitorJobDefinitionId",
                         "batchJobDefinitionId": "aBatchJobDefinitionId",
                         "tenantId": "aTenantId",
                         "suspended": false,
                         "createUserId": "demo"
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "If neither externalTaskIds nor externalTaskQuery are present or externalTaskIds contains null value or 
                the number of retries is negative or null, an exception of type `InvalidRequestException` is returned.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the task does not exist. This could indicate a wrong task id as well as a cancelled task, 
                e.g., due to a caught BPMN boundary event. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>