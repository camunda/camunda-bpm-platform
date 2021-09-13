<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "correlateMessageAsyncOperation"
      tag = "Process Instance"
      summary = "Correlate Message Async (POST)"
      desc = "Correlates a message asynchronously to executions that are waiting for this message. 
              Messages will not be correlated to process definition-level start message events to start process instances." />


  <@lib.requestBody
      mediaType = "application/json"
      dto = "CorrelationMessageAsyncDto"
      examples = ['"example-1": {
                     "summary": "POST /process-instance/message-async",
                     "description": "Correlate a message to process instances in a batch",
                     "value": {
                                "messageName" : "a-message-name",
                                "processInstanceIds" : [
                                  "b4d2ad98-7240-11e9-98b7-be5e0f7575b7"
                                ],
                                "processInstanceQuery": {
                                  "processDefinitionKey": "my-process-definition-key"
                                },
                                "variables" : {
                                  "myVariableName": {
                                    "value": "myStringValue"
                                  }
                                }
                              }
                   }'
      ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "BatchDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "Status 200 Response",
                       "value": {
                                  "id": "120b568d-724a-11e9-98b7-be5e0f7575b7",
                                  "type": "correlate-message",
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
        desc = "Bad Request
                * If none of `processInstanceIds`, `processInstanceQuery`, and `historicProcessInstanceQuery` is given
                * If no process instance ids where found" />

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        last = true
        desc = "Returned if the user is not allowed to create the batch.

                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the
                error response format."/>
  }
}
</#macro>