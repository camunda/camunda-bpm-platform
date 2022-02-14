<#macro endpoint_macro docsUrl="">
{
  <@lib.endpointInfo
      id = "getProcessInstanceVariableBinary"
      tag = "Process Instance"
      summary = "Get Process Variable (Binary)"
      desc = "Retrieves the content of a Process Variable by the Process Instance id and the Process Variable name.
              Applicable for byte array or file Process Variables." />

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
  "responses": {
    "200": {
      "description": "Request successful.
        For binary variables or files without any MIME type information, a byte stream is returned.
        File variables with MIME type information are returned as the saved type.
        Additionally, for file variables the Content-Disposition header will be set.",
      "content": {
        "application/octet-stream": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "For binary variables or files without any MIME type information, a byte stream is returned."
          },
          "examples": {
            "example-1" :{
              "summary": "binary variable: Status 200",
              "value": "Content-Type: application/octet-stream"
            }
          }
        },
        "text/plain": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "File variables with MIME type information are returned as the saved type.
            Additionally, for file variables the Content-Disposition header will be set."
          },
          "examples": {
            "example-1" :{
              "summary": "file variable: Status 200",
              "value": "Content-Type: text/plain; charset=UTF-8. Content-Disposition: attachment; filename=\"someFile.txt\""
            }
          }
        }
      }
    },

    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Bad Request
                A Process Variable with the given id exists but does not serialize as binary data."/>

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        last = true
        desc = "Not Found
                A Process Variable with the given id does not exist. "/>

  }
}
</#macro>