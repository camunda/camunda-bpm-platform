<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/get-query/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricBatches"
      tag = "Historic Batch"
      summary = "Get Historic Batches"
      desc = "Queries for historic batches that fulfill given parameters. Parameters may be
              the properties of batches, such as the id or type. The
              size of the result set can be retrieved by using the
              [Get Historic Batch Count](${docsUrl}/reference/rest/history/batch/get-query-count/)
              method."
  />

  "parameters" : [

    <#assign last = false >
    <#include "/lib/commons/historic-batch-params.ftl" >
    <@lib.parameters
        object = params
        last = last
    />
    <#include "/lib/commons/sort-params.ftl">
    <#assign last = true >
    <#include "/lib/commons/pagination-params.ftl">

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricBatchDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/history/batch?type=aBatchType&completed=true&sortBy=batchId&sortOrder=asc`",
                       "value": [
                         {
                           "id": "aBatchId",
                           "type": "aBatchType",
                           "totalJobs": 10,
                           "batchJobsPerSeed": 100,
                           "invocationsPerBatchJob": 1,
                           "seedJobDefinitionId": "aSeedJobDefinitionId",
                           "monitorJobDefinitionId": "aMonitorJobDefinitionId",
                           "batchJobDefinitionId": "aBatchJobDefinitionId",
                           "tenantId": "aTenantId",
                           "createUserId": "aUserId",
                           "startTime": "2016-04-12T15:29:33.000+0200",
                           "endTime": "2016-04-12T16:23:34.000+0200",
                           "removalTime": "2016-04-15T16:23:34.000+0200"
                         }
                       ]
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "
                Returned if some of the query parameters are invalid, for example if
                a `sortOrder` parameter is supplied, but no `sortBy`.
                      See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>