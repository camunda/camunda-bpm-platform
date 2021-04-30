<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-instance/post-delete/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "deleteAsync"
      tag = "Historic Decision Instance"
      summary = "Delete Async (POST)"
      desc = "Delete multiple historic decision instances asynchronously (batch).
              At least `historicDecisionInstanceIds` or `historicDecisionInstanceQuery` 
              has to be provided. If both are provided then all instances matching query 
              criterion and instances from the list will be deleted."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "DeleteHistoricDecisionInstancesDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/decision-instance/delete`",
                     "description": "POST `/history/decision-instance/delete`",
                     "value": {
                       "historicDecisionInstanceIds": [
                         "aDecision",
                         "secondDecision"
                       ],
                       "historicDecisionInstanceQuery": {
                         "decisionDefinitionKey": "a-definition-key"
                       },
                       "deleteReason": "a delete reason"
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/decision-instance/delete`",
                       "description": "POST `/history/decision-instance/delete`",
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
                         "suspened": false,
                         "tenantId": null,
                         "createUserId": "aUser"
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if some of the query parameters are invalid, i.e. neither
                `historicDecisionInstanceIds` nor `historicDecisionInstanceQuery` is
                present. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) 
                for the error response format."
        last = true
    />

  }

}
</#macro>
