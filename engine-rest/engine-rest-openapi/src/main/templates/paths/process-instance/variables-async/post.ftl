<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "setVariablesAsyncOperation"
      tag = "Process Instance"
      summary = "Set Variables Async (POST)"
      desc = "Update or create runtime process variables in the root scope of process instances." />


  <@lib.requestBody
      mediaType = "application/json"
      dto = "SetVariablesAsyncDto"
      examples = ['"example-1": {
                     "summary": "POST /process-instance/variables-async",
                     "description": "Set variables to process instances in a batch",
                     "value": {
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
                                  "type": "set-variables",
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
                * The variable value or type is invalid, for example if the value could not be parsed to an Integer value or
                  the passed variable type is not supported
                * If none of `processInstanceIds`, `processInstanceQuery` and `historicProcessInstanceQuery` is given
                * If no or an empty array of `variables` is given
                * If no process instance ids where found
                * If a transient variable is set
                * If the engine config flag `javaSerializationFormatEnabled` is `false` and a Java serialized variable is given"/>

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