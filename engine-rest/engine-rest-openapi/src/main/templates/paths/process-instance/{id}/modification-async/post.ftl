<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "modifyProcessInstanceAsyncOperation"
      tag = "Process Instance"
      summary = "Modify Process Instance Execution State Async"
      desc = "Submits a list of modification instructions to change a process instance's execution state async.
              A modification instruction is one of the following:
      
              * Starting execution before an activity
              * Starting execution after an activity on its single outgoing sequence flow
              * Starting execution on a specific sequence flow
              * Cancelling an activity instance, transition instance, or all instances (activity or transition) for an activity
      
              Instructions are executed asynchronous and in the order they are provided in this request's body.
              Variables can be provided with every starting instruction.
      
              The exact semantics of modification can be read about in the
              [User guide](${docsUrl}/user-guide/process-engine/process-instance-modification/)." />

  "parameters" : [
    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process instance to modify."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ProcessInstanceModificationDto"
      examples = ['"example-1": {
                     "summary": "POST `/process-instance/aProcessInstanceId/modification-async`",
                     "value": {
                       "skipCustomListeners": true,
                       "skipIoMappings": true,
                       "instructions": [{
                         "type": "startBeforeActivity",
                         "activityId": "activityId"
                       }, {
                         "type": "cancel",
                         "activityInstanceId": "anActivityInstanceId"
                       }
                       ],
                       "annotation": "Modified to resolve an error."
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
                         "id": "aBatchId",
                         "type": "aBatchType",
                         "totalJobs": 10,
                         "jobsCreated": 10,
                         "batchJobsPerSeed": 100,
                         "invocationsPerBatchJob": 1,
                         "seedJobDefinitionId": "aSeedJobDefinitionId",
                         "monitorJobDefinitionId": "aMonitorJobDefinitionId",
                         "batchJobDefinitionId": "aBatchJobDefinitionId",
                         "tenantId": "aTenantId",
                         "suspended": false,
                         "createUserId": "demo"
                       }
                     }'] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Bad Request
        At least one modification instruction misses required parameters."/>

    <@lib.response
        code = "403"
        dto = "AuthorizationExceptionDto"
        desc = "Forbidden
                If the user is not allowed to execute batches. See the Introduction for the error response format."/>

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The modification cannot be performed, for example because it starts a failing activity."/>

   }
}
</#macro>