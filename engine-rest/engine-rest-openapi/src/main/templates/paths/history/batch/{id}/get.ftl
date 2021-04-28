<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/batch/get/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getHistoricBatch"
      tag = "Historic Batch"
      summary = "Get Historic Batch"
      desc = "Retrieves a historic batch by id, according to the `HistoricBatch` interface in the
              engine."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the historic batch to be retrieved."
          last = true
      />

  ],

  "responses": {

    <@lib.response
        code = "200"
        dto = "HistoricBatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "GET `/history/batch/aBatchId`",
                       "value": {
                         "id": "aBatchId",
                         "type": "aBatchType",
                         "size": 10,
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
                     }']
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "
                Historic batch with given id does not exist.
                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>