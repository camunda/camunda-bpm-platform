<#-- Generated From File: camunda-docs-manual/public/reference/rest/migration/generate-migration/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "generateMigrationPlan"
      tag = "Migration"
      summary = "Generate Migration Plan"
      desc = "Generates a migration plan for two process definitions. The generated migration
              plan contains migration instructions which map equal activities
              between the
              two process definitions."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "MigrationPlanGenerationDto"
      examples = ['"example-1": {
                     "summary": "POST `/migration/generate`",
                     "value": {
                       "sourceProcessDefinitionId": "aProcessDefinitionId1",
                       "targetProcessDefinitionId": "aProcessDefinitionId2",
                       "updateEventTriggers": true,
                       "variables": {
                         "foo": {
                           "type": "Object",
                           "value": "[5,6]",
                           "valueInfo": {
                             "objectTypeName": "java.util.ArrayList",
                             "serializationDataFormat": "application/json"
                           }
                         }
                       }
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "200"
        dto = "MigrationPlanDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "POST `/migration/generate`",
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
                             ],
                             "updateEventTrigger": false
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
                             "value": "[5,6]",
                             "valueInfo": {
                               "objectTypeName": "java.util.ArrayList",
                               "serializationDataFormat": "application/json"
                             }
                           }
                         }
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
        desc = "
                The requested migration was invalid. See
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>