<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "resolve"
      tag = "Task"
      summary = "Resolve"
      desc = "Resolves a task and updates execution variables.

              Resolving a task marks that the assignee is done with the task delegated to them, and
              that it can be sent back to the owner. Can only be executed when the task has been
              delegated. The assignee will be set to the owner, who performed the delegation." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to resolve."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CompleteTaskDto"
      examples = ['"example-1": {
                     "summary": "Request Body",
                     "description": "POST `/task/anId/resolve`",
                     "value":     {
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
        desc = "The variable value or type is invalid, for example if the value could not be parsed
                to an Integer value or the passed variable type is not supported. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "If the task does not exist or the corresponding process instance could not be
                resumed successfully. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>