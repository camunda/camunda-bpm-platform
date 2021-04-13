<#macro endpoint_macro docsUrl="">
{

  <@lib.endpointInfo
      id = "putTaskVariable"
      tag = "Task Variable"
      summary = "Update Task Variable"
      desc = "Updates a process variable that is visible from the Task scope. A variable is visible from the task if it
              is a local task variable, or declared in a parent scope of the task. See the documentation on
              [variable scopes and visibility](${docsUrl}/user-guide/process-engine/variables#variable-scopes-and-variable-visibility).

              **Note**: If a variable doesn't exist, the variable is created in the top-most scope
              visible from the task." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the task to set the variable for." />

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The name of the variable to set." />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "VariableValueDto"
      examples = [ '"example-1": {
                       "summary": "PUT /task/aTaskId/variables/aVarName",
                       "description": "Status 204. No content.",
                       "value": {
                         "value": "someValue",
                         "type": "String"
                       }
                     }',
                    '"example-2": {
                       "summary": "PUT /task/aTaskId/variables/aVarName",
                       "description": "Status 204. No content.",
                       "value": {
                         "value": "ab",
                         "type": "Object",
                         "valueInfo": {
                           "objectTypeName": "com.example.MyObject",
                           "serializationDataFormat": "application/xml"
                         }
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
        desc = "The variable name, value or type is invalid, for example if the value could not be parsed to an `Integer`
                value or the passed variable type is not supported or a new transient variable has the name that is
                already persisted. See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "The variable name is `null`, or the Task id is `null` or does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format." />

  }
}

</#macro>