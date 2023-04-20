<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/execution/local-variables/post-local-variables/index.html -->
{
  <@lib.endpointInfo
      id = "modifyLocalExecutionVariables"
      tag = "Execution"
      summary = "Update/Delete Local Execution Variables"
      desc = "Updates or deletes the variables in the context of an execution by id. The updates
              do not propagate upwards in the execution hierarchy.
              Updates precede deletions. So, if a variable is updated AND deleted,
              the deletion overrides the update."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the execution to set variables for."
          last = true
      />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "PatchVariablesDto"
      examples = ['"example-1": {
                     "summary": "POST `/execution/anExecutionId/localVariables`",
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
                   }']
  />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful. This method returns no content."
    />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "The variable value or type is invalid, for example if the value could not be parsed
                to an Integer value or the passed variable type is not supported.
                See the [Introduction](${docsUrl}/reference/rest/overview/#error-handling) for the error response format."
    />

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        desc = "Update or delete could not be executed, for example because the execution does not
                exist."
        last = true
    />

  }

}
</#macro>