<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "complete"
      tag = "Task"
      summary = "Complete"
      desc = "Completes a task and updates process variables." />

  "parameters" : [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The id of the task to complete."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "CompleteTaskDto"
      examples = ['"example-1": {
                     "summary": "POST `/task/anId/complete`",
                     "description": "Complete Task with variables in return",
                     "value": {
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
                       "withVariablesInReturn": true
                     }
                   }',
                  '"example-2": {
                     "summary": "POST `/task/anId/complete`",
                     "description": "Complete Task without variables in return",
                     "value": {
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
                   }'
      ] />

  "responses" : {

    <@lib.response
        code = "200"
        dto = "VariableValueDto"
        additionalProperties = true
        desc = "Request successful. The response contains the process variables."
        examples = ['"example-1": {
                       "summary": "POST `/task/anId/complete`",
                       "description": "Response Body",
                       "value": {
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
                     }'
        ] />

    <@lib.response
        code = "204"
        desc = "Request successful. The response contains no variables." />

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