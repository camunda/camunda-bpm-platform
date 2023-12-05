<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteProcessInstancesAsyncOperation"
      tag = "Process Instance"
      summary = "Delete Async (POST)"
      desc = "Deletes multiple process instances asynchronously (batch)." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "DeleteProcessInstancesDto"
      requestDesc = "**Unallowed property**: `historicProcessInstanceQuery`"
      examples = [
                  '"example-1": {
                     "summary": "POST `/process-instance/delete` Request Body 1",
                     "value": {
                       "deleteReason" : "aReason",
                       "processInstanceIds": ["aProcess","secondProcess"],
                       "skipCustomListeners" : true,
                       "skipSubprocesses" : true,
                       "skipIoMappings" : false
                     }
                   }'
                ] />

  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response 1",
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
                Returned if some of the query parameters are invalid, i.e., neither processInstanceIds, nor processInstanceQuery is present"/>

  }
}
</#macro>