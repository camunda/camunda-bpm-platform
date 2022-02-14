<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "updateSuspensionStateAsyncOperation"
      tag = "Process Instance"
      summary = "Activate/Suspend In Batch"
      desc = "Activates or suspends process instances asynchronously with a list of process instance ids,
              a process instance query, and/or a historical process instance query." />


  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceSuspensionStateAsyncDto"
      examples = ['"example-1": {
                     "summary": "POST `/process-instance/suspended-async`",
                     "description": "Suspend Process Instance In Batch",
                     "value": {
                       "processInstanceIds" : [
                         "processInstanceId1",
                         "processInstanceIdN"
                       ],
                       "suspended" : true
                     }
                   }'
      ] />

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
                Returned if some of the request parameters are invalid,
                for example if the provided processDefinitionId or processDefinitionKey parameter is null."/>
  }
}
</#macro>