{
  "operationId": "modifyProcessInstanceVariables",
  "description": "Updates or deletes the variables of a process instance by id. Updates precede deletions. So, if a variable is updated AND deleted, the deletion overrides the update.",
  "tags": [
    "Process instance"
  ],
  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        last  = true
        description = "The id of the process instance to set variables for."/>

  ],

  <@lib.requestBody
      dto = "PatchVariablesDto" />

  "responses": {

    <@lib.response
        code = "204"
        description = "Request successful." />

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        description = "Bad Request\n\nThe variable value or type is invalid, for example if the value could not be parsed to an Integer value or the passed variable type is not supported."/>

    <@lib.response
        code = "500"
        dto = "ExceptionDto"
        last = true
        description = "Update or delete could not be executed, for example because the process instance does not exist."/>

  }
}