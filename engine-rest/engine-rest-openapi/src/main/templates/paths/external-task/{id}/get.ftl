<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "getExternalTask"
      tag = "External Task"
      summary = "Get"
      desc = "Retrieves an external task by id, corresponding to the `ExternalTask` interface in the engine." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the external task to be retrieved." />

  ],

  "responses" : {

    <@lib.response
        code = "200"
        dto = "ExternalTaskDto"
        desc = "Request successful."
        examples = ['"example-1": {
                       "summary": "GET /external-task/anExternalTaskId",
                       "value": {
                         "activityId": "anActivityId",
                         "activityInstanceId": "anActivityInstanceId",
                         "errorMessage": "anErrorMessage",
                         "executionId": "anExecutionId",
                         "id": "anExternalTaskId",
                         "lockExpirationTime": "2015-10-06T16:34:42.000+0200",
                         "processDefinitionId": "aProcessDefinitionId",
                         "processDefinitionKey": "aProcessDefinitionKey",
                         "processInstanceId": "aProcessInstanceId",
                         "tenantId": null,
                         "retries": 3,
                         "suspended": false,
                         "workerId": "aWorkerId",
                         "priority":0,
                         "topicName": "aTopic",
                         "businessKey": "aBusinessKey"
                       }
                     }'] />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "External task with the given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>