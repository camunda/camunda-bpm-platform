<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setRetriesByProcessHistoricQueryBased"
      tag = "Process Instance"
      summary = "Set Job Retries Async Historic Query Based (POST)"
      desc = "Create a batch to set retries of jobs asynchronously based on a historic process instance query." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetJobRetriesByProcessDto"
      requestDesc = "Please note that if both processInstances and historicProcessInstanceQuery are provided,
                     then the resulting execution will be performed on the union of these sets.
                     **Unallowed property**: `processInstanceQuery`"
      examples = ['"example-1": {
                     "summary": "POST `/process-instance/job-retries-historic-query-based` Request Body 1",
                     "value": {
                       "retries": 5,
                       "dueDate": "2017-04-06T13:57:45.000+0200",
                       "historicProcessInstanceQuery": {
                         "startedBefore": "2017-04-28T11:24:37.769+0200"
                       },
                       "processInstances": ["aProcess","secondProcess"]
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
        last = true
        desc = "Bad Request
                Returned if some of the query parameters are invalid, for example if neither processInstanceIds, nor historicProcessInstanceQuery is present.
                Or if the retry count is not specified."/>

  }
}
</#macro>