<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/execute-migration-async/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "executeMigrationPlanAsync"
      tag = "Migration"
      summary = "Execute Migration Plan Async (Batch)"
      desc = "Executes a migration plan asynchronously (batch) for multiple process instances.
              To execute a migration plan synchronously, use the
              [Execute MigrationPlan](${docsUrl}/reference/rest/migration/execute-migration/)
              method.

              For more information about the difference between synchronous and asynchronous
              execution of a migration plan, please refer to the related section of
              [the user guide](${docsUrl}/user-guide/process-engine/process-instance-migration/#executing-a-migration-plan)."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "MigrationExecutionDto"
      examples = ['"example-1": {
                     "summary": "POST `/migration/executeAsync`",
                     "value": {
                       "migrationPlan": {
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
                         ],
                         "variables": {
                           "foo": {
                             "type": "Object",
                             "value": "[5,9]",
                             "valueInfo": {
                               "objectTypeName": "java.util.ArrayList",
                               "serializationDataFormat": "application/json"
                             }
                           }
                         }
                       },
                       "processInstanceIds": [
                         "aProcessInstance",
                         "anotherProcessInstance"
                       ],
                       "processInstanceQuery": {
                         "processDefinitionId": "aProcessDefinitionId1"
                       },
                       "skipCustomListeners": true
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "POST `/migration/executeAsync`",
                       "value": {
                         "id": "aBatchId",
                         "type": "aBatchType",
                         "totalJobs": 10,
                         "batchJobsPerSeed": 100,
                         "invocationsPerBatchJob": 1,
                         "seedJobDefinitionId": "aSeedJobDefinitionId",
                         "monitorJobDefinitionId": "aMonitorJobDefinitionId",
                         "batchJobDefinitionId": "aBatchJobDefinitionId",
                         "tenantId": "aTenantId"
                       }
                     }']
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Invalid variable value, for example if the value could not be parsed to an Integer value or the passed variable type is not supported.
                See the  [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The request is not valid if one or more of the following statements apply:

                * The provided migration plan is not valid, so an exception of type
                `MigrationPlanValidationException` is returned.
                * In case additional parameters of the request are unexpected, an
                exception of type `InvalidRequestException` is returned.

                See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>