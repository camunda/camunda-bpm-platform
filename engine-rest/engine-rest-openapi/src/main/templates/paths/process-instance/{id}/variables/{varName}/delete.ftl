{
  "operationId": "deleteProcessInstanceVariable",
  "description": "Deletes a variable of a process instance by id.",
  "tags": [
    "Process instance"
  ],
  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the process instance to delete the variable from."/>

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The name of the variable to delete."/>

  ],
  "responses": {

    <@lib.response
        code = "204"
        last = true
        desc = "Request successful."/>

  }
}