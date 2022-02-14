<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "restartProcessInstanceAsyncOperation"
      tag = "Process Definition"
      summary = "Restart Process Instance Async"
      desc = "Restarts process instances that were canceled or terminated asynchronously.
              Can also restart completed process instances.
              It will create a new instance using the original instance information.
              To execute the restart asynchronously, use the
              [Restart Process Instance](${docsUrl}/reference/rest/process-definition/post-restart-process-instance-sync/) method.

              For more information about the difference between synchronous and asynchronous execution,
              please refer to the related section of the
              [User Guide](${docsUrl}/user-guide/process-engine/process-instance-restart/#execution)." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the process definition of the process instances to restart."/>
  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "RestartProcessInstanceDto"
      examples = ['"example-1": {
                     "summary": "Restarting one or more Process Instances with known processInstanceIds",
                     "value": {
                       "instructions": [
                         {
                           "type": "startAfterActivity",
                           "activityId": "aUserTask"
                         }
                       ],
                       "processInstanceIds": [
                         "aProcessInstance",
                         "anotherProcessInstance"
                       ],
                       "initialVariables" : true,
                       "skipCustomListeners" : true,
                       "withoutBusinessKey" : true
                     }
                   }',
                   '"example-2": {
                     "summary": "Restarting one or more Process Instances using a historicProcessInstanceQuery",
                     "value": {
                       "instructions": [
                         {
                           "type": "startAfterActivity",
                           "activityId": "aUserTask"
                         }
                       ],
                       "historicProcessInstanceQuery": {
                         "processDefinitionId": "aProcessDefinitionId",
                         "processInstanceBusinessKey" : "businessKey"
                       },
                       "initialVariables" : true,
                       "skipCustomListeners" : true,
                       "withoutBusinessKey" : true
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
        desc = "In case following parameters are missing: `instructions`, `activityId` or `transitionId`,
                `processInstanceIds` or `historicProcessInstanceQuery`,
                an exception of type `InvalidRequestException` is returned. 
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Process definition with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
  }
}

</#macro>