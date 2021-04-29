<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/validate-migration-plan/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "validateMigrationPlan"
      tag = "Migration"
      summary = "Validate Migration Plan"
      desc = "Validates a migration plan statically without executing it. This
              corresponds to the
              [creation time validation](${docsUrl}/user-guide/process-engine/process-instance-migration/#creation-time-validation)
              described in the user guide."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "MigrationPlanDto"
      examples = ['"example-1": {
                     "summary": "POST `/migration/validate`",
                     "value": {
                       "sourceProcessDefinitionId": "aProcessDefinitionId1",
                       "targetProcessDefinitionId": "aProcessDefinitionId2",
                       "instructions": [
                         {
                           "sourceActivityIds": [
                             "aUserTask"
                           ],
                           "targetActivityIds": [
                             "aUserTask"
                           ]
                         },
                         {
                           "sourceActivityIds": [
                             "anEvent"
                           ],
                           "targetActivityIds": [
                             "anotherEvent"
                           ],
                           "updateEventTrigger": true
                         }
                       ]
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "MigrationPlanReportDto"
        desc = "Request successful. The validation report was returned."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "POST `/migration/validate`",
                       "value": {
                         "instructionReports": [
                           {
                             "instruction": {
                               "sourceActivityIds": [
                                 "aUserTask"
                               ],
                               "targetActivityIds": [
                                 "aUserTask"
                               ],
                               "updateEventTrigger": false
                             },
                             "failures": [
                               "failure1",
                               "failure2"
                             ]
                           },
                           {
                             "instruction": {
                               "sourceActivityIds": [
                                 "anEvent"
                               ],
                               "targetActivityIds": [
                                 "anotherEvent"
                               ],
                               "updateEventTrigger": true
                             },
                             "failures": [
                               "failure1",
                               "failure2"
                             ]
                           }
                         ]
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "In case additional parameters of the request are unexpected, an
                exception of type `InvalidRequestException` is returned. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>
