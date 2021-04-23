<#-- Generated From File: camunda-docs-manual/public/reference/rest/modification/post-modification-sync/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "executeModification"
      tag = "Modification"
      summary = "Execute Modification"
      desc = "Executes a modification synchronously for multiple process instances.
              To modify a single process instance, use the
              [Modify Process Instance Execution State](${docsUrl}/reference/rest/process-instance/post-modification/) method.
              To execute a modification asynchronously, use the
              [Execute Modification Async (Batch)](${docsUrl}/reference/rest/modification/post-modification-async/) method.

              For more information about the difference between synchronous and
              asynchronous execution of a modification, please refer to the related
              section of the
              [user guide](${docsUrl}/user-guide/process-engine/process-instance-migration.md#executing-a-migration-plan)."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ModificationDto"
      examples = ['"example-1": {
                     "summary": "POST `/modification/execute`",
                     "value": {
                       "processDefinitionId": "aProcessDefinitionId",
                       "instructions": [
                         {
                           "type": "startAfterActivity",
                           "activityId": "aUserTask"
                         },
                         {
                           "type": "cancel",
                           "activityId": "anotherTask",
                           "cancelCurrentActiveActivityInstances": true
                         }
                       ],
                       "processInstanceIds": [
                         "aProcessInstance",
                         "anotherProcessInstance"
                       ],
                       "processInstanceQuery": {
                         "processDefinitionId": "aProcessDefinitionId"
                       },
                       "skipCustomListeners": true,
                       "annotation": "Modified to resolve an error."
                     }
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "
                In case following parameters are missing: instructions,
                processDefinitionId, activityId or transitionId, processInstanceIds
                or processInstanceQuery, an exception of type
                `InvalidRequestException` is returned. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format.
                "
        last = true
    />

  }

}
</#macro>