<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "handleEscalation"
      tag = "Task"
      summary = "Handle BPMN Escalation"
      desc = "Reports an escalation in the context of a running task by id. The escalation code must
              be specified to identify the escalation handler. See the documentation for
              [Reporting Bpmn Escalation](${docsUrl}/reference/bpmn20/tasks/user-task/#reporting-bpmn-escalation)
              in User Tasks." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task in which context a BPMN escalation is reported."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "TaskEscalationDto"
      examples = ['"example-1": {
                     "summary": "Request Body",
                     "description": "POST `/task/aTaskId/bpmnEscalation`",
                     "value": {
                       "escalationCode": "bpmn-escalation-432",
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
        code = "400"
        dto = "ExceptionDto"
        desc = "Returned if the <code>escalationCode</code> is not provided in
                the request. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

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
        desc = "Returned if the task does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>