<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getBatch"
      tag = "Batch"
      summary = "Get"
      desc = "Retrieves a batch by id, according to the Batch interface in the engine." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the batch to be retrieved." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "Response for GET `/batch/aBatchId`",
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
                         "createUserId": "aUserId"
                       }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Batch with given id does not exist.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}
</#macro>