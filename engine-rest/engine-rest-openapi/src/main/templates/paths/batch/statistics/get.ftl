<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getBatchStatistics"
      tag = "Batch"
      summary = "Get Statistics"
      desc = "Queries for batch statistics that fulfill given parameters.
              Parameters may be the properties of batches, such as the id or type.
              The size of the result set can be retrieved by using the 
              [Get Batch Statistics Count](${docsUrl}/reference/rest/batch/get-statistics-query-count/) method." />

  "parameters" : [

    <#assign requestMethod="GET"/>
    <#include "/lib/commons/batch.ftl" >

    <#assign last = false >
    <#include "/lib/commons/sort-params.ftl" >

    <#include "/lib/commons/pagination-params.ftl" >

    <@lib.parameters
        object = params
        last = true />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchStatisticsDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "description": "Response for GET `/batch/statistics?type=aBatchType&sortBy=batchId&sortOrder=asc`",
                       "value": [{
                         "id": "aBatchId",
                         "type": "aBatchType",
                         "totalJobs": 10,
                         "batchJobsPerSeed": 100,
                         "jobsCreated": 10,
                         "invocationsPerBatchJob": 1,
                         "seedJobDefinitionId": "aSeedJobDefinitionId",
                         "monitorJobDefinitionId": "aMonitorJobDefinitionId",
                         "batchJobDefinitionId": "aBatchJobDefinitionId",
                         "remainingJobs": 3,
                         "completedJobs": 7,
                         "failedJobs": 1,
                         "suspended": false,
                         "tenantId": "aTenantId",
                         "createUserId": "aUserId"
                       }]
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if some of the query parameters are invalid,
                for example if a `sortOrder` parameter is supplied, but no `sortBy`.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>

  }
}
</#macro>