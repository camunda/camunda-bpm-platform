<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "modifyProcessInstanceVariables"
      tag = "Process Instance"
      summary = "Update/Delete Process Variables"
      desc = "Updates or deletes the variables of a process instance by id. Updates precede deletions.
              So, if a variable is updated AND deleted, the deletion overrides the update." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last  = true
        desc = "The id of the process instance to set variables for."/>

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "PatchVariablesDto"
      examples = ['"example-1": {
                     "summary": "POST `/process-instance/aProcessInstanceId/variables`",
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

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Bad Request
                The variable value or type is invalid, for example if the value could not be parsed to an Integer value or
                the passed variable type is not supported."/>

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        desc = "Update or delete could not be executed, for example because the process instance does not exist."/>

  }
}
</#macro>