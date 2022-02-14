<#macro endpoint_macro docsUrl="">
<#-- Generated From File: camunda-docs-manual/public/reference/rest/variable-instance/get-binary/index.html -->
{
  <@lib.endpointInfo
      id = "getVariableInstanceBinary"
      tag = "Variable Instance"
      summary = "Get Variable Instance (Binary)"
      desc = "Retrieves the content of a variable by id. Applicable for byte array and file
              variables."
  />

  "parameters" : [

      <@lib.parameter
          name = "id"
          location = "path"
          type = "string"
          required = true
          desc = "The id of the variable instance."
          last = true
      />

  ],

  "responses": {

    "200": {
      "description": "Request successful. For binary variables or files without any MIME type information, a byte stream is returned.
                      File variables with MIME type information are returned as the saved type. Additionally, for file
                      variables the Content-Disposition header will be set.",
      "content": {
        "application/octet-stream": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "For files without any MIME type information, a byte stream is returned."
          }
        },
        "*/*": {
          "schema": {
            "type": "string",
            "format": "binary",
            "description": "Files with MIME type information are returned as the saved type. Additionally, for file
                            responses, the Content-Disposition header will be set."
          }
        }
      }
    },


    <@lib.response
        code = "400"
        dto = "ExceptionDto"
        desc = "Variable with given id exists but does not serialize as binary data. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
    />

    <@lib.response
        code = "404"
        dto = "ExceptionDto"
        desc = "Variable with given id does not exist. See the
                [Introduction](${docsUrl}/reference/rest/overview/#error-handling)
                for the error response format."
        last = true
    />

  }

}
</#macro>