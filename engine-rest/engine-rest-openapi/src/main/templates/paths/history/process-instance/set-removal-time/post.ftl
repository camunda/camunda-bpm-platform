<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setRemovalTimeAsync"
      tag = "Historic Process Instance"
      summary = "Set Removal Time Async (POST)"
      desc = "Sets the removal time to multiple historic process instances asynchronously (batch).

              At least `historicProcessInstanceIds` or `historicProcessInstanceQuery` has to be provided.
              If both are provided, all instances matching query criterion and instances from the list will be updated with a removal time." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetRemovalTimeToHistoricProcessInstancesDto"
      examples = [
                  '"example-1": {
                     "summary": "POST `/history/process-instance/set-removal-time`",
                     "value": {
                                "absoluteRemovalTime": "2019-05-05T11:56:24.725+0200",
                                "hierarchical": true,
                                "historicProcessInstanceQuery": {
                                  "unfinished": true
                                },
                                "historicProcessInstanceIds": [
                                  "b4d2ad98-7240-11e9-98b7-be5e0f7575b7",
                                  "b4d2ad94-7240-11e9-98b7-be5e0f7575b7"
                                ]
                              }
                   }',
                   '"example-2": {
                     "summary": "POST `/history/process-instance/set-removal-time`",
                     "value": {
                                "absoluteRemovalTime": "2019-05-05T11:56:24.725+0200",
                                "hierarchical": true,
                                "updateInChunks": true,
                                "updateChunkSize": 300,
                                "historicProcessInstanceQuery": {
                                  "unfinished": true
                                },
                                "historicProcessInstanceIds": [
                                  "b4d2ad98-7240-11e9-98b7-be5e0f7575b7",
                                  "b4d2ad94-7240-11e9-98b7-be5e0f7575b7"
                                ]
                              }
                   }'
                ] />
  "responses" : {
    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for POST `/history/process-instance/set-removal-time`",
                       "value": {
                                  "id": "120b568d-724a-11e9-98b7-be5e0f7575b7",
                                  "type": "process-set-removal-time",
                                  "totalJobs": 12,
                                  "batchJobsPerSeed": 100,
                                  "invocationsPerBatchJob": 1,
                                  "seedJobDefinitionId": "120b5690-724a-11e9-98b7-be5e0f7575b7",
                                  "monitorJobDefinitionId": "120b568f-724a-11e9-98b7-be5e0f7575b7",
                                  "batchJobDefinitionId": "120b568e-724a-11e9-98b7-be5e0f7575b7",
                                  "tenantId": "accounting",
                                  "suspended": false,
                                  "createUserId": null
                                }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                Request was unsuccessfull due to a bad user request. This occurs if some of the query parameters are invalid,
                e. g. if neither `historicProcessInstances` nor `historicProcessInstanceQuery` is present or if no mode is specified.

                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."/>
  }
}
</#macro>