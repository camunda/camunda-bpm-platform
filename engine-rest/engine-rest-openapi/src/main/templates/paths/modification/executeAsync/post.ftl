<#-- Generated From File: camunda-docs-manual/public/reference/rest/modification/post-modification-async/index.html -->
<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "executeModificationAsync"
      tag = "Modification"
      summary = "Execute Modification Async (Batch)"
      desc = "Executes a modification asynchronously for multiple process instances. To execute a
              modification synchronously, use the
              [Execute Modification](${docsUrl}/reference/rest/modification/post-modification-sync/) method.

              For more information about the difference between synchronous and
              asynchronous execution of a modification, please refer to the related
              section of the
              [user guide](${docsUrl}/user-guide/process-engine/process-instance-migration.md#executing-a-migration-plan)."
  />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ModificationDto"
      examples = ['"example-1": {
                     "summary": "POST `/modification/executeAsync`",
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
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200.",
                       "description": "POST `/modification/executeAsync`",
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