<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "handleExternalTaskBpmnError"
      tag = "External Task"
      summary = "Handle BPMN Error"
      desc = "Reports a business error in the context of a running external task by id. The error code must be specified
              to identify the BPMN error handler." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the external task in which context a BPMN error is reported."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "ExternalTaskBpmnError"
      examples = ['"example-1": {
                       "summary": "POST /external-task/anId/bpmnError",
                       "value": {
                         "workerId": "aWorker",
                         "errorCode": "bpmn-error",
                         "errorMessage": "anErrorMessage",
                         "variables": {
                           "aVariable": {
                             "value": "aStringValue",
                             "type": "String"
                           },
                           "anotherVariable": {
                             "value": true,
                             "type": "Boolean"
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
        desc = "Returned if the task's most recent lock was not acquired by the provided worker.

                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Returned if the task does not exist. This could indicate a wrong task id as well as a cancelled task,
                e.g., due to a caught BPMN boundary event.

                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the corresponding process instance could not be resumed successfully.

                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>