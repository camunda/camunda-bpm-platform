{
  "operationId": "setProcessInstanceVariableBinary",
  "description": "Sets the serialized value for a binary variable or the binary value for a file variable.",
  "tags": [
    "Process instance"
  ],
  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        description = "The id of the process instance to retrieve the variable for."/>

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        description = "The name of the variable to retrieve."/>

  ],

  <@lib.requestBody
      mediaType = "multipart/form-data"
      dto = "MultiFormVariableBinaryDto"
      requestDescription = "For binary variables a multipart form submit with the following parts:" />

  "responses": {

    <@lib.response
        code = "204"
        description = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        description = "Bad Request\n\nThe variable value or type is invalid, for example if no filename is set."/>

  }
}