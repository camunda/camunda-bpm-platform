<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteHistoricProcessInstancesAsync"
      tag = "Historic Process Instance"
      summary = "Delete Async (POST)"
      desc = "Delete multiple historic process instances asynchronously (batch).
              At least `historicProcessInstanceIds` or `historicProcessInstanceQuery` has to be provided.
              If both are provided then all instances matching query criterion and instances from the list will be deleted." />


  <@lib.requestBody
      mediaType = "application/json"
      dto = "DeleteHistoricProcessInstancesDto"
      examples = [
                  '"example-1": {
                     "summary": "POST `/history/process-instance/delete`",
                     "value": {
                                "deleteReason" : "aReason",
                                "historicProcessInstanceIds": ["aProcess","secondProcess"],
                                "historicProcessInstanceQuery": {
                                  "startedAfter": "2016-10-11T11:44:13.000+0200",
                                  "finishedBefore": "2016-10-13T11:44:17.000+0200"
                                }
                              }
                   }'
                ] />
  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 response",
                       "description": "Response for POST `/history/process-instance/delete`",
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
        desc = "Returned if some of the query parameters are invalid, i.e. neither historicProcessInstanceIds,
                nor historicProcessInstanceQuery is present. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
</#macro>