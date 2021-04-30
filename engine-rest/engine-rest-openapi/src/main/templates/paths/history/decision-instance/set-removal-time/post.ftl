<#-- Generated From File: camunda-docs-manual/public/reference/rest/history/decision-instance/post-decision-instance-set-removal-time/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setRemovalTimeAsyncHistoricDecisionInstance"
      tag = "Historic Decision Instance"
      summary = "Set Removal Time Async (POST)"
      desc = "Sets the removal time to multiple historic decision instances asynchronously
              (batch).

              At least `historicDecisionInstanceIds` or
              `historicDecisionInstanceQuery` has to be provided. If both are
              provided, all instances matching query criterion and instances from the list
              will be updated with a removal time."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetRemovalTimeToHistoricDecisionInstancesDto"
      examples = ['"example-1": {
                     "summary": "POST `/history/decision-instance/set-removal-time`",
                     "description": "POST `/history/decision-instance/set-removal-time`",
                     "value": {
                       "absoluteRemovalTime": "2019-05-05T11:56:24.725+0200",
                       "hierarchical": true,
                       "historicDecisionInstanceQuery": {
                         "evaluatedBefore": "2019-09-05T17:02:10.123+0200"
                       },
                       "historicDecisionInstanceIds": [
                         "b4d2ad98-7240-11e9-98b7-be5e0f7575b7",
                         "b4d2ad94-7240-11e9-98b7-be5e0f7575b7"
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST `/history/decision-instance/set-removal-time`",
                       "description": "POST `/history/decision-instance/set-removal-time`",
                       "value": {
                         "id": "120b568d-724a-11e9-98b7-be5e0f7575b7",
                         "type": "decision-set-removal-time",
                         "totalJobs": 12,
                         "batchJobsPerSeed": 100,
                         "jobsCreated": 10,
                         "invocationsPerBatchJob": 1,
                         "seedJobDefinitionId": "120b5690-724a-11e9-98b7-be5e0f7575b7",
                         "monitorJobDefinitionId": "120b568f-724a-11e9-98b7-be5e0f7575b7",
                         "batchJobDefinitionId": "120b568e-724a-11e9-98b7-be5e0f7575b7",
                         "suspended": false,
                         "tenantId": "accounting",
                         "createUserId": "demo-user"
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Request was unsuccessfull due to a bad user request. This occurs if
                some of the query parameters are invalid, e. g. if neither
                historicDecisionInstances nor historicDecisionInstanceQuery is
                present or if no mode is specified.

                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>
