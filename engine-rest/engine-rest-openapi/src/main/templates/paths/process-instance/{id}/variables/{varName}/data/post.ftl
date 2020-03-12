{
  <@lib.endpointInfo
      id = "setProcessInstanceVariableBinary"
      tag = "Process instance"
      desc = "Sets the serialized value for a binary variable or the binary value for a file variable." />

  "parameters": [

    <@lib.parameter
        name = "id"
        location = "path"
        type = "string"
        required = true
        desc = "The id of the process instance to retrieve the variable for."/>

    <@lib.parameter
        name = "varName"
        location = "path"
        type = "string"
        required = true
        last = true
        desc = "The name of the variable to retrieve."/>

  ],

  <@lib.requestBody
      mediaType = "multipart/form-data"
      dto = "MultiFormVariableBinaryDto"
      requestDesc = "For binary variables a multipart form submit with the following parts:" />

  "responses": {

    <@lib.response
        code = "204"
        desc = "Request successful."/>

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        last = true
        desc = "Bad Request
                The variable value or type is invalid, for example if no filename is set."/>

  }
}