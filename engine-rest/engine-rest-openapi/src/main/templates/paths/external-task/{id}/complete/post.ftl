<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "completeExternalTaskResource"
      tag = "External Task"
      summary = "Complete"
      desc = "Completes an external task by id and updates process variables." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to complete." />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CompleteExternalTaskDto"
      examples = ['"example-1": {
                       "summary": "POST /external-task/anId/complete",
                       "value": {
                         "workerId": "aWorker",
                         "variables": {
                           "aVariable": {
                             "value": "aStringValue"
                           },
                           "anotherVariable": {
                             "value": 42
                           },
                           "aThirdVariable": {
                             "value": true
                           }
                         },
                         "localVariables": {
                           "aLocalVariable": {
                             "value": "aStringValue"
                           }
                         }
                       }
                     }'] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if the task's most recent lock was not acquired by the provided worker. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Returned if the task does not exist. This could indicate a wrong task id as well as a cancelled task,
                e.g., due to a caught BPMN boundary event. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the corresponding process instance could not be resumed successfully. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>