{

  <@lib.endpointInfo
      id = "handleBpmnError"
      tag = "Task"
      desc = "Reports a business error in the context of a running task by id. The error code must
              be specified to identify the BPMN error handler. See the documentation for
              [Reporting Bpmn Error](${docsUrl}/reference/bpmn20/tasks/user-task/#reporting-bpmn-error)
              in User Tasks." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task a BPMN error is reported for."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TaskBpmnErrorDto"
      examples = ['"example-1": {
                     "summary": "Request Body",
                     "description": "POST `/task/aTaskId/bpmnError`",
                     "value": {
                       "errorCode": "bpmn-error-543",
                       "errorMessage": "anErrorMessage",
                       "variables": {
                         "aVariable" : {
                           "value" : "aStringValue",
                           "type": "String"
                         },
                         "anotherVariable" : {
                           "value" : true,
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
        code = "403"
        dto = "AuthorizationExceptionDto"
        desc = "If the authenticated user is unauthorized to update the process instance. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Returned if the task does not exist or <code>errorCode</code> is not presented in
                the request. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}
