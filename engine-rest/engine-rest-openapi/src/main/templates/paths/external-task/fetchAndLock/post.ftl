{

  <@lib.endpointInfo
      id = "fetchAndLock"
      tag = "External Task"
      desc = "Fetches and locks a specific number of external tasks for execution by a worker. Query can be restricted
              to specific task topics and for each task topic an individual lock time can be provided." />

  <@lib.requestBody
      mediaType = "application/json"
      dto = "FetchExternalTasksDto"
      examples = ['"example-1": {
                       "summary": "POST /external-task/fetchAndLock (1)",
                       "description": "Request with variable filtering",
                       "value": {
                         "workerId": "aWorkerId",
                         "maxTasks": 2,
                         "usePriority": true,
                         "topics": [
                           {
                             "topicName": "createOrder",
                             "lockDuration": 10000,
                             "variables": [
                               "orderId"
                             ]
                           }
                         ]
                       }
                     }',
                    '"example-2": {
                       "summary": "POST /external-task/fetchAndLock (2)",
                       "description": "Request with all variables included",
                       "value": {
                         "workerId": "aWorkerId",
                         "maxTasks": 2,
                         "usePriority": true,
                         "topics": [
                           {
                             "topicName": "createOrder",
                             "lockDuration": 10000,
                             "processDefinitionId": "aProcessDefinitionId",
                             "tenantIdIn": "tenantOne"
                           }
                         ]
                       }
                     }'
      ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "LockedExternalTaskDto"
        array = true
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "POST /external-task/fetchAndLock (1)",
                       "description": "Request with variable filtering",
                       "value": [
                         {
                           "activityId": "anActivityId",
                           "activityInstanceId": "anActivityInstanceId",
                           "errorMessage": "anErrorMessage",
                           "errorDetails": "anErrorDetails",
                           "executionId": "anExecutionId",
                           "id": "anExternalTaskId",
                           "lockExpirationTime": "2015-10-06T16:34:42.000+0200",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processDefinitionKey": "aProcessDefinitionKey",
                           "processInstanceId": "aProcessInstanceId",
                           "tenantId": null,
                           "retries": 3,
                           "workerId": "aWorkerId",
                           "priority": 4,
                           "topicName": "createOrder",
                           "variables": {
                             "orderId": {
                               "type": "String",
                               "value": "1234",
                               "valueInfo": {}
                             }
                           }
                         },
                         {
                           "activityId": "anActivityId",
                           "activityInstanceId": "anActivityInstanceId",
                           "errorMessage": "anErrorMessage",
                           "errorDetails": "anotherErrorDetails",
                           "executionId": "anExecutionId",
                           "id": "anExternalTaskId",
                           "lockExpirationTime": "2015-10-06T16:34:42.000+0200",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processDefinitionKey": "aProcessDefinitionKey",
                           "processInstanceId": "aProcessInstanceId",
                           "tenantId": null,
                           "retries": 3,
                           "workerId": "aWorkerId",
                           "priority": 0,
                           "topicName": "createOrder",
                           "variables": {
                             "orderId": {
                               "type": "String",
                               "value": "3456",
                               "valueInfo": {}
                             }
                           }
                         }
                       ]
                     }',
                    '"example-2": {
                       "summary": "POST /external-task/fetchAndLock (2)",
                       "description": "Request with all variables included",
                       "value": [
                         {
                           "activityId": "anActivityId",
                           "activityInstanceId": "anActivityInstanceId",
                           "errorMessage": "anErrorMessage",
                           "errorDetails": "anErrorDetails",
                           "executionId": "anExecutionId",
                           "id": "anExternalTaskId",
                           "lockExpirationTime": "2015-10-06T16:34:42.00+0200",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processDefinitionKey": "aProcessDefinitionKey",
                           "processInstanceId": "aProcessInstanceId",
                           "tenantId": "tenantOne",
                           "retries": 3,
                           "workerId": "aWorkerId",
                           "priority": 4,
                           "topicName": "createOrder",
                           "businessKey": "aBusinessKey",
                           "variables": {
                             "orderId": {
                               "type": "String",
                               "value": "1234",
                               "valueInfo": {}
                             }
                           }
                         },
                         {
                           "activityId": "anActivityId",
                           "activityInstanceId": "anActivityInstanceId",
                           "errorMessage": "anErrorMessage",
                           "errorDetails": "anotherErrorDetails",
                           "executionId": "anExecutionId",
                           "id": "anExternalTaskId",
                           "lockExpirationTime": "2015-10-06T16:34:42.000+0200",
                           "processDefinitionId": "aProcessDefinitionId",
                           "processDefinitionKey": "aProcessDefinitionKey",
                           "processInstanceId": "aProcessInstanceId",
                           "tenantId": null,
                           "retries": 3,
                           "workerId": "aWorkerId",
                           "priority": 0,
                           "topicName": "createOrder",
                           "businessKey": "aBusinessKey",
                           "variables": {
                             "orderId": {
                               "type": "String",
                               "value": "3456",
                               "valueInfo": {}
                             }
                           }
                         }
                       ]
                     }'
        ] />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
