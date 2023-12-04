<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteAsyncHistoricQueryBased"
      tag = "Process Instance"
      summary = "Delete Async Historic Query Based (POST)"
      desc = "Deletes a set of process instances asynchronously (batch) based on a historic process instance query." />


  <@lib.requestBody
      mediaType = "application/json"
      dto = "DeleteProcessInstancesDto"
      requestDesc = "**Unallowed property**: `processInstanceQuery`"
      examples = [
                  '"example-1": {
                     "summary": "POST `/process-instance/delete-historic-query-based` Request Body 1",
                     "value": {
                       "deleteReason" : "aReason",
                       "historicProcessInstanceQuery": {
                         "startedBefore": "2017-04-28T11:24:37.765+0200"
                       },
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
                Returned if some of the query parameters are invalid, i.e., neither processInstanceIds, nor historicProcessInstanceQuery is present"/>

  }
}
</#macro>