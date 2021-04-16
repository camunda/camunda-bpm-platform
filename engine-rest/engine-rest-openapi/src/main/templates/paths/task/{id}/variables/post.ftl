<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "modifyTaskVariables"
      tag = "Task Variable"
      summary = "Update/Delete Task Variables"
      desc = "Updates or deletes the variables visible from the task. Updates precede deletions. So, if a variable is
              updated AND deleted, the deletion overrides the update. A variable is visible from the task if it is a
              local task variable or declared in a parent scope of the task. See documentation on
              [visiblity of variables](${docsUrl}/user-guide/process-engine/variables/)." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last  = true
        desc = "The id of the task to set variables for."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "PatchVariablesDto"
      examples = ['"example-1": {
                     "summary": "POST `/task/aTaskId/variables`",
                     "description": "Status 204 Response: No content.",
                     "value": {
                       "modifications": {
                         "aVariable": {
                           "value": "aValue",
                           "type": "String"
                         },
                         "anotherVariable": {
                           "value": 42,
                           "type": "Integer"
                         }
                       },
                       "deletions": [
                         "aThirdVariable",
                         "FourthVariable"
                       ]
                     }
                   }'
      ] />

  "responses" : {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The variable value or type is invalid. For example the value could not be parsed to an `Integer` value
                or the passed variable type is not supported. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Update or delete could not be executed because the task is `null` or does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />
  }
}

</#macro>