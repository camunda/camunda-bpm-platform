{
  "operationId": "setProcessInstanceVariable",
  "description": "Sets a variable of a given process instance by id.",
  "tags": [
    "Process instance"
  ],
  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        description = "The id of the process instance to set the variable for." />

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        description = "The name of the variable to set." />

  ],

  <@lib.requestBody
      mediaType = "application/json"
      dto = "VariableValueDto" />

  "responses": {

    <@lib.response
        code = "204"
        description = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        description = "Bad Request
The variable value or type is invalid, for example if the value could not be parsed to an Integer value or
the passed variable type is not supported."/>
  }
}